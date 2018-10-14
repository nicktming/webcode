FROM ubuntu:14.04
MAINTAINER nicktming

ENV DEBIAN_FRONTEND noninteractive

RUN echo "Asia/Shanghai" > /etc/timezone && dpkg-reconfigure -f noninteractive tzdata

RUN apt-get update && apt-get install -yg --no-install-recommends wget pwgen ca-certificates && apt-get clean && rm -rf /var/lib/apt/lists/*

ENV CATALINA_HOME /tomcat
ENV JAVA_HOME /jdk

ADD apache-tomcat-7.0.56 /tomcat
ADD jdk /jdk

ADD create_tomcat_admin_user.sh /create_tomcat_admin_user.sh
ADD run.sh /run.sh
RUN chmod +x /*.sh
RUN chmod +x /tomcat/bin/*.sh

EXPOSE 8080
CMD ["/run.sh"]
