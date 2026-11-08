package com.rutalista.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.rutalista.app.data.model.DeliveryOrder
import com.rutalista.app.data.model.DeliveryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OrdersUiState(
    val orders: List<DeliveryOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val pendingCount: Int get() = orders.count { it.status == DeliveryStatus.PENDING }
    val deliveredCount: Int get() = orders.count { it.status == DeliveryStatus.DELIVERED }
}

class OrdersViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    fun addOrders(newOrders: List<DeliveryOrder>) {
        _uiState.update { it.copy(orders = it.orders + newOrders) }
    }

    fun updateOrder(updated: DeliveryOrder) {
        _uiState.update { state ->
            state.copy(orders = state.orders.map { if (it.id == updated.id) updated else it })
        }
    }

    fun setStatus(orderId: Long, status: DeliveryStatus) {
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map {
                    if (it.id == orderId) it.copy(status = status) else it
                }
            )
        }
    }

    fun removeOrder(orderId: Long) {
        _uiState.update { state ->
            state.copy(orders = state.orders.filterNot { it.id == orderId })
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}