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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantRegisterScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var logoPath by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)

    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color(0xFFFBFBFB)

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
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // --- ส่วนหัว (Top Bar) ---
        Spacer(modifier = Modifier.height(40.dp))
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.Black)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Merchant Registration",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2D2D2D),
            letterSpacing = (-1).sp
        )
        Text(
            "Open your restaurant with Three Musketeers",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- ส่วนเลือกโลโก้ร้าน (Logo Picker) ---
        Box(
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(Color.White)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = Color(0xFFF5F5F5)
            ) {
                if (logoPath != null) {
                    AsyncImage(
                        model = logoPath,
                        contentDescription = "Store Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = primaryColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
        Text(
            "Upload Store Logo",
            fontSize = 12.sp,
            color = primaryColor,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- ส่วนกรอกข้อมูลร้าน (Information Card) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinimalTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = "Store Name",
                    icon = Icons.Default.Storefront,
                    primaryColor = primaryColor,
                    isError = showError && storeName.isEmpty()
                )

                MinimalTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    icon = Icons.Default.Person,
                    primaryColor = primaryColor,
                    isError = showError && username.isEmpty()
                )

                MinimalTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    primaryColor = primaryColor,
                    isPassword = true,
                    isError = showError && password.isEmpty()
                )

                MinimalTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Store Phone",
                    icon = Icons.Default.Phone,
                    primaryColor = primaryColor
                )

                MinimalTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Store Location",
                    icon = Icons.Default.LocationOn,
                    primaryColor = primaryColor,
                    isError = showError && address.isEmpty()
                )
            }
        }

        if (showError) {
            Text(
                "Please fill in all required fields",
                color = primaryColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 16.dp, start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- ปุ่มยืนยัน ---
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
                } else {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Launch My Store", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MerchantHomeScreen(navController: NavHostController, merchantId: Int) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    var merchantData by remember { mutableStateOf<MerchantEntity?>(null) }

    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color(0xFFFBFBFB)

    LaunchedEffect(merchantId) {
        merchantData = db.merchantDao().getMerchantById(merchantId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp)
    ) {
        // --- 1. Header ส่วนบน (Profile) ---
        Spacer(modifier = Modifier.height(60.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color(0xFFF5F5F5)
                ) {
                    if (merchantData?.logoPath != null) {
                        AsyncImage(
                            model = merchantData?.logoPath,
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = primaryColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ยินดีต้อนรับกลับมา",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = merchantData?.storeName ?: "กำลังโหลด...",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2D2D2D),
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "แผงควบคุมร้านค้า",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        // --- 2. เมนูจัดการ (Dashboard Grid) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MerchantMenuCard(
                title = "จัดการสินค้า",
                subtitle = "เพิ่ม/ลบ เมนูอาหาร",
                icon = Icons.Default.Inventory,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("manage_products/$merchantId") }
            )
            MerchantMenuCard(
                title = "คำสั่งซื้อ",
                subtitle = "ดูรายการที่รอทำ",
                icon = Icons.Default.ReceiptLong,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("merchant_orders/$merchantId") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // เมนูเสริม (ถ้ามีในอนาคต เช่น แก้ไขโปรไฟล์)
        MerchantMenuCardWide(
            title = "ข้อมูลร้านค้า",
            subtitle = "แก้ไขที่อยู่และเบอร์โทรศัพท์",
            icon = Icons.Default.Edit,
            onClick = { navController.navigate("edit_merchant_profile/$merchantId") }
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- 3. ปุ่มออกจากระบบสไตล์ Minimal ---
        TextButton(
            onClick = {
                SessionManager.currentMerchant = null
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .height(56.dp)
        ) {
            Text(
                "ออกจากระบบ",
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun MerchantMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(10.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D2D2D))
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MerchantMenuCardWide(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFFF5F5F5)
            ) {
                Icon(icon, null, tint = Color.Gray, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
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

    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color(0xFFFBFBFB)

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
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- ส่วนหัวและปุ่มย้อนกลับ ---
        Spacer(modifier = Modifier.height(40.dp))
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, tint = Color.Black)
        }

        Text(
            "แก้ไขข้อมูลร้านค้า",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2D2D2D),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- ส่วนแก้ไขโลโก้ (Logo Picker) ---
        Box(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                border = BorderStroke(2.dp, Color.White)
            ) {
                if (logoPath != null) {
                    AsyncImage(
                        model = logoPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Storefront,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray,
                            contentDescription = null
                        )
                    }
                }
            }
            // ปุ่มไอคอนกล้องทับซ้อน
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = primaryColor,
                shadowElevation = 4.dp
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- ส่วนกรอกข้อมูล (Information Card) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MinimalTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = "ชื่อร้านค้า",
                    icon = Icons.Default.Storefront,
                    primaryColor = primaryColor
                )

                MinimalTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "เบอร์โทรศัพท์",
                    icon = Icons.Default.Phone,
                    primaryColor = primaryColor
                )

                MinimalTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "ที่อยู่ร้าน",
                    icon = Icons.Default.LocationOn,
                    primaryColor = primaryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- ปุ่มบันทึก ---
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
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("บันทึกการเปลี่ยนแปลง", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(navController: NavHostController, merchantId: Int) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    // ดึงข้อมูลสินค้าจาก Database จริง
    val products by db.productDao().getProductsByMerchant(merchantId).collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color(0xFFFBFBFB)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "จัดการสินค้า",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2D2D2D)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_product/$merchantId") },
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        // ใช้ paddingValues ที่ได้จาก Scaffold เพื่อกันพื้นที่ส่วนหัวโดยอัตโนมัติ
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("ยังไม่มีสินค้าในร้านของคุณ", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,   // 🌟 ปรับระยะห่างให้ดูชิดพอดีสวยงาม
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products) { product ->
                        ProductItemCardMinimal(
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
    }

    // --- Confirmation Dialog ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("ยืนยันการลบ", fontWeight = FontWeight.Bold) },
            text = { Text("คุณต้องการลบสินค้า '${productToDelete?.productName}' ใช่หรือไม่?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        productToDelete?.let { db.productDao().deleteProduct(it) }
                        showDeleteDialog = false
                    }
                }) {
                    Text("ลบสินค้า", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ProductItemCardMinimal(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // แสดงรูปสินค้า
            AsyncImage(
                model = product.imagePath,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2D2D2D))
                Text(product.category, fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(4.dp))

                val hasDiscount = product.discountPrice != null && product.discountPrice!! > 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "฿${if (hasDiscount) product.discountPrice else product.price}",
                        fontWeight = FontWeight.Bold,
                        color = if (hasDiscount) Color(0xFFE53935) else Color(0xFF2E7D32)
                    )
                    if (hasDiscount) {
                        Text(
                            " ฿${product.price}",
                            style = TextStyle(textDecoration = TextDecoration.LineThrough),
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // ปุ่มจัดการ (Edit/Delete) สไตล์มินิมัล
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFFFCDD2), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(navController: NavHostController, merchantId: Int, productId: Int? = null) {
    // --- States (คงเดิมตาม Logic ของคุณ) ---
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
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color(0xFFFBFBFB)

    // Image Picker Launcher (คงเดิม)
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

    // Load Data (คงเดิม)
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

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (productId == null || productId == 0) "เพิ่มสินค้าใหม่" else "แก้ไขสินค้า",
                        fontSize = 20.sp, fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- 1. ส่วนรูปภาพสินค้า (Image Picker) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .shadow(1.dp, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (inputImagePath != null) {
                    AsyncImage(
                        model = inputImagePath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // ปุ่มแก้ไขรูปเล็กๆ มุมขวา
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                        shape = CircleShape, color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp).size(16.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp), tint = primaryColor.copy(alpha = 0.4f))
                        Text("แตะเพื่อเพิ่มรูปภาพสินค้า", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. ฟอร์มข้อมูลสินค้า (Information Card) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MinimalTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = "ชื่อสินค้า (เช่น ข้าวกะเพราไก่)",
                        icon = Icons.Default.Restaurant,
                        primaryColor = primaryColor
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            MinimalTextField(
                                value = inputPrice,
                                onValueChange = { inputPrice = it },
                                label = "ราคาปกติ",
                                icon = Icons.Default.Payments,
                                primaryColor = primaryColor
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MinimalTextField(
                                value = inputDiscountPrice,
                                onValueChange = { inputDiscountPrice = it },
                                label = "ราคาโปร",
                                icon = Icons.Default.LocalOffer,
                                primaryColor = primaryColor
                            )
                        }
                    }

                    // Dropdown หมวดหมู่แบบ Minimal
                    CategoryDropdownMinimal(
                        selectedCategory = inputCategory,
                        onCategorySelected = { inputCategory = it },
                        primaryColor = primaryColor
                    )

                    // ส่วนเวลาจัดส่ง
                    Column {
                        Text("เวลาเตรียมอาหารโดยประมาณ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = minTime,
                                onValueChange = { if (it.all { c -> c.isDigit() }) minTime = it },
                                placeholder = { Text("เร็วสุด") },
                                modifier = Modifier.weight(1f),
                                suffix = { Text("นาที", fontSize = 12.sp) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                            )
                            Text("-", fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = maxTime,
                                onValueChange = { if (it.all { c -> c.isDigit() }) maxTime = it },
                                placeholder = { Text("ช้าสุด") },
                                modifier = Modifier.weight(1f),
                                suffix = { Text("นาที", fontSize = 12.sp) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. ปุ่มบันทึก ---
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("บันทึกข้อมูลสินค้า", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdownMinimal(selectedCategory: String, onCategorySelected: (String) -> Unit, primaryColor: Color) {
    val categories = listOf("อาหารตามสั่ง", "ก๋วยเตี๋ยว", "อาหารญี่ปุ่น", "เครื่องดื่ม", "ของหวาน", "ฟาสต์ฟู้ด")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("เลือกหมวดหมู่สินค้า") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = { Icon(Icons.Default.Category, null, tint = primaryColor) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = primaryColor
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
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

@Composable
fun MerchantBottomNavigation(navController: NavHostController, currentRoute: String?, merchantId: Int) {
    val primaryColor = Color(0xFFE53935)

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("หน้าแรก", "merchant_home", Icons.Default.Home),
            Triple("สินค้า", "manage_products", Icons.Default.List),
            Triple("คำสั่งซื้อ", "merchant_orders", Icons.Default.ReceiptLong),
            Triple("โปรไฟล์", "edit_merchant_profile", Icons.Default.Store)
        )

        items.forEach { (label, routePrefix, icon) ->
            val isSelected = currentRoute?.startsWith(routePrefix) == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate("$routePrefix/$merchantId") {
                            popUpTo("merchant_home/$merchantId") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryColor,
                    selectedTextColor = primaryColor,
                    indicatorColor = primaryColor.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantOrderScreen(navController: NavHostController, merchantId: Int, database: AppDatabase) {
    val scope = rememberCoroutineScope()
    // ดึงออเดอร์ของร้านนี้จาก DB
    val orders by database.orderDao().getOrdersByMerchant(merchantId).collectAsState(initial = emptyList())
    val backgroundColor = Color(0xFFFBFBFB)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("รายการสั่งซื้อ", fontSize = 22.sp, fontWeight = FontWeight.Black)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Text(
                            "ยังไม่มีคำสั่งซื้อเข้ามา",
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // เรียงออเดอร์ใหม่ล่าสุดขึ้นก่อนเพื่อความสะดวกของร้านค้า
                    items(orders.reversed()) { order ->
                        MerchantOrderCardMinimal(order = order) { newStatus ->
                            scope.launch {
                                database.orderDao().updateOrderStatus(order.orderId, newStatus)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantOrderCardMinimal(order: OrderEntity, onStatusChange: (String) -> Unit) {
    val primaryRed = Color(0xFFE53935)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // ส่วนหัว: เลขที่ออเดอร์ และ สถานะ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.orderId}",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFF2D2D2D)
                )
                // เรียกใช้ Badge ที่เราเคยทำไว้เพื่อให้สีตรงกันทั้งแอป
                StatusBadgeMinimal(order.status)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFF5F5F5)
            )

            // รายละเอียดสินค้า
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFBFBFB)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = order.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        text = "จำนวน ${order.qty} รายการ",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ยอดรวม และ ปุ่มจัดการ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ยอดรวมสุทธิ", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "฿${String.format("%.2f", order.totalAmount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = primaryRed
                    )
                }

                // ปุ่ม Action: แสดงเฉพาะออเดอร์ที่ยังไม่เสร็จ
                if (order.status != "สำเร็จ" && order.status != "ยกเลิก") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (order.status != "กำลังเตรียมอาหาร") {
                            OutlinedButton(
                                onClick = { onStatusChange("กำลังเตรียมอาหาร") },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFB8C00)),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("เริ่มทำ", color = Color(0xFFFB8C00), fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { onStatusChange("สำเร็จ") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("เสร็จสิ้น", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}