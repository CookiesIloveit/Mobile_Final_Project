package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentScreen(
    navController: NavHostController,
    orderId: String?,
    cartViewModel: CartViewModel
) {
    var selectedPaymentMethod by remember { mutableIntStateOf(1) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val currentUser = SessionManager.currentUser
    val userId = currentUser?.userId ?: 0
    val merchantIdInt = orderId?.toIntOrNull() ?: 0
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color(0xFFFBFBFB)

    val allCartItems by cartViewModel.cartItems.collectAsState()
    val storeCartItems = allCartItems.filter { it.merchantId == merchantIdInt }
    val totalPrice = storeCartItems.sumOf { it.price * it.qty }

    LaunchedEffect(currentUser, storeCartItems) {
        if (currentUser == null) {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        } else if (storeCartItems.isEmpty() && !showSuccessDialog) {
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp) // เผื่อที่ให้ปุ่มด้านล่าง
        ) {
            // ส่วนหัว (Header)
            item {
                Text(
                    text = "สรุปคำสั่งซื้อ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    color = Color(0xFF2D2D2D)
                )
            }

            // 1. รายการอาหาร
            item {
                PaymentSectionCard(title = "รายการอาหาร") {
                    storeCartItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.productName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "จำนวน ${item.qty}", fontSize = 13.sp, color = Color.Gray)
                            }
                            Text(
                                text = "฿${String.format("%.2f", item.price * item.qty)}",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2D2D2D)
                            )
                        }
                        if (index != storeCartItems.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF5F5F5))
                        }
                    }
                }
            }

            // 2. รายละเอียดจัดส่ง
            item {
                PaymentSectionCard(title = "ที่อยู่จัดส่ง") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = currentUser?.username ?: "", fontWeight = FontWeight.Bold)
                            Text(
                                text = currentUser?.address ?: "กรุณาระบุที่อยู่",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 3. วิธีชำระเงิน
            item {
                PaymentSectionCard(title = "ช่องทางชำระเงิน") {
                    Column {
                        PaymentOptionMinimal(
                            title = "เก็บเงินปลายทาง",
                            selected = selectedPaymentMethod == 1,
                            onClick = { selectedPaymentMethod = 1 }
                        )
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                        PaymentOptionMinimal(
                            title = "ชำระผ่านธนาคาร (QR Code)",
                            selected = selectedPaymentMethod == 2,
                            onClick = { selectedPaymentMethod = 2 }
                        )
                    }
                }
            }
        }

        // ปุ่มยืนยันด้านล่างดีไซน์ Floating
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Button(
                onClick = { showSuccessDialog = true },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ยืนยันคำสั่งซื้อ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("฿${String.format("%.2f", totalPrice)}", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }

    // Success Dialog (มินิมัล)
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            shape = RoundedCornerShape(28.dp),
            title = { Text("ชำระเงินสำเร็จ", fontWeight = FontWeight.Bold) },
            text = { Text("เราได้รับคำสั่งซื้อแล้ว ร้านค้ากำลังเตรียมอาหารให้คุณ") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        cartViewModel.createOrderAndCheckout(userId, merchantIdInt)
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                ) {
                    Text("ติดตามสถานะ", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun PaymentSectionCard(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
fun PaymentOptionMinimal(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 15.sp, color = if (selected) Color.Black else Color.Gray)
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE53935))
        )
    }
}