FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY stats/stats-server/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]