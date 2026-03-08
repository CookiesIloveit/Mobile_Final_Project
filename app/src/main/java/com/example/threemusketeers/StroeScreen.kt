package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun StoreScreen(
    navController: NavHostController,
    storeId: String?,
    cartViewModel: CartViewModel
) {
    val restaurant = mockRestaurants.find { it.id == storeId } ?: mockRestaurants[0]

    val userId = SessionManager.currentUser?.userId ?: 1
    val merchantIdInt = restaurant.id.toIntOrNull() ?: 1

    val allCartItems by cartViewModel.cartItems.collectAsState()

    val storeCartItems = allCartItems.filter { it.merchantId == merchantIdInt }
    val totalItems = storeCartItems.sumOf { it.qty }
    val totalPrice = storeCartItems.sumOf { it.price * it.qty }

    // ดึงหมวดหมู่ออกมาจากเมนูที่มีอยู่จริงในร้านนี้เท่านั้น
    val availableCategories = restaurant.menus.map { it.category }.distinct()
    val menuCategories = listOf("ทั้งหมด") + availableCategories

    var selectedMenuCategory by remember { mutableStateOf("ทั้งหมด") }

    val filteredMenus = restaurant.menus.filter {
        selectedMenuCategory == "ทั้งหมด" || it.category == selectedMenuCategory
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            item { RestaurantHeader(restaurant) }

            // แถบปุ่มกดเลือกหมวดหมู่
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(menuCategories) { category ->
                        FilterChip(
                            selected = selectedMenuCategory == category,
                            onClick = { selectedMenuCategory = category },
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

            // แสดงรายการอาหาร
            items(filteredMenus) { menu ->
                val cartItem = storeCartItems.find { it.productId == menu.id }
                val currentQty = cartItem?.qty ?: 0

                MenuCard(
                    menu = menu,
                    qty = currentQty,
                    onAddClick = {
                        if (currentQty == 0) {
                            cartViewModel.addToCart(
                                CartEntity(
                                    userId = userId,
                                    merchantId = merchantIdInt,
                                    productId = menu.id,
                                    productName = menu.name,
                                    price = menu.price,
                                    qty = 1,
                                    imagePath = menu.imageRes
                                )
                            )
                        } else {
                            cartViewModel.updateQuantity(userId, menu.id, currentQty + 1)
                        }
                    },
                    onDecreaseClick = {
                        val newQty = currentQty - 1
                        if (newQty <= 0) {
                            cartViewModel.removeFromCart(userId, menu.id)
                        } else {
                            cartViewModel.updateQuantity(userId, menu.id, newQty)
                        }
                    }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }

            if (filteredMenus.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("ไม่มีเมนูในหมวดหมู่นี้", color = Color.Gray)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        // แถบสรุปยอดด้านล่าง
        if (totalItems > 0) {
            CartSummaryBar(
                totalItems = totalItems,
                totalPrice = totalPrice,
                onCheckoutClick = {
                    navController.navigate(Screen.Payment.createRoute(restaurant.id))
                },
                onClearClick = {
                    storeCartItems.forEach { item ->
                        cartViewModel.removeFromCart(userId, item.productId)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun RestaurantHeader(restaurant: Restaurant) {
    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0E0E0))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = restaurant.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "⭐ ${restaurant.rating} · ${restaurant.deliveryTime}", color = Color.Gray)
        Text(text = restaurant.address, fontSize = 14.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "เมนูอาหาร", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MenuCard(menu: FoodMenu, qty: Int, onAddClick: () -> Unit, onDecreaseClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = menu.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)

            Text(
                text = menu.category,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Text(text = "฿${menu.price}", color = Color(0xFF006400), fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
        ) {
            if (qty == 0) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.White, RoundedCornerShape(topStart = 8.dp))
                        .size(30.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                }
            } else {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.White, RoundedCornerShape(topStart = 8.dp))
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDecreaseClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("-", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = qty.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    TextButton(
                        onClick = onAddClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("+", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CartSummaryBar(
    totalItems: Int,
    totalPrice: Double,
    onCheckoutClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shadowElevation = 15.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(text = "$totalItems รายการ", fontSize = 12.sp, color = Color.Gray)
                Text(text = "รวม ฿$totalPrice", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006400))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onClearClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ล้างรายการ", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onCheckoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ชำระเงิน", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}