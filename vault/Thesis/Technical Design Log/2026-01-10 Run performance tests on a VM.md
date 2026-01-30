## Current setup - Recap
- One script 
- Runs on one machine 
- Starts docker containers, then runs k6 
Problem: The k6 script and the server compete for CPU time 
## Solution 
- Use two machines - one as orchestrator (client), one as server under test 
- Proxmox setup 
## Proxmox VM configuration
Gemini suggests:

For performance testing, standard virtualization overhead can skew results. Use these specific settings for both VMs:
- **CPU Type:** Set to **"host"** (Host Passthrough). This allows the VM to use the host's specialized instructions (AES-NI, etc.), improving performance by 3-5% and ensuring the benchmark is representative of real hardware.
- **Disk Controller:** Use **VirtIO SCSI Single**. Enable the **"IO Thread"** and **"Discard"** (TRIM) options for the best I/O throughput.
- **Memory:** Disable "Ballooning." Pre-allocating the full RAM amount prevents the Proxmox host from reclaiming memory during a heavy load test, which would cause unpredictable latency spikes.
## Creating VM
1. Download / upload Ubuntu iso (Ubuntu 24.04, latest as of today)
2. Click "Create VM"
3. Settings:
   CPU: type "host"; 4 cores 
   RAM: 12288MiB (12GB)
   Disk: 32 GB
   BIOS: UEFI
> UPDATE: no manual setup, use scripts and cloud-init [[#Script setup]]
## Script setup 
1. Script creates template:
	1. Create VM
	2. Add -cicustom cloud-init file 
	3. Wait for cloud-init to finish 
	4. Stop VM
	5. Turn VM into template 
2. Create VMs from template 
`/performance-tests/vm`
## Orchestration
Can use docker contexts https://docs.docker.com/engine/manage-resources/contexts/
```bash
docker context create sut-remote --docker "host=ssh://user@vm-sut-ip"
docker context use sut-remote
```
Default context (own machine) is `default`.
 ```bash
 cd /code 
 docker compose up # will run on the remote machine if context is active
 ```
## Long running task with tmux 
When starting many_runs, it takes a long time (several hours depending on configuration). Closing the terminal kills the task. 
I can use `tmux` to start the task in a terminal session which is detached from my client. 
```bash
tmux
# run command 
# Detach with CTRL+B, let go and hit D 
```
Later, I can reattach using 
```bash 
tmux attach
```
Once attached, I can kill the session:
```bash
# type CTRL+B
:kill-session
```
## Zip files 
To transfer files from the VM to my local machine, I want to use a combination of zip and scp. 
```bash
# on the VM
cd performance-tests/
zip -r run-k6.zip run-k6
# on the local machine 
scp -i "path/to/sshkey" thesis@vm_ip:code/performance-tests/run-k6.zip run-k6.zip
```
## Troubleshooting 
If something isnt working...

Docker permission denied: 
``` bash
sudo usermod -aG docker $USER`
sudo reboot 
```
^RUN ON BOTH MACHINES !

Need to add ssh key to hop from one VM to the other 
1. on Orchestrator, run "ssh-keygen"
2. copy id_ed2519.pub 
3. Go to server 
4. `nano authorized_keys` > paste 
