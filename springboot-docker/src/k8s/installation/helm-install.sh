#!/usr/bin/env bash

wget https://storage.googleapis.com/kubernetes-helm/helm-v2.11.0-linux-amd64.tar.gz
tar -zxvf helm-v2.11.0-linux-amd64.tar.gz
cd linux-amd64/
cp helm /usr/local/bin/

kubectl apply -f tiller-rbac-config.yaml

helm init --service-account tiller --skip-refresh

