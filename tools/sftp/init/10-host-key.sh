#!/bin/sh
set -eu

cp /etc/ssh/hostkeys/ssh_host_ed25519_key /etc/ssh/ssh_host_ed25519_key
chown root:root /etc/ssh/ssh_host_ed25519_key
chmod 600 /etc/ssh/ssh_host_ed25519_key

ssh-keygen -y -f /etc/ssh/ssh_host_ed25519_key > /etc/ssh/ssh_host_ed25519_key.pub
