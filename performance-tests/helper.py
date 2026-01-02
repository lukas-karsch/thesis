import subprocess


def run_command(cmd: list[str], check: bool = True) -> subprocess.CompletedProcess:
    print(f"> {' '.join(cmd)}")
    return subprocess.run(cmd, check=check)


def stop_and_remove_container(container_name: str) -> None:
    run_command(["docker", "stop", container_name])
    run_command(["docker", "rm", container_name])
