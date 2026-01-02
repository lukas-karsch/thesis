import argparse
import json
import subprocess
import time
from datetime import datetime
from pathlib import Path
from urllib.parse import urlparse

import requests

# ============================================================================
# Defaults / Configuration
# ============================================================================

ES_CQRS_HOST = "http://localhost:8081"
CRUD_HOST = "http://localhost:8080"

PROMETHEUS_IMAGE = "prom/prometheus"
PROMETHEUS_PORT = 9090
PROMETHEUS_SCRAPE_INTERVAL_SECONDS = 5
PROMETHEUS_READY_RETRIES = 5
PROMETHEUS_READY_SLEEP_SECONDS = 2

PROMETHEUS_LATENCY_WINDOW = "2m"

# PROMETHEUS_QUERIES = {
#     "latency_avg.json": (
#         "rate(http_server_requests_seconds_sum[{w}]) "
#         "/ rate(http_server_requests_seconds_count[{w}])"
#     ),
#     "latency_p50.json": (
#         "histogram_quantile(0.50, sum by (le) "
#         "(rate(http_server_requests_seconds_bucket[{w}])))"
#     ),
#     "latency_p95.json": (
#         "histogram_quantile(0.95, sum by (le) "
#         "(rate(http_server_requests_seconds_bucket[{w}])))"
#     ),
#     "latency_p99.json": (
#         "histogram_quantile(0.99, sum by (le) "
#         "(rate(http_server_requests_seconds_bucket[{w}])))"
#     ),
# }

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
}


# ============================================================================
# Utility helpers
# ============================================================================


def run(cmd: list[str], check: bool = True) -> subprocess.CompletedProcess:
    print(f"> {' '.join(cmd)}")
    return subprocess.run(cmd, check=check)


def docker_available() -> bool:
    try:
        run(["docker", "--version"])
        return True
    except Exception as exc:
        print(exc)
        return False


def resolve_host(app: str) -> str:
    if app == "crud":
        return CRUD_HOST
    if app == "es-cqrs":
        return ES_CQRS_HOST
    raise ValueError(f"Unsupported app: {app}")


# ============================================================================
# Run setup
# ============================================================================


def create_run_dirs(k6_script: str, app: str) -> tuple[str, Path, Path]:
    script_name = Path(k6_script).stem
    run_date = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    run_id = f"run-{script_name}-{app}-{run_date}"

    run_dir = Path("run-k6") / Path(run_id)
    prom_dir = run_dir / "prometheus"

    prom_dir.mkdir(parents=True)

    return run_id, run_dir, prom_dir


def write_prometheus_config(
    config_path: Path,
    app_port: int,
) -> None:
    print(f"Writing prometheus config to '{config_path}'")
    config_path.touch()
    config_path.write_text(
        f"""
global:
  scrape_interval: {PROMETHEUS_SCRAPE_INTERVAL_SECONDS}s

scrape_configs:
  - job_name: "spring"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["host.docker.internal:{app_port}"]
""".strip()
    )


# ============================================================================
# Prometheus (Docker)
# ============================================================================


def start_prometheus_container(
    config_path: Path,
    run_date: str,
) -> str:
    container_name = f"prometheus-{run_date}"

    run(
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

    wait_for_prometheus()

    return container_name


def wait_for_prometheus() -> None:
    base_url = f"http://localhost:{PROMETHEUS_PORT}"

    for attempt in range(PROMETHEUS_READY_RETRIES):
        print(f"Waiting for Prometheus... ({attempt + 1}/{PROMETHEUS_READY_RETRIES})")
        try:
            resp = requests.get(f"{base_url}/-/ready")
            if resp.status_code == 200:
                print("Prometheus is healthy")
                return
        except Exception:
            pass

        time.sleep(PROMETHEUS_READY_SLEEP_SECONDS)

    raise RuntimeError("Prometheus did not become ready in time")


def stop_and_remove_container(container_name: str) -> None:
    run(["docker", "stop", container_name])
    run(["docker", "rm", container_name])


# ============================================================================
# k6
# ============================================================================


def run_k6(
    host_url: str,
    k6_script: str,
    run_dir: Path,
) -> tuple[int, int]:
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
    prom_url = f"http://localhost:{PROMETHEUS_PORT}/api/v1/query"

    for filename, query in PROMETHEUS_QUERIES.items():
        rendered_query = query.format(w=window)
        resp = requests.get(prom_url, params={"query": rendered_query})
        resp.raise_for_status()
        (prom_dir / filename).write_text(json.dumps(resp.json(), indent=2))


# ============================================================================
# Metadata
# ============================================================================


def write_metadata(
    run_dir: Path,
    run_id: str,
    host_url: str,
    k6_script: str,
    test_start: int,
    test_end: int,
    window: str,
) -> None:
    (run_dir / "notes.md").write_text(
        f"""# Prometheus Notes
> Run ID: {run_id}

- Host: {host_url}
- K6 script: {k6_script}
- Test start (epoch): {test_start}
- Test end (epoch):   {test_end}
- Scrape interval: {PROMETHEUS_SCRAPE_INTERVAL_SECONDS}s
- Latency window: {window}
"""
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--app", required=True, choices=["es-cqrs", "crud"])
    parser.add_argument("--k6-script", required=True)
    args = parser.parse_args()

    if not docker_available():
        raise RuntimeError("Docker is required but not available")

    host_url = resolve_host(args.app)
    app_port = urlparse(host_url).port

    run_id, run_dir, prom_dir = create_run_dirs(args.k6_script, args.app)

    prom_config = run_dir / "prometheus.yml"
    write_prometheus_config(prom_config, app_port)

    prom_container = start_prometheus_container(
        config_path=prom_config,
        run_date=run_id.split("-")[-1],
    )

    try:
        test_start, test_end = run_k6(
            host_url=host_url,
            k6_script=args.k6_script,
            run_dir=run_dir,
        )

        query_prometheus(
            prom_dir=prom_dir,
            window=PROMETHEUS_LATENCY_WINDOW,
        )

        write_metadata(
            run_dir=run_dir,
            run_id=run_id,
            host_url=host_url,
            k6_script=args.k6_script,
            test_start=test_start,
            test_end=test_end,
            window=PROMETHEUS_LATENCY_WINDOW,
        )
    finally:
        stop_and_remove_container(prom_container)

    print(f"\nDone in {test_end - test_start}s. Results in {run_dir.absolute()}")


if __name__ == "__main__":
    main()
