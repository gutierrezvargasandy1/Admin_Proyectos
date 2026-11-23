#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Practica 1 - Pruebas de Base de Datos y Endpoints del Backend
Sistema Gestor de Proyectos - API REST Ejercicio5MyAPIGestor

Ejecuta pruebas contra los 28 endpoints de la API y verifica en MariaDB
(SELECT antes / despues) que cada operacion se refleje en la base de datos.

Cada caso registra: ID, metodo, ruta, datos enviados, codigo HTTP, respuesta,
consulta SQL de verificacion, resultado esperado, resultado obtenido y estado.

Uso:
    py -m pip install -r requirements.txt
    set DB_PASSWORD=...            (PowerShell: $env:DB_PASSWORD="...")
    py pruebas_endpoints.py --etiqueta despues

Salida: consola + resultados/<etiqueta>/resultados.json + resultados/<etiqueta>/log.txt
"""
import argparse
import datetime
import getpass
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

try:
    import pymysql
    import pymysql.cursors
except ImportError:  # pragma: no cover
    print("Falta la libreria pymysql. Instalala con:  py -m pip install pymysql")
    sys.exit(2)

# ------------------------------------------------------------------ consola
VERDE, ROJO, AMARILLO, CIAN, GRIS, NEGRITA, FIN = (
    "\033[92m", "\033[91m", "\033[93m", "\033[96m", "\033[90m", "\033[1m", "\033[0m")
if os.name == "nt":
    os.system("")  # habilita colores ANSI en la consola de Windows

INEXISTENTE = 999999


class Suite:
    def __init__(self, args):
        self.api = args.api.rstrip("/")
        self.etiqueta = args.etiqueta
        self.tag = datetime.datetime.now().strftime("%H%M%S")
        self.casos = []
        self.log_lineas = []
        self.db = pymysql.connect(
            host=args.db_host, port=args.db_port, user=args.db_user, password=args.db_password,
            database=args.db_name, charset="utf8mb4", autocommit=True,
            cursorclass=pymysql.cursors.DictCursor)
        self.config = {
            "api": self.api, "bd": f"{args.db_user}@{args.db_host}:{args.db_port}/{args.db_name}",
            "etiqueta": self.etiqueta, "inicio": datetime.datetime.now().isoformat(timespec="seconds"),
            "python": sys.version.split()[0],
        }

    # ------------------------------------------------------------ utilidades
    def out(self, texto="", color=""):
        print(f"{color}{texto}{FIN if color else ''}")
        self.log_lineas.append(texto)

    def texto_sql(self, consulta, *params):
        with self.db.cursor() as cur:
            return cur.mogrify(consulta, params) if params else consulta

    def sql(self, consulta, *params):
        with self.db.cursor() as cur:
            cur.execute(consulta, params)
            filas = cur.fetchall()
        return [{k: (str(v) if v is not None and not isinstance(v, (int, float)) else v)
                 for k, v in f.items()} for f in filas]

    def uno(self, consulta, *params):
        filas = self.sql(consulta, *params)
        return filas[0] if filas else None

    def contar(self, tabla, where="1=1", *params):
        return self.uno(f"SELECT COUNT(*) AS n FROM {tabla} WHERE {where}", *params)["n"]

    def http(self, metodo, ruta, params=None, cuerpo=None, crudo=None):
        url = self.api + ruta
        if params:
            url += "?" + urllib.parse.urlencode(params)
        datos = None
        headers = {}
        if crudo is not None:
            datos = crudo.encode("utf-8")
            headers["Content-Type"] = "application/json"
        elif cuerpo is not None:
            datos = json.dumps(cuerpo).encode("utf-8")
            headers["Content-Type"] = "application/json"
        elif metodo in ("POST", "DELETE"):
            headers["Content-Type"] = "application/json"
        req = urllib.request.Request(url, data=datos, method=metodo, headers=headers)
        t0 = time.perf_counter()
        try:
            with urllib.request.urlopen(req, timeout=20) as r:
                estado, texto = r.status, r.read().decode("utf-8")
        except urllib.error.HTTPError as e:
            estado, texto = e.code, e.read().decode("utf-8", "replace")
        ms = round((time.perf_counter() - t0) * 1000)
        try:
            js = json.loads(texto) if texto else None
        except ValueError:
            js = None
        return estado, texto, js, ms

    # ------------------------------------------------------------ registro
    def caso(self, cid, modulo, descripcion, metodo, ruta, esperado, http_esperado,
             verificar, params=None, cuerpo=None, crudo=None, sql_antes=None, sql_despues=None,
             tipo="Endpoint"):
        """Ejecuta un caso. verificar(estado, js, texto, antes, despues) -> (ok, obtenido)."""
        antes = self.sql(*sql_antes) if sql_antes else None
        estado, texto, js, ms = (None, "", None, 0)
        if ruta:
            estado, texto, js, ms = self.http(metodo, ruta, params, cuerpo, crudo)
        despues = self.sql(*sql_despues) if sql_despues else None
        try:
            ok, obtenido = verificar(estado, js, texto, antes, despues)
        except Exception as e:  # una respuesta inesperada no debe detener la suite
            ok, obtenido = False, f"Respuesta inesperada ({type(e).__name__}: {e})"
        entrada = {}
        if params:
            entrada["query"] = params
        if cuerpo is not None:
            entrada["json"] = cuerpo
        if crudo is not None:
            entrada["crudo"] = crudo
        reg = {
            "id": cid, "modulo": modulo, "tipo": tipo, "descripcion": descripcion,
            "metodo": metodo, "ruta": ruta,
            "url": (self.api + ruta + ("?" + urllib.parse.urlencode(params) if params else "")) if ruta else None,
            "entrada": entrada, "esperado": esperado, "http_esperado": http_esperado,
            "http": estado, "ms": ms, "respuesta": texto[:1500],
            "sql_antes": self.texto_sql(*sql_antes) if sql_antes else None, "bd_antes": antes,
            "sql_despues": self.texto_sql(*sql_despues) if sql_despues else None, "bd_despues": despues,
            "obtenido": obtenido, "estado": "PASA" if ok else "FALLA",
        }
        self.casos.append(reg)
        self.imprimir(reg)
        return reg

    def imprimir(self, r):
        color = VERDE if r["estado"] == "PASA" else ROJO
        self.out(f"[{r['id']}] {r['descripcion']}", NEGRITA + CIAN)
        if r["ruta"]:
            self.out(f"   Peticion : {r['metodo']} {r['url']}")
            if r["entrada"]:
                self.out(f"   Enviado  : {json.dumps(r['entrada'], ensure_ascii=False)}")
            resp = r["respuesta"] if len(r["respuesta"]) <= 300 else r["respuesta"][:300] + " ..."
            self.out(f"   HTTP     : {r['http']}  ({r['ms']} ms)   Respuesta: {resp}")
        if r["sql_antes"]:
            self.out(f"   BD antes : {r['sql_antes']}  ->  {json.dumps(r['bd_antes'], ensure_ascii=False)}", GRIS)
        if r["sql_despues"]:
            self.out(f"   BD despues: {r['sql_despues']}  ->  {json.dumps(r['bd_despues'], ensure_ascii=False)}", GRIS)
        self.out(f"   Esperado : {r['esperado']}")
        self.out(f"   Obtenido : {r['obtenido']}")
        self.out(f"   Estado   : {r['estado']}", NEGRITA + color)
        self.out()

    def seccion(self, titulo):
        self.out("=" * 100, AMARILLO)
        self.out(f"  {titulo}", NEGRITA + AMARILLO)
        self.out("=" * 100, AMARILLO)

    # ------------------------------------------------------------ verificadores
    @staticmethod
    def es_texto(valor):
        return lambda st, js, tx, a, d: (st == 200 and tx.strip() == valor,
                                          f"HTTP {st}, respuesta {tx.strip()[:120]!r}")

    @staticmethod
    def error_controlado(http_esperado, palabra=None):
        def v(st, js, tx, a, d):
            ok = (st == http_esperado and isinstance(js, dict) and js.get("resultado") == 0
                  and bool(js.get("mensaje")) and (palabra is None or palabra in js.get("mensaje", "")))
            msg = js.get("mensaje") if isinstance(js, dict) else None
            return ok, f"HTTP {st}, mensaje={msg!r}" + ("" if msg else f", cuerpo={tx[:160]!r}")
        return v

    # ================================================================ PRUEBAS
    def ejecutar(self):
        t = self.tag
        self.out(f"Pruebas de BD y endpoints - Gestor de Proyectos   etiqueta={self.etiqueta}", NEGRITA)
        self.out(f"API: {self.config['api']}   BD: {self.config['bd']}   inicio: {self.config['inicio']}")
        self.out()

        # ------------------------------------------------------------ BD
        self.seccion("1. BASE DE DATOS: conexion, esquema y datos")
        self.caso("BD-01", "Base de datos", "Conexion a MariaDB y tablas del esquema", "SQL", None,
                  "Conexion exitosa y 6 tablas (usuario, proyectos, tareas, asignaciones, comentarios, hibernate_sequence)",
                  None,
                  lambda st, js, tx, a, d: (len(d) == 6, f"{len(d)} tablas: {', '.join(r['TABLE_NAME'] for r in d)}"),
                  sql_antes=("SELECT VERSION() AS version, DATABASE() AS base_datos, CURRENT_USER() AS usuario",),
                  sql_despues=("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME",),
                  tipo="Base de datos")
        self.caso("BD-02", "Base de datos", "Registros existentes por tabla (datos semilla)", "SQL", None,
                  "Todas las tablas de negocio con al menos 1 registro", None,
                  lambda st, js, tx, a, d: (all(v > 0 for v in d[0].values()), f"Conteos: {d[0]}"),
                  sql_despues=("SELECT (SELECT COUNT(*) FROM usuario) AS usuario, (SELECT COUNT(*) FROM proyectos) AS proyectos, "
                               "(SELECT COUNT(*) FROM tareas) AS tareas, (SELECT COUNT(*) FROM asignaciones) AS asignaciones, "
                               "(SELECT COUNT(*) FROM comentarios) AS comentarios",),
                  tipo="Base de datos")

        # ------------------------------------------------------------ USUARIOS
        self.seccion("2. USUARIO  (/addUsuario /buscarUsuario /getUsuarioLogin /updateUsuario /buscarUsuarios /deleteUsuario)")
        correo = f"prueba.{t}@ejemplo.com"
        clave = "Prueba#2026"
        sel_u = ("SELECT id_usuario, nombre, correo_electronico, LEFT(contrasena, 7) AS hash_inicio, rol FROM usuario WHERE correo_electronico = %s", correo)
        nuevo_usuario = {"nombre": f"Usuario Prueba {t}", "correo_electronico": correo, "contrasena": clave,
                         "rol": "Estudiante", "imagen_usuario": "#3366FF"}
        self.caso("USR-01", "Usuario", "Alta de usuario con datos validos (INSERT)", "POST", "/addUsuario",
                  "HTTP 200, respuesta 1; el registro existe en la tabla usuario con la contrasena cifrada (BCrypt)", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and len(a) == 0 and len(d) == 1
                                            and d[0]["hash_inicio"].startswith("$2"),
                                            f"HTTP {st}, respuesta {tx.strip()!r}; filas antes={len(a)}, despues={len(d)}"
                                            + (f", hash={d[0]['hash_inicio']}..." if d else "")),
                  cuerpo=nuevo_usuario, sql_antes=sel_u, sql_despues=sel_u)
        uid = (self.uno("SELECT id_usuario FROM usuario WHERE correo_electronico = %s", correo) or {}).get("id_usuario")

        correo_sin = f"sin.clave.{t}@ejemplo.com"
        sel_sin = ("SELECT COUNT(*) AS registros FROM usuario WHERE correo_electronico = %s", correo_sin)
        self.caso("USR-02", "Usuario", "Alta de usuario sin contrasena (dato obligatorio faltante)", "POST", "/addUsuario",
                  "HTTP 200, respuesta 0; no se inserta ningun registro", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "0" and d[0]["registros"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; registros despues={d[0]['registros']}"),
                  cuerpo={"nombre": "Sin Clave", "correo_electronico": correo_sin, "rol": "Estudiante"},
                  sql_despues=sel_sin)

        sel_dup = ("SELECT COUNT(*) AS registros FROM usuario WHERE correo_electronico = %s", "ana.demo@ejemplo.com")
        v_dup = self.error_controlado(409)
        self.caso("USR-03", "Usuario", "Alta de usuario con correo duplicado (restriccion UNIQUE)", "POST", "/addUsuario",
                  "Error controlado: HTTP 409 con JSON {resultado:0, mensaje}; el correo sigue registrado una sola vez", 409,
                  lambda st, js, tx, a, d: (v_dup(st, js, tx, a, d)[0] and d[0]["registros"] == 1,
                                            v_dup(st, js, tx, a, d)[1] + f"; registros con ese correo={d[0]['registros']}"),
                  cuerpo={"nombre": "Ana Duplicada", "correo_electronico": "ana.demo@ejemplo.com", "contrasena": "Otra#123", "rol": "Estudiante"},
                  sql_antes=sel_dup, sql_despues=sel_dup)

        correo_nn = f"sin.nombre.{t}@ejemplo.com"
        sel_nn = ("SELECT COUNT(*) AS registros FROM usuario WHERE correo_electronico = %s", correo_nn)
        v_nn = self.error_controlado(409)
        self.caso("USR-04", "Usuario", "Alta de usuario sin nombre (columna NOT NULL)", "POST", "/addUsuario",
                  "Error controlado: HTTP 409 con JSON {resultado:0, mensaje}; no se inserta el registro", 409,
                  lambda st, js, tx, a, d: (v_nn(st, js, tx, a, d)[0] and d[0]["registros"] == 0,
                                            v_nn(st, js, tx, a, d)[1] + f"; registros={d[0]['registros']}"),
                  cuerpo={"correo_electronico": correo_nn, "contrasena": "Clave#123", "rol": "Estudiante"},
                  sql_despues=sel_nn)

        self.caso("USR-05", "Usuario", "Alta de usuario con JSON mal formado", "POST", "/addUsuario",
                  "Error controlado: HTTP 400 con JSON {resultado:0, mensaje}", 400,
                  self.error_controlado(400), crudo='{"nombre": "Incompleto", ')

        self.caso("USR-06", "Usuario", "Buscar usuario por id existente (SELECT)", "GET", "/buscarUsuario",
                  "HTTP 200; JSON del usuario creado y SIN el campo contrasena", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("id_usuario") == uid and js.get("correo_electronico") == correo
                                            and "contrasena" not in js,
                                            f"HTTP {st}, id_usuario={js.get('id_usuario')}, nombre={js.get('nombre')!r}, "
                                            f"contrasena presente={'contrasena' in js}"),
                  params={"id": uid}, sql_despues=sel_u)

        self.caso("USR-07", "Usuario", "Buscar usuario con id inexistente", "GET", "/buscarUsuario",
                  "HTTP 200; objeto vacio (id_usuario = null)", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("id_usuario") is None and d[0]["registros"] == 0,
                                            f"HTTP {st}, id_usuario={js.get('id_usuario')}; registros en BD={d[0]['registros']}"),
                  params={"id": INEXISTENTE},
                  sql_despues=("SELECT COUNT(*) AS registros FROM usuario WHERE id_usuario = %s", INEXISTENTE))

        self.caso("USR-08", "Usuario", "Buscar usuario SIN el parametro id", "GET", "/buscarUsuario",
                  "Error controlado: HTTP 400 con mensaje que indique el parametro faltante 'id'", 400,
                  self.error_controlado(400, "'id'"))

        self.caso("USR-09", "Usuario", "Buscar usuario con id no numerico", "GET", "/buscarUsuario",
                  "Error controlado: HTTP 400 con mensaje que indique que 'id' debe ser numerico", 400,
                  self.error_controlado(400, "'id'"), params={"id": "abc"})

        self.caso("USR-10", "Usuario", "Login con credenciales validas", "POST", "/getUsuarioLogin",
                  "HTTP 200; JSON del usuario autenticado, sin contrasena", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("correo_electronico") == correo and js.get("nombre") != "000"
                                            and "contrasena" not in js,
                                            f"HTTP {st}, nombre={js.get('nombre')!r}, contrasena presente={'contrasena' in js}"),
                  cuerpo={"correo_electronico": correo, "contrasena": clave})

        self.caso("USR-11", "Usuario", "Login con contrasena incorrecta", "POST", "/getUsuarioLogin",
                  "HTTP 200; usuario centinela con nombre = '000' (acceso rechazado)", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("nombre") == "000", f"HTTP {st}, nombre={js.get('nombre')!r}"),
                  cuerpo={"correo_electronico": correo, "contrasena": "Incorrecta#1"})

        self.caso("USR-12", "Usuario", "Login con correo no registrado", "POST", "/getUsuarioLogin",
                  "HTTP 200; nombre = '000'", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("nombre") == "000", f"HTTP {st}, nombre={js.get('nombre')!r}"),
                  cuerpo={"correo_electronico": f"no.existe.{t}@ejemplo.com", "contrasena": clave})

        self.caso("USR-13", "Usuario", "Login con campos vacios", "POST", "/getUsuarioLogin",
                  "HTTP 200; nombre = '000' sin consultar credenciales", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("nombre") == "000", f"HTTP {st}, nombre={js.get('nombre')!r}"),
                  cuerpo={"correo_electronico": "", "contrasena": ""})

        sel_u2 = ("SELECT id_usuario, nombre, rol, LEFT(contrasena, 20) AS hash_inicio FROM usuario WHERE id_usuario = %s", uid)
        self.caso("USR-14", "Usuario", "Actualizar usuario existente sin enviar contrasena (UPDATE)", "POST", "/updateUsuario",
                  "HTTP 200, respuesta 1; cambian nombre y rol y se conserva el mismo hash de contrasena", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and d[0]["nombre"].endswith("(editado)")
                                            and d[0]["rol"] == "Docente" and a[0]["hash_inicio"] == d[0]["hash_inicio"],
                                            f"HTTP {st}, respuesta {tx.strip()!r}; nombre={d[0]['nombre']!r}, rol={d[0]['rol']!r}, "
                                            f"hash conservado={a[0]['hash_inicio'] == d[0]['hash_inicio']}"),
                  cuerpo={"id_usuario": uid, "nombre": f"Usuario Prueba {t} (editado)", "correo_electronico": correo,
                          "rol": "Docente", "imagen_usuario": "#3366FF"},
                  sql_antes=sel_u2, sql_despues=sel_u2)

        correo_fant = f"fantasma.{t}@ejemplo.com"
        sel_f = ("SELECT (SELECT COUNT(*) FROM usuario) AS total_usuarios, "
                 "(SELECT COUNT(*) FROM usuario WHERE correo_electronico = %s) AS con_ese_correo", correo_fant)
        self.caso("USR-15", "Usuario", "Actualizar usuario con id inexistente", "POST", "/updateUsuario",
                  "HTTP 200, respuesta 0; NO se inserta un usuario nuevo", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "0" and a[0]["total_usuarios"] == d[0]["total_usuarios"]
                                            and d[0]["con_ese_correo"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()[:120]!r}; usuarios antes={a[0]['total_usuarios']}, "
                                            f"despues={d[0]['total_usuarios']}"),
                  cuerpo={"id_usuario": INEXISTENTE, "nombre": "Fantasma", "correo_electronico": correo_fant, "rol": "Estudiante"},
                  sql_antes=sel_f, sql_despues=sel_f)

        cnt_u = ("SELECT COUNT(*) AS total FROM usuario",)
        self.caso("USR-16", "Usuario", "Listar todos los usuarios", "POST", "/buscarUsuarios",
                  "HTTP 200; arreglo con el mismo numero de registros que la tabla y ninguno expone la contrasena", 200,
                  lambda st, js, tx, a, d: (st == 200 and isinstance(js, list) and len(js) == d[0]["total"]
                                            and all("contrasena" not in u for u in js),
                                            f"HTTP {st}, {len(js)} usuarios en la respuesta, {d[0]['total']} en la BD; "
                                            f"alguno con contrasena={any('contrasena' in u for u in js)}"),
                  sql_despues=cnt_u)

        sel_del_u = ("SELECT COUNT(*) AS registros FROM usuario WHERE id_usuario = %s", uid)
        self.caso("USR-17", "Usuario", "Eliminar usuario existente (DELETE)", "DELETE", "/deleteUsuario",
                  "HTTP 200, respuesta 1; el registro ya no existe en la tabla", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and a[0]["registros"] == 1 and d[0]["registros"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; registros antes={a[0]['registros']}, despues={d[0]['registros']}"),
                  cuerpo={"id_usuario": uid}, sql_antes=sel_del_u, sql_despues=sel_del_u)

        self.caso("USR-18", "Usuario", "Eliminar usuario con id inexistente", "DELETE", "/deleteUsuario",
                  "HTTP 200, respuesta 0; no se elimina nada", 200, self.es_texto("0"),
                  cuerpo={"id_usuario": INEXISTENTE})

        # ------------------------------------------------------------ PROYECTOS
        self.seccion("3. PROYECTOS  (/addProyecto /buscarProyecto /updateProyecto /buscarProyectos /deleteProyecto)")
        nom_p = f"Proyecto Prueba {t}"
        sel_p = ("SELECT id_proyecto, nombre, estado_proyecto, fecha_inicio, fecha_finalizacion FROM proyectos WHERE nombre = %s", nom_p)
        self.caso("PRY-01", "Proyectos", "Alta de proyecto con JSON valido (INSERT)", "POST", "/addProyecto",
                  "HTTP 200, respuesta 1; el proyecto existe en la tabla proyectos", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and len(a) == 0 and len(d) == 1,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; filas antes={len(a)}, despues={len(d)}"
                                            + (f", id_proyecto={d[0]['id_proyecto']}" if d else "")),
                  cuerpo={"nombre": nom_p, "descripcion": "Proyecto creado por la suite de pruebas", "fecha_inicio": "2026-11-01",
                          "fecha_finalizacion": "2026-12-15", "estado_proyecto": "En proceso", "fondo": "/Images/azulReal.png"},
                  sql_antes=sel_p, sql_despues=sel_p)
        pid = (self.uno("SELECT id_proyecto FROM proyectos WHERE nombre = %s", nom_p) or {}).get("id_proyecto")

        cnt_p = ("SELECT COUNT(*) AS total FROM proyectos",)
        v_pn = self.error_controlado(409)
        self.caso("PRY-02", "Proyectos", "Alta de proyecto sin nombre (columna NOT NULL)", "POST", "/addProyecto",
                  "Error controlado: HTTP 409 con JSON {resultado:0, mensaje}; no cambia el numero de proyectos", 409,
                  lambda st, js, tx, a, d: (v_pn(st, js, tx, a, d)[0] and a[0]["total"] == d[0]["total"],
                                            v_pn(st, js, tx, a, d)[1] + f"; proyectos antes={a[0]['total']}, despues={d[0]['total']}"),
                  cuerpo={"descripcion": "Sin nombre", "estado_proyecto": "En proceso"}, sql_antes=cnt_p, sql_despues=cnt_p)

        nom_pf = f"Fecha Invalida {t}"
        sel_pf = ("SELECT COUNT(*) AS registros FROM proyectos WHERE nombre = %s", nom_pf)
        v_pf = self.error_controlado(409)
        self.caso("PRY-03", "Proyectos", "Alta de proyecto con fecha invalida (2026-13-45)", "POST", "/addProyecto",
                  "Error controlado: HTTP 409 con JSON {resultado:0, mensaje}; no se inserta el registro", 409,
                  lambda st, js, tx, a, d: (v_pf(st, js, tx, a, d)[0] and d[0]["registros"] == 0,
                                            v_pf(st, js, tx, a, d)[1] + f"; registros={d[0]['registros']}"),
                  cuerpo={"nombre": nom_pf, "fecha_inicio": "2026-13-45", "estado_proyecto": "En proceso"}, sql_despues=sel_pf)

        self.caso("PRY-04", "Proyectos", "Buscar proyecto por id existente", "GET", "/buscarProyecto",
                  "HTTP 200; JSON con el proyecto creado", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("id_proyecto") == pid and js.get("nombre") == nom_p,
                                            f"HTTP {st}, id_proyecto={js.get('id_proyecto')}, nombre={js.get('nombre')!r}"),
                  params={"id_proyecto": pid}, sql_despues=sel_p)

        self.caso("PRY-05", "Proyectos", "Buscar proyecto con id inexistente", "GET", "/buscarProyecto",
                  "HTTP 200; objeto vacio (id_proyecto = null)", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("id_proyecto") is None, f"HTTP {st}, id_proyecto={js.get('id_proyecto')}"),
                  params={"id_proyecto": INEXISTENTE})

        self.caso("PRY-06", "Proyectos", "Buscar proyecto SIN el parametro id_proyecto", "GET", "/buscarProyecto",
                  "Error controlado: HTTP 400 con mensaje que indique el parametro faltante 'id_proyecto'", 400,
                  self.error_controlado(400, "'id_proyecto'"))

        sel_p2 = ("SELECT id_proyecto, nombre, estado_proyecto FROM proyectos WHERE id_proyecto = %s", pid)
        self.caso("PRY-07", "Proyectos", "Actualizar proyecto existente (UPDATE)", "POST", "/updateProyecto",
                  "HTTP 200, respuesta 1; estado_proyecto cambia a 'Terminado'", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and d[0]["estado_proyecto"] == "Terminado",
                                            f"HTTP {st}, respuesta {tx.strip()!r}; estado antes={a[0]['estado_proyecto']!r}, "
                                            f"despues={d[0]['estado_proyecto']!r}"),
                  cuerpo={"id_proyecto": pid, "nombre": nom_p, "descripcion": "Proyecto creado por la suite de pruebas",
                          "fecha_inicio": "2026-11-01", "fecha_finalizacion": "2026-12-15", "estado_proyecto": "Terminado",
                          "fondo": "/Images/azulReal.png"},
                  sql_antes=sel_p2, sql_despues=sel_p2)

        nom_fant = f"Proyecto Fantasma {t}"
        sel_pfa = ("SELECT (SELECT COUNT(*) FROM proyectos) AS total, (SELECT COUNT(*) FROM proyectos WHERE nombre = %s) AS con_ese_nombre", nom_fant)
        self.caso("PRY-08", "Proyectos", "Actualizar proyecto con id inexistente", "POST", "/updateProyecto",
                  "HTTP 200, respuesta 0; NO se inserta un proyecto nuevo", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "0" and a[0]["total"] == d[0]["total"] and d[0]["con_ese_nombre"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()[:120]!r}; proyectos antes={a[0]['total']}, despues={d[0]['total']}, "
                                            f"insertados con ese nombre={d[0]['con_ese_nombre']}"),
                  cuerpo={"id_proyecto": INEXISTENTE, "nombre": nom_fant, "estado_proyecto": "En proceso"},
                  sql_antes=sel_pfa, sql_despues=sel_pfa)

        self.caso("PRY-09", "Proyectos", "Listar todos los proyectos", "POST", "/buscarProyectos",
                  "HTTP 200; arreglo con el mismo numero de registros que la tabla proyectos", 200,
                  lambda st, js, tx, a, d: (st == 200 and isinstance(js, list) and len(js) == d[0]["total"],
                                            f"HTTP {st}, {len(js)} proyectos en la respuesta, {d[0]['total']} en la BD"),
                  sql_despues=cnt_p)

        # ------------------------------------------------------------ TAREAS
        self.seccion("4. TAREAS  (/addTareas /getTarea /buscarTarea /updateTareas /buscarTareas /deleteTareas)")
        nom_t = f"Tarea Prueba {t}"
        sel_t = ("SELECT id_tarea, nombre, lista, estado, id_proyecto FROM tareas WHERE nombre = %s", nom_t)
        self.caso("TAR-01", "Tareas", "Alta de tarea ligada a un proyecto (relacion proyecto-tarea)", "POST", "/addTareas",
                  "HTTP 200, respuesta 1; la tarea existe con id_proyecto del proyecto creado", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and len(d) == 1 and d[0]["id_proyecto"] == pid,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; filas despues={len(d)}"
                                            + (f", id_tarea={d[0]['id_tarea']}, id_proyecto={d[0]['id_proyecto']}" if d else "")),
                  cuerpo={"nombre": nom_t, "descripcion": "Tarea de la suite", "fecha_vencimiento": "2026-12-01",
                          "lista": "pendientes", "estado": "warning", "id_proyecto": pid},
                  sql_antes=sel_t, sql_despues=sel_t)
        tid = (self.uno("SELECT id_tarea FROM tareas WHERE nombre = %s", nom_t) or {}).get("id_tarea")

        nom_tfk = f"Tarea Huerfana {t}"
        sel_tfk = ("SELECT COUNT(*) AS registros FROM tareas WHERE nombre = %s", nom_tfk)
        v_tfk = self.error_controlado(409)
        self.caso("TAR-02", "Tareas", "Alta de tarea con id_proyecto inexistente (llave foranea)", "POST", "/addTareas",
                  "Error controlado: HTTP 409 con JSON {resultado:0, mensaje}; no se crea una tarea huerfana", 409,
                  lambda st, js, tx, a, d: (v_tfk(st, js, tx, a, d)[0] and d[0]["registros"] == 0,
                                            v_tfk(st, js, tx, a, d)[1] + f"; registros={d[0]['registros']}"),
                  cuerpo={"nombre": nom_tfk, "lista": "pendientes", "estado": "danger", "id_proyecto": INEXISTENTE},
                  sql_despues=sel_tfk)

        sel_tp = ("SELECT id_tarea, nombre FROM tareas WHERE id_proyecto = %s", pid)
        self.caso("TAR-03", "Tareas", "Listar tareas de un proyecto (consulta por id_proyecto)", "POST", "/getTarea",
                  "HTTP 200; arreglo con las tareas del proyecto, igual a lo que hay en la BD", 200,
                  lambda st, js, tx, a, d: (st == 200 and isinstance(js, list) and len(js) == len(d) == 1 and js[0]["nombre"] == nom_t,
                                            f"HTTP {st}, {len(js)} tarea(s) en la respuesta, {len(d)} en la BD"),
                  cuerpo={"id_proyecto": pid}, sql_despues=sel_tp)

        self.caso("TAR-04", "Tareas", "Listar tareas de un proyecto inexistente", "POST", "/getTarea",
                  "HTTP 200; arreglo vacio []", 200,
                  lambda st, js, tx, a, d: (st == 200 and js == [], f"HTTP {st}, respuesta {tx.strip()[:80]!r}"),
                  cuerpo={"id_proyecto": INEXISTENTE})

        self.caso("TAR-05", "Tareas", "Listar tareas sin enviar id_proyecto", "POST", "/getTarea",
                  "HTTP 200; arreglo vacio [] (filtro nulo controlado)", 200,
                  lambda st, js, tx, a, d: (st == 200 and js == [], f"HTTP {st}, respuesta {tx.strip()[:80]!r}"),
                  cuerpo={})

        self.caso("TAR-06", "Tareas", "Buscar tarea por id existente", "GET", "/buscarTarea",
                  "HTTP 200; JSON con la tarea creada", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("id_tarea") == tid and js.get("nombre") == nom_t,
                                            f"HTTP {st}, id_tarea={js.get('id_tarea')}, nombre={js.get('nombre')!r}"),
                  params={"id_tarea": tid})

        self.caso("TAR-07", "Tareas", "Buscar tarea con id no numerico", "GET", "/buscarTarea",
                  "Error controlado: HTTP 400 con mensaje que indique que 'id_tarea' debe ser numerico", 400,
                  self.error_controlado(400, "'id_tarea'"), params={"id_tarea": "abc"})

        sel_t2 = ("SELECT id_tarea, lista, estado FROM tareas WHERE id_tarea = %s", tid)
        self.caso("TAR-08", "Tareas", "Actualizar tarea existente (mover a 'terminado')", "POST", "/updateTareas",
                  "HTTP 200, respuesta 1; lista = 'terminado' y estado = 'success'", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and d[0]["lista"] == "terminado" and d[0]["estado"] == "success",
                                            f"HTTP {st}, respuesta {tx.strip()!r}; lista {a[0]['lista']!r} -> {d[0]['lista']!r}"),
                  cuerpo={"id_tarea": tid, "nombre": nom_t, "descripcion": "Tarea de la suite", "fecha_vencimiento": "2026-12-01",
                          "lista": "terminado", "estado": "success", "id_proyecto": pid},
                  sql_antes=sel_t2, sql_despues=sel_t2)

        nom_tfa = f"Tarea Fantasma {t}"
        sel_tfa = ("SELECT (SELECT COUNT(*) FROM tareas) AS total, (SELECT COUNT(*) FROM tareas WHERE nombre = %s) AS con_ese_nombre", nom_tfa)
        self.caso("TAR-09", "Tareas", "Actualizar tarea con id inexistente", "POST", "/updateTareas",
                  "HTTP 200, respuesta 0; NO se inserta una tarea nueva", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "0" and a[0]["total"] == d[0]["total"] and d[0]["con_ese_nombre"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()[:120]!r}; tareas antes={a[0]['total']}, despues={d[0]['total']}"),
                  cuerpo={"id_tarea": INEXISTENTE, "nombre": nom_tfa, "lista": "pendientes", "estado": "danger", "id_proyecto": pid},
                  sql_antes=sel_tfa, sql_despues=sel_tfa)

        cnt_t = ("SELECT COUNT(*) AS total FROM tareas",)
        self.caso("TAR-10", "Tareas", "Listar todas las tareas", "POST", "/buscarTareas",
                  "HTTP 200; arreglo con el mismo numero de registros que la tabla tareas", 200,
                  lambda st, js, tx, a, d: (st == 200 and isinstance(js, list) and len(js) == d[0]["total"],
                                            f"HTTP {st}, {len(js)} tareas en la respuesta, {d[0]['total']} en la BD"),
                  sql_despues=cnt_t)

        # ------------------------------------------------------------ ASIGNACIONES
        self.seccion("5. ASIGNACIONES  (/addAsignacion /buscarAsignacion /updateAsignacion /buscarAsignaciones /deleteAsignacion)")
        sel_a = ("SELECT id_asignacion, id_usuario, id_tarea, fecha_asignacion FROM asignaciones WHERE id_tarea = %s", tid)
        self.caso("ASG-01", "Asignaciones", "Asignar la tarea creada al usuario 1 (INSERT)", "POST", "/addAsignacion",
                  "HTTP 200, respuesta 1; la asignacion existe con id_usuario=1 e id_tarea de la tarea creada", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and len(a) == 0 and len(d) == 1 and d[0]["id_usuario"] == 1,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; filas antes={len(a)}, despues={len(d)}"),
                  cuerpo={"id_usuario": 1, "id_tarea": tid, "fecha_asignacion": "2026-11-22"},
                  sql_antes=sel_a, sql_despues=sel_a)
        aid = (self.uno("SELECT id_asignacion FROM asignaciones WHERE id_tarea = %s", tid) or {}).get("id_asignacion")

        cnt_a = ("SELECT COUNT(*) AS total FROM asignaciones",)
        v_afk = self.error_controlado(409)
        self.caso("ASG-02", "Asignaciones", "Asignar a un usuario inexistente (llave foranea)", "POST", "/addAsignacion",
                  "Error controlado: HTTP 409 con JSON {resultado:0, mensaje}; no cambia el numero de asignaciones", 409,
                  lambda st, js, tx, a, d: (v_afk(st, js, tx, a, d)[0] and a[0]["total"] == d[0]["total"],
                                            v_afk(st, js, tx, a, d)[1] + f"; asignaciones antes={a[0]['total']}, despues={d[0]['total']}"),
                  cuerpo={"id_usuario": INEXISTENTE, "id_tarea": tid, "fecha_asignacion": "2026-11-22"},
                  sql_antes=cnt_a, sql_despues=cnt_a)

        self.caso("ASG-03", "Asignaciones", "Buscar asignacion por id existente", "GET", "/buscarAsignacion",
                  "HTTP 200; JSON con la asignacion creada", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("id_asignacion") == aid and js.get("id_tarea") == tid,
                                            f"HTTP {st}, id_asignacion={js.get('id_asignacion')}, id_tarea={js.get('id_tarea')}"),
                  params={"id": aid})

        self.caso("ASG-04", "Asignaciones", "Buscar asignacion con id inexistente", "GET", "/buscarAsignacion",
                  "HTTP 200; objeto vacio (id_asignacion = null)", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("id_asignacion") is None, f"HTTP {st}, id_asignacion={js.get('id_asignacion')}"),
                  params={"id": INEXISTENTE})

        sel_a2 = ("SELECT id_asignacion, fecha_asignacion FROM asignaciones WHERE id_asignacion = %s", aid)
        self.caso("ASG-05", "Asignaciones", "Actualizar fecha de una asignacion existente", "POST", "/updateAsignacion",
                  "HTTP 200, respuesta 1; fecha_asignacion = 2026-11-30", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and d[0]["fecha_asignacion"] == "2026-11-30",
                                            f"HTTP {st}, respuesta {tx.strip()!r}; fecha {a[0]['fecha_asignacion']} -> {d[0]['fecha_asignacion']}"),
                  cuerpo={"id_asignacion": aid, "id_usuario": 1, "id_tarea": tid, "fecha_asignacion": "2026-11-30"},
                  sql_antes=sel_a2, sql_despues=sel_a2)

        self.caso("ASG-06", "Asignaciones", "Actualizar asignacion con id inexistente", "POST", "/updateAsignacion",
                  "HTTP 200, respuesta 0; NO se inserta una asignacion nueva", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "0" and a[0]["total"] == d[0]["total"],
                                            f"HTTP {st}, respuesta {tx.strip()[:120]!r}; asignaciones antes={a[0]['total']}, despues={d[0]['total']}"),
                  cuerpo={"id_asignacion": INEXISTENTE, "id_usuario": 1, "id_tarea": tid, "fecha_asignacion": "2026-11-30"},
                  sql_antes=cnt_a, sql_despues=cnt_a)

        self.caso("ASG-07", "Asignaciones", "Listar todas las asignaciones", "POST", "/buscarAsignaciones",
                  "HTTP 200; arreglo con el mismo numero de registros que la tabla asignaciones", 200,
                  lambda st, js, tx, a, d: (st == 200 and isinstance(js, list) and len(js) == d[0]["total"],
                                            f"HTTP {st}, {len(js)} asignaciones en la respuesta, {d[0]['total']} en la BD"),
                  sql_despues=cnt_a)

        sel_a3 = ("SELECT COUNT(*) AS registros FROM asignaciones WHERE id_asignacion = %s", aid)
        self.caso("ASG-08", "Asignaciones", "Eliminar asignacion existente", "DELETE", "/deleteAsignacion",
                  "HTTP 200, respuesta 1; el registro ya no existe", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and a[0]["registros"] == 1 and d[0]["registros"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; registros antes={a[0]['registros']}, despues={d[0]['registros']}"),
                  cuerpo={"id_asignacion": aid}, sql_antes=sel_a3, sql_despues=sel_a3)

        self.caso("ASG-09", "Asignaciones", "Eliminar asignacion con id inexistente", "DELETE", "/deleteAsignacion",
                  "HTTP 200, respuesta 0", 200, self.es_texto("0"), cuerpo={"id_asignacion": INEXISTENTE})

        # ------------------------------------------------------------ COMENTARIOS
        self.seccion("6. COMENTARIOS  (/addComentario /getComentario /buscarComentario /updateComentario /buscarComentarios /deleteComentario)")
        texto_c = f"Comentario de prueba {t}"
        sel_c = ("SELECT id_comentario, texto_comentario, id_usuario, id_tarea, fecha_hora FROM comentarios WHERE texto_comentario = %s", texto_c)
        self.caso("COM-01", "Comentarios", "Alta de comentario en la tarea creada (INSERT)", "POST", "/addComentario",
                  "HTTP 200, respuesta 1; el comentario existe ligado a la tarea", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and len(d) == 1 and d[0]["id_tarea"] == tid,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; filas despues={len(d)}"),
                  cuerpo={"id_usuario": "Ana Demo", "id_tarea": tid, "texto_comentario": texto_c, "fecha_hora": "2026-11-22 10:30:00"},
                  sql_antes=sel_c, sql_despues=sel_c)
        cid = (self.uno("SELECT id_comentario FROM comentarios WHERE texto_comentario = %s", texto_c) or {}).get("id_comentario")

        cnt_c = ("SELECT COUNT(*) AS total FROM comentarios",)
        v_cn = self.error_controlado(409)
        self.caso("COM-02", "Comentarios", "Alta de comentario sin texto (columna NOT NULL)", "POST", "/addComentario",
                  "Error controlado: HTTP 409 con JSON {resultado:0, mensaje}; no cambia el numero de comentarios", 409,
                  lambda st, js, tx, a, d: (v_cn(st, js, tx, a, d)[0] and a[0]["total"] == d[0]["total"],
                                            v_cn(st, js, tx, a, d)[1] + f"; comentarios antes={a[0]['total']}, despues={d[0]['total']}"),
                  cuerpo={"id_usuario": "Ana Demo", "id_tarea": tid, "fecha_hora": "2026-11-22 10:31:00"},
                  sql_antes=cnt_c, sql_despues=cnt_c)

        sel_ct = ("SELECT id_comentario FROM comentarios WHERE id_tarea = %s", tid)
        self.caso("COM-03", "Comentarios", "Listar comentarios de una tarea", "POST", "/getComentario",
                  "HTTP 200; arreglo con los comentarios de la tarea, igual a la BD", 200,
                  lambda st, js, tx, a, d: (st == 200 and isinstance(js, list) and len(js) == len(d) == 1,
                                            f"HTTP {st}, {len(js)} comentario(s) en la respuesta, {len(d)} en la BD"),
                  cuerpo={"id_tarea": tid}, sql_despues=sel_ct)

        self.caso("COM-04", "Comentarios", "Listar comentarios de una tarea inexistente", "POST", "/getComentario",
                  "HTTP 200; arreglo vacio []", 200,
                  lambda st, js, tx, a, d: (st == 200 and js == [], f"HTTP {st}, respuesta {tx.strip()[:80]!r}"),
                  cuerpo={"id_tarea": INEXISTENTE})

        self.caso("COM-05", "Comentarios", "Buscar comentario por id existente", "GET", "/buscarComentario",
                  "HTTP 200; JSON con el comentario creado", 200,
                  lambda st, js, tx, a, d: (st == 200 and js.get("id_comentario") == cid and js.get("texto_comentario") == texto_c,
                                            f"HTTP {st}, id_comentario={js.get('id_comentario')}, texto={js.get('texto_comentario')!r}"),
                  params={"id": cid})

        self.caso("COM-06", "Comentarios", "Buscar comentario SIN el parametro id", "GET", "/buscarComentario",
                  "Error controlado: HTTP 400 con mensaje que indique el parametro faltante 'id'", 400,
                  self.error_controlado(400, "'id'"))

        sel_c2 = ("SELECT id_comentario, texto_comentario FROM comentarios WHERE id_comentario = %s", cid)
        self.caso("COM-07", "Comentarios", "Actualizar texto de un comentario existente", "POST", "/updateComentario",
                  "HTTP 200, respuesta 1; texto_comentario actualizado", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and d[0]["texto_comentario"].endswith("(editado)"),
                                            f"HTTP {st}, respuesta {tx.strip()!r}; texto -> {d[0]['texto_comentario']!r}"),
                  cuerpo={"id_comentario": cid, "id_usuario": "Ana Demo", "id_tarea": tid,
                          "texto_comentario": texto_c + " (editado)", "fecha_hora": "2026-11-22 10:30:00"},
                  sql_antes=sel_c2, sql_despues=sel_c2)

        self.caso("COM-08", "Comentarios", "Actualizar comentario con id inexistente", "POST", "/updateComentario",
                  "HTTP 200, respuesta 0; NO se inserta un comentario nuevo", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "0" and a[0]["total"] == d[0]["total"],
                                            f"HTTP {st}, respuesta {tx.strip()[:120]!r}; comentarios antes={a[0]['total']}, despues={d[0]['total']}"),
                  cuerpo={"id_comentario": INEXISTENTE, "id_usuario": "Ana Demo", "id_tarea": tid,
                          "texto_comentario": "Comentario fantasma", "fecha_hora": "2026-11-22 10:30:00"},
                  sql_antes=cnt_c, sql_despues=cnt_c)

        self.caso("COM-09", "Comentarios", "Listar todos los comentarios", "POST", "/buscarComentarios",
                  "HTTP 200; arreglo con el mismo numero de registros que la tabla comentarios", 200,
                  lambda st, js, tx, a, d: (st == 200 and isinstance(js, list) and len(js) == d[0]["total"],
                                            f"HTTP {st}, {len(js)} comentarios en la respuesta, {d[0]['total']} en la BD"),
                  sql_despues=cnt_c)

        sel_c3 = ("SELECT COUNT(*) AS registros FROM comentarios WHERE id_comentario = %s", cid)
        self.caso("COM-10", "Comentarios", "Eliminar comentario existente", "DELETE", "/deleteComentario",
                  "HTTP 200, respuesta 1; el registro ya no existe", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and a[0]["registros"] == 1 and d[0]["registros"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; registros antes={a[0]['registros']}, despues={d[0]['registros']}"),
                  cuerpo={"id_comentario": cid}, sql_antes=sel_c3, sql_despues=sel_c3)

        self.caso("COM-11", "Comentarios", "Eliminar comentario con id inexistente", "DELETE", "/deleteComentario",
                  "HTTP 200, respuesta 0", 200, self.es_texto("0"), cuerpo={"id_comentario": INEXISTENTE})

        # ------------------------------------------------------------ BAJAS DE TAREA
        self.seccion("7. BAJAS DE TAREAS Y PROYECTOS, INTEGRIDAD REFERENCIAL")
        sel_t3 = ("SELECT COUNT(*) AS registros FROM tareas WHERE id_tarea = %s", tid)
        self.caso("TAR-11", "Tareas", "Eliminar tarea existente", "DELETE", "/deleteTareas",
                  "HTTP 200, respuesta 1; el registro ya no existe", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and a[0]["registros"] == 1 and d[0]["registros"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; registros antes={a[0]['registros']}, despues={d[0]['registros']}"),
                  cuerpo={"id_tarea": tid}, sql_antes=sel_t3, sql_despues=sel_t3)

        self.caso("TAR-12", "Tareas", "Eliminar tarea con id inexistente", "DELETE", "/deleteTareas",
                  "HTTP 200, respuesta 0", 200, self.es_texto("0"), cuerpo={"id_tarea": INEXISTENTE})

        # Preparacion del caso de cascada (se registra en el log)
        nom_pc = f"Proyecto Cascada {t}"
        self.http("POST", "/addProyecto", cuerpo={"nombre": nom_pc, "estado_proyecto": "En proceso"})
        pid2 = (self.uno("SELECT id_proyecto FROM proyectos WHERE nombre = %s", nom_pc) or {}).get("id_proyecto")
        nom_tc = f"Tarea Cascada {t}"
        self.http("POST", "/addTareas", cuerpo={"nombre": nom_tc, "lista": "pendientes", "estado": "danger", "id_proyecto": pid2})
        tid2 = (self.uno("SELECT id_tarea FROM tareas WHERE nombre = %s", nom_tc) or {}).get("id_tarea")
        self.http("POST", "/addComentario", cuerpo={"id_usuario": "Ana Demo", "id_tarea": tid2, "texto_comentario": f"Cascada {t}"})
        self.http("POST", "/addAsignacion", cuerpo={"id_usuario": 1, "id_tarea": tid2, "fecha_asignacion": "2026-11-22"})
        self.out(f"   (preparacion INT-01: proyecto {pid2}, tarea {tid2}, 1 comentario y 1 asignacion creados por la API)", GRIS)
        sel_casc = ("SELECT (SELECT COUNT(*) FROM proyectos WHERE id_proyecto = %s) AS proyecto, "
                    "(SELECT COUNT(*) FROM tareas WHERE id_proyecto = %s) AS tareas, "
                    "(SELECT COUNT(*) FROM comentarios WHERE id_tarea = %s) AS comentarios, "
                    "(SELECT COUNT(*) FROM asignaciones WHERE id_tarea = %s) AS asignaciones", pid2, pid2, tid2, tid2)
        self.caso("INT-01", "Integridad", "Eliminar proyecto con tarea, comentario y asignacion (ON DELETE CASCADE)", "DELETE", "/deleteProyecto",
                  "HTTP 200, respuesta 1; se eliminan en cascada el proyecto, sus tareas, comentarios y asignaciones", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and a[0] == {"proyecto": 1, "tareas": 1, "comentarios": 1, "asignaciones": 1}
                                            and all(v == 0 for v in d[0].values()),
                                            f"HTTP {st}, respuesta {tx.strip()!r}; antes={a[0]}, despues={d[0]}"),
                  cuerpo={"id_proyecto": pid2}, sql_antes=sel_casc, sql_despues=sel_casc, tipo="Base de datos")

        sel_p3 = ("SELECT COUNT(*) AS registros FROM proyectos WHERE id_proyecto = %s", pid)
        self.caso("PRY-10", "Proyectos", "Eliminar proyecto existente", "DELETE", "/deleteProyecto",
                  "HTTP 200, respuesta 1; el registro ya no existe", 200,
                  lambda st, js, tx, a, d: (st == 200 and tx.strip() == "1" and a[0]["registros"] == 1 and d[0]["registros"] == 0,
                                            f"HTTP {st}, respuesta {tx.strip()!r}; registros antes={a[0]['registros']}, despues={d[0]['registros']}"),
                  cuerpo={"id_proyecto": pid}, sql_antes=sel_p3, sql_despues=sel_p3)

        self.caso("PRY-11", "Proyectos", "Eliminar proyecto con id inexistente", "DELETE", "/deleteProyecto",
                  "HTTP 200, respuesta 0", 200, self.es_texto("0"), cuerpo={"id_proyecto": INEXISTENTE})

    # ------------------------------------------------------------ resumen
    def resumen(self, carpeta):
        total = len(self.casos)
        pasan = sum(1 for c in self.casos if c["estado"] == "PASA")
        fallan = [c for c in self.casos if c["estado"] == "FALLA"]
        rutas = sorted({c["ruta"] for c in self.casos if c["ruta"]})
        self.seccion("RESUMEN")
        self.out(f"Casos ejecutados : {total}")
        self.out(f"Endpoints probados: {len(rutas)} de 28")
        self.out(f"PASA             : {pasan}", VERDE)
        self.out(f"FALLA            : {len(fallan)}", ROJO if fallan else VERDE)
        self.out(f"Porcentaje de exito: {pasan * 100 / total:.1f} %")
        for c in fallan:
            self.out(f"   - {c['id']}: {c['descripcion']}  ->  {c['obtenido']}", ROJO)
        os.makedirs(carpeta, exist_ok=True)
        datos = {"config": {**self.config, "fin": datetime.datetime.now().isoformat(timespec="seconds")},
                 "resumen": {"total": total, "pasan": pasan, "fallan": len(fallan), "endpoints": rutas},
                 "casos": self.casos}
        with open(os.path.join(carpeta, "resultados.json"), "w", encoding="utf-8") as f:
            json.dump(datos, f, ensure_ascii=False, indent=2)
        with open(os.path.join(carpeta, "log.txt"), "w", encoding="utf-8") as f:
            f.write("\n".join(self.log_lineas))
        self.out(f"Resultados guardados en: {os.path.abspath(carpeta)}")
        return 0 if not fallan else 1


def main():
    p = argparse.ArgumentParser(description="Pruebas de BD y endpoints - Gestor de Proyectos")
    p.add_argument("--api", default=os.getenv("API_URL", "http://localhost:8081"))
    p.add_argument("--db-host", default=os.getenv("DB_HOST", "127.0.0.1"))
    p.add_argument("--db-port", type=int, default=int(os.getenv("DB_PORT", "3307")))
    p.add_argument("--db-name", default=os.getenv("DB_NAME", "db_pruebas"))
    p.add_argument("--db-user", default=os.getenv("DB_USER", "user"))
    p.add_argument("--db-password", default=os.getenv("DB_PASSWORD"))
    p.add_argument("--etiqueta", default="ejecucion", help="nombre de la corrida, p. ej. antes / despues")
    args = p.parse_args()
    if not args.db_password:
        args.db_password = getpass.getpass(f"Contrasena de MariaDB para '{args.db_user}': ")
    suite = Suite(args)
    suite.ejecutar()
    carpeta = os.path.join(os.path.dirname(os.path.abspath(__file__)), "resultados", args.etiqueta)
    sys.exit(suite.resumen(carpeta))


if __name__ == "__main__":
    main()
