package com.example.threemusketeers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@Composable
fun StoreScreen(
    navController: NavHostController,
    storeId: String?,
    cartViewModel: CartViewModel,
    database: AppDatabase
) {
    val merchantIdInt = storeId?.toIntOrNull() ?: 0
    val primaryColor = Color(0xFFE53935)

    // --- ดึงข้อมูลจาก Database ---
    var merchant by remember { mutableStateOf<MerchantEntity?>(null) }
    LaunchedEffect(merchantIdInt) {
        merchant = database.merchantDao().getMerchantById(merchantIdInt)
        val userId = SessionManager.currentUser?.userId
        if (userId != null) cartViewModel.loadCartFromDatabase(userId)
    }

    val products by database.productDao().getProductsByMerchant(merchantIdInt)
        .collectAsState(initial = emptyList())

    val userId = SessionManager.currentUser?.userId ?: 0
    val allCartItems by cartViewModel.cartItems.collectAsState()
    val storeCartItems = allCartItems.filter { it.merchantId == merchantIdInt }
    val totalItems = storeCartItems.sumOf { it.qty }
    val totalPrice = storeCartItems.sumOf { it.price * it.qty }

    val availableCategories = products.map { it.category }.distinct()
    val menuCategories = listOf("ทั้งหมด") + availableCategories
    var selectedMenuCategory by remember { mutableStateOf("ทั้งหมด") }

    val filteredProducts = products.filter {
        selectedMenuCategory == "ทั้งหมด" || it.category == selectedMenuCategory
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFBFBFB))) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // ส่วนหัวดีไซน์ใหม่
            item {
                merchant?.let { RestaurantHeaderMinimal(it, navController) }
            }

            // แถบเลือกหมวดหมู่แบบมินิมัล
            item {
                LazyRow(
                    modifier = Modifier.padding(vertical = 16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(menuCategories) { category ->
                        val isSelected = selectedMenuCategory == category
                        Surface(
                            modifier = Modifier.clickable { selectedMenuCategory = category },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) primaryColor else Color(0xFFF0F0F0),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E0E0))
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

            // รายการเมนู
            items(filteredProducts) { product ->
                val cartItem = storeCartItems.find { it.productId == product.productId }
                MenuCardMinimal(
                    product = product,
                    qty = cartItem?.qty ?: 0,
                    onAddClick = {
                        val priceToUse = if (product.discountPrice != null && product.discountPrice!! > 0)
                            product.discountPrice!! else product.price

                        if (cartItem == null) {
                            cartViewModel.addToCart(CartEntity(
                                userId = userId, merchantId = merchantIdInt,
                                productId = product.productId, productName = product.productName,
                                price = priceToUse, qty = 1, imagePath = product.imagePath
                            ))
                        } else {
                            cartViewModel.updateQuantity(userId, product.productId, cartItem.qty + 1)
                        }
                    },
                    onDecreaseClick = {
                        cartItem?.let {
                            if (it.qty > 1) cartViewModel.updateQuantity(userId, it.productId, it.qty - 1)
                            else cartViewModel.removeFromCart(userId, it.productId)
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }

        // Summary Bar แบบมินิมัล
        if (totalItems > 0) {
            CartSummaryMinimal(
                totalItems = totalItems,
                totalPrice = totalPrice,
                onCheckoutClick = { navController.navigate("payment/${merchantIdInt}") },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun RestaurantHeaderMinimal(merchant: MerchantEntity, navController: NavHostController) {
    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        AsyncImage(
            model = merchant.logoPath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Overlay ไล่เฉดสีดำบาง ๆ เพื่อให้ตัวหนังสืออ่านง่าย
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
        ))

        // ปุ่มย้อนกลับ
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 40.dp, start = 16.dp).background(Color.White.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
        ) {
            Text(merchant.storeName, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(merchant.address, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun MenuCardMinimal(product: ProductEntity, qty: Int, onAddClick: () -> Unit, onDecreaseClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imagePath,
                contentDescription = null,
                modifier = Modifier.size(85.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D2D2D))
                Text(product.category, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))

                val hasDiscount = product.discountPrice != null && product.discountPrice!! > 0
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "฿${String.format("%.2f", if (hasDiscount) product.discountPrice else product.price)}",
                        color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                    if (hasDiscount) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "฿${String.format("%.2f", product.price)}",
                            style = TextStyle(textDecoration = TextDecoration.LineThrough),
                            color = Color.LightGray, fontSize = 12.sp
                        )
                    }
                }
            }

            // ปุ่มจัดการจำนวนสไตล์มินิมัล
            if (qty == 0) {
                Surface(
                    modifier = Modifier.size(32.dp).clickable { onAddClick() },
                    shape = CircleShape,
                    color = Color(0xFFE53935)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(20.dp)).padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clickable { onDecreaseClick() }.padding(4.dp)
                    )
                    Text(qty.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clickable { onAddClick() }.padding(4.dp),
                        tint = Color(0xFFE53935)
                    )
                }
            }
        }
    }
}

@Composable
fun CartSummaryMinimal(totalItems: Int, totalPrice: Double, onCheckoutClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.padding(20.dp).fillMaxWidth().height(70.dp),
        shape = RoundedCornerShape(35.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)), // สีดำถ่านดูหรู
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("$totalItems รายการในตะกร้า", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                Text("฿${String.format("%.2f", totalPrice)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Button(
                onClick = onCheckoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                Text("ชำระเงิน", fontWeight = FontWeight.Bold)
            }
        }
    }
}
