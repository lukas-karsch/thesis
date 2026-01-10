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
> UPDATE: no manual setup, use scripts and clout-init 
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
```bash
docker context create sut-remote --docker "host=ssh://user@vm-sut-ip"
docker context use sut-remote
```
Will turn this into python script.
Make sure maven project is built during cloud-init 
