package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class) // จำเป็นต้องใส่เพื่อใช้งาน ModalBottomSheet
@Composable
fun HomeScreen(navController: NavHostController) {
    val categories = listOf("ทั้งหมด", "อาหารอีสาน", "อาหารตามสั่ง", "ก๋วยเตี๋ยว", "กาแฟ/ของว่าง", "สเต็ก", "อาหารญี่ปุ่น")
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }

    var filterHighRating by remember { mutableStateOf(false) }
    var filterFastDelivery by remember { mutableStateOf(false) }

    // 1. สถานะสำหรับเปิด/ปิดหน้าต่างตัวกรอง (Bottom Sheet)
    var showFilterSheet by remember { mutableStateOf(false) }

    val filteredRestaurants = mockRestaurants.filter { restaurant ->
        val matchCategory = selectedCategory == "ทั้งหมด" || restaurant.category == selectedCategory
        val matchRating = !filterHighRating || restaurant.rating >= 4.5
        val matchDelivery = !filterFastDelivery || run {
            val maxTime = restaurant.deliveryTime.split("-").lastOrNull()?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 99
            maxTime <= 20
        }
        matchCategory && matchRating && matchDelivery
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // --- แถบหมวดหมู่ + ปุ่ม Filter ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ปุ่มเปิดหน้าต่างตัวกรอง
            IconButton(
                onClick = { showFilterSheet = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Filter", tint = Color(0xFFD32F2F))
            }

            // แถบเลื่อนหมวดหมู่อาหาร
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFC107),
                            selectedLabelColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 2.dp)

        // --- รายการร้านค้า ---
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredRestaurants) { restaurant ->
                Box(modifier = Modifier.clickable {
                    navController.navigate(Screen.Store.createRoute(restaurant.id))
                }) {
                    RestaurantCard(restaurant)
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }

            if (filteredRestaurants.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ไม่พบร้านที่ตรงกับเงื่อนไข", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
    }

    // --- 2. หน้าต่างตัวกรองเพิ่มเติม (Bottom Sheet) ---
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(text = "ตัวกรองเพิ่มเติม", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                // สวิตช์เปิด-ปิด กรองเรตติ้ง
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐ เรตติ้ง 4.5 ขึ้นไป", fontSize = 16.sp)
                    Switch(
                        checked = filterHighRating,
                        onCheckedChange = { filterHighRating = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD32F2F))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // สวิตช์เปิด-ปิด กรองเวลาจัดส่ง
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚀 จัดส่งเร็ว (ไม่เกิน 20 นาที)", fontSize = 16.sp)
                    Switch(
                        checked = filterFastDelivery,
                        onCheckedChange = { filterFastDelivery = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD32F2F))
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ปุ่มยืนยัน
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ดูผลลัพธ์", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp)) // เผื่อพื้นที่ให้ Navigation Bar ของมือถือ
            }
        }
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // --- ส่วนรูปภาพ ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            if (restaurant.isAd) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF006400), RoundedCornerShape(topStart = 8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("MEGA\nGALE", color = Color.Yellow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- ส่วนรายละเอียด ---
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = restaurant.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${restaurant.rating} · ${restaurant.category} · ${restaurant.priceLevel}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(text = restaurant.deliveryTime, fontSize = 14.sp, color = Color.Gray)
            Text(text = restaurant.address, fontSize = 12.sp, color = Color.LightGray, maxLines = 1)
        }
    }
}