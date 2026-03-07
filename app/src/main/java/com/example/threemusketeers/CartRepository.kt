package com.example.threemusketeers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
class CartRepository(
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) {

    suspend fun getCartItems(userId: Int): List<CartEntity> {
        return withContext(Dispatchers.IO) {
            cartDao.getCartByUserId(userId)
        }
    }

    suspend fun saveCartItem(cartItem: CartEntity) {
        withContext(Dispatchers.IO) {
            cartDao.removeProductFromCart(cartItem.userId, cartItem.productId)
            cartDao.insertOrUpdateCart(cartItem)
        }
    }

    suspend fun removeCartItem(userId: Int, productId: Int) {
        withContext(Dispatchers.IO) {
            cartDao.removeProductFromCart(userId, productId)
        }
    }

    suspend fun clearCartByMerchant(userId: Int, merchantId: Int) {
        withContext(Dispatchers.IO) {
            cartDao.clearCartByMerchant(userId, merchantId)
        }
    }

    suspend fun saveOrders(orders: List<OrderEntity>) {
        withContext(Dispatchers.IO) {
            orderDao.insertOrders(orders)
        }
    }

    fun getOrdersByUser(userId: Int): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByUser(userId)
    }
}