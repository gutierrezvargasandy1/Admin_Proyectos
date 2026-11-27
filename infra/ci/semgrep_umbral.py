#!/usr/bin/env python3
"""Resume el reporte JSON de Semgrep y aplica el umbral del pipeline.

Uso: python3 semgrep_umbral.py <semgrep.json> <resumen.txt>
Codigos de salida: 0 = sin hallazgos ERROR, 2 = hay hallazgos ERROR, 1 = reporte invalido.
"""
import collections
import json
import sys


def main():
    if len(sys.argv) != 3:
        print(__doc__)
        return 1
    try:
        with open(sys.argv[1], encoding="utf-8") as f:
            datos = json.load(f)
    except (OSError, ValueError) as exc:
        print(f"No se pudo leer el reporte de Semgrep: {exc}")
        return 1

    hallazgos = datos.get("results", [])
    errores_analisis = datos.get("errors", [])
    analizados = datos.get("paths", {}).get("scanned", [])
    por_severidad = collections.Counter(h["extra"].get("severity", "INFO") for h in hallazgos)
    por_regla = collections.Counter(h["check_id"].split(".")[-1] for h in hallazgos)

    lineas = [
        "RESUMEN SEMGREP (SAST)",
        f"Archivos analizados : {len(analizados)}",
        f"Hallazgos totales   : {len(hallazgos)}",
        f"  ERROR             : {por_severidad.get('ERROR', 0)}",
        f"  WARNING           : {por_severidad.get('WARNING', 0)}",
        f"  INFO              : {por_severidad.get('INFO', 0)}",
        f"Errores de analisis : {len(errores_analisis)}",
        "",
    ]
    if hallazgos:
        lineas.append("Hallazgos por regla:")
        lineas += [f"  {n:3d}  {regla}" for regla, n in por_regla.most_common()]
        lineas.append("")
        lineas.append("Detalle:")
        for h in sorted(hallazgos, key=lambda x: (x["extra"].get("severity", ""), x["path"], x["start"]["line"])):
            lineas.append(f"  [{h['extra'].get('severity', 'INFO')}] {h['path']}:{h['start']['line']}  {h['check_id']}")
    else:
        lineas.append("Sin hallazgos.")

    texto = "\n".join(lineas)
    print(texto)
    with open(sys.argv[2], "w", encoding="utf-8") as f:
        f.write(texto + "\n")
    return 2 if por_severidad.get("ERROR", 0) else 0


if __name__ == "__main__":
    sys.exit(main())
