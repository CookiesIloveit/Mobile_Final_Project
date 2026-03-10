package com.example.threemusketeers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    var currentUser: UserEntity? = null
    var currentMerchant: MerchantEntity? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRegisterScreen(navController: NavHostController, userDao: UserDao) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color(0xFFFBFBFB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.Black)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2D2D2D),
            letterSpacing = (-1).sp
        )
        Text(
            "Join Coconut family today",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                    value = username,
                    onValueChange = { username = it },
                    label = "Username / Email",
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
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    isError = showError && password.isEmpty()
                )

                MinimalTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    icon = Icons.Default.Phone,
                    primaryColor = primaryColor
                )

                MinimalTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Shipping Address",
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

        Spacer(modifier = Modifier.weight(1f)) // ดันปุ่มลงด้านล่างตามหลัก Thumb Zone

        // --- ปุ่มยืนยัน ---
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
                            navController.navigate(Screen.Home.route) {
                                popUpTo("customer_register") { inclusive = true }
                            }
                        }
                    }
                } else {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .navigationBarsPadding(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Register Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    primaryColor: Color,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    isError: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, fontSize = 14.sp, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = if (isError) Color.Red else primaryColor,
            unfocusedIndicatorColor = if (isError) Color.Red.copy(0.5f) else Color(0xFFF0F0F0),
            cursorColor = primaryColor
        ),
        leadingIcon = { Icon(icon, null, tint = primaryColor, modifier = Modifier.size(20.dp)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, userDao: UserDao, merchantDao: MerchantDao) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showAuthError by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val primaryColor = Color(0xFFE53935)
    val backgroundColor = Color(0xFFFBFBFB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Coconut",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2D2D2D),
            letterSpacing = (-1).sp
        )
        Text(
            "Be Best Friend When You Hungry",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFFEEEEEE)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Customer", "Merchant").forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (selectedTab == index) Color.White else Color.Transparent)
                            .clickable {
                                selectedTab = index
                                showAuthError = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) primaryColor else Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,    // พื้นหลังตอนกด
                        unfocusedContainerColor = Color.Transparent,  // พื้นหลังตอนปกติ
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = primaryColor,         // เส้นใต้ตอนกด
                        unfocusedIndicatorColor = Color(0xFFEEEEEE),  // เส้นใต้ตอนปกติ
                        cursorColor = primaryColor                    // สีตัวขีดพิมพ์
                    ),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = primaryColor) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,    // พื้นหลังตอนกด
                        unfocusedContainerColor = Color.Transparent,  // พื้นหลังตอนปกติ
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = primaryColor,         // เส้นใต้ตอนกด
                        unfocusedIndicatorColor = Color(0xFFEEEEEE),  // เส้นใต้ตอนปกติ
                        cursorColor = primaryColor                    // สีตัวขีดพิมพ์
                    ),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = primaryColor) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    singleLine = true
                )
            }
        }

        if (showAuthError) {
            Text(
                "Invalid username or password",
                color = primaryColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    coroutineScope.launch(Dispatchers.IO) {
                        if (selectedTab == 0) {
                            val user = userDao.login(username, password)
                            withContext(Dispatchers.Main) {
                                if (user != null) {
                                    SessionManager.currentUser = user
                                    SessionManager.currentMerchant = null
                                    navController.navigate(Screen.Home.route) { popUpTo("login") { inclusive = true } }
                                } else { showAuthError = true }
                            }
                        } else {
                            val merchant = merchantDao.loginMerchant(username, password)
                            withContext(Dispatchers.Main) {
                                if (merchant != null) {
                                    SessionManager.currentMerchant = merchant
                                    SessionManager.currentUser = null
                                    navController.navigate("merchant_home/${merchant.merchantId}") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else { showAuthError = true }
                            }
                        }
                    }
                } else { showAuthError = true }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(onClick = {
            if (selectedTab == 0) navController.navigate("customer_register")
            else navController.navigate("merchant_register")
        }) {
            Row {
                Text("Don't have an account? ", color = Color.Gray)
                Text("Register", color = primaryColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}