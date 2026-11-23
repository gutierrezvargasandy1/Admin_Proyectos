#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Crea (o recrea) la base de datos de pruebas a partir de las migraciones oficiales
del proyecto (database/src/main/resources/db/migration/V1 y V2), para que todas
las corridas de la suite partan del mismo estado conocido.

No toca la base de trabajo (db_cbtis): usa una base aparte, db_pruebas por defecto.

Uso (PowerShell):
    $env:DB_ROOT_PASSWORD = "..."     # contrasena de root del contenedor MariaDB
    py preparar_bd.py
"""
import argparse
import getpass
import os
import re
import sys

try:
    import pymysql
except ImportError:  # pragma: no cover
    print("Falta la libreria pymysql. Instalala con:  py -m pip install pymysql")
    sys.exit(2)

AQUI = os.path.dirname(os.path.abspath(__file__))
MIGRACIONES = os.path.normpath(os.path.join(AQUI, "..", "..", "database", "src", "main", "resources", "db", "migration"))


def sentencias(ruta):
    with open(ruta, encoding="utf-8") as f:
        sin_comentarios = "\n".join(l for l in f.read().splitlines() if not l.strip().startswith("--"))
    return [s.strip() for s in sin_comentarios.split(";") if s.strip()]


def main():
    p = argparse.ArgumentParser(description="Prepara la base de datos de pruebas")
    p.add_argument("--host", default=os.getenv("DB_HOST", "127.0.0.1"))
    p.add_argument("--port", type=int, default=int(os.getenv("DB_PORT", "3307")))
    p.add_argument("--root-user", default=os.getenv("DB_ROOT_USER", "root"))
    p.add_argument("--root-password", default=os.getenv("DB_ROOT_PASSWORD"))
    p.add_argument("--db-name", default=os.getenv("DB_NAME", "db_pruebas"))
    p.add_argument("--app-user", default=os.getenv("DB_USER", "user"))
    a = p.parse_args()
    if not re.fullmatch(r"[A-Za-z0-9_]+", a.db_name) or not re.fullmatch(r"[A-Za-z0-9_]+", a.app_user):
        sys.exit("Nombre de base de datos o de usuario no valido")
    if not a.root_password:
        a.root_password = getpass.getpass(f"Contrasena de MariaDB para '{a.root_user}': ")

    con = pymysql.connect(host=a.host, port=a.port, user=a.root_user, password=a.root_password,
                          charset="utf8mb4", autocommit=True)
    with con.cursor() as cur:
        cur.execute("SELECT VERSION()")
        print(f"Conectado a MariaDB {cur.fetchone()[0]} en {a.host}:{a.port}")
        cur.execute(f"DROP DATABASE IF EXISTS `{a.db_name}`")
        cur.execute(f"CREATE DATABASE `{a.db_name}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
        cur.execute(f"USE `{a.db_name}`")
        for archivo in ("V1__esquema_inicial.sql", "V2__datos_semilla.sql"):
            lista = sentencias(os.path.join(MIGRACIONES, archivo))
            for s in lista:
                cur.execute(s)
            print(f"  {archivo}: {len(lista)} sentencias ejecutadas")
        cur.execute(f"GRANT ALL PRIVILEGES ON `{a.db_name}`.* TO '{a.app_user}'@'%'")
        cur.execute("FLUSH PRIVILEGES")
        cur.execute("SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_SCHEMA = %s ORDER BY 1", (a.db_name,))
        print(f"Base '{a.db_name}' lista. Tablas:")
        for nombre, filas in cur.fetchall():
            print(f"  - {nombre}")
    con.close()
    print(f"Permisos otorgados a '{a.app_user}'@'%'. Ya puedes arrancar la API apuntando a {a.db_name}.")


if __name__ == "__main__":
    main()
