#!/usr/bin/env bash

# https://www.cnblogs.com/breezey/p/8849466.html
# https://www.jianshu.com/p/1069ddaaea78
# http://rdc.hundsun.com/portal/article/826.html
# http://rdcqii.hundsun.com/portal/article/827.html


yum install heketi heketi-client -y

ssh-keygen -t rsa

ssh-copy-id -i /root/.ssh/id_rsa.pub root@
ssh-copy-id -i /root/.ssh/id_rsa.pub root@

# vim /etc/heketi/heketi.json
#"executor": "ssh",
#
#    "_sshexec_comment": "SSH username and private key file information",
#    "sshexec": {
#      "keyfile": "/root/.ssh/id_rsa",
#      "user": "root",
#      "port": "22",
#      "fstab": "/etc/fstab"
#    },

# vim /usr/lib/systemd/system/heketi.service
# -> User=root

systemctl daemon-reload
systemctl enable heketi
systemctl start heketi

journalctl -u heketi

heketi-cli --server http://localhost:8089 topology load  --json=topology.json

heketi-cli --server http://localhost:8089 volume create --size=10 --replica=2