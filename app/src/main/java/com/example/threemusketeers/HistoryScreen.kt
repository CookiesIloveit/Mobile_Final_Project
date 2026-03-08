package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(cartViewModel: CartViewModel, database: AppDatabase) {
    val userId = SessionManager.currentUser?.userId ?: 0

    LaunchedEffect(userId) {
        cartViewModel.loadOrders(userId)
    }

    val dbOrders by cartViewModel.orderHistory.collectAsState()

    // จัดกลุ่มออเดอร์ตาม timestamp เพื่อแสดงเป็นบิลใบเดียว
    val groupedOrders = remember(dbOrders) {
        dbOrders.groupBy { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        if (groupedOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ยังไม่มีประวัติการสั่งซื้อ", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // วนลูปตามกลุ่มของออเดอร์ (1 กลุ่ม = 1 บิล)
                items(groupedOrders.keys.toList()) { timestamp ->
                    val itemsInOrder = groupedOrders[timestamp] ?: emptyList()
                    if (itemsInOrder.isNotEmpty()) {
                        // ส่งลิสต์ของ OrderEntity เข้าไปใน Card
                        OrderHistoryCard(orderItems = itemsInOrder, database = database)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(orderItems: List<OrderEntity>, database: AppDatabase) {
    val firstItem = orderItems.first()

    // ดึงชื่อร้านค้าจาก DB (ใช้ produceState เพื่อโหลดชื่อร้าน)
    val merchantName by produceState(initialValue = "กำลังโหลด...", firstItem.merchantId) {
        val merchant = database.merchantDao().getMerchantById(firstItem.merchantId)
        value = merchant?.storeName ?: "ร้านอาหาร"
    }

    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th", "TH"))
    val dateStr = sdf.format(Date(firstItem.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = merchantName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFD32F2F))
                StatusBadge(firstItem.status)
            }

            Text(text = dateStr, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF5F5F5))

            // แสดงรายการสินค้าในบิลนั้น (ถ้ามีหลายอย่าง)
            orderItems.forEach { item ->
                Text(text = "${item.productName} x ${item.qty}", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "ทั้งหมด ${orderItems.sumOf { it.qty }} รายการ", fontSize = 14.sp)
                Text(
                    text = "฿${String.format("%.2f", orderItems.sumOf { it.totalAmount })}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}
@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "จัดส่งสำเร็จ", "สำเร็จ" -> Color(0xFFE8F5E9) to Color(0xFF4CAF50)
        "กำลังดำเนินการ", "กำลังเตรียมอาหาร" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "ยกเลิก" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        else -> Color(0xFFEEEEEE) to Color(0xFF757575)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}