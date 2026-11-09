'use strict';

// Pruebas del esquema. Usan una base en memoria, así que no tocan datos reales.

const test = require('node:test');
const assert = require('node:assert/strict');
const { createDatabase } = require('../src/db/database');

// Crea una jornada y devuelve su id (ayuda para no repetir código).
function crearJornada(db) {
  return db.prepare("INSERT INTO workdays (name) VALUES ('Jornada de prueba')")
    .run().lastInsertRowid;
}

test('un pedido nuevo queda con estados por defecto seguros', () => {
  const db = createDatabase(':memory:');
  const workdayId = crearJornada(db);

  const { lastInsertRowid } = db.prepare(
    `INSERT INTO delivery_orders (workday_id, customer_name, address_text)
     VALUES (?, 'Cliente Ficticio', 'Calle 1 # 2-3')`
  ).run(workdayId);

  const pedido = db.prepare('SELECT * FROM delivery_orders WHERE id = ?').get(lastInsertRowid);
  assert.equal(pedido.payment_status, 'PENDING_CONFIRMATION'); // nunca asume "pagado"
  assert.equal(pedido.delivery_status, 'PENDING');
  assert.equal(pedido.latitude, null); // las coordenadas son opcionales
});

test('rechaza un estado de pago que no está en la lista controlada', () => {
  const db = createDatabase(':memory:');
  const workdayId = crearJornada(db);

  assert.throws(() =>
    db.prepare(
      `INSERT INTO delivery_orders (workday_id, customer_name, address_text, payment_status)
       VALUES (?, 'Cliente Ficticio', 'Calle 1', 'PAGO')`
    ).run(workdayId)
  );
});

test('rechaza un pedido que apunta a una jornada inexistente', () => {
  const db = createDatabase(':memory:');

  assert.throws(() =>
    db.prepare(
      `INSERT INTO delivery_orders (workday_id, customer_name, address_text)
       VALUES (999, 'Cliente Ficticio', 'Calle 1')`
    ).run()
  );
});

test('al borrar una jornada se borran sus pedidos', () => {
  const db = createDatabase(':memory:');
  const workdayId = crearJornada(db);
  db.prepare(
    `INSERT INTO delivery_orders (workday_id, customer_name, address_text)
     VALUES (?, 'Cliente Ficticio', 'Calle 1')`
  ).run(workdayId);

  db.prepare('DELETE FROM workdays WHERE id = ?').run(workdayId);

  const { total } = db.prepare('SELECT COUNT(*) AS total FROM delivery_orders').get();
  assert.equal(total, 0);
});