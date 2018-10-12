# 基础镜像信息
FROM ubuntu:14.04

# 维护者信息
MAINTAINER staryjie staryjie@163.com

# 更新apt缓存、安装ssh服务
RUN apt-get update && apt-get install -y openssh-server
RUN mkdir -p /var/run/sshd /root/.ssh
RUN sed -ri 's#session    required     pam_loginuid.so#session    required     pam_loginuid.so#g' /etc/pam.d/sshd

# 配置免密要和自启动脚本
ADD authorized_keys /root/.ssh/authorized_keys
ADD run.sh /run.sh
RUN chmod 755 /run.sh

# 暴露22端口
EXPOSE 22

# 设置脚本自启动
CMD ["/run.sh"]
