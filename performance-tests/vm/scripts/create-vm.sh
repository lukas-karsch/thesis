#!/bin/bash

set -euo pipefail

VMID=9000 # will be your VM template's ID
RAM_MB=12288 # RAM for the VM in MBs
CORES=4

echo "Starting script"

qm create $VMID \
  --name ubuntu-24.04-cloud \
  --memory $RAM_MB \
  --cores $CORES \
  --cpu host \
  --net0 virtio,bridge=vmbr0 \
  --ostype l26

echo "Created new VM"

IMAGE_PATH="/var/lib/vz/template/iso/noble-server-cloudimg-amd64.img"

if [[ ! -f "$IMAGE_PATH" ]]; then
  echo "ERROR: Cloud image not found at $IMAGE_PATH"
  exit 1
fi

qm importdisk $VMID "$IMAGE_PATH" local-lvm

echo "Imported disk"

qm set $VMID \
  --scsihw virtio-scsi-pci \
  --scsi0 local-lvm:vm-$VMID-disk-0

qm set $VMID \
  --boot order=scsi0 \
  --serial0 socket \
  --vga serial0

echo "Boot order set"

qm set $VMID --ide2 local-lvm:cloudinit

echo "Set cloud-init drive"

qm set $VMID \
  --cicustom "user=local:snippets/cloud-init.yml"\
  --ipconfig0 ip=dhcp

qm resize $VMID scsi0 20G

echo "Resized disk to 20GB"
echo "Starting VM"

qm set $VMID --agent enabled=1

qm start $VMID

echo "Waiting for QEMU guest agent"
# Wait until QEMU guest agent is ready
until qm agent $VMID ping &>/dev/null; do
  sleep 1
done

echo "Waiting for cloud-init completion"

# Wait for cloud-init completion
qm guest exec $VMID --timeout 0 -- bash -lc 'cloud-init status --wait'

echo "Cloud init completed."

qm shutdown $VMID

echo "VM was shut down"

qm template $VMID

echo "Turned VM into template."
