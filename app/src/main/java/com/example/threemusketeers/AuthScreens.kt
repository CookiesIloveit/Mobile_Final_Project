package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SessionManager {
    var currentUser: UserEntity? = null      // สำหรับลูกค้า
    var currentMerchant: MerchantEntity? = null // สำหรับร้านค้า (เพิ่มตัวนี้)
}

@Composable
fun CustomerRegisterScreen(navController: NavHostController, userDao: UserDao) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("สร้างบัญชีลูกค้า", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text("Email / ชื่อบัญชี") }, modifier = Modifier.fillMaxWidth(),
            isError = showError && username.isEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = image, contentDescription = null) }
            },
            isError = showError && password.isEmpty()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone, onValueChange = { phone = it },
            label = { Text("เบอร์โทรศัพท์") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address, onValueChange = { address = it },
            label = { Text("ที่อยู่จัดส่ง") }, modifier = Modifier.fillMaxWidth(),
            isError = showError && address.isEmpty()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (showError) {
            Text("กรุณากรอกข้อมูลที่จำเป็นให้ครบถ้วน", color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank() && address.isNotBlank()) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val newUser = UserEntity(
                            username = username, password = password,
                            address = address, phone = phone
                        )

                        val generatedId = userDao.insertUser(newUser)
                        SessionManager.currentUser = newUser.copy(userId = generatedId.toInt())

                        withContext(Dispatchers.Main) {
                            navController.navigate(Screen.Home.route) { popUpTo("customer_register") { inclusive = true } }
                        }
                    }
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ลงทะเบียน", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController, userDao: UserDao, merchantDao: MerchantDao) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showAuthError by remember { mutableStateOf(false) }

    // 0 = ลูกค้า, 1 = ร้านค้า
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("สำหรับลูกค้า", "สำหรับร้านค้า")

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // หัวข้อแอป
        Text("Three Musketeers", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD32F2F))
        Spacer(modifier = Modifier.height(24.dp))

        // --- ส่วนเลือกประเภท (Tabs) ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFFD32F2F)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        showAuthError = false // ล้าง error เวลาสลับ tab
                    },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ช่องกรอกข้อมูล (ใช้ร่วมกัน)
        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text(if (selectedTab == 0) "Email / ชื่อบัญชีลูกค้า" else "ชื่อบัญชีร้านค้า") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = image, contentDescription = null) }
            }
        )

        if (showAuthError) {
            Text("ชื่อบัญชีหรือรหัสผ่านไม่ถูกต้อง!", color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ปุ่ม Login ที่ทำงานแยกกันตาม Tab
        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    coroutineScope.launch(Dispatchers.IO) {
                        if (selectedTab == 0) {
                            // --- Login ลูกค้า (โค้ดเพื่อน) ---
                            val user = userDao.login(username, password)
                            withContext(Dispatchers.Main) {
                                if (user != null) {
                                    SessionManager.currentUser = user
                                    SessionManager.currentMerchant = null // เคลียร์ของร้านค้าออก
                                    navController.navigate(Screen.Home.route) { popUpTo("login") { inclusive = true } }
                                } else { showAuthError = true }
                            }
                        } else {
                            // --- Login ร้านค้า (โค้ดคุณ) ---
                            val merchant = merchantDao.loginMerchant(username, password)
                            withContext(Dispatchers.Main) {
                                if (merchant != null) {
                                    SessionManager.currentMerchant = merchant
                                    SessionManager.currentUser = null // เคลียร์ของลูกค้าออก
                                    navController.navigate("merchant_home/${merchant.merchantId}") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else { showAuthError = true }
                            }
                        }
                    }
                } else {
                    showAuthError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (selectedTab == 0) "เข้าสู่ระบบลูกค้า" else "เข้าสู่ระบบร้านค้า", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ปุ่มสมัครสมาชิก แยกหน้าตาม Tab
        TextButton(onClick = {
            if (selectedTab == 0) navController.navigate("customer_register")
            else navController.navigate("merchant_register") // ชื่อ Route ที่คุณตั้งไว้
        }) {
            Text(
                if (selectedTab == 0) "ยังไม่มีบัญชีลูกค้า? สมัครเลย" else "ยังไม่ได้ลงทะเบียนร้านค้า? คลิกที่นี่",
                color = Color(0xFFD32F2F)
            )
        }
    }
}