package com.example.threemusketeers
import androidx.compose.runtime.mutableStateListOf

data class Restaurant(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val deliveryTime: String,
    val priceLevel: String,
    val address: String,
    val isAd: Boolean = false,
    val menus: List<FoodMenu>
)

data class FoodMenu(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String,
    val imageRes: String? = null
)

data class OrderHistory(
    val orderId: String,
    val restaurantName: String,
    val date: String,
    val itemsCount: Int,
    val totalPrice: Double,
    val status: String
)

// --- ข้อมูลจำลอง (Mock Data) สำหรับใช้แสดงผลไปก่อน ---
val mockUser = UserEntity(
    userId = 1,
    username = "student_ku@ku.th",
    password = "password",
    address = "123 หอพักใน มหาวิทยาลัยเกษตรศาสตร์ ศรีราชา",
    phone = "081-234-5678"
)

val mockRestaurants = listOf(
    Restaurant(
        id = "1", name = "ร้านป้าแจ๋ว อาหารตามสั่ง", category = "อาหารตามสั่ง",
        rating = 4.8, deliveryTime = "15-20 นาที", priceLevel = "฿",
        address = "หน้า ม.เกษตร ศรีราชา", isAd = true,
        menus = listOf(
            // 🌟 ปรับปรุง Mock Data ให้หมวดหมู่ตรงกับระบบของเพื่อน
            FoodMenu(101, "ข้าวกะเพราหมูสับไข่ดาว", 50.0, "อาหารตามสั่ง"),
            FoodMenu(102, "ข้าวผัดต้มยำทะเล", 60.0, "อาหารตามสั่ง"),
            FoodMenu(103, "ชาเย็น", 25.0, "เครื่องดื่ม"),
            FoodMenu(104, "บัวลอยไข่หวาน", 35.0, "ของหวาน")
        )
    ),
    Restaurant(
        id = "2", name = "แซ่บนัว อีสานคลาสสิก", category = "อาหารอีสาน",
        rating = 4.5, deliveryTime = "20-30 นาที", priceLevel = "฿฿",
        address = "อ่าวอุดม",
        menus = listOf(
            FoodMenu(201, "ส้มตำไทยไข่เค็ม", 50.0, "อาหารตามสั่ง"),
            FoodMenu(202, "เฟรนช์ฟรายส์", 40.0, "ฟาสต์ฟู้ด"),
            FoodMenu(203, "น้ำตกเนื้อ", 90.0, "อาหารตามสั่ง")
        )
    )
)

val mockOrders = mutableStateListOf(
    OrderHistory("ORD001", "ร้านป้าแจ๋ว อาหารตามสั่ง", "06 มี.ค. 2026 12:30", 2, 110.0, "จัดส่งสำเร็จ"),
    OrderHistory("ORD002", "แซ่บนัว อีสานคลาสสิก", "05 มี.ค. 2026 18:45", 3, 220.0, "จัดส่งสำเร็จ")
)
