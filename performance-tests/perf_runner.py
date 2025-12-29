import argparse
import subprocess
import time
from datetime import datetime
from pathlib import Path
from urllib.parse import urlparse

ES_CQRS_HOST = "http://localhost:8081"
CRUD_HOST = "http://localhost:8080"


def run(cmd, check=True):
    print(f"> {' '.join(cmd)}")
    return subprocess.run(cmd, check=check)


def docker_available():
    try:
        print("Checking docker...")
        run(["docker", "--version"])
        return True
    except Exception as e:
        print(e)
        return False


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--app",
        required=True,
        help="Which app is running.",
        choices=["es-cqrs", "crud"],
    )
    parser.add_argument("--k6-script", required=True, help="Path to k6 script")
    args = parser.parse_args()

    if not docker_available():
        raise RuntimeError("Docker is required but not available")

    host_url = (
        CRUD_HOST
        if args.app == "crud"
        else (ES_CQRS_HOST if args.app == "cqrs" else None)
    )
    if not host_url:
        raise ValueError(f"host_url can not be set from {args.app}")
    parsed = urlparse(host_url)

    app_port = parsed.port

    ########################################
    # Run folder
    ########################################
    script_name = args.k6_script.replace(".js", "")
    run_id = (
        f"run-{script_name}-{args.app}-{datetime.now().strftime('%Y-%m-%d_%H-%M-%S')}"
    )
    run_dir = Path(run_id)
    prom_dir = run_dir / "prometheus"
    prom_dir.mkdir(parents=True)

    ########################################
    # Prometheus config
    ########################################
    prom_config = run_dir / "prometheus.yml"
    prom_config.write_text(
        f"""
global:
  scrape_interval: 2s

scrape_configs:
  - job_name: "spring"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["host.docker.internal:{app_port}"]
""".strip()
    )

    prom_container = f"prometheus-{run_id}"

    # run(
    #     [
    #         "docker",
    #         "run",
    #         "-d",
    #         "--name",
    #         prom_container,
    #         "-p",
    #         "9090:9090",
    #         "-v",
    #         f"{prom_config.absolute()}:/etc/prometheus/prometheus.yml",
    #         "prom/prometheus",
    #     ]
    # )

    # print("Prometheus started")
    # time.sleep(5)

    ########################################
    # Run k6
    ########################################
    test_start = int(time.time())

    k6_summary_json = run_dir / "k6-summary.json"
    k6_summary_txt = run_dir / "k6-summary.txt"

    with k6_summary_txt.open("w") as out:
        print("Running k6...")
        subprocess.run(
            [
                "k6",
                "run",
                f"--summary-export={k6_summary_json}",
                "-e",
                f"HOST={host_url}",
                args.k6_script,
            ],
            stdout=out,
            stderr=subprocess.STDOUT,
            check=True,
        )

    test_end = int(time.time())

    ########################################
    # Prometheus queries
    ########################################
    prom_url = "http://localhost:9090/api/v1/query"
    window = "1m"

    queries = {
        "latency_avg.json": f"rate(http_server_requests_seconds_sum[{window}])"
        f" / rate(http_server_requests_seconds_count[{window}])",
        "latency_p50.json": f"histogram_quantile(0.50, sum by (le) "
        f"(rate(http_server_requests_seconds_bucket[{window}])))",
        "latency_p95.json": f"histogram_quantile(0.95, sum by (le) "
        f"(rate(http_server_requests_seconds_bucket[{window}])))",
        "latency_p99.json": f"histogram_quantile(0.99, sum by (le) "
        f"(rate(http_server_requests_seconds_bucket[{window}])))",
    }

    #     for filename, query in queries.items():
    #         resp = requests.get(prom_url, params={"query": query})
    #         resp.raise_for_status()
    #         (prom_dir / filename).write_text(json.dumps(resp.json(), indent=2))
    #
    #     ########################################
    #     # Metadata
    #     ########################################
    #     notes = run_dir / "notes.md"
    #     notes.write_text(
    #         f"""Run ID: {run_id}
    # Host: {host_url}
    # K6 script: {args.k6_script}
    # Test start (epoch): {test_start}
    # Test end (epoch):   {test_end}
    # Scrape interval: 2s
    # Latency window: {window}
    # """
    #     )
    #
    #     ########################################
    #     # Cleanup
    #     ########################################
    #     run(["docker", "stop", prom_container])
    #     run(["docker", "rm", prom_container])

    print(f"\nDone in {test_end - test_start}s. Results stored in {run_dir.absolute()}")


if __name__ == "__main__":
    main()
