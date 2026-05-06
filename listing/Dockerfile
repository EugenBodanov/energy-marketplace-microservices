# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src src/
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /build/target/listing-*.jar listing-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "listing-service.jar"]

