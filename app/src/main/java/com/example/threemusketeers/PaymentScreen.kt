package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    val userId = SessionManager.currentUser?.userId ?: 1
    val merchantIdInt = orderId?.toIntOrNull() ?: 1

    val allCartItems by cartViewModel.cartItems.collectAsState()

    val storeCartItems = allCartItems.filter { it.merchantId == merchantIdInt }
    val totalPrice = storeCartItems.sumOf { it.price * it.qty }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(bottom = 100.dp)
        ) {
            item {
                SectionTitle("รายการอาหาร")
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        storeCartItems.forEachIndexed { index, item -> // 🌟 วนลูปเฉพาะร้านนี้
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = item.productName, fontWeight = FontWeight.Medium)
                                    Text(text = "x ${item.qty}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Text(
                                    text = "฿${item.price * item.qty}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF006400)
                                )
                            }
                            if (index != storeCartItems.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color(0xFFF5F5F5)
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle("รายละเอียดการจัดส่ง")
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            val currentName = SessionManager.currentUser?.username ?: mockUser.username
                            Text(text = currentName, fontWeight = FontWeight.Bold)
                        }
                        val currentAddress = SessionManager.currentUser?.address ?: mockUser.address
                        Text(
                            text = currentAddress,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(text = "เวลาจัดส่งโดยประมาณ: 25-30 นาที", fontSize = 14.sp)
                    }
                }
            }

            item {
                SectionTitle("วิธีการชำระเงิน")
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        PaymentOption(
                            title = "เก็บเงินปลายทาง",
                            selected = selectedPaymentMethod == 1,
                            onClick = { selectedPaymentMethod = 1 }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        PaymentOption(
                            title = "ชำระเงินผ่านธนาคาร",
                            selected = selectedPaymentMethod == 2,
                            onClick = { selectedPaymentMethod = 2 }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Button(
                onClick = { showSuccessDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "ยืนยันการชำระเงิน (รวม ฿$totalPrice)",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* ไม่ให้ปิดด้วยการกดข้างนอก */ },
            title = { Text(text = "คำสั่งซื้อสำเร็จ", fontWeight = FontWeight.Bold) },
            text = { Text("กำลังเตรียมจัดส่งอาหารให้คุณ กรุณารอสักครู่") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false

                        cartViewModel.createOrderAndCheckout(userId, merchantIdInt)

                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                ) {
                    Text("ตกลง", color = Color.Black)
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
    )
}

@Composable
fun PaymentOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title)
        RadioButton(selected = selected, onClick = onClick)
    }
}