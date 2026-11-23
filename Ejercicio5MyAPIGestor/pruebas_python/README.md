# Pruebas de base de datos y endpoints (Práctica 1)

Suite en Python que prueba los **28 endpoints** de la API `Ejercicio5MyAPIGestor` y verifica en MariaDB
(consultas `SELECT` antes y después) que cada operación se refleje en la base de datos.

| Archivo | Para qué sirve |
|---|---|
| `preparar_bd.py` | Crea la base `db_pruebas` desde las migraciones oficiales (`database/.../V1` y `V2`). No toca `db_cbtis`. |
| `pruebas_endpoints.py` | Ejecuta 64 casos (2 de BD, 61 de endpoints y 1 de integridad en cascada) y guarda `resultados/<etiqueta>/resultados.json` y `log.txt`. |
| `requirements.txt` | Única dependencia: `pymysql`. |

## Cómo ejecutarla (Windows / PowerShell)

Requisitos: MariaDB del `docker-compose.yml` en ejecución (puerto 3307) y Python 3.10 o superior.

```powershell
cd Ejercicio5MyAPIGestor\pruebas_python
py -m pip install -r requirements.txt

# 1. Base de datos de pruebas (estado conocido)
$env:DB_ROOT_PASSWORD = "<contraseña de root de MariaDB>"
py preparar_bd.py

# 2. En OTRA terminal: arrancar la API contra db_pruebas
cd Ejercicio5MyAPIGestor
$env:DB_URL = "jdbc:mariadb://127.0.0.1:3307/db_pruebas"
$env:DB_PASSWORD = "<contraseña del usuario user>"
mvn spring-boot:run

# 3. Ejecutar la suite
$env:DB_PASSWORD = "<contraseña del usuario user>"
py pruebas_endpoints.py --etiqueta despues
```

Las contraseñas se leen de variables de entorno (o se piden en pantalla); no se guardan en el código.

## Códigos de respuesta esperados

| Situación | Respuesta |
|---|---|
| Operación correcta (alta, cambio, baja) | HTTP 200 con `1` |
| Operación no realizada (registro inexistente, contraseña vacía) | HTTP 200 con `0` |
| Falta un parámetro, parámetro no numérico o JSON mal formado | HTTP 400 con `{"resultado":0, "mensaje": "..."}` |
| Dato que viola una regla de la BD (NOT NULL, UNIQUE, llave foránea, fecha inválida) | HTTP 409 con `{"resultado":0, "mensaje": "..."}` |
| Login inválido | HTTP 200 con un usuario cuyo `nombre` es `"000"` |
