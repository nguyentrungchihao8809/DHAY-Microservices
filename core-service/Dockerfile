# Bước 1: Build ứng dụng bằng Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1.1. Chỉ copy file cấu hình Maven trước
COPY pom.xml .

# 1.2. Tải toàn bộ dependencies về máy (Layer này sẽ được Docker cache lại)
# Nếu bạn không sửa pom.xml, lần build sau Docker sẽ BỎ QUA bước tải này.
RUN mvn dependency:go-offline -B

# 1.3. Bây giờ mới copy mã nguồn và thực hiện build
COPY src ./src
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng với JRE nhẹ
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file jar từ bước build sang
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]