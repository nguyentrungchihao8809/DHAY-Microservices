# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# BƯỚC QUAN TRỌNG: Tạo thư mục bằng quyền ROOT trước
RUN mkdir -p /app/secrets

# Sau đó mới tạo user và phân quyền cho thư mục app
RUN addgroup -S dhaygroup && adduser -S dhayuser -G dhaygroup \
    && chown -R dhayuser:dhaygroup /app

# Chuyển sang user thường để chạy app
USER dhayuser

# Copy file JAR từ stage build
COPY --from=build /app/target/*.jar app.jar

# Tối ưu hóa JVM
ENTRYPOINT ["java", "-XX:+UseParallelGC", "-jar", "app.jar"]