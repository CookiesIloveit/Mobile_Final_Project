package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    database: AppDatabase // เพิ่ม database เข้ามา
) {
    val context = LocalContext.current
    val merchantIdInt = storeId?.toIntOrNull() ?: 0

    // --- ดึงข้อมูลจาก Database ---
    var merchant by remember { mutableStateOf<MerchantEntity?>(null) }
    LaunchedEffect(merchantIdInt) {
        merchant = database.merchantDao().getMerchantById(merchantIdInt)
        
        // โหลดข้อมูลตะกร้าเพื่อให้แน่ใจว่าค่าล่าสุดถูกดึงมาแสดง
        val userId = SessionManager.currentUser?.userId
        if (userId != null) {
            cartViewModel.loadCartFromDatabase(userId)
        }
    }

    // 2. ดึงรายการสินค้าของร้านนี้จาก Database
    val products by database.productDao().getProductsByMerchant(merchantIdInt)
        .collectAsState(initial = emptyList())

    val userId = SessionManager.currentUser?.userId ?: 0
    val allCartItems by cartViewModel.cartItems.collectAsState()

    // กรองสินค้าในตะกร้าเฉพาะของร้านนี้
    val storeCartItems = allCartItems.filter { it.merchantId == merchantIdInt }
    val totalItems = storeCartItems.sumOf { it.qty }
    val totalPrice = storeCartItems.sumOf { it.price * it.qty }

    // จัดการหมวดหมู่สินค้าจากข้อมูลจริง
    val availableCategories = products.map { it.category }.distinct()
    val menuCategories = listOf("ทั้งหมด") + availableCategories
    var selectedMenuCategory by remember { mutableStateOf("ทั้งหมด") }

    val filteredProducts = products.filter {
        selectedMenuCategory == "ทั้งหมด" || it.category == selectedMenuCategory
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // ส่วนหัวร้านค้า
            item {
                merchant?.let {
                    RestaurantHeaderFromDb(it)
                } ?: Box(Modifier.height(200.dp).fillMaxWidth().background(Color.Gray))
            }

            // แถบเลือกหมวดหมู่
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
                                selectedContainerColor = Color(0xFFD32F2F),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // แสดงรายการสินค้าจาก Database
            items(filteredProducts) { product ->
                val cartItem = storeCartItems.find { it.productId == product.productId }
                val currentQty = cartItem?.qty ?: 0

                MenuCardFromDb(
                    product = product,
                    qty = currentQty,
                    onAddClick = {
                        val hasValidDiscount = product.discountPrice != null && product.discountPrice!! > 0
                        val priceToUse = if (hasValidDiscount) product.discountPrice!! else product.price
                        
                        if (currentQty == 0) {
                            cartViewModel.addToCart(
                                CartEntity(
                                    userId = userId,
                                    merchantId = merchantIdInt,
                                    productId = product.productId,
                                    productName = product.productName,
                                    price = priceToUse,
                                    qty = 1,
                                    imagePath = product.imagePath
                                )
                            )
                        } else {
                            cartViewModel.updateQuantity(userId, product.productId, currentQty + 1)
                        }
                    },
                    onDecreaseClick = {
                        if (currentQty > 1) {
                            cartViewModel.updateQuantity(userId, product.productId, currentQty - 1)
                        } else {
                            cartViewModel.removeFromCart(userId, product.productId)
                        }
                    }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }

            if (filteredProducts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("ยังไม่มีสินค้าในหมวดหมู่นี้", color = Color.Gray)
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
                    navController.navigate("payment/${merchantIdInt}")
                },
                onClearClick = {
                    cartViewModel.clearCartByMerchant(userId, merchantIdInt)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun RestaurantHeaderFromDb(merchant: MerchantEntity) {
    Column(modifier = Modifier.padding(16.dp)) {
        AsyncImage(
            model = merchant.logoPath,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = merchant.storeName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "📍 ${merchant.address}", fontSize = 14.sp, color = Color.Gray)
        Text(text = "📞 ${merchant.phone}", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "รายการอาหาร", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MenuCardFromDb(product: ProductEntity, qty: Int, onAddClick: () -> Unit, onDecreaseClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = product.category, fontSize = 12.sp, color = Color.Gray)

            Row(verticalAlignment = Alignment.CenterVertically) {
                val hasValidDiscount = product.discountPrice != null && product.discountPrice!! > 0
                val displayPrice = if (hasValidDiscount) product.discountPrice else product.price

                Text(text = "฿${String.format("%.2f", displayPrice)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)

                if (hasValidDiscount) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "฿${String.format("%.2f", product.price)}",
                        style = TextStyle(textDecoration = TextDecoration.LineThrough),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Box(modifier = Modifier.size(80.dp)) {
            AsyncImage(
                model = product.imagePath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop
            )

            Surface(
                modifier = Modifier.align(Alignment.BottomEnd),
                shape = RoundedCornerShape(topStart = 8.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                if (qty == 0) {
                    IconButton(onClick = onAddClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                        Text(
                            "-",
                            Modifier.clickable { onDecreaseClick() }.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = qty.toString(), fontWeight = FontWeight.Bold)
                        Text(
                            "+",
                            Modifier.clickable { onAddClick() }.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
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
                Text(
                    text = "รวม ฿${String.format("%.2f", totalPrice)}", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFF006400)
                )
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
