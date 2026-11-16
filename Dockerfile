# ---------- Етап 1: збірка ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Етап 2: запуск ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/logs /app/data
COPY --from=build /build/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
