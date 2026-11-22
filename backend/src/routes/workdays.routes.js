// backend/src/routes/workdays.routes.js
// Define las URLs de /workdays y arma las capas repositorio -> servicio -> controlador.

const { Router } = require('express');
const { crearRepositorioJornadas } = require('../repositories/workdays.repository');
const { crearServicioJornadas } = require('../services/workdays.service');
const { crearControladorJornadas } = require('../controllers/workdays.controller');

function crearRutasJornadas(db) {
  const repositorio = crearRepositorioJornadas(db);
  const servicio = crearServicioJornadas(repositorio);
  const controlador = crearControladorJornadas(servicio);

  const router = Router();
  router.post('/', controlador.crear);     // POST /workdays
  router.get('/', controlador.listar);     // GET  /workdays
  router.get('/:id', controlador.obtener); // GET  /workdays/:id
  return router;
}

module.exports = { crearRutasJornadas };