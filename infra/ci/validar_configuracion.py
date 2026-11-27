#!/usr/bin/env python3
"""Valida la configuracion de despliegue y monitoreo antes de llevarla a la nube.

Revisa que los YAML y JSON sean validos y aplica reglas propias del proyecto:
  - cada servicio de docker-compose.aws.yml tiene limite de memoria, politica de reinicio y rotacion de registros;
  - solo el cliente web se publica en todas las interfaces; Prometheus y Grafana solo en 127.0.0.1;
  - no hay contrasenas escritas en el compose (se leen del archivo .env);
  - cada regla de alerta tiene duracion, severidad y resumen.
Codigo de salida 0 si todo cumple, 1 si hay errores.
"""
import json
import pathlib
import re
import sys

import yaml

RAIZ = pathlib.Path(__file__).resolve().parents[2]
errores = []


def revisar(condicion, mensaje):
    print(("  OK    " if condicion else "  ERROR ") + mensaje)
    if not condicion:
        errores.append(mensaje)


def cargar_yaml(ruta):
    try:
        return yaml.safe_load(ruta.read_text(encoding="utf-8"))
    except yaml.YAMLError as exc:
        revisar(False, f"{ruta.relative_to(RAIZ)}: YAML invalido ({exc})")
        return None


print("1. Sintaxis de archivos YAML y JSON")
for ruta in sorted([*RAIZ.glob("docker-compose*.yml"), *RAIZ.glob("infra/monitoreo/**/*.yml")]):
    revisar(cargar_yaml(ruta) is not None, f"{ruta.relative_to(RAIZ)}")
for ruta in sorted(RAIZ.glob("infra/monitoreo/**/*.json")):
    try:
        json.loads(ruta.read_text(encoding="utf-8"))
        revisar(True, f"{ruta.relative_to(RAIZ)}")
    except ValueError as exc:
        revisar(False, f"{ruta.relative_to(RAIZ)}: JSON invalido ({exc})")

print("2. Reglas del despliegue en AWS (docker-compose.aws.yml)")
compose = cargar_yaml(RAIZ / "docker-compose.aws.yml") or {}
for nombre, servicio in compose.get("services", {}).items():
    revisar("mem_limit" in servicio, f"{nombre}: tiene limite de memoria")
    revisar(servicio.get("restart") == "unless-stopped", f"{nombre}: se reinicia solo (unless-stopped)")
    revisar(servicio.get("logging", {}).get("options", {}).get("max-size") is not None, f"{nombre}: rota sus registros")
    for puerto in servicio.get("ports", []):
        publico = not str(puerto).startswith("127.0.0.1:")
        revisar(not publico or nombre == "gestor-cliente", f"{nombre}: puerto {puerto} {'publico' if publico else 'solo local'}")
    for clave, valor in (servicio.get("environment") or {}).items():
        if re.search(r"PASSWORD", clave):
            revisar(str(valor).startswith("${"), f"{nombre}: {clave} se lee del archivo .env")

print("3. Reglas de alerta de Prometheus")
alertas = cargar_yaml(RAIZ / "infra/monitoreo/prometheus/alertas.yml") or {}
total = 0
for grupo in alertas.get("groups", []):
    for regla in grupo.get("rules", []):
        total += 1
        nombre = regla.get("alert", "?")
        revisar(bool(regla.get("expr")) and bool(regla.get("for")), f"{grupo['name']}/{nombre}: expresion y duracion")
        revisar(regla.get("labels", {}).get("severidad") in {"critica", "advertencia"}, f"{grupo['name']}/{nombre}: severidad")
        revisar(bool(regla.get("annotations", {}).get("resumen")), f"{grupo['name']}/{nombre}: resumen")
revisar(total > 0, f"{total} reglas de alerta definidas")

print()
if errores:
    print(f"Validacion con {len(errores)} error(es).")
    sys.exit(1)
print("Validacion correcta.")
