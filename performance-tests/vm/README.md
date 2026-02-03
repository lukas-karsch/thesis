# VM Setup

Performance tests were run on two Proxmox VMs. Instructions to replicate the setup can be
found [here](scripts/instructions.md)

## What's missing

The setup is almost completely automated. Two things are missing:

- the Ubuntu base image has to be extended manually [using instructions](scripts/instructions.md)
- The two VMs can not SSH into each other; an ssh key needs to be added
    - On the orchestrator, cd into `~/.ssh` and run `ssh keygen`
    - copy the public key
    - ssh into the server VM
    - `nano ~/.ssh/authorized_keys` > paste 

