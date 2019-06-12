ip link delet veth1 type veth
ip netns delete ns1
iptables -t nat -F
iptables -F

ip link add veth1 type veth peer name veth2
ip link set veth1 up
brctl addif docker0 veth1
ip netns add ns1
ip link set veth2 netns ns1

ip netns exec ns1 ip addr add 192.1.78.2/24 dev veth2
ip netns exec ns1 ip link set lo up
ip netns exec ns1 ip link set veth2 up
ip netns exec ns1 route add default gw 192.1.78.1

iptables -t nat -A POSTROUTING -s 192.1.78.0/24 -o eth0 -j MASQUERADE

iptables -t filter -A FORWARD -s 192.1.0.0/16 -j ACCEPT
iptables -t filter -A FORWARD -d 192.1.0.0/16 -j ACCEPT
