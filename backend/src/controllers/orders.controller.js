// backend/src/controllers/orders.controller.js
// Traduce la petición HTTP a llamadas al servicio de pedidos.

function crearControladorPedidos(servicio) {
  return {
    crear(req, res) {
      const pedido = servicio.crear(req.params.workdayId, req.body);
      res.status(201).json(pedido); // 201 = recurso creado
    },

    listar(req, res) {
      res.json(servicio.listar(req.params.workdayId));
    },

    obtener(req, res) {
      res.json(servicio.obtener(req.params.workdayId, req.params.id));
    },

    actualizar(req, res) {
      res.json(servicio.actualizar(req.params.workdayId, req.params.id, req.body));
    },

    eliminar(req, res) {
      servicio.eliminar(req.params.workdayId, req.params.id);
      res.status(204).end(); // 204 = hecho, sin cuerpo en la respuesta
    },
  };
}

module.exports = { crearControladorPedidos };