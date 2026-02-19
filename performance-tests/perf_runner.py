import argparse
import csv
import json
import os.path
import subprocess
import time
from contextlib import contextmanager
from datetime import datetime, timezone, timedelta
from pathlib import Path
from typing import Literal, Any
from urllib.parse import urlparse

import requests

import polling
from helper import run_command, stop_and_remove_container, docker_available

# ============================================================================
# Defaults / Configuration
# ============================================================================

CRUD_PORT = 8080
ES_CQRS_PORT = 8081

PROMETHEUS_IMAGE = "prom/prometheus"
PROMETHEUS_PORT = 9090
PROMETHEUS_SCRAPE_INTERVAL_SECONDS = 2
PROMETHEUS_READY_RETRIES = 5
PROMETHEUS_READY_SLEEP_SECONDS = 2

PROMETHEUS_LATENCY_WINDOW = "2m"

PROMETHEUS_QUERIES = {
    # Average latency per endpoint
    "latency_avg.json": (
        "sum by (uri, method) ("
        "  rate(http_server_requests_seconds_sum[{w}])"
        ") "
        "/ "
        "sum by (uri, method) ("
        "  rate(http_server_requests_seconds_count[{w}])"
        ")"
    ),
    # p50 (median) per endpoint
    "latency_p50.json": (
        "histogram_quantile(0.50, "
        "  sum by (le, uri, method) ("
        "    rate(http_server_requests_seconds_bucket[{w}])"
        "  )"
        ")"
    ),
    # p95 per endpoint
    "latency_p95.json": (
        "histogram_quantile(0.95, "
        "  sum by (le, uri, method) ("
        "    rate(http_server_requests_seconds_bucket[{w}])"
        "  )"
        ")"
    ),
    # p99 per endpoint
    "latency_p99.json": (
        "histogram_quantile(0.99, "
        "  sum by (le, uri, method) ("
        "    rate(http_server_requests_seconds_bucket[{w}])"
        "  )"
        ")"
    ),
    # hikari connection acquire time (P95)
    # "hikari_connection_acquire_time_p95.json": (
    #     "histogram_quantile(0.95,"
    #     "    rate(hikaricp_connections_acquire_seconds_bucket[1m])"
    #     ")"
    # ),
}

PROMETHEUS_RANGE_QUERIES = {
    "cpu_usage.json": "process_cpu_usage",  # process_cpu_usage | system_cpu_usage
    # RAM Usage: Total used memory in bytes (Heap + Non-Heap)
    "ram_usage_total_bytes.json": (
        "sum(jvm_memory_used_bytes{area='heap'}) + sum(jvm_memory_used_bytes{area='nonheap'})"
    ),
    "ram_usage_heap_bytes.json": "sum(jvm_memory_used_bytes{area='heap'})",
    "tomcat_threads_current_threads.json": "tomcat_threads_current_threads",
    "hikaricp_connections_max.json": "hikaricp_connections_max",
    "hikaricp_connections_active.json": "hikaricp_connections_active",
}


# ============================================================================
# Utility helpers
# ============================================================================


def resolve_host(app: Literal["crud", "es-cqrs"], config: dict | None) -> str:
    if app == "crud":
        if config is None:
            return f"http://localhost:{CRUD_PORT}"
        return f'http://{config["SERVER_IP"]}:{CRUD_PORT}'
    if app == "es-cqrs":
        if config is None:
            return f"http://localhost:{ES_CQRS_PORT}"
        return f'http://{config["SERVER_IP"]}:{ES_CQRS_PORT}'
    raise ValueError(f"Unsupported app: {app}")


def create_remote_context(config: dict | None) -> None:
    server_ip = config["SERVER_IP"]

    print(f"Try to create docker remote context for {server_ip}")

    result = subprocess.run(["docker", "context", "ls"], capture_output=True, text=True)
    if "server-remote" in result.stdout:
        print("Context 'server-remote' already exists.")
        return

    run_command(
        [
            "docker",
            "context",
            "create",
            "server-remote",
            "--docker",
            f'"host=ssh://thesis@{server_ip}"',
        ]
    )


@contextmanager
def docker_remote(config: dict | None):
    try:
        if config is not None:
            print("Enabling docker remote context")
            run_command(["docker", "context", "use", "server-remote"])
        yield
    finally:
        if config is not None:
            print("Disabling docker remote context")
            run_command(["docker", "context", "use", "default"])


def get_app_service(app: Literal["crud", "es-cqrs"]) -> str:
    return "crud-app" if app == "crud" else "es-cqrs-app"


def start_app_with_docker_compose(
    app: Literal["crud", "es-cqrs"], config: dict | None
) -> None:
    app_service = get_app_service(app)

    with docker_remote(config):
        run_command(["docker", "compose", "up", "-d", app_service])

    host = resolve_host(app, config)
    polling.poll(f"{host}/actuator/health", interval_seconds=5, retries=25)


def docker_compose_down() -> None:
    """
    Runs `docker compose down -v`
    """
    run_command(["docker", "compose", "down", "-v"])


# ============================================================================
# Run setup
# ============================================================================


def create_run_dirs(k6_script: Path, app: str, VUs: int) -> tuple[str, Path, Path]:
    script_name = Path(k6_script).stem
    run_date = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    run_id = f"run-{script_name}-{VUs}-{app}-{run_date}"

    run_dir = Path("run-k6") / Path(run_id)
    prom_dir = run_dir / "prometheus"

    prom_dir.mkdir(parents=True)

    return run_id, run_dir, prom_dir


def write_prometheus_config(
    config_path: Path, service_name: str, app_port: int, test_config: dict | None
) -> None:
    print(f"Writing prometheus config to '{config_path}'")
    config_path.touch()

    if test_config is None:
        import platform

        if platform.system() == "Windows":
            prometheus_target = f"host.docker.internal:{app_port}"
        else:
            prometheus_target = f"{service_name}:{app_port}"  # TODO this doesnt work when starting prometheus without the docker compose file
            # containers are not in the same network
    else:
        prometheus_target = f"{test_config['SERVER_IP']}:{app_port}"
        print(f"prometheus_target={prometheus_target}")

    if service_name == "es-cqrs-app":
        if test_config is None:  # TODO this only works on Windows (see above)
            axonserver_prometheus_endpoint = "host.docker.internal:8024"
        else:
            axonserver_prometheus_endpoint = f"{test_config['SERVER_IP']}:8024"

        additional_scrape_config = f"""
  - job_name: "axonserver"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["{axonserver_prometheus_endpoint}"]
"""
    else:
        additional_scrape_config = ""

    config_path.write_text(
        f"""
global:
  scrape_interval: {PROMETHEUS_SCRAPE_INTERVAL_SECONDS}s

scrape_configs:
  - job_name: "spring"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["{prometheus_target}"]
{additional_scrape_config}
""".strip()
    )

    print(config_path.read_text())


# ============================================================================
# Prometheus (Docker)
# ============================================================================


def start_prometheus_container(config_path: Path) -> str:
    dt = datetime.now().isoformat().replace(":", "_")
    container_name = f"prometheus-{dt}"

    run_command(
        [
            "docker",
            "run",
            "-d",
            "--name",
            container_name,
            "-p",
            f"{PROMETHEUS_PORT}:9090",
            "-v",
            f"{config_path.absolute()}:/etc/prometheus/prometheus.yml",
            PROMETHEUS_IMAGE,
        ]
    )

    polling.poll(
        f"http://localhost:{PROMETHEUS_PORT}/-/ready",
        interval_seconds=PROMETHEUS_READY_SLEEP_SECONDS,
        retries=PROMETHEUS_READY_RETRIES,
    )

    return container_name


# ============================================================================
# k6
# ============================================================================


def run_k6(host_url: str, k6_script: Path, run_dir: Path, VUs: int) -> tuple[int, int]:
    print("Starting k6 run.")
    start_ts = int(time.time())

    summary_json = run_dir / "k6-summary.json"
    summary_txt = run_dir / "k6-summary.txt"

    with summary_txt.open("w") as out:
        subprocess.run(
            [
                "k6",
                "run",
                f"--summary-export={summary_json}",
                "-e",
                f"HOST={host_url}",
                "-e",
                f"VUs={VUs}",
                k6_script,
            ],
            stdout=out,
            stderr=subprocess.STDOUT,
            check=True,
        )

    end_ts = int(time.time())
    print("k6 run finished.")
    return start_ts, end_ts


# ============================================================================
# Prometheus queries
# ============================================================================


def query_prometheus(
    prom_dir: Path,
    window: str,
) -> None:
    query_url = f"http://localhost:{PROMETHEUS_PORT}/api/v1/query"

    def _get_axon_storage_size():
        axon_storage_size_query = "axon_storage_size"
        resp = requests.get(query_url, params={"query": axon_storage_size_query})
        resp.raise_for_status()
        (prom_dir / "axon_storage_size_bytes.json").write_text(
            json.dumps(resp.json(), indent=2)
        )

    def _get_pg_size():
        pgsize_query = "database_size_bytes"
        resp = requests.get(query_url, params={"query": pgsize_query})
        resp.raise_for_status()
        (prom_dir / "postgres_size_bytes.json").write_text(
            json.dumps(resp.json(), indent=2)
        )

    _get_axon_storage_size()
    _get_pg_size()

    for filename, query in PROMETHEUS_QUERIES.items():
        rendered_query = query.format(w=window)
        resp = requests.get(query_url, params={"query": rendered_query})
        resp.raise_for_status()
        (prom_dir / filename).write_text(json.dumps(resp.json(), indent=2))

    query_range_url = f"http://localhost:{PROMETHEUS_PORT}/api/v1/query_range"
    for filename, range_query in PROMETHEUS_RANGE_QUERIES.items():
        now = datetime.now(timezone.utc)
        if window.endswith("s"):
            now_minus_window = now - timedelta(seconds=int(window[:-1]))
        elif window.endswith("m"):
            now_minus_window = now - timedelta(minutes=int(window[:-1]))
        elif window.endswith("h"):
            now_minus_window = now - timedelta(hours=int(window[:-1]))
        elif window.endswith("d"):
            now_minus_window = now - timedelta(days=int(window[:-1]))
        else:
            raise ValueError(
                f"Window '{window}' not supported. Use seconds, minutes, hours or days"
            )

        res = requests.get(
            query_range_url,
            params={
                "query": range_query,
                "start": now_minus_window.timestamp(),
                "end": now.timestamp(),
                "step": "5s",
            },
        )
        res.raise_for_status()
        (prom_dir / filename).write_text(json.dumps(res.json(), indent=2))


# ============================================================================
# Metadata
# ============================================================================


def write_metadata(
    metric_definition_path: Path,
    run_dir: Path,
    run_id: str,
    host_url: str,
    k6_script: Path,
    test_start: int,
    test_end: int,
    window: str,
) -> None:
    metric_def = json.loads(metric_definition_path.read_text())
    metric_title = metric_def["metadata"]["title"]

    (run_dir / "notes.md").write_text(
        f"""# Prometheus Notes
> Run ID: {run_id}

- Title: '{metric_title}'
- Host: {host_url}
- K6 script: {k6_script}
- Test start (epoch): {test_start}
- Test end (epoch):   {test_end}
- Scrape interval: {PROMETHEUS_SCRAPE_INTERVAL_SECONDS}s
- Latency window: {window}
"""
    )


def extract_server_metrics_to_csv(
    metric_definition_path: Path, prom_dir: Path, output_csv: Path, app: str
) -> None:
    """
    Extracts relevant Prometheus metrics (based on metric.json)
    and writes them to a CSV file.
    """
    metric_def = json.loads(metric_definition_path.read_text())
    target_method = metric_def["metric"]["method"]
    target_uri = metric_def["metric"]["uri"]
    VUs = metric_def["VUs"]

    rows: list[dict[str, str]] = []

    for prom_file in prom_dir.glob("*.json"):
        metric_name = prom_file.stem
        content = json.loads(prom_file.read_text())

        results = content.get("data", {}).get("result", [])
        for entry in results:
            labels = entry.get("metric", {})
            value = entry.get("value", [None, None])[1]

            if (
                labels.get("method") == target_method
                and labels.get("uri") == target_uri
                and value is not None
            ):
                rows.append(
                    {
                        "app": app,
                        "metric": metric_name,
                        "method": target_method,
                        "uri": target_uri,
                        "value": value,
                        "virtual_users": VUs,
                    }
                )

    if not rows:
        print("⚠️ No matching metrics found for CSV export.")
        return

    with output_csv.open("w", newline="") as f:
        writer = csv.DictWriter(
            f,
            fieldnames=["metric", "method", "uri", "value", "app", "virtual_users"],
        )
        writer.writeheader()
        writer.writerows(rows)

    print(f"📄 Extracted metrics written to {output_csv}")


def extract_k6_summary_to_csv(
    summary_path: Path, metric_definition_path: Path, output_csv: Path, app: str
) -> None:
    """
    Extracts group_duration metrics from k6-summary.json and appends/writes
    them to CSV in the same format as the Prometheus extractor.
    """
    if not summary_path.exists():
        print(f"⚠️ k6 summary file {summary_path} not found.")
        return

    metric_def = json.loads(metric_definition_path.read_text())
    summary = json.loads(summary_path.read_text())

    vus = metric_def.get("VUs", "unknown")
    target_method = metric_def["metric"]["method"]

    group_metrics = summary.get("metrics", {}).get("group_duration", {})
    dropped = summary.get("metrics", {}).get("dropped_iterations", {})
    if not group_metrics:
        print("⚠️ No group_duration metrics found in summary.")
        return

    rows = []

    # lookup table to rename metrics, so that they match the prometheus metrics
    k6_stat_name_to_metric_stat_name = {
        "med": "latency_p50",
        "p(95)": "latency_p95",
        "p(90)": "latency_p90",
        "p(99)": "latency_p99",
        "avg": "latency_avg",
    }

    uri = metric_def["metric"].get("uri", "all_groups")

    for stat_name, value in group_metrics.items():
        # Skip non-numeric metadata if any exists
        if not isinstance(value, (int, float)):
            continue

        rows.append(
            {
                "app": app,
                "metric": k6_stat_name_to_metric_stat_name.get(stat_name, stat_name),
                "method": target_method,
                "uri": uri,
                "value": value / 1000,  # turn into seconds
                "virtual_users": vus,
            }
        )

    rows.append(
        {
            "app": app,
            "metric": "dropped_iterations_rate",
            "method": target_method,
            "uri": uri,
            "value": dropped.get("rate", 0),
            "virtual_users": vus,
        }
    )

    fieldnames = ["metric", "method", "uri", "value", "app", "virtual_users"]

    with output_csv.open("w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    print(f"📄 k6 summary metrics written to {output_csv}")


def _get_metric_content(metric: Path) -> tuple[Any, Path]:
    metric_content = json.loads(Path(metric).read_text())
    k6_script = os.path.dirname(metric) / Path(metric_content["file"])

    return metric_content, k6_script


def do_run(app: Literal["crud", "es-cqrs"], metric: Path, config_file: Path | None):
    config = None

    if config_file is not None:
        print("INFO: VM run detected (config_file provided)")
        config_file_text = config_file.read_text()
        config = {
            line.split("=", 1)[0].strip(): line.split("=", 1)[1].strip()
            for line in config_file_text.strip().splitlines()
            if "=" in line and not line.startswith("#")
        }
        print(config)
    else:
        print("INFO: Local run")

    if not docker_available():
        raise RuntimeError("Docker is required but not available")

    prom_container = None
    try:
        if config is not None:
            create_remote_context(config)

        start_app_with_docker_compose(app, config)

        host_url = resolve_host(app, config)
        app_port = urlparse(host_url).port

        metric_content, k6_script = _get_metric_content(metric)

        VUs = metric_content.get("VUs")
        if VUs is None:
            raise ValueError(
                "Configuration Error: No field 'VUs' configured in metric.json"
            )

        run_id, run_dir, prom_dir = create_run_dirs(k6_script, app, VUs)

        prom_config = run_dir / "prometheus.yml"
        write_prometheus_config(prom_config, get_app_service(app), app_port, config)

        prom_container = start_prometheus_container(config_path=prom_config)

        test_start, test_end = run_k6(
            host_url=host_url,
            k6_script=k6_script,
            run_dir=run_dir,
            VUs=VUs,
        )

        query_prometheus(
            prom_dir=prom_dir,
            window=PROMETHEUS_LATENCY_WINDOW,
        )

        extract_server_metrics_to_csv(
            metric_definition_path=Path(metric),
            prom_dir=prom_dir,
            output_csv=run_dir / "server_metrics.csv",
            app=app,
        )

        extract_k6_summary_to_csv(
            metric_definition_path=Path(metric),
            summary_path=run_dir / "k6-summary.json",
            output_csv=run_dir / "client_metrics.csv",
            app=app,
        )

        write_metadata(
            metric_definition_path=Path(metric),
            run_dir=run_dir,
            run_id=run_id,
            host_url=host_url,
            k6_script=k6_script,
            test_start=test_start,
            test_end=test_end,
            window=PROMETHEUS_LATENCY_WINDOW,
        )

    finally:
        if prom_container is not None:
            stop_and_remove_container(prom_container)
        with docker_remote(config):
            docker_compose_down()

    print(f"\nDone in {test_end - test_start}s. Results in {run_dir.absolute()}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--app", required=True, choices=["es-cqrs", "crud"])
    parser.add_argument(
        "--metric",
        required=True,
        help="Path to the metric.json file which describes the metric.",
    )
    parser.add_argument(
        "--config",
        required=False,
        help="When trying to run on two VMs, must provide this file",
    )
    args = parser.parse_args()

    config_file = None
    if args.config is not None:
        config_file = Path(args.config)

    do_run(app=args.app, metric=Path(args.metric), config_file=config_file)


if __name__ == "__main__":
    main()
