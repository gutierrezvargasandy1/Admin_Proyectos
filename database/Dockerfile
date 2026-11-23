# Imagen de la base de datos (gestor-db): MariaDB 10.11 con el esquema y los datos semilla del proyecto.
# Los scripts de /docker-entrypoint-initdb.d se ejecutan solo la primera vez (volumen vacio),
# en orden alfabetico, sobre la base indicada en MARIADB_DATABASE.
FROM mariadb:10.11
LABEL org.opencontainers.image.title="gestor-db" \
      org.opencontainers.image.description="MariaDB 10.11 con las migraciones V1 (esquema) y V2 (datos semilla)"
COPY src/main/resources/db/migration/V1__esquema_inicial.sql /docker-entrypoint-initdb.d/01_V1__esquema_inicial.sql
COPY src/main/resources/db/migration/V2__datos_semilla.sql   /docker-entrypoint-initdb.d/02_V2__datos_semilla.sql
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=10 \
  CMD ["healthcheck.sh", "--connect", "--innodb_initialized"]
