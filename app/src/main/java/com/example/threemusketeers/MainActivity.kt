package com.example.threemusketeers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room


@Composable
fun AppNavHost(navController: NavHostController,
               database: AppDatabase,
               cartViewModel: CartViewModel,
               modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            // ส่งทั้ง userDao และ merchantDao เข้าไป
            LoginScreen(
                navController = navController,
                userDao = database.userDao(),     // ดึงจาก database ตรงนี้เลย
                merchantDao = database.merchantDao() // ดึงจาก database ตรงนี้เลย
            )
        }
        composable("customer_register") { CustomerRegisterScreen(navController = navController, userDao = database.userDao()) }
        composable("merchant_register") {
            MerchantRegisterScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController, database = database)
        }
        composable("merchant_home/{merchantId}") { backStackEntry ->
            val merchantId = backStackEntry.arguments?.getString("merchantId")?.toIntOrNull() ?: 0
            MerchantHomeScreen(navController = navController, merchantId = merchantId)
        }

        composable(Screen.History.route) {
            HistoryScreen(
                cartViewModel = cartViewModel,
                database = database // ส่ง database เข้าไปด้วย
            )
        }
        composable(Screen.Account.route) { AccountScreen(navController = navController) }

        composable("store/{storeId}") { backStackEntry -> // ตรวจสอบว่าใน Screen.Store.route คือ "store/{storeId}"
            val storeId = backStackEntry.arguments?.getString("storeId")
            StoreScreen(
                navController = navController,
                storeId = storeId,
                cartViewModel = cartViewModel,
                database = database
            )
        }
        composable(Screen.Payment.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            PaymentScreen(navController = navController, orderId = orderId, cartViewModel = cartViewModel)
        }
        // ภายใน NavHost { ... } ในไฟล์ AppNavHost
        composable("merchant_home/{merchantId}") { backStackEntry ->
            val mId = backStackEntry.arguments?.getString("merchantId")?.toIntOrNull() ?: 0
            MerchantHomeScreen(navController, mId) // อย่าลืมสร้างหน้าเมนูหลักของร้านค้า
        }

        composable("manage_products/{merchantId}") { backStackEntry ->
            val mId = backStackEntry.arguments?.getString("merchantId")?.toIntOrNull() ?: 0
            ManageProductsScreen(navController, mId)
        }

        composable("add_product/{merchantId}") { backStackEntry ->
            val mId = backStackEntry.arguments?.getString("merchantId")?.toIntOrNull() ?: 0
            AddEditProductScreen(navController, mId)
        }

        composable("edit_product/{productId}") { backStackEntry ->
            val pId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
            // สำหรับการแก้ไข เราดึง merchantId จากฐานข้อมูลในหน้า screen อยู่แล้ว ส่งเป็น 0 ไปก่อนได้
            AddEditProductScreen(navController, merchantId = 0, productId = pId)
        }

        composable("edit_merchant_profile/{merchantId}") { backStackEntry ->
            val mId = backStackEntry.arguments?.getString("merchantId")?.toIntOrNull() ?: 0
            EditMerchantProfileScreen(navController, mId)
        }

        composable("merchant_orders/{merchantId}") { backStackEntry ->
            val mId = backStackEntry.arguments?.getString("merchantId")?.toIntOrNull() ?: 0
            MerchantOrderScreen(navController, mId, database)
        }
    }
}

@Composable
fun AppBottomNavigation(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("หน้าแรก") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "History") },
            label = { Text("ประวัติ") },
            selected = currentRoute == Screen.History.route,
            onClick = {
                navController.navigate(Screen.History.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
            label = { Text("บัญชี") },
            selected = currentRoute == Screen.Account.route,
            onClick = {
                navController.navigate(Screen.Account.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // กำหนดว่าหน้าไหนจะโชว์ BottomBar (เฉพาะหน้าหลักของลูกค้า)
    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.History.route, Screen.Account.route)

    // สร้าง Database Instance
    val database = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "threemusketeers_db"
        )
            .fallbackToDestructiveMigration() // ช่วยให้แอปไม่ค้างเวลาเปลี่ยนโครงสร้างตาราง
            .build()
    }

    // เตรียม Repository และ ViewModels
    val repository = remember { CartRepository(database.cartDao(), database.orderDao()) }
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModelFactory(repository))


    // ตรวจสอบว่าเป็นหน้าของ Merchant หรือไม่
    val isMerchantRoute = currentRoute?.startsWith("merchant_home") == true ||
            currentRoute?.startsWith("manage_products") == true ||
            currentRoute?.startsWith("add_product") == true ||
            currentRoute?.startsWith("edit_product") == true ||
            currentRoute?.startsWith("edit_merchant_profile") == true ||
            currentRoute?.startsWith("merchant_orders") == true // เพิ่มบรรทัดนี้ครับ

    // จัดการเรื่องการโหลดข้อมูลตะกร้าเมื่อมีการเปลี่ยนหน้า
    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Home.route) {
            val loggedInUserId = SessionManager.currentUser?.userId
            if (loggedInUserId != null) {
                cartViewModel.loadCartFromDatabase(loggedInUserId)
            }
        }
    }
    Scaffold(
        bottomBar = {
            if (isMerchantRoute) {
                val merchantIdFromRoute = navBackStackEntry?.arguments?.getString("merchantId")?.toIntOrNull()
                    ?: SessionManager.currentMerchant?.merchantId
                    ?: 0
                MerchantBottomNavigation(navController, currentRoute, merchantIdFromRoute)
            } else if (currentRoute in listOf(Screen.Home.route, Screen.History.route, Screen.Account.route)) {
                AppBottomNavigation(navController, currentRoute)
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            database = database,
            cartViewModel = cartViewModel,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isMerchantRoute) Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    else Modifier.padding(innerPadding)
                )
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp()
                }
            }
        }
    }
}