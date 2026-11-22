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

  return {
    // Inserta el pedido y devuelve el registro completo ya guardado
    crear(datos) {
      const resultado = insertar.run(datos);
      return aModelo(buscarPorId.get(resultado.lastInsertRowid));
    },
  };
}

module.exports = { crearRepositorioPedidos };