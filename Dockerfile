# ==========================================
# STAGE 1: Build JAR with Maven & OpenJDK 21
# ==========================================
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy pom.xml và tải dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn và biên dịch package
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Lightweight Runtime Container
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Tạo user phi đặc quyền (non-root) để tăng tính bảo mật
RUN addgroup -S vnrgroup && adduser -S vnruser -G vnrgroup
USER vnruser

# Copy file jar từ builder stage
COPY --from=builder /build/target/*.jar app.jar

# Biến môi trường mặc định (Railway sẽ tự override biến PORT)
ENV PORT=8080
EXPOSE 8080

# Chạy ứng dụng Spring Boot với cấu hình nhận PORT từ Railway
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
