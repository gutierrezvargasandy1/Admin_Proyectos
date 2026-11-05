# gestor-db — Base de datos del Gestor de Proyectos

Migraciones versionadas (Flyway), datos semilla ficticios y pruebas automatizadas de la base de datos MariaDB.

| Archivo | Contenido |
|---|---|
| `src/main/resources/db/migration/V1__esquema_inicial.sql` | Tablas, llaves primarias y foraneas, utf8mb4 |
| `src/main/resources/db/migration/V2__datos_semilla.sql` | Datos de prueba ficticios (contrasena demo: `Demo1234`, cifrada con BCrypt) |
| `src/test/java/.../ScriptsSqlLintTest.java` | Analisis estatico de los scripts SQL (no necesita BD) |
| `src/test/java/.../EsquemaBaseDatosTest.java` | Pruebas contra MariaDB real en una BD temporal |
| `Jenkinsfile` | Pipeline: lint SQL -> migracion y pruebas -> SonarQube -> Quality Gate |

## Ejecutar las pruebas en local (PowerShell)

```powershell
$env:DB_ADMIN_PASSWORD = "rootpassword"   # MariaDB de Docker publicado en 127.0.0.1:3307
cd database
mvn verify
```

Las pruebas crean una base temporal `gestor_ci_xxxx`, le aplican las migraciones y la eliminan al terminar:
nunca modifican `db_cbtis`.
