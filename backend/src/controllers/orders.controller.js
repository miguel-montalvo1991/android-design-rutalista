// backend/src/controllers/orders.controller.js
// Traduce la petición HTTP a llamadas al servicio de pedidos.

function crearControladorPedidos(servicio) {
  return {
    crear(req, res) {
      const pedido = servicio.crear(req.params.workdayId, req.body);
      res.status(201).json(pedido);
    },
  };
}

module.exports = { crearControladorPedidos };