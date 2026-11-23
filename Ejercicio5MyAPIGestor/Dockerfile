# syntax=docker/dockerfile:1
# Imagen del microservicio API REST (gestor-api) - Spring Boot 2.7.17 / Java 21

# ---------- Etapa 1: compilacion con Maven y JDK 21 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
# Las pruebas ya se ejecutan en el pipeline de Jenkins (mvn verify); aqui solo se empaqueta.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package \
 && cp target/*.jar /build/app.jar

# ---------- Etapa 2: imagen de ejecucion (solo JRE 21, usuario sin privilegios) ----------
FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.title="gestor-api" \
      org.opencontainers.image.description="API REST del Gestor de Proyectos (Ejercicio5MyAPIGestor)"
RUN addgroup -S gestor && adduser -S gestor -G gestor
WORKDIR /app
COPY --from=build /build/app.jar app.jar
USER gestor
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=6 \
  CMD wget -q -O /dev/null --post-data='' http://localhost:8081/buscarProyectos || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
