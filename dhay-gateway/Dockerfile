# Bước 1: Build ứng dụng Gateway
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Gateway của Hào đang để SERVER_PORT=8080 trong docker-compose
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]