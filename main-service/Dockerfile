FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY main-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]