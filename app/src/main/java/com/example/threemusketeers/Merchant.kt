package com.example.threemusketeers

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MerchantRegisterScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var logoPath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                logoPath = it.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "ลงทะเบียนร้านค้าใหม่",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5))
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (logoPath != null) {
                AsyncImage(
                    model = logoPath,
                    contentDescription = "Store Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                    Text("เพิ่มโลโก้", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = { Text("ชื่อร้านค้า") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("เบอร์โทรศัพท์") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("ที่ตั้งร้านค้า") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (storeName.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    scope.launch {
                        val newMerchant = MerchantEntity(
                            username = username,
                            storeName = storeName,
                            password = password,
                            address = address,
                            phone = phone,
                            logoPath = logoPath
                        )
                        db.merchantDao().registerMerchant(newMerchant)
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("ยืนยันการลงทะเบียน", fontSize = 16.sp)
        }
    }
}

@Composable
fun MerchantHomeScreen(navController: NavHostController, merchantId: Int) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    var merchantData by remember { mutableStateOf<MerchantEntity?>(null) }

    LaunchedEffect(merchantId) {
        merchantData = db.merchantDao().getMerchantById(merchantId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            if (merchantData?.logoPath != null) {
                AsyncImage(
                    model = merchantData?.logoPath,
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(50.dp), tint = Color(0xFFD32F2F))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "ยินดีต้อนรับ", fontSize = 18.sp, color = Color.Gray)
        Text(
            text = merchantData?.storeName ?: "กำลังโหลด...",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ปุ่มจัดการรายการสินค้า
        Button(
            onClick = { navController.navigate("manage_products/$merchantId") },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Inventory, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("จัดการรายการสินค้า", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ปุ่มดูออเดอร์
        Button(
            onClick = { navController.navigate("merchant_orders/$merchantId") },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("รายการคำสั่งซื้อ", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                SessionManager.currentMerchant = null
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            border = BorderStroke(1.dp, Color.Red),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("ออกจากระบบ")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMerchantProfileScreen(navController: NavHostController, merchantId: Int) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var storeName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var logoPath by remember { mutableStateOf<String?>(null) }
    var originalMerchant by remember { mutableStateOf<MerchantEntity?>(null) }

    LaunchedEffect(merchantId) {
        val merchant = db.merchantDao().getMerchantById(merchantId)
        merchant?.let {
            originalMerchant = it
            storeName = it.storeName
            address = it.address
            phone = it.phone
            logoPath = it.logoPath
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            logoPath = it.toString()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("แก้ไขข้อมูลร้านค้า", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.LightGray).clickable {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            contentAlignment = Alignment.Center
        ) {
            if (logoPath != null) {
                AsyncImage(model = logoPath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White)
            }
        }

        OutlinedTextField(value = storeName, onValueChange = { storeName = it }, label = { Text("ชื่อร้านค้า") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ที่อยู่ร้าน") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("เบอร์โทรศัพท์") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                scope.launch {
                    originalMerchant?.let {
                        val updatedMerchant = it.copy(storeName = storeName, address = address, phone = phone, logoPath = logoPath)
                        db.merchantDao().updateMerchant(updatedMerchant)
                        SessionManager.currentMerchant = updatedMerchant
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("บันทึกการเปลี่ยนแปลง")
        }
    }
}

@Composable
fun ManageProductsScreen(navController: NavHostController, merchantId: Int) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    val products by db.productDao().getProductsByMerchant(merchantId).collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_product/$merchantId") },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("ยังไม่มีสินค้าในร้านของคุณ", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(products) { product ->
                    ProductItemRow(
                        product = product,
                        onEdit = { navController.navigate("edit_product/${product.productId}") },
                        onDelete = {
                            productToDelete = product
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ยืนยันการลบ") },
            text = { Text("คุณต้องการลบสินค้า '${productToDelete?.productName}' ใช่หรือไม่?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        productToDelete?.let { db.productDao().deleteProduct(it) }
                        showDeleteDialog = false
                    }
                }) {
                    Text("ลบ", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("อาหารตามสั่ง", "ก๋วยเตี๋ยว", "อาหารญี่ปุ่น", "เครื่องดื่ม", "ของหวาน", "ฟาสต์ฟู้ด")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("เลือกหมวดหมู่สินค้า") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(navController: NavHostController, merchantId: Int, productId: Int? = null) {
    var inputName by remember { mutableStateOf("") }
    var inputPrice by remember { mutableStateOf("") }
    var inputDiscountPrice by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("") }
    var minTime by remember { mutableStateOf("") }
    var maxTime by remember { mutableStateOf("") }
    var inputImagePath by remember { mutableStateOf<String?>(null) }
    var currentMerchantId by remember { mutableIntStateOf(merchantId) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                inputImagePath = it.toString()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    LaunchedEffect(productId) {
        if (productId != null && productId != 0) {
            val p = db.productDao().getProductById(productId)
            p?.let {
                inputName = it.productName
                inputPrice = it.price.toString()
                inputDiscountPrice = it.discountPrice?.toString() ?: ""
                inputCategory = it.category
                currentMerchantId = it.merchantId
                inputImagePath = it.imagePath
                val numbers = Regex("\\d+").findAll(it.timeDelivery).map { m -> m.value }.toList()
                if (numbers.size >= 2) {
                    minTime = numbers[0]
                    maxTime = numbers[1]
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (productId == null || productId == 0) "เพิ่มสินค้าใหม่" else "แก้ไขสินค้า", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        Box(
            modifier = Modifier.size(150.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)).clickable {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            contentAlignment = Alignment.Center
        ) {
            if (inputImagePath != null) {
                AsyncImage(model = inputImagePath, contentDescription = "Product Image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                    Text("เพิ่มรูปภาพ", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        OutlinedTextField(value = inputName, onValueChange = { inputName = it }, label = { Text("ชื่อสินค้า") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = inputPrice, onValueChange = { inputPrice = it }, label = { Text("ราคาปกติ") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        OutlinedTextField(value = inputDiscountPrice, onValueChange = { inputDiscountPrice = it }, label = { Text("ราคาลดโปรโมชั่น (ถ้ามี)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        CategoryDropdown(selectedCategory = inputCategory, onCategorySelected = { inputCategory = it })

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = minTime, onValueChange = { if (it.all { c -> c.isDigit() }) minTime = it }, label = { Text("เร็วสุด") }, modifier = Modifier.weight(1f), suffix = { Text("นาที") })
            Text("-")
            OutlinedTextField(value = maxTime, onValueChange = { if (it.all { c -> c.isDigit() }) maxTime = it }, label = { Text("ช้าสุด") }, modifier = Modifier.weight(1f), suffix = { Text("นาที") })
        }

        Button(
            onClick = {
                if (inputName.isBlank() || inputPrice.toDoubleOrNull() == null) {
                    Toast.makeText(context, "กรุณากรอกข้อมูลให้ครบและถูกต้อง", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        val product = ProductEntity(
                            productId = productId ?: 0,
                            merchantId = currentMerchantId,
                            productName = inputName,
                            price = inputPrice.toDoubleOrNull() ?: 0.0,
                            discountPrice = inputDiscountPrice.toDoubleOrNull(),
                            category = inputCategory,
                            timeDelivery = "$minTime-$maxTime นาที",
                            imagePath = inputImagePath
                        )
                        if (product.productId == 0) db.productDao().insertProduct(product)
                        else db.productDao().updateProduct(product)
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("บันทึกข้อมูลสินค้า")
        }
    }
}

@Composable
fun ProductItemRow(product: ProductEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F0F0)), contentAlignment = Alignment.Center) {
                if (!product.imagePath.isNullOrEmpty()) {
                    AsyncImage(model = product.imagePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Fastfood, contentDescription = null, tint = Color.LightGray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.productName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "ราคา: ฿${product.price}", style = if (product.discountPrice != null) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default)
                if (product.discountPrice != null) Text(text = "ลดเหลือ: ฿${product.discountPrice}", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Blue) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
        }
    }
}

@Composable
fun MerchantBottomNavigation(navController: NavHostController, currentRoute: String?, merchantId: Int) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("หน้าแรก") },
            selected = currentRoute?.startsWith("merchant_home") == true,
            onClick = { navController.navigate("merchant_home/$merchantId") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text("จัดการสินค้า") },
            selected = currentRoute?.startsWith("manage_products") == true,
            onClick = { navController.navigate("manage_products/$merchantId") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Store, contentDescription = null) },
            label = { Text("โปรไฟล์ร้าน") },
            selected = currentRoute?.startsWith("edit_merchant_profile") == true,
            onClick = { navController.navigate("edit_merchant_profile/$merchantId") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
            label = { Text("คำสั่งซื้อ") },
            selected = currentRoute?.startsWith("merchant_orders") == true,
            onClick = {
                navController.navigate("merchant_orders/$merchantId") {
                    popUpTo("merchant_home/$merchantId") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

@Composable
fun MerchantOrderScreen(navController: NavHostController, merchantId: Int, database: AppDatabase) {
    val scope = rememberCoroutineScope()
    // ดึงออเดอร์ของร้านนี้จาก DB
    val orders by database.orderDao().getOrdersByMerchant(merchantId).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("รายการสั่งซื้อจากลูกค้า", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
        Spacer(modifier = Modifier.height(16.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ยังไม่มีคำสั่งซื้อเข้ามา", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(orders) { order ->
                    MerchantOrderCard(order = order) { newStatus ->
                        scope.launch {
                            database.orderDao().updateOrderStatus(order.orderId, newStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantOrderCard(order: OrderEntity, onStatusChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Order #${order.orderId}", fontWeight = FontWeight.Bold)
                Text(text = order.status, color = if(order.status == "สำเร็จ") Color(0xFF2E7D32) else Color(0xFFE65100))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(text = "สินค้า: ${order.productName} x ${order.qty}", fontSize = 16.sp)
            Text(text = "ยอดรวม: ฿${order.totalAmount}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))

            Spacer(modifier = Modifier.height(8.dp))

            // ปุ่มจัดการสถานะ
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStatusChange("กำลังเตรียมอาหาร") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) {
                    Text("เริ่มทำ", fontSize = 12.sp)
                }
                Button(onClick = { onStatusChange("สำเร็จ") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("เสร็จสิ้น", fontSize = 12.sp)
                }
            }
        }
    }
}