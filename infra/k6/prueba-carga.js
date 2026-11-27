// Prueba de carga del Gestor de Proyectos con Grafana k6.
// Simula usuarios que inician sesion y consultan sus proyectos y tareas a traves del cliente web,
// por lo que cada peticion recorre las tres capas: cliente -> API -> MariaDB.
//
// Ejecucion (desde la raiz del repositorio, con Docker):
//   docker run --rm -i -v "${PWD}/infra/k6:/k6" -e BASE_URL=http://<IP_PUBLICA> \
//     -e K6_WEB_DASHBOARD=true -e K6_WEB_DASHBOARD_EXPORT=/k6/resultados/reporte-k6.html \
//     grafana/k6:1.8.3 run --summary-export=/k6/resultados/resumen-k6.json /k6/prueba-carga.js
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8092').replace(/\/$/, '');
const CORREO = __ENV.CORREO || 'ana.demo@ejemplo.com';
const CLAVE = __ENV.CLAVE || 'Demo1234';

const loginExitoso = new Rate('login_exitoso');
const tiempoLogin = new Trend('tiempo_login', true);
const tiempoProyectos = new Trend('tiempo_consulta_proyectos', true);

const JSON_HEADERS = { 'Content-Type': 'application/json' };

export const options = {
  scenarios: {
    usuarios_del_gestor: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 5 },   // calentamiento
        { duration: '1m', target: 10 },   // carga normal
        { duration: '1m', target: 20 },   // carga alta
        { duration: '1m', target: 20 },   // sostenida
        { duration: '30s', target: 0 },   // descenso
      ],
      gracefulRampDown: '15s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1500'],
    'http_req_duration{tipo:consulta}': ['p(95)<800'],
    checks: ['rate>0.99'],
    login_exitoso: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'max'],
};

export function setup() {
  const r = http.get(`${BASE_URL}/login`);
  if (r.status !== 200) {
    throw new Error(`El cliente web no responde en ${BASE_URL}/login (HTTP ${r.status})`);
  }
  return { inicio: new Date().toISOString() };
}

export default function () {
  group('1. Pantalla de inicio de sesion', () => {
    const r = http.get(`${BASE_URL}/login`, { tags: { tipo: 'pagina' } });
    check(r, { 'GET /login responde 200': (x) => x.status === 200 });
  });
  sleep(1);

  group('2. Autenticacion', () => {
    const cuerpo = JSON.stringify({ correo_electronico: CORREO, contrasena: CLAVE });
    const r = http.post(`${BASE_URL}/verificarLogin`, cuerpo, { headers: JSON_HEADERS, tags: { tipo: 'login' } });
    const ok = check(r, {
      'POST /verificarLogin responde 200': (x) => x.status === 200,
      'credenciales aceptadas': (x) => x.status === 200 && x.json('nombre') !== '000',
    });
    loginExitoso.add(ok);
    tiempoLogin.add(r.timings.duration);
  });
  sleep(1);

  group('3. Tablero de proyectos', () => {
    const pagina = http.get(`${BASE_URL}/inicio`, { tags: { tipo: 'pagina' } });
    check(pagina, { 'GET /inicio responde 200': (x) => x.status === 200 });

    const r = http.post(`${BASE_URL}/consultarProyectos`, null, { tags: { tipo: 'consulta' } });
    const lista = r.status === 200 ? r.json() : [];
    check(r, { 'POST /consultarProyectos devuelve una lista': () => r.status === 200 && Array.isArray(lista) });
    tiempoProyectos.add(r.timings.duration);

    if (Array.isArray(lista) && lista.length > 0) {
      const proyecto = lista[Math.floor(Math.random() * lista.length)];
      const t = http.post(`${BASE_URL}/obtenerTareas`, JSON.stringify({ id_proyecto: proyecto.id_proyecto }),
        { headers: JSON_HEADERS, tags: { tipo: 'consulta' } });
      check(t, { 'POST /obtenerTareas responde 200': (x) => x.status === 200 });
    }
  });
  sleep(1 + Math.random() * 2);
}
