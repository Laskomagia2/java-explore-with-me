FROM amazoncorretto:21-alpine
WORKDIR /app
COPY main-service/target/main-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]