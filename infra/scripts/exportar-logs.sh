#!/usr/bin/env bash
# Exporta los registros recientes de los contenedores y el estado del servidor a evidencias/.
# Uso, dentro de la instancia:  bash ~/gestor/infra/scripts/exportar-logs.sh [periodo]   (por defecto 2h)
set -euo pipefail

cd "$(dirname "$0")/../.."
PERIODO="${1:-2h}"
SELLO=$(date +%Y%m%d_%H%M)
mkdir -p evidencias
SALIDA="evidencias/logs_${SELLO}.txt"

{
  echo "Registros del Gestor de Proyectos en AWS - $(date -Is) - ultimos ${PERIODO}"
  echo
  echo "== Estado de los contenedores"
  docker compose -f docker-compose.aws.yml ps
  echo
  echo "== Uso de recursos por contenedor"
  docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}'
  echo
  echo "== Memoria y disco del servidor"
  free -h
  df -h /
  echo
  echo "== Registros"
  docker compose -f docker-compose.aws.yml logs --no-color --timestamps --since "$PERIODO"
} > "$SALIDA" 2>&1

echo "Registros exportados en $SALIDA ($(wc -l < "$SALIDA") lineas)"
