#!/usr/bin/env bash

docker run -d -p 9090:9090 --name=prometheus06 -v /root/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml --network=host prom/prometheus