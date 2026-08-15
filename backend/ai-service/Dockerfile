FROM maven:3.9.9-eclipse-temurin-11 AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:11-jre
WORKDIR /app
ENV TZ=Asia/Shanghai
COPY --from=builder /workspace/target/ai-service-1.0.0.jar /app/ai-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/ai-service.jar"]
