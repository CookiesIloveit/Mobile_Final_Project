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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, database: AppDatabase) { // เพิ่ม database เข้ามา
    val categories = listOf("ทั้งหมด", "อาหารตามสั่ง", "ก๋วยเตี๋ยว", "อาหารญี่ปุ่น", "เครื่องดื่ม", "ของหวาน", "ฟาสต์ฟู้ด")
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }

    // 🌟 ดึงข้อมูลร้านค้าทั้งหมดจากฐานข้อมูลจริง
    val merchants by database.merchantDao().getAllMerchants().collectAsState(initial = emptyList())

    // ระบบกรองร้านอาหาร (เบื้องต้นกรองตามชื่อหรือประเภทที่เก็บไว้)
    val filteredMerchants = merchants.filter { merchant ->
        selectedCategory == "ทั้งหมด" || /* เพิ่มเงื่อนไขการกรองร้านค้าที่นี่หากต้องการ */ true
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {
        // แถบ Filter (คงเดิม)
        LazyRow(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                    // ... colors และ shape คงเดิม
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            if (filteredMerchants.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("ยังไม่มีร้านค้าในระบบ", color = Color.Gray)
                    }
                }
            } else {
                items(filteredMerchants) { merchant ->
                    // ใช้ UI เดิมของเพื่อนแต่ส่งข้อมูล MerchantEntity เข้าไป
                    RestaurantCardFromDb(
                        merchant = merchant,
                        onClick = {
                            // 🌟 ส่ง merchantId ไปยัง StoreScreen
                            navController.navigate("store/${merchant.merchantId}")
                        }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

@Composable
fun RestaurantCardFromDb(merchant: MerchantEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // แสดงโลโก้ร้านค้าที่อัปโหลด
        AsyncImage(
            model = merchant.logoPath,
            contentDescription = null,
            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0E0E0)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = merchant.storeName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "📍 ${merchant.address}", fontSize = 14.sp, color = Color.Gray, maxLines = 1)
            Text(text = "📞 ${merchant.phone}", fontSize = 12.sp, color = Color.LightGray)
        }
    }
}