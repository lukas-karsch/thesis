#performance  #vm
## Provision VM 
This is using proxmox 
1. SSH into root@pve (root proxmox user)
2. `cd /var/lib/vz/snippets `
3. `./create-vm.sh`
4. Clone the created template 
5. In Orchestrator, create ssh key
6. Copy that ssh key to `Server/authorized_keys`
## Run tests 
1. ssh into Orchestrator 
2. `cd code/performance-tests`
3. `tmux`
4. chmod +x `name_of_the_script` 
   This makes the test script executable 
5. Run test script 
6. Detach from `tmux` using "CTRL+B"; followed by "D"
7. Results land in `run-k6` folder 
## Zip files 
To transfer files from the VM to my local machine, use this combination of zip and scp 
```bash
# on the VM
cd performance-tests/
zip -r run-k6.zip run-k6
# on the local machine 
scp -i "path/to/sshkey" thesis@vm_ip:code/performance-tests/run-k6.zip run-k6.zip
```
## Troubleshooting 
**Docker permission denied:** (but this should be run by cloud-init, usually)
``` bash
sudo usermod -aG docker $USER`
sudo reboot 
```
^RUN ON BOTH MACHINES !

**Need to add ssh key to hop from one VM to the other** 
1. on Orchestrator, run "ssh-keygen"
2. copy `id_ed2519.pub` 
3. Go to server 
4. `nano ~/.ssh/authorized_keys` > paste 
## Links 
- [[2026-01-10 Run performance tests on a VM]]
- [[2026-01-28 PERFORMANCE MUST DO]]
