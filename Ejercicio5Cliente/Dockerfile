# syntax=docker/dockerfile:1
# Imagen del microservicio cliente web (gestor-cliente) - Spring Boot 3.2.5 / Thymeleaf / Java 21

# ---------- Etapa 1: compilacion con Maven y JDK 21 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package \
 && cp target/*.jar /build/app.jar

# ---------- Etapa 2: imagen de ejecucion (solo JRE 21, usuario sin privilegios) ----------
FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.title="gestor-cliente" \
      org.opencontainers.image.description="Cliente web del Gestor de Proyectos (Ejercicio5Cliente)"
RUN addgroup -S gestor && adduser -S gestor -G gestor
WORKDIR /app
COPY --from=build /build/app.jar app.jar
USER gestor
EXPOSE 8082
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=6 \
  CMD wget -q -O /dev/null http://localhost:8082/login || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
