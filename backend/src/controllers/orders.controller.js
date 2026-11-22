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
  };
}

module.exports = { crearControladorPedidos };