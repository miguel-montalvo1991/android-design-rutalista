// backend/test/orders.test.js
// Pruebas de POST /workdays/:workdayId/orders, con base de datos en memoria.

const test = require('node:test');
const assert = require('node:assert/strict');
const { crearApp } = require('../src/app');
const { createDatabase } = require('../src/db/database');

// Arranca la app con una BD limpia y devuelve { servidor, db, url }
function iniciar() {
  const db = createDatabase(':memory:');
  return new Promise((resolve) => {
    const servidor = crearApp(db).listen(0, () =>
      resolve({ servidor, db, url: `http://127.0.0.1:${servidor.address().port}` })
    );
  });
}

// POST con cuerpo JSON
function post(url, ruta, cuerpo) {
  return fetch(`${url}${ruta}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(cuerpo),
  });
}

// Crea una jornada y devuelve su id
async function crearJornada(url) {
  return (await (await post(url, '/workdays', { name: 'Jornada de prueba' })).json()).id;
}

test('POST orders con datos mínimos responde 201 y estados por defecto seguros', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const res = await post(url, `/workdays/${id}/orders`, {
      customerName: 'Cliente Ficticio',
      addressText: 'Calle 10 # 20-30',
    });
    const pedido = await res.json();

    assert.equal(res.status, 201);
    assert.equal(pedido.workdayId, id);
    assert.equal(pedido.paymentStatus, 'PENDING_CONFIRMATION'); // nunca se asume pagado
    assert.equal(pedido.deliveryStatus, 'PENDING');
    assert.equal(pedido.latitude, null);
  } finally {
    servidor.close();
  }
});

test('POST orders con todos los campos los guarda correctamente', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const res = await post(url, `/workdays/${id}/orders`, {
      customerName: 'Cliente Completo',
      phone: '300 123 4567',
      addressText: 'Carrera 43A # 1 Sur-100',
      neighborhood: 'El Poblado',
      municipality: 'Medellín',
      latitude: 6.2,
      longitude: -75.57,
      productDescription: '2 hamburguesas',
      paymentStatus: 'COLLECT_ON_DELIVERY',
      deadlineAt: '2026-11-23T15:00:00.000Z',
      notes: 'Llamar al llegar',
    });
    const pedido = await res.json();

    assert.equal(res.status, 201);
    assert.equal(pedido.municipality, 'Medellín');
    assert.equal(pedido.paymentStatus, 'COLLECT_ON_DELIVERY');
    assert.equal(pedido.deadlineAt, '2026-11-23T15:00:00.000Z');
    assert.equal(pedido.longitude, -75.57);
  } finally {
    servidor.close();
  }
});

test('POST orders sin customerName responde 400', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const res = await post(url, `/workdays/${id}/orders`, { addressText: 'Calle 1' });

    assert.equal(res.status, 400);
    assert.equal((await res.json()).error.code, 'VALIDATION_ERROR');
  } finally {
    servidor.close();
  }
});

test('POST orders con paymentStatus fuera de la lista responde 400', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const res = await post(url, `/workdays/${id}/orders`, {
      customerName: 'Cliente',
      addressText: 'Calle 1',
      paymentStatus: 'PAGO',
    });

    assert.equal(res.status, 400);
  } finally {
    servidor.close();
  }
});

test('POST orders en una jornada inexistente responde 404', async () => {
  const { servidor, url } = await iniciar();
  try {
    const res = await post(url, '/workdays/999/orders', {
      customerName: 'Cliente',
      addressText: 'Calle 1',
    });

    assert.equal(res.status, 404);
    assert.equal((await res.json()).error.code, 'NOT_FOUND');
  } finally {
    servidor.close();
  }
});

test('POST orders en una jornada cerrada responde 409', async () => {
  const { servidor, db, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    // Todavía no existe el endpoint para cerrar jornadas: se cierra directo en la BD de prueba
    db.prepare("UPDATE workdays SET status = 'CLOSED' WHERE id = ?").run(id);

    const res = await post(url, `/workdays/${id}/orders`, {
      customerName: 'Cliente',
      addressText: 'Calle 1',
    });

    assert.equal(res.status, 409);
    assert.equal((await res.json()).error.code, 'WORKDAY_CLOSED');
  } finally {
    servidor.close();
  }
});