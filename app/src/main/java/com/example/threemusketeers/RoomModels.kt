package com.example.threemusketeers

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
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
    val phone: String,
    val logoPath: String? = null // เพิ่มโลโก้ร้าน
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
interface MerchantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registerMerchant(merchant: MerchantEntity): Long

    @Update
    suspend fun updateMerchant(merchant: MerchantEntity)

    @Query("SELECT * FROM merchants WHERE username = :user AND password = :pass LIMIT 1")
    suspend fun loginMerchant(user: String, pass: String): MerchantEntity?

    @Query("SELECT * FROM merchants WHERE merchantId = :mId LIMIT 1")
    suspend fun getMerchantById(mId: Int): MerchantEntity?

    @Query("SELECT * FROM merchants")
    fun getAllMerchants(): Flow<List<MerchantEntity>>


}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE merchantId = :mId")
    fun getProductsByMerchant(mId: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :pId")
    suspend fun getProductById(pId: Int): ProductEntity?
}

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("SELECT * FROM orders WHERE userId = :uId ORDER BY timestamp DESC")
    fun getOrdersByUser(uId: Int): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE merchantId = :mId ORDER BY timestamp DESC")
    fun getOrdersByMerchant(mId: Int): Flow<List<OrderEntity>>

    @Query("UPDATE orders SET status = :newStatus WHERE orderId = :oId")
    suspend fun updateOrderStatus(oId: Int, newStatus: String)
}

@Dao
interface CartDao {
    // ... ฟังก์ชันอื่นๆ ที่มีอยู่แล้ว ...

    @Query("SELECT * FROM cart WHERE userId = :uId")
    suspend fun getCartByUserId(uId: Int): List<CartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCart(cartItem: CartEntity)

    @Query("DELETE FROM cart WHERE userId = :uId AND productId = :pId")
    suspend fun removeProductFromCart(uId: Int, pId: Int)

    // --- เพิ่มบรรทัดนี้ลงไปเพื่อให้ Repository หายแดง ---
    @Query("DELETE FROM cart WHERE userId = :userId AND merchantId = :merchantId")
    suspend fun clearCartByMerchant(userId: Int, merchantId: Int)

    @Query("DELETE FROM cart")
    suspend fun clearAll()


}

@Database(
    entities = [
        UserEntity::class,
        MerchantEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        CartEntity::class
    ],
    version = 4, // เพิ่มเป็น 4 เพราะมีการเปลี่ยนโครงสร้างตาราง
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun merchantDao(): MerchantDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "threemusketeers_db" // ใช้ชื่อเดียวกับเพื่อนเพื่อความเข้ากันได้
                )
                    .fallbackToDestructiveMigration()
                    .build()
                Instance = instance
                instance
            }
        }
    }
}