# Despliegue con Docker y Cloudflare Tunnel

El sistema se despliega con **una imagen Docker por microservicio** y se publica en internet a través de la red de **Cloudflare** con un túnel. No se modifica nada de la cuenta de Cloudflare: Pages (GestoPago) y R2 (El Viejo Capi) quedan igual.

```
Internet ──https──▶ Cloudflare (trycloudflare.com) ──túnel──▶ gestor-tunel
                                                               │ red interna gestor-net
                                         gestor-cliente:8082 ◀─┘
                                                │ http://gestor-api:8081
                                         gestor-api:8081
                                                │ jdbc:mariadb://gestor-db:3306/db_cbtis
                                         gestor-db (MariaDB 10.11, volumen gestor-db-data)
```

| Imagen | Origen | Contenido |
|---|---|---|
| `gestor-db:1.0` | `database/Dockerfile` | MariaDB 10.11 + migraciones V1 (esquema) y V2 (datos semilla) |
| `gestor-api:1.0` | `Ejercicio5MyAPIGestor/Dockerfile` | API REST, Spring Boot 2.7.17, JRE 21, usuario sin privilegios |
| `gestor-cliente:1.0` | `Ejercicio5Cliente/Dockerfile` | Cliente web, Spring Boot 3.2.5 + Thymeleaf, JRE 21 |
| `cloudflare/cloudflared` | Docker Hub | Túnel de Cloudflare que publica el cliente |

Las imágenes de la API y del cliente se construyen en dos etapas: Maven + JDK 21 compila y una imagen JRE 21 Alpine ejecuta. Cada una tiene `HEALTHCHECK`, y el compose levanta los servicios en orden (BD sana → API sana → cliente sano → túnel).

## Pasos (PowerShell, en la carpeta del repositorio)

```powershell
copy .env.example .env
notepad .env          # define DB_ROOT_PASSWORD y DB_PASSWORD
docker compose -f docker-compose.despliegue.yml up -d --build
docker compose -f docker-compose.despliegue.yml ps      # los 4 servicios "running (healthy)"
docker logs gestor-tunel                                 # busca la línea https://….trycloudflare.com
```

- La aplicación queda en la URL `https://….trycloudflare.com` (pública) y en http://localhost:8092 (local).
- La API solo escucha en `127.0.0.1:8091` y la BD en `127.0.0.1:3308`; no se exponen a internet.
- La URL del túnel rápido cambia cada vez que se reinicia el contenedor `gestor-tunel`.

## Verificación del despliegue con la suite de pruebas

```powershell
cd Ejercicio5MyAPIGestor\pruebas_python
$env:DB_PASSWORD = "<la de .env>"
py pruebas_endpoints.py --api http://localhost:8091 --db-port 3308 --db-name db_cbtis --db-user gestor --etiqueta despliegue
```

## Detener, actualizar o borrar

```powershell
docker compose -f docker-compose.despliegue.yml down              # detiene y conserva los datos
docker compose -f docker-compose.despliegue.yml up -d --build     # reconstruye tras un cambio
docker compose -f docker-compose.despliegue.yml down -v           # detiene y BORRA el volumen de datos
```

## Opción con dominio propio

Si se agrega un dominio a Cloudflare, se puede usar un túnel con nombre (Zero Trust → Networks → Tunnels) y un subdominio fijo: en el servicio `gestor-tunel` se cambia el comando por `tunnel --no-autoupdate run` y se agrega la variable `TUNNEL_TOKEN` con el token del túnel.
