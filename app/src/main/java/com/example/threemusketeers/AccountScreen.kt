package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun AccountScreen(navController: NavHostController) {
    // 🌟 ดึงข้อมูลจาก Session จริงเท่านั้น
    val currentUser = SessionManager.currentUser

    // 🛡️ ระบบป้องกัน: ถ้าไม่มี User ใน Session ให้กลับไปหน้า Login ทันที
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // ถ้า currentUser เป็น null (ในช่วงรอเปลี่ยนหน้า) ไม่ต้องวาด UI ส่วนที่เหลือ
    if (currentUser == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        ProfileHeader(currentUser.username)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                AccountInfoItem(
                    icon = Icons.Default.Person,
                    label = "ชื่อบัญชี",
                    value = currentUser.username
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))

                AccountInfoItem(
                    icon = Icons.Default.Phone,
                    label = "เบอร์โทร",
                    value = if (currentUser.phone.isNotBlank()) currentUser.phone else "ไม่ได้ระบุ"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF5F5F5))

                AccountInfoItem(
                    icon = Icons.Default.LocationOn,
                    label = "ที่อยู่จัดส่ง",
                    value = currentUser.address
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // ผลักปุ่มลงไปด้านล่าง (Easy territory)

        Button(
            onClick = {
                // 🌟 เคลียร์ทุก Session ทั้ง User และ Merchant
                SessionManager.currentUser = null
                SessionManager.currentMerchant = null

                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ออกจากระบบ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ProfileHeader(username: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFD32F2F)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.3f)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = username, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "ลูกค้า Three Musketeers", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}

@Composable
fun AccountInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF6E5034), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}