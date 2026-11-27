#!/usr/bin/env bash
# Despliega (o actualiza) el Gestor de Proyectos en la instancia EC2.
# Uso, dentro de la instancia:  bash ~/gestor/infra/scripts/desplegar.sh
set -euo pipefail

cd "$(dirname "$0")/../.."
COMPOSE="docker compose -f docker-compose.aws.yml"

if [ ! -f .env ]; then
  echo "Falta el archivo .env. Crea uno con: cp .env.aws.example .env  (y escribe las contrasenas)"
  exit 1
fi

echo "== 1/4 Actualizando el codigo desde GitHub"
git pull --ff-only
git log --oneline -1

echo "== 2/4 Construyendo las imagenes una por una (la instancia tiene 2 GB de RAM)"
for servicio in gestor-db gestor-api gestor-cliente; do
  $COMPOSE build "$servicio"
done

echo "== 3/4 Levantando los contenedores"
$COMPOSE up -d --remove-orphans

echo "== 4/4 Esperando a que los microservicios esten saludables"
for intento in $(seq 1 40); do
  sanos=$($COMPOSE ps --format '{{.Name}} {{.Health}}' | grep -c ' healthy' || true)
  if [ "$sanos" -ge 3 ]; then
    break
  fi
  sleep 5
done
$COMPOSE ps

echo
echo "URL publica por HTTPS (tunel de Cloudflare):"
docker logs gestor-tunel 2>&1 | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1 || echo "  (aun no disponible: docker logs gestor-tunel)"
