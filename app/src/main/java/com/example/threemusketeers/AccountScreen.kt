package com.example.threemusketeers

import androidx.compose.foundation.BorderStroke
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
    val currentUser = SessionManager.currentUser
    val primaryColor = Color(0xFFE53935) // สีแดงที่ดูทันสมัยขึ้น
    val backgroundColor = Color(0xFFFBFBFB)

    // 🛡️ ระบบป้องกัน: ถ้าไม่มี User ให้กลับไปหน้า Login
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (currentUser == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // --- 1. Header ดีไซน์ใหม่ ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(primaryColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentUser.username,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Coconut Member",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // --- 2. ข้อมูลผู้ใช้ใน Card แบบลอย (Floating Card) ---
        // ใช้ Offset เพื่อให้ Card ทับส่วน Header เล็กน้อย เพิ่มมิติ
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-30).dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                AccountInfoItemMinimal(
                    icon = Icons.Default.Person,
                    label = "Username",
                    value = currentUser.username
                )
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF5F5F5))

                AccountInfoItemMinimal(
                    icon = Icons.Default.Phone,
                    label = "Phone Number",
                    value = if (currentUser.phone.isNotBlank()) currentUser.phone else "Not specified"
                )
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF5F5F5))

                AccountInfoItemMinimal(
                    icon = Icons.Default.LocationOn,
                    label = "Delivery Address",
                    value = currentUser.address
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. ปุ่มออกจากระบบสไตล์มินิมัล ---
        TextButton(
            onClick = {
                SessionManager.currentUser = null
                SessionManager.currentMerchant = null
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp)
        ) {
            Text(
                "Sign Out",
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AccountInfoItemMinimal(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D2D2D)
            )
        }
    }
}