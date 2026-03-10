package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.History


@Composable
fun HistoryScreen(cartViewModel: CartViewModel, database: AppDatabase) {
    val userId = SessionManager.currentUser?.userId ?: 0
    val backgroundColor = Color(0xFFFBFBFB) // สีพื้นหลังขาวนวลสไตล์มินิมัล

    LaunchedEffect(userId) {
        cartViewModel.loadOrders(userId)
    }

    val dbOrders by cartViewModel.orderHistory.collectAsState()

    val groupedOrders = remember(dbOrders) {
        dbOrders.groupBy { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Text(
            text = "ประวัติการสั่งซื้อ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            color = Color(0xFF2D2D2D)
        )

        if (groupedOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp), // กำหนดขนาดที่นี่
                        tint = Color.LightGray
                    )
                    Text("ยังไม่มีประวัติการสั่งซื้อ", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 100.dp,
                    top = 0.dp // หรือใส่ค่าที่ต้องการ
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val sortedTimestamps = groupedOrders.keys.toList().sortedDescending()
                items(sortedTimestamps) { timestamp ->
                    val itemsInOrder = groupedOrders[timestamp] ?: emptyList()
                    if (itemsInOrder.isNotEmpty()) {
                        OrderHistoryCardMinimal(orderItems = itemsInOrder, database = database)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCardMinimal(orderItems: List<OrderEntity>, database: AppDatabase) {
    val firstItem = orderItems.first()
    val primaryRed = Color(0xFFE53935) // สีแดงมินิมัลที่เราใช้

    val merchantName by produceState(initialValue = "Loading...", firstItem.merchantId) {
        val merchant = database.merchantDao().getMerchantById(firstItem.merchantId)
        value = merchant?.storeName ?: "Restaurant"
    }

    val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("th", "TH"))
    val dateStr = sdf.format(Date(firstItem.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // เงาเบา ๆ เพิ่มมิติ
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = merchantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                StatusBadgeMinimal(firstItem.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                orderItems.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = item.productName, fontSize = 13.sp, color = Color.DarkGray)
                        Text(text = "x${item.qty}", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ยอดรวมสุทธิ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "฿${String.format("%.2f", orderItems.sumOf { it.totalAmount })}",
                    fontWeight = FontWeight.Black,
                    color = primaryRed,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun StatusBadgeMinimal(status: String) {
    val (bgColor, textColor) = when (status) {

        "จัดส่งสำเร็จ", "สำเร็จ" ->
            Color(0xFFC8E6C9) to Color(0xFF1B5E20)

        "กำลังเตรียมอาหาร" ->
            Color(0xFFFFE0B2) to Color(0xFFE65100)

        "กำลังดำเนินการ" ->
            Color(0xFFE1F5FE) to Color(0xFF01579B)

        else ->
            Color(0xFFEEEEEE) to Color(0xFF616161)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}