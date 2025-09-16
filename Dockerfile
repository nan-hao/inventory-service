# Build stage
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

# Cache dependencies first
ARG GITHUB_PACKAGES_USER
ARG GITHUB_PACKAGES_TOKEN
ENV GITHUB_PACKAGES_USER=${GITHUB_PACKAGES_USER}
ENV GITHUB_PACKAGES_TOKEN=${GITHUB_PACKAGES_TOKEN}
RUN mkdir -p /root/.m2 && \
    printf '<settings>\n  <servers>\n    <server>\n      <id>github-recipeforcode-platform</id>\n      <username>${env.GITHUB_PACKAGES_USER}</username>\n      <password>${env.GITHUB_PACKAGES_TOKEN}</password>\n    </server>\n  </servers>\n</settings>\n' > /root/.m2/settings.xml
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build application
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:24-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
