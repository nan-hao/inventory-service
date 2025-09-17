# syntax=docker/dockerfile:1.7
# Runtime-only image; JAR is built in CI
FROM azul/zulu-openjdk:25-jre
WORKDIR /app

# Copy the already-built Spring Boot fat JAR from the CI workspace
COPY target/*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]