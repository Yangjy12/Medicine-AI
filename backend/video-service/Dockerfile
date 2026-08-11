FROM maven:3.9.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:11-jre
WORKDIR /app
ENV TZ=Asia/Shanghai
COPY --from=build /app/target/video-service-1.0.0.jar /app/video-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/video-service.jar"]
