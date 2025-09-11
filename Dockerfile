# Build stage
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Build application
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:24-jre
WORKDIR /app

ENV JAVA_OPTS=""

COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

