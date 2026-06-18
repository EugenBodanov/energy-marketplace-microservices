FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY messaging-contracts messaging-contracts/
RUN mvn -f messaging-contracts/pom.xml clean install -DskipTests

COPY trade/pom.xml trade/pom.xml
RUN mvn -f trade/pom.xml -DskipTests dependency:go-offline

COPY trade/src trade/src

RUN mvn -f trade/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/trade/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
