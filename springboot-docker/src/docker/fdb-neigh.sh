ip route add 192.1.87.0/24 via 192.1.87.0 dev vxlan.1 onlink
bridge fdb add $1 dev vxlan.1 dst 172.19.0.8
#ip neighbor add 172.19.0.8 lladdr $1 dev vxlan.1
ip neighbor add 192.1.87.0 lladdr $1 dev vxlan.1
