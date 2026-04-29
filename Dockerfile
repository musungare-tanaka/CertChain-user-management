FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /app/target/*.jar /app/app.jar
RUN mkdir -p /tmp/msu-cert-storage && chown -R spring:spring /tmp/msu-cert-storage /app

USER spring

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
