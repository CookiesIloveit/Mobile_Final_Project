package com.example.threemusketeers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class CartViewModel(
    private val repository: CartRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartEntity>>(emptyList())
    val cartItems: StateFlow<List<CartEntity>> = _cartItems.asStateFlow()

    private val recentlyDeletedItems = mutableMapOf<Int, CartEntity>()
    private val debounceJobs = mutableMapOf<Int, Job>()
    private val _orderHistory = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orderHistory: StateFlow<List<OrderEntity>> = _orderHistory.asStateFlow()

    fun loadCartFromDatabase(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _cartItems.value = repository.getCartItems(userId)
        }
    }

    fun clearCartByMerchant(userId: Int, merchantId: Int) {
        viewModelScope.launch {
            repository.clearCartByMerchant(userId, merchantId)
            loadCartFromDatabase(userId)
        }
    }

    fun addToCart(newItem: CartEntity) {
        val currentCart = _cartItems.value.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.productId == newItem.productId }

        if (existingIndex != -1) {
            val updatedItem = currentCart[existingIndex].copy(
                qty = currentCart[existingIndex].qty + newItem.qty
            )
            currentCart[existingIndex] = updatedItem
            _cartItems.value = currentCart
            debounceSave(updatedItem)
        } else {
            currentCart.add(newItem)
            _cartItems.value = currentCart
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveCartItem(newItem)
            }
        }
    }

    fun updateQuantity(userId: Int, productId: Int, newQty: Int) {
        if (newQty <= 0) {
            removeFromCart(userId, productId)
            return
        }
        val currentCart = _cartItems.value.toMutableList()
        val index = currentCart.indexOfFirst { it.productId == productId }
        if (index != -1) {
            val updatedItem = currentCart[index].copy(qty = newQty)
            currentCart[index] = updatedItem
            _cartItems.value = currentCart
            debounceSave(updatedItem)
        }
    }

    private fun debounceSave(item: CartEntity) {
        debounceJobs[item.productId]?.cancel()
        debounceJobs[item.productId] = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            repository.saveCartItem(item)
        }
    }

    fun removeFromCart(userId: Int, productId: Int) {
        val currentCart = _cartItems.value.toMutableList()
        val itemToRemove = currentCart.find { it.productId == productId }

        if (itemToRemove != null) {
            // ลบจาก RAM ทันที
            currentCart.remove(itemToRemove)
            _cartItems.value = currentCart

            // ยกเลิกคำสั่งรอ Save ของสินค้านี้
            debounceJobs[productId]?.cancel()
            debounceJobs.remove(productId)

            // ลบออกจาก Database
            viewModelScope.launch(Dispatchers.IO) {
                repository.removeCartItem(userId, productId)
            }
        }
    }

    fun loadOrders(userId: Int) {
        viewModelScope.launch {
            repository.getOrdersByUser(userId).collect { orders ->
                _orderHistory.value = orders
            }
        }
    }

    fun createOrderAndCheckout(userId: Int, merchantId: Int) {
        val currentCart = _cartItems.value
        // กรองสินค้าเฉพาะของร้านที่กำลังจะชำระเงิน
        val storeCartItems = currentCart.filter { it.merchantId == merchantId }

        if (storeCartItems.isEmpty()) return

        val currentTimestamp = System.currentTimeMillis()

        val summaryProductName = storeCartItems.joinToString(", ") { it.productName }

        val totalAmount = storeCartItems.sumOf { it.price * it.qty }

        val totalQty = storeCartItems.sumOf { it.qty }

        val summaryOrder = OrderEntity(
            userId = userId,
            merchantId = merchantId,
            productId = 0,
            productName = summaryProductName,
            price = totalAmount / totalQty,
            qty = totalQty,
            totalAmount = totalAmount,
            status = "กำลังเตรียมอาหาร", // เปลี่ยนสถานะเริ่มต้นให้ดูเป็นมิตรขึ้น
            imagePath = storeCartItems.firstOrNull()?.imagePath,
            timestamp = currentTimestamp
        )

        viewModelScope.launch(Dispatchers.IO) {
            // บันทึกออเดอร์เดียวเข้า Database
            repository.saveOrders(listOf(summaryOrder))

            // ล้างตะกร้าเฉพาะของร้านนี้ออกจาก RAM และ Database
            val itemsToKeep = currentCart.filter { it.merchantId != merchantId }
            _cartItems.value = itemsToKeep
            repository.clearCartByMerchant(userId, merchantId)

            // ยกเลิก Job การบันทึกที่ค้างอยู่
            storeCartItems.forEach { item ->
                debounceJobs[item.productId]?.cancel()
                debounceJobs.remove(item.productId)
            }
        }
    }
}

class CartViewModelFactory(
    private val repository: CartRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}