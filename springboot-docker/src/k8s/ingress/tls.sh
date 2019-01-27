#!/usr/bin/env bash

# http://blog.51cto.com/11205200/2316239?source=dra
# https://my.oschina.net/u/2306127/blog/1930169?from=timeline&isappinstalled=0

#openssl req -x509 -nodes -days 5000 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=tomcat.mtz.com"
#
#kubectl -n kube-system create secret tls dashboard-ingress-secret --key tls.key --cert tls.crt

openssl req -x509 -nodes -days 5000 -newkey rsa:2048 -keyout tls.key -out tls.crt -subj "/CN=k8s.dashboard.com"
kubectl create -n kube-system secret tls k8s-dashboard-ingress-secret --key tls.key --cert tls.crt