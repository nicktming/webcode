FROM ubuntu:14.04

MAINTAINER staryjie staryjie@163.com

RUN apt-get update && apt-get install -y openssh-server
RUN mkdir -p /var/run/sshd /root/.ssh
RUN sed -ri 's/session    required     pam_loginuid.so/#session    required     pam_loginuid.so/g' /etc/pam.d/sshd

ADD authorized_keys /root/.ssh/authorized_keys
ADD run.sh /run.sh
RUN chmod 755 /run.sh

EXPOSE 22

CMD ["/run.sh"]
