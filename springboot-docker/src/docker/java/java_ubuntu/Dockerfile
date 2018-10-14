FROM sshd:dockerfile-v1

RUN apt-get update && apt-get install -y openjdk-7-jre && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /data

WORKDIR /data

ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64

CMD ["/run.sh"]

