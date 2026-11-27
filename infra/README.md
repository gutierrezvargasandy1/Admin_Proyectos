# Infraestructura y operación del Gestor de Proyectos

| Carpeta / archivo | Contenido |
|---|---|
| `terraform/` | Infraestructura como código en AWS: instancia EC2 Ubuntu 24.04 (t3.small), IP elástica, grupo de seguridad, llave SSH y aprovisionamiento con cloud-init (Docker, Compose y swap). |
| `../docker-compose.aws.yml` | Servicios que corren en la instancia: MariaDB, API, cliente web, túnel de Cloudflare, Prometheus, node-exporter, blackbox-exporter y Grafana. |
| `monitoreo/prometheus/` | Objetivos de Prometheus (`prometheus.yml`) y 9 reglas de alerta (`alertas.yml`). |
| `monitoreo/blackbox/` | Módulos de sondeo HTTP y TCP. |
| `monitoreo/grafana/` | Fuente de datos y tablero «Gestor de Proyectos - Operación», cargados al iniciar Grafana. |
| `k6/prueba-carga.js` | Prueba de carga: inicio de sesión y consulta de proyectos y tareas con 5 a 20 usuarios virtuales. |
| `scripts/desplegar.sh` | Actualiza el código, construye las imágenes y levanta los contenedores en la instancia. |
| `scripts/exportar-logs.sh` | Guarda en `evidencias/` el estado, el consumo y los registros de los contenedores. |
| `ci/` | Validación de la configuración y umbral de Semgrep usados por Jenkins. |
| `Jenkinsfile` | Pipeline de infraestructura: `terraform fmt`, `terraform validate`, validación de la configuración y Semgrep. |

## Puertos

| Servicio | Puerto | Acceso |
|---|---|---|
| Cliente web | 80 → 8082 | Público (HTTP) y túnel HTTPS de Cloudflare |
| API REST | 8081 | Solo red interna de Docker |
| Actuator API / cliente | 9081 / 9082 | Solo red interna (Prometheus) |
| MariaDB | 3306 | Solo red interna |
| Prometheus | 9090 | 127.0.0.1 de la instancia (túnel SSH) |
| Grafana | 3000 | 127.0.0.1 de la instancia (túnel SSH) |
| SSH | 22 | Solo la IP del administrador |
