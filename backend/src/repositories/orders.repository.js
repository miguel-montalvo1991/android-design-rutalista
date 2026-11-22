// backend/src/repositories/orders.repository.js
// Acceso a datos de la tabla delivery_orders. No valida ni responde HTTP.

// Convierte una fila de la BD (snake_case) al JSON de la API (camelCase)
function aModelo(f) {
  return {
    id: f.id,
    workdayId: f.workday_id,
    customerName: f.customer_name,
    phone: f.phone,
    addressText: f.address_text,
    neighborhood: f.neighborhood,
    municipality: f.municipality,
    latitude: f.latitude,
    longitude: f.longitude,
    productDescription: f.product_description,
    paymentStatus: f.payment_status,
    deadlineAt: f.deadline_at,
    notes: f.notes,
    deliveryStatus: f.delivery_status,
    position: f.position,
    sourceText: f.source_text,
    createdAt: f.created_at,
  };
}

function crearRepositorioPedidos(db) {
  // Parámetros con nombre (@campo): evitan errores de orden y la inyección SQL
  const insertar = db.prepare(`
    INSERT INTO delivery_orders
      (workday_id, customer_name, phone, address_text, neighborhood, municipality,
       latitude, longitude, product_description, payment_status, deadline_at, notes, source_text)
    VALUES
      (@workdayId, @customerName, @phone, @addressText, @neighborhood, @municipality,
       @latitude, @longitude, @productDescription, @paymentStatus, @deadlineAt, @notes, @sourceText)
  `);
  const buscarPorId = db.prepare('SELECT * FROM delivery_orders WHERE id = ?');

  // Pedidos de una jornada: primero los que ya tienen posición (de menor a mayor),
  // y al final los que aún no la tienen, por orden de creación
  const listarDeJornada = db.prepare(`
    SELECT * FROM delivery_orders
    WHERE workday_id = ?
    ORDER BY position IS NULL, position ASC, id ASC
  `);

  // Un pedido solo cuenta si pertenece a esa jornada
  const buscarEnJornada = db.prepare(
    'SELECT * FROM delivery_orders WHERE id = ? AND workday_id = ?'
  );

  // Actualiza solo los datos editables; el estado de entrega y la posición NO se tocan aquí
  const actualizar = db.prepare(`
    UPDATE delivery_orders SET
      customer_name       = @customerName,
      phone               = @phone,
      address_text        = @addressText,
      neighborhood        = @neighborhood,
      municipality        = @municipality,
      latitude            = @latitude,
      longitude           = @longitude,
      product_description = @productDescription,
      payment_status      = @paymentStatus,
      deadline_at         = @deadlineAt,
      notes               = @notes,
      source_text         = @sourceText
    WHERE id = @id AND workday_id = @workdayId
  `);

  const borrar = db.prepare('DELETE FROM delivery_orders WHERE id = ? AND workday_id = ?');

  return {
    // Inserta el pedido y devuelve el registro completo ya guardado
    crear(datos) {
      const resultado = insertar.run(datos);
      return aModelo(buscarPorId.get(resultado.lastInsertRowid));
    },

    // Devuelve los pedidos de la jornada (lista vacía si no tiene)
    listarPorJornada(workdayId) {
      return listarDeJornada.all(workdayId).map(aModelo);
    },

    // Devuelve el pedido o null si no existe en esa jornada
    obtenerEnJornada(workdayId, orderId) {
      const fila = buscarEnJornada.get(orderId, workdayId);
      return fila ? aModelo(fila) : null;
    },

    // Guarda los cambios y devuelve el pedido ya actualizado
    actualizar(datos) {
      actualizar.run(datos);
      return aModelo(buscarPorId.get(datos.id));
    },

    // Borra el pedido; devuelve true si existía y se borró
    eliminar(workdayId, orderId) {
      return borrar.run(orderId, workdayId).changes > 0;
    },
  };
}

module.exports = { crearRepositorioPedidos };