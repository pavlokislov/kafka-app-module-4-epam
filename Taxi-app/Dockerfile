FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/Taxi-app-0.0.1-SNAPSHOT.jar taxi-app.jar

EXPOSE 8080 5005
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/taxi-app.jar"]

