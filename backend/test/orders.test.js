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

// GET simple
function get(url, ruta) {
  return fetch(`${url}${ruta}`);
}

// Crea un pedido de prueba en la jornada indicada y devuelve el pedido creado
async function crearPedido(url, workdayId, nombre) {
  const res = await post(url, `/workdays/${workdayId}/orders`, {
    customerName: nombre,
    addressText: 'Calle 1 # 2-3',
  });
  return res.json();
}

test('GET orders responde 200 con lista vacía si la jornada no tiene pedidos', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const res = await get(url, `/workdays/${id}/orders`);

    assert.equal(res.status, 200);
    assert.deepEqual(await res.json(), []);
  } finally {
    servidor.close();
  }
});

test('GET orders lista solo los pedidos de esa jornada', async () => {
  const { servidor, url } = await iniciar();
  try {
    const jornadaA = await crearJornada(url);
    const jornadaB = await crearJornada(url);
    await crearPedido(url, jornadaA, 'Cliente A1');
    await crearPedido(url, jornadaA, 'Cliente A2');
    await crearPedido(url, jornadaB, 'Cliente B1');

    const lista = await (await get(url, `/workdays/${jornadaA}/orders`)).json();

    assert.equal(lista.length, 2);
    assert.deepEqual(lista.map((p) => p.customerName), ['Cliente A1', 'Cliente A2']);
  } finally {
    servidor.close();
  }
});

test('GET orders/:id devuelve el pedido creado', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const pedido = await crearPedido(url, id, 'Cliente Uno');

    const res = await get(url, `/workdays/${id}/orders/${pedido.id}`);
    const cuerpo = await res.json();

    assert.equal(res.status, 200);
    assert.equal(cuerpo.id, pedido.id);
    assert.equal(cuerpo.customerName, 'Cliente Uno');
  } finally {
    servidor.close();
  }
});

test('GET orders/:id responde 404 si el pedido no existe o es de otra jornada', async () => {
  const { servidor, url } = await iniciar();
  try {
    const jornadaA = await crearJornada(url);
    const jornadaB = await crearJornada(url);
    const pedido = await crearPedido(url, jornadaA, 'Cliente A');

    // Pedido inexistente
    const noExiste = await get(url, `/workdays/${jornadaA}/orders/999`);
    assert.equal(noExiste.status, 404);

    // El pedido existe, pero pertenece a la jornada A, no a la B
    const otraJornada = await get(url, `/workdays/${jornadaB}/orders/${pedido.id}`);
    assert.equal(otraJornada.status, 404);
  } finally {
    servidor.close();
  }
});

test('GET orders valida los ids y la existencia de la jornada', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);

    const jornadaInexistente = await get(url, '/workdays/999/orders');
    assert.equal(jornadaInexistente.status, 404);

    const pedidoInvalido = await get(url, `/workdays/${id}/orders/abc`);
    assert.equal(pedidoInvalido.status, 400);
    assert.equal((await pedidoInvalido.json()).error.code, 'VALIDATION_ERROR');
  } finally {
    servidor.close();
  }
});

// PUT con cuerpo JSON
function put(url, ruta, cuerpo) {
  return fetch(`${url}${ruta}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(cuerpo),
  });
}

// DELETE simple
function eliminar(url, ruta) {
  return fetch(`${url}${ruta}`, { method: 'DELETE' });
}

test('PUT orders actualiza los datos y conserva el estado de pago si no se envía', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const creado = await (
      await post(url, `/workdays/${id}/orders`, {
        customerName: 'Cliente Original',
        addressText: 'Calle 1 # 2-3',
        phone: '3001234567',
        paymentStatus: 'COLLECT_ON_DELIVERY',
      })
    ).json();

    const res = await put(url, `/workdays/${id}/orders/${creado.id}`, {
      customerName: 'Cliente Corregido',
      addressText: 'Carrera 50 # 10-20',
      municipality: 'Bello',
    });
    const pedido = await res.json();

    assert.equal(res.status, 200);
    assert.equal(pedido.customerName, 'Cliente Corregido');
    assert.equal(pedido.municipality, 'Bello');
    assert.equal(pedido.phone, null);                           // lo no enviado se vacía
    assert.equal(pedido.paymentStatus, 'COLLECT_ON_DELIVERY'); // el pago se conserva
    assert.equal(pedido.deliveryStatus, 'PENDING');
  } finally {
    servidor.close();
  }
});

test('PUT orders puede cambiar el estado de pago', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const creado = await crearPedido(url, id, 'Cliente Pago');

    const res = await put(url, `/workdays/${id}/orders/${creado.id}`, {
      customerName: 'Cliente Pago',
      addressText: 'Calle 1 # 2-3',
      paymentStatus: 'PAID',
    });

    assert.equal(res.status, 200);
    assert.equal((await res.json()).paymentStatus, 'PAID');
  } finally {
    servidor.close();
  }
});

test('PUT orders sin addressText responde 400 y no modifica el pedido', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const creado = await crearPedido(url, id, 'Cliente Estable');

    const res = await put(url, `/workdays/${id}/orders/${creado.id}`, { customerName: 'Otro nombre' });
    assert.equal(res.status, 400);

    const despues = await (await get(url, `/workdays/${id}/orders/${creado.id}`)).json();
    assert.equal(despues.customerName, 'Cliente Estable');
  } finally {
    servidor.close();
  }
});

test('PUT orders responde 404 si el pedido no existe', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const res = await put(url, `/workdays/${id}/orders/999`, {
      customerName: 'Cliente',
      addressText: 'Calle 1',
    });

    assert.equal(res.status, 404);
  } finally {
    servidor.close();
  }
});

test('DELETE orders responde 204 y el pedido deja de existir', async () => {
  const { servidor, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const creado = await crearPedido(url, id, 'Cliente Borrable');

    const res = await eliminar(url, `/workdays/${id}/orders/${creado.id}`);
    assert.equal(res.status, 204);

    const despues = await get(url, `/workdays/${id}/orders/${creado.id}`);
    assert.equal(despues.status, 404);
  } finally {
    servidor.close();
  }
});

test('PUT y DELETE en una jornada cerrada responden 409', async () => {
  const { servidor, db, url } = await iniciar();
  try {
    const id = await crearJornada(url);
    const creado = await crearPedido(url, id, 'Cliente Cerrado');
    // Todavía no existe el endpoint para cerrar jornadas: se cierra directo en la BD de prueba
    db.prepare("UPDATE workdays SET status = 'CLOSED' WHERE id = ?").run(id);

    const editar = await put(url, `/workdays/${id}/orders/${creado.id}`, {
      customerName: 'Intento',
      addressText: 'Calle 1',
    });
    assert.equal(editar.status, 409);

    const borrar = await eliminar(url, `/workdays/${id}/orders/${creado.id}`);
    assert.equal(borrar.status, 409);
  } finally {
    servidor.close();
  }
});