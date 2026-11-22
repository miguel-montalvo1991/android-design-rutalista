// backend/test/workdays.test.js
// Pruebas de los endpoints de jornadas, con una base de datos en memoria.

const test = require('node:test');
const assert = require('node:assert/strict');
const { crearApp } = require('../src/app');
const { createDatabase } = require('../src/db/database');

// Arranca la app con una BD limpia en memoria y devuelve el servidor
function iniciarServidor() {
  return new Promise((resolve) => {
    const servidor = crearApp(createDatabase(':memory:')).listen(0, () => resolve(servidor));
  });
}

// Envía un POST /workdays con el cuerpo indicado
async function postJornada(servidor, cuerpo) {
  const { port } = servidor.address();
  return fetch(`http://127.0.0.1:${port}/workdays`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(cuerpo),
  });
}

test('POST /workdays crea una jornada y responde 201', async () => {
  const servidor = await iniciarServidor();
  try {
    const res = await postJornada(servidor, { name: '  Jornada de prueba  ' });
    const cuerpo = await res.json();

    assert.equal(res.status, 201);
    assert.equal(cuerpo.name, 'Jornada de prueba'); // se guarda sin espacios sobrantes
    assert.equal(cuerpo.status, 'OPEN');
    assert.ok(cuerpo.id > 0);
    assert.equal(cuerpo.closedAt, null);
  } finally {
    servidor.close();
  }
});

test('POST /workdays sin name responde 400 VALIDATION_ERROR', async () => {
  const servidor = await iniciarServidor();
  try {
    const res = await postJornada(servidor, {});
    const cuerpo = await res.json();

    assert.equal(res.status, 400);
    assert.equal(cuerpo.error.code, 'VALIDATION_ERROR');
  } finally {
    servidor.close();
  }
});

test('POST /workdays con name en blanco responde 400', async () => {
  const servidor = await iniciarServidor();
  try {
    const res = await postJornada(servidor, { name: '   ' });

    assert.equal(res.status, 400);
  } finally {
    servidor.close();
  }
});
// Envía un GET a la ruta indicada (ej. '/workdays/1')
async function getRuta(servidor, ruta) {
  const { port } = servidor.address();
  return fetch(`http://127.0.0.1:${port}${ruta}`);
}

test('GET /workdays responde 200 con lista vacía si no hay jornadas', async () => {
  const servidor = await iniciarServidor();
  try {
    const res = await getRuta(servidor, '/workdays');

    assert.equal(res.status, 200);
    assert.deepEqual(await res.json(), []);
  } finally {
    servidor.close();
  }
});

test('GET /workdays lista las jornadas con la más reciente primero', async () => {
  const servidor = await iniciarServidor();
  try {
    await postJornada(servidor, { name: 'Primera' });
    await postJornada(servidor, { name: 'Segunda' });

    const res = await getRuta(servidor, '/workdays');
    const cuerpo = await res.json();

    assert.equal(res.status, 200);
    assert.equal(cuerpo.length, 2);
    assert.equal(cuerpo[0].name, 'Segunda');
  } finally {
    servidor.close();
  }
});

test('GET /workdays/:id devuelve la jornada creada', async () => {
  const servidor = await iniciarServidor();
  try {
    const creada = await (await postJornada(servidor, { name: 'Jornada uno' })).json();

    const res = await getRuta(servidor, `/workdays/${creada.id}`);
    const cuerpo = await res.json();

    assert.equal(res.status, 200);
    assert.equal(cuerpo.id, creada.id);
    assert.equal(cuerpo.name, 'Jornada uno');
  } finally {
    servidor.close();
  }
});

test('GET /workdays/:id responde 404 si no existe y 400 si el id es inválido', async () => {
  const servidor = await iniciarServidor();
  try {
    const noExiste = await getRuta(servidor, '/workdays/999');
    assert.equal(noExiste.status, 404);
    assert.equal((await noExiste.json()).error.code, 'NOT_FOUND');

    const invalido = await getRuta(servidor, '/workdays/abc');
    assert.equal(invalido.status, 400);
    assert.equal((await invalido.json()).error.code, 'VALIDATION_ERROR');
  } finally {
    servidor.close();
  }
});