FROM java:8
RUN mkdir -p /usr/src/myapp
COPY Main.java /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac Main.java
CMD ["java", "Main"]
