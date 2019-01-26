#!/usr/bin/env bash

yum install centos-release-gluster
yum -y install glusterfs glusterfs-fuse glusterfs-server

systemctl start glusterd.service
systemctl enable glusterd.service

