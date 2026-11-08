// backend/test/health.test.js
// Pruebas con el ejecutor incluido en Node (node:test): no requiere instalar nada.

const test = require('node:test');
const assert = require('node:assert/strict');
const { crearApp } = require('../src/app');

// Arranca la app en un puerto libre elegido por el sistema (puerto 0) y devuelve el servidor
function iniciarServidor() {
  return new Promise((resolve) => {
    const servidor = crearApp().listen(0, () => resolve(servidor));
  });
}

test('GET /health responde 200 con status ok', async () => {
  const servidor = await iniciarServidor();
  try {
    const { port } = servidor.address();
    const res = await fetch(`http://127.0.0.1:${port}/health`);
    const cuerpo = await res.json();

    assert.equal(res.status, 200);
    assert.equal(cuerpo.status, 'ok');
  } finally {
    // Cierra el servidor aunque la prueba falle, para que el proceso termine
    servidor.close();
  }
});

test('Una ruta inexistente responde 404 con el formato de error de la API', async () => {
  const servidor = await iniciarServidor();
  try {
    const { port } = servidor.address();
    const res = await fetch(`http://127.0.0.1:${port}/ruta-que-no-existe`);
    const cuerpo = await res.json();

    assert.equal(res.status, 404);
    assert.equal(cuerpo.error.code, 'NOT_FOUND');
  } finally {
    servidor.close();
  }
});