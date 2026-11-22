// backend/src/services/orders.service.js
// Validación y reglas de negocio de los pedidos.

const { ApiError } = require('../errors');

// Lista controlada: debe coincidir con el CHECK de schema.sql y el enum de la app
const ESTADOS_PAGO = ['PAID', 'COLLECT_ON_DELIVERY', 'PENDING_CONFIRMATION', 'NOT_APPLICABLE'];

const error400 = (mensaje) => new ApiError(400, 'VALIDATION_ERROR', mensaje);

// Texto obligatorio: string no vacío y con longitud máxima
function textoObligatorio(cuerpo, campo, max) {
  const valor = cuerpo[campo];
  if (typeof valor !== 'string' || valor.trim() === '') {
    throw error400(`El campo "${campo}" es obligatorio`);
  }
  if (valor.trim().length > max) {
    throw error400(`El campo "${campo}" no puede superar ${max} caracteres`);
  }
  return valor.trim();
}

// Texto opcional: devuelve null si no vino o está vacío
function textoOpcional(cuerpo, campo, max) {
  const valor = cuerpo[campo];
  if (valor === undefined || valor === null || valor === '') return null;
  if (typeof valor !== 'string') throw error400(`El campo "${campo}" debe ser texto`);
  if (valor.trim().length > max) {
    throw error400(`El campo "${campo}" no puede superar ${max} caracteres`);
  }
  return valor.trim() || null;
}

// Teléfono opcional: acepta espacios, guiones y paréntesis; exige 7 a 15 dígitos (con "+" opcional)
function telefonoOpcional(cuerpo) {
  const telefono = textoOpcional(cuerpo, 'phone', 30);
  if (telefono === null) return null;
  if (!/^\+?\d{7,15}$/.test(telefono.replace(/[\s\-()]/g, ''))) {
    throw error400('El campo "phone" no es un teléfono válido');
  }
  return telefono;
}

// Fecha límite opcional: debe ser una fecha válida; se guarda en ISO 8601
function fechaOpcional(cuerpo, campo) {
  const valor = cuerpo[campo];
  if (valor === undefined || valor === null || valor === '') return null;
  const ms = typeof valor === 'string' ? Date.parse(valor) : NaN;
  if (Number.isNaN(ms)) throw error400(`El campo "${campo}" debe ser una fecha válida (ISO 8601)`);
  return new Date(ms).toISOString();
}

// Coordenadas opcionales: ambas o ninguna, dentro de rango
function coordenadasOpcionales(cuerpo) {
  const { latitude, longitude } = cuerpo;
  const hayLat = latitude !== undefined && latitude !== null;
  const hayLon = longitude !== undefined && longitude !== null;

  if (!hayLat && !hayLon) return { latitude: null, longitude: null };
  if (hayLat !== hayLon) throw error400('Debe enviar "latitude" y "longitude" juntas');

  if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90) {
    throw error400('El campo "latitude" debe ser un número entre -90 y 90');
  }
  if (!Number.isFinite(longitude) || longitude < -180 || longitude > 180) {
    throw error400('El campo "longitude" debe ser un número entre -180 y 180');
  }
  return { latitude, longitude };
}

// Valida todos los campos editables de un pedido y devuelve los datos listos para guardar.
// "actual" es el pedido existente (solo en actualizaciones): si no se envía paymentStatus
// o sourceText, se conservan los valores actuales en lugar de reiniciarlos.
function validarDatos(cuerpo, actual = null) {
  // Si no se envía el estado de pago: se conserva el actual o, en un pedido nuevo,
  // queda pendiente de confirmar (nunca se asume pagado)
  const paymentStatus = cuerpo.paymentStatus ?? actual?.paymentStatus ?? 'PENDING_CONFIRMATION';
  if (!ESTADOS_PAGO.includes(paymentStatus)) {
    throw error400(`"paymentStatus" debe ser uno de: ${ESTADOS_PAGO.join(', ')}`);
  }

  return {
    customerName: textoObligatorio(cuerpo, 'customerName', 100),
    phone: telefonoOpcional(cuerpo),
    addressText: textoObligatorio(cuerpo, 'addressText', 200),
    neighborhood: textoOpcional(cuerpo, 'neighborhood', 100),
    municipality: textoOpcional(cuerpo, 'municipality', 100),
    ...coordenadasOpcionales(cuerpo),
    productDescription: textoOpcional(cuerpo, 'productDescription', 300),
    paymentStatus,
    deadlineAt: fechaOpcional(cuerpo, 'deadlineAt'),
    notes: textoOpcional(cuerpo, 'notes', 500),
    // El texto original solo se reemplaza si el cliente lo envía explícitamente
    sourceText:
      actual && cuerpo.sourceText === undefined
        ? actual.sourceText
        : textoOpcional(cuerpo, 'sourceText', 2000),
  };
}

function crearServicioPedidos(repositorioPedidos, repositorioJornadas) {
  // El id llega como texto en la URL: debe ser un entero positivo (ej. "3", no "abc" ni "1.5")
  function validarId(texto, nombre) {
    if (!/^[1-9]\d*$/.test(texto)) {
      throw error400(`El id ${nombre} debe ser un número entero positivo`);
    }
    return Number(texto);
  }

  // Valida el id de la jornada y comprueba que exista (abierta o cerrada)
  function obtenerJornada(workdayIdTexto) {
    const jornada = repositorioJornadas.obtener(validarId(workdayIdTexto, 'de la jornada'));
    if (!jornada) throw new ApiError(404, 'NOT_FOUND', `No existe la jornada ${workdayIdTexto}`);
    return jornada;
  }

  // Igual que obtenerJornada, pero además exige que esté abierta (para crear, editar y borrar)
  function obtenerJornadaAbierta(workdayIdTexto) {
    const jornada = obtenerJornada(workdayIdTexto);
    if (jornada.status !== 'OPEN') {
      throw new ApiError(409, 'WORKDAY_CLOSED', 'La jornada está cerrada y no admite cambios en sus pedidos');
    }
    return jornada;
  }

  // Busca un pedido dentro de una jornada o lanza 404
  function obtenerPedido(jornada, orderIdTexto) {
    const orderId = validarId(orderIdTexto, 'del pedido');
    const pedido = repositorioPedidos.obtenerEnJornada(jornada.id, orderId);
    if (!pedido) {
      throw new ApiError(404, 'NOT_FOUND', `No existe el pedido ${orderIdTexto} en la jornada ${jornada.id}`);
    }
    return pedido;
  }

  return {
    crear(workdayIdTexto, cuerpo = {}) {
      const jornada = obtenerJornadaAbierta(workdayIdTexto);
      return repositorioPedidos.crear({ workdayId: jornada.id, ...validarDatos(cuerpo) });
    },

    // Lista los pedidos de una jornada (también las cerradas, para consultar el historial)
    listar(workdayIdTexto) {
      const jornada = obtenerJornada(workdayIdTexto);
      return repositorioPedidos.listarPorJornada(jornada.id);
    },

    // Consulta un pedido concreto de una jornada
    obtener(workdayIdTexto, orderIdTexto) {
      const jornada = obtenerJornada(workdayIdTexto);
      return obtenerPedido(jornada, orderIdTexto);
    },

    // Actualiza los datos editables de un pedido
    actualizar(workdayIdTexto, orderIdTexto, cuerpo = {}) {
      const jornada = obtenerJornadaAbierta(workdayIdTexto);
      const actual = obtenerPedido(jornada, orderIdTexto);

      return repositorioPedidos.actualizar({
        id: actual.id,
        workdayId: jornada.id,
        ...validarDatos(cuerpo, actual),
      });
    },

    // Elimina un pedido
    eliminar(workdayIdTexto, orderIdTexto) {
      const jornada = obtenerJornadaAbierta(workdayIdTexto);
      const pedido = obtenerPedido(jornada, orderIdTexto);
      repositorioPedidos.eliminar(jornada.id, pedido.id);
    },
  };
}

module.exports = { crearServicioPedidos };