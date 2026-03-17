FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
# Tạo thư mục secrets để mount file firebase vào
RUN mkdir -p /app/secrets
ENTRYPOINT ["java", "-jar", "app.jar"]