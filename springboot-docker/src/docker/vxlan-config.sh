PREFIX=vxlan 
IP=172.19.0.12
DESTIP=172.19.0.8
PORT=4789
VNI=1
SUBNETID=78
SUBNET=192.$VNI.$SUBNETID.0/24
VXSUBNET=192.$VNI.$SUBNETID.0/32
DEVNAME=$PREFIX.$VNI

ip link delete $DEVNAME 
ip link add $DEVNAME type vxlan id $VNI dev eth0 local $IP dstport $PORT nolearning
#echo '3' > /proc/sys/net/ipv4/neigh/$DEVNAME/app_solicit 
ip address add $VXSUBNET dev $DEVNAME 
ip link set $DEVNAME up 
#ip route del $SUBNET dev $DEVNAME scope global
#ip route add $SUBNET dev $DEVNAME scope global
