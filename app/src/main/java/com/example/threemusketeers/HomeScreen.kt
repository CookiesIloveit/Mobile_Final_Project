package com.example.threemusketeers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
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
fun HomeScreen(navController: NavHostController, database: AppDatabase) {
    val categories = listOf("ทั้งหมด", "อาหารตามสั่ง", "ก๋วยเตี๋ยว", "อาหารญี่ปุ่น", "เครื่องดื่ม", "ของหวาน", "ฟาสต์ฟู้ด")
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }

    val merchants by database.merchantDao().getAllMerchants().collectAsState(initial = emptyList())
    val primaryColor = Color(0xFFE53935) // สีแดงมินิมัลที่เราใช้ในหน้าอื่น ๆ

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB)) // พื้นหลังขาวนวล
    ) {
        // --- ส่วนหัวและแถบ Filter ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "เลือกร้านอาหารที่ชอบ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = Color(0xFF2D2D2D)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            modifier = Modifier.clickable { selectedCategory = category },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) primaryColor else Color(0xFFF5F5F5),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFEEEEEE))
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // --- รายการร้านอาหาร ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (merchants.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("ยังไม่มีร้านค้าในระบบ", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(merchants) { merchant ->
                    RestaurantCardMinimal(
                        merchant = merchant,
                        onClick = {
                            navController.navigate("store/${merchant.merchantId}")
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun RestaurantCardMinimal(merchant: MerchantEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // โลโก้ร้านค้าแบบมุมโค้งมนนุ่มนวล
            AsyncImage(
                model = merchant.logoPath,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = merchant.storeName,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = merchant.address,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = merchant.phone,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }

            // ลูกศรบอกใบ้ว่ากดได้
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFEEEEEE),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}