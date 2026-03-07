package com.example.threemusketeers

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- 1. ส่วนของ Entity ---
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val password: String,
    val address: String,
    val phone: String
)

@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey(autoGenerate = true) val merchantId: Int = 0,
    val username: String,
    val storeName: String,
    val password: String,
    val address: String,
    val phone: String
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val productId: Int = 0,
    val merchantId: Int,
    val productName: String,
    val price: Double,
    val discountPrice: Double? = null,
    val category: String,
    val timeDelivery: String,
    val imagePath: String?
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val userId: Int,
    val merchantId: Int,
    val productId: Int,
    val productName: String,
    val price: Double,
    val qty: Int,
    val totalAmount: Double,
    val status: String,
    val imagePath: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val cartId: Int = 0,
    val userId: Int,
    val productId: Int,
    val merchantId: Int,
    val productName: String,
    val price: Double,
    val qty: Int,
    val imagePath: String?
)
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): UserEntity?
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE merchantId = :mId")
    fun getProductsByMerchant(mId: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :pId")
    suspend fun getProductById(pId: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE discountPrice IS NOT NULL AND discountPrice > 0")
    fun getPromotionProducts(): Flow<List<ProductEntity>>
}

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("SELECT * FROM orders WHERE userId = :uId ORDER BY timestamp DESC")
    fun getOrdersByUser(uId: Int): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE merchantId = :mId ORDER BY timestamp DESC")
    fun getOrdersByMerchant(mId: Int): Flow<List<OrderEntity>>
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart WHERE userId = :uId")
    suspend fun getCartByUserId(uId: Int): List<CartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCart(cartItem: CartEntity)

    @Query("DELETE FROM cart WHERE userId = :uId AND productId = :pId")
    suspend fun removeProductFromCart(uId: Int, pId: Int)

    @Query("DELETE FROM cart WHERE userId = :uId AND merchantId = :mId")
    suspend fun clearCartByMerchant(uId: Int, mId: Int)

    @Query("SELECT DISTINCT merchantId FROM cart WHERE userId = :uId")
    suspend fun getMerchantIdsInCart(uId: Int): List<Int>
}

@Database(
    entities = [UserEntity::class, MerchantEntity::class, ProductEntity::class, OrderEntity::class, CartEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao
    abstract fun userDao(): UserDao
}