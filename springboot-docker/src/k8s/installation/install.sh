#!/usr/bin/env bash

curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add -
cat <<EOF > /etc/apt/sources.list.d/kubernetes.list
deb http://apt.kubernetes.io/ kubernetes-xenial main
EOF
apt-get update
apt-get install -y docker.io kubelet=1.11.3-00 kubectl=1.11.3-00 kubeadm=1.11.3-00
#kubeadm init --config kubeadm.yaml