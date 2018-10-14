FROM java:8
EXPOSE 8081
ADD target/springboot-docker-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
CMD ["java", "-jar", "app.jar"]
