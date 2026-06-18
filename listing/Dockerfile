# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY messaging-contracts messaging-contracts/
RUN mvn -f messaging-contracts/pom.xml clean install -DskipTests
COPY listing/pom.xml listing/pom.xml
COPY listing/src listing/src/
RUN mvn -f listing/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /build/listing/target/listing-*.jar listing-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "listing-service.jar"]

