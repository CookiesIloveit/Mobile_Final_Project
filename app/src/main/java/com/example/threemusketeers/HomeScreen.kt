package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    // 🌟 1. ดึงหมวดหมู่ให้ตรงกับระบบของเพื่อน (Merchant Side)
    val categories = listOf("ทั้งหมด", "อาหารตามสั่ง", "ก๋วยเตี๋ยว", "อาหารญี่ปุ่น", "เครื่องดื่ม", "ของหวาน", "ฟาสต์ฟู้ด")
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }

    // 🌟 2. ระบบกรองร้านอาหาร:
    // เช็คว่าในร้านค้านั้นๆ มีเมนู (menus) ที่มีหมวดหมู่ตรงกับที่ผู้ใช้กดเลือกหรือไม่
    /* ======================================================================================
       🔥 TO-DO สำหรับเพื่อนที่ทำระบบ DB:
       ตอนที่ดึงข้อมูลร้านค้า (MerchantEntity) ให้ใช้โค้ดเช็คว่า ร้านนี้มี ProductEntity
       ที่มี category ตรงกับที่ผู้ใช้กดเลือกหรือไม่ แล้วค่อยโชว์ขึ้นมาครับ
       ======================================================================================
    */
    val filteredRestaurants = mockRestaurants.filter { restaurant ->
        selectedCategory == "ทั้งหมด" || restaurant.menus.any { menu -> menu.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        // แถบ Filter หมวดหมู่อาหารด้านบน
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
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

        // รายการร้านอาหารที่ผ่านการกรองแล้ว
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // เผื่อพื้นที่ให้แถบเมนูด้านล่าง
        ) {
            if (filteredRestaurants.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("ไม่มีร้านอาหารที่มีเมนูหมวดหมู่นี้", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                items(filteredRestaurants) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onClick = {
                            // 🌟 พอกดที่ร้าน ก็ส่ง ID ของร้านนั้นไปที่หน้า StoreScreen เพื่อเริ่มสั่งอาหาร
                            navController.navigate(Screen.Store.createRoute(restaurant.id))
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // 🌟 กดที่การ์ดแล้วทำคำสั่ง onClick
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- ส่วนรูปภาพ ---
        Box(
            modifier = Modifier
                .size(90.dp)
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

        Spacer(modifier = Modifier.width(16.dp))

        // --- ส่วนรายละเอียดร้าน ---
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = restaurant.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
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

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = restaurant.deliveryTime, fontSize = 14.sp, color = Color.Gray)
            Text(text = restaurant.address, fontSize = 12.sp, color = Color.LightGray, maxLines = 1)
        }
    }
}