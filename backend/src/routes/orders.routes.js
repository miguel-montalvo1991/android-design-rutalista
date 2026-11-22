// backend/src/routes/orders.routes.js
// URLs de los pedidos. Se monta en /workdays/:workdayId/orders.

const { Router } = require('express');
const { crearRepositorioPedidos } = require('../repositories/orders.repository');
const { crearRepositorioJornadas } = require('../repositories/workdays.repository');
const { crearServicioPedidos } = require('../services/orders.service');
const { crearControladorPedidos } = require('../controllers/orders.controller');

function crearRutasPedidos(db) {
  const servicio = crearServicioPedidos(
    crearRepositorioPedidos(db),
    crearRepositorioJornadas(db) // para comprobar que la jornada existe
  );
  const controlador = crearControladorPedidos(servicio);

  // mergeParams: permite leer :workdayId definido en app.js
  const router = Router({ mergeParams: true });
  router.post('/', controlador.crear);     // POST /workdays/:workdayId/orders
  router.get('/', controlador.listar);     // GET  /workdays/:workdayId/orders
  router.get('/:id', controlador.obtener); // GET  /workdays/:workdayId/orders/:id
  return router;
}

module.exports = { crearRutasPedidos };