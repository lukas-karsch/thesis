import time

import requests


def poll(
    url: str,
    interval_seconds=2,
    retries=5,
    message: str | None = None,
    expected_status_code=200,
) -> None:
    for attempt in range(retries):
        msg = message if message is not None else f"Waiting for {url}..."
        print(f"{msg} ({attempt + 1}/{retries})")
        try:
            resp = requests.get(url)
            if resp.status_code == expected_status_code:
                print(f"Health check successful: {url}")
                return
        except Exception:
            pass

        time.sleep(interval_seconds)

    raise RuntimeError(f"{url} did not get healthy in time")
