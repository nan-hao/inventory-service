# Build stage
# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

# Cache dependencies first
ARG GITHUB_PACKAGES_USER
ARG GITHUB_PACKAGES_TOKEN
COPY pom.xml .
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -q -DskipTests dependency:go-offline

# Build application
COPY src ./src
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:24-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
