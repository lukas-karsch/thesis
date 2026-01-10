# VM Setup

This setup works with Proxmox. It creates a ubuntu server VM with necessary packages to run performance tests: Python,
Conda, Docker.

## Prerequisites

- Install this image in proxmox: https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-amd64.img
- Run the following command to add qemu-guest-agent to the image:

```bash
virt-customize \
  -a noble-server-cloudimg-amd64.img \
  --install qemu-guest-agent \
  --run-command 'systemctl enable qemu-guest-agent'
```
- Copy the files `cloud-init.yml` and `create-vm.sh` to the Proxmox host (e.g. to `/var/lib/vz/snippets`)
- Customize user:
    - change name
    - **IMPORTANT:** change your ssh key in `cloud-init.yml` (paste your public key)
- Customize variables in `create-vm.sh`
- Make `create-vm-sh` executable:

```bash
chmod +x create-vm.sh
```

## Create the template

Run `create-vm.sh` on the proxmox host.

This will start the VM, do the setup tasks, then shut the VM down and turn it into a template.

## Run a VM

After setup is done, clone the template in Proxmox and run the server.

## Links

- https://www.proxmox.com/en/
- https://cloud-init.io/
