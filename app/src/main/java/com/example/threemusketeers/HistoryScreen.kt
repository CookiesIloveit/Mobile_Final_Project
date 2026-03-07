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
fun HistoryScreen(cartViewModel: CartViewModel) {
    val userId = SessionManager.currentUser?.userId ?: 1

    // โหลดประวัติสั่งซื้อของจริงตอนเปิดหน้าจอ
    LaunchedEffect(Unit) {
        cartViewModel.loadOrders(userId)
    }

    val dbOrders by cartViewModel.orderHistory.collectAsState()

    // นำข้อมูลแต่ละชิ้นใน DB มามัดรวมกันเป็นบิล 1 ใบ (จัดกลุ่มตาม timestamp)
    val groupedOrders = remember(dbOrders) {
        dbOrders.groupBy { it.timestamp }.map { (timestamp, items) ->
            val firstItem = items.first()
            val restaurantName = mockRestaurants.find { it.id == firstItem.merchantId.toString() }?.name ?: "ร้านอาหาร"

            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("th", "TH"))
            val dateStr = sdf.format(Date(timestamp))

            OrderHistory(
                orderId = "ORD-${firstItem.orderId}",
                restaurantName = restaurantName,
                date = dateStr,
                itemsCount = items.sumOf { it.qty },
                totalPrice = items.sumOf { it.totalAmount },
                status = firstItem.status
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // เอาออเดอร์จาก DB ผสมกับของปลอม (mockOrders) จะได้มีข้อมูลโชว์เยอะๆ
            val allDisplayOrders = groupedOrders + mockOrders

            items(allDisplayOrders) { order ->
                OrderHistoryCard(order)
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: OrderHistory) {
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
                Text(text = order.restaurantName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                StatusBadge(order.status)
            }

            Text(text = order.date, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF5F5F5))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${order.itemsCount} รายการ", fontSize = 14.sp)
                Text(
                    text = "฿${order.totalPrice}",
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
    // 🌟 ใช้ when เพื่อล็อคสีให้ตรงกับสถานะเป๊ะๆ
    val bgColor = when (status) {
        "กำลังดำเนินการ" -> Color(0xFFE3F2FD) // สีฟ้าอ่อน
        "จัดส่งสำเร็จ" -> Color(0xFFE8F5E9)   // สีเขียวอ่อน
        else -> Color(0xFFEEEEEE)            // สีเทาอ่อน (เผื่อสถานะอื่นๆ ในอนาคต)
    }

    val textColor = when (status) {
        "กำลังดำเนินการ" -> Color(0xFF1976D2) // สีฟ้าเข้ม
        "จัดส่งสำเร็จ" -> Color(0xFF4CAF50)   // สีเขียวเข้ม
        else -> Color(0xFF757575)            // สีเทาเข้ม
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = status, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}