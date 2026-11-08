package com.rutalista.app.data.model

/** Estado de la entrega de un pedido. */
enum class DeliveryStatus {
    PENDING,      // Pendiente
    DELIVERED,    // Entregado
    FAILED,       // Fallido
    RESCHEDULED   // Reprogramado
}

/** Estado de verificación de la dirección. Solo CONFIRMED y MANUAL_PIN cuentan como verificadas. */
enum class AddressStatus {
    UNVERIFIED,   // Sin geocodificar
    SUGGESTED,    // Hay una coincidencia, el domiciliario aún no la confirma
    CONFIRMED,    // El domiciliario confirmó la coincidencia
    MANUAL_PIN    // El domiciliario fijó el punto en el mapa (plan B)
}

enum class PaymentMethod {
    CASH,
    TRANSFER,
    PAID,         // Ya pagado
    UNKNOWN       // Ambiguo: requiere validación del usuario
}

data class DeliveryOrder(
    val id: Long,
    val customerName: String = "",
    val phone: String = "",
    val rawAddress: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addressStatus: AddressStatus = AddressStatus.UNVERIFIED,
    val paymentMethod: PaymentMethod = PaymentMethod.UNKNOWN,
    val paymentConfirmed: Boolean = false,
    /** Monto en pesos colombianos. Long porque el COP no usa decimales en la práctica. */
    val amountCop: Long? = null,
    /** Hora límite en formato "HH:mm", o null si no tiene. */
    val deadline: String? = null,
    val notes: String = "",
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    /** Posición en el orden de entrega sugerido (editable). */
    val routePosition: Int? = null
) {
    val isAddressVerified: Boolean
        get() = addressStatus == AddressStatus.CONFIRMED ||
                addressStatus == AddressStatus.MANUAL_PIN
}