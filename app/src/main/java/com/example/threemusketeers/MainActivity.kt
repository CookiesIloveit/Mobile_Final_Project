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

// --- 1. Factory สำหรับสร้าง ViewModel ---
class CartViewModelFactory(
    private val repository: CartRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AppNavHost(navController: NavHostController, userDao: UserDao, cartViewModel: CartViewModel, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") { LoginScreen(navController = navController, userDao = userDao) }
        composable("customer_register") { CustomerRegisterScreen(navController = navController, userDao = userDao) }
        composable(Screen.Home.route) { HomeScreen(navController = navController) }
        composable(Screen.History.route) { HistoryScreen(cartViewModel = cartViewModel) }
        composable(Screen.Account.route) { AccountScreen(navController = navController) }

        composable(Screen.Store.route) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId")
            StoreScreen(navController = navController, storeId = storeId, cartViewModel = cartViewModel)
        }
        composable(Screen.Payment.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            PaymentScreen(navController = navController, orderId = orderId, cartViewModel = cartViewModel)
        }
    }
}

// (AppTopBar และ AppBottomNavigation เหมือนเดิมทุกประการ ย่อไว้เพื่อความกระชับ)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavHostController, currentRoute: String?) {
    val title = when {
        currentRoute == Screen.Home.route -> "หน้าแรก"
        currentRoute == Screen.History.route -> "ประวัติการสั่งซื้อ"
        currentRoute == Screen.Account.route -> "บัญชีของฉัน"
        currentRoute?.startsWith("store/") == true -> "รายละเอียดร้านอาหาร"
        currentRoute?.startsWith("payment/") == true -> "ชำระเงิน"
        currentRoute == "login" -> "เข้าสู่ระบบ"
        currentRoute == "customer_register" -> "สมัครสมาชิก"
        else -> "Three Musketeers"
    }

    val showBackButton = currentRoute?.startsWith("store/") == true ||
            currentRoute?.startsWith("payment/") == true ||
            currentRoute == "customer_register"

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ย้อนกลับ")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFD32F2F),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
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

    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.History.route, Screen.Account.route)
    val showTopBar = currentRoute != "login"

    val database = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "threemusketeers_db"
        ).build()
    }

    val repository = remember { CartRepository(database.cartDao(), database.orderDao()) }
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModelFactory(repository))
    val userDao = database.userDao()

    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Home.route) {
            val loggedInUserId = SessionManager.currentUser?.userId
            if (loggedInUserId != null) {
                cartViewModel.loadCartFromDatabase(loggedInUserId)
            }
        }
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                AppTopBar(navController = navController, currentRoute = currentRoute)
            }
        },
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(navController = navController, currentRoute = currentRoute)
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            userDao = database.userDao(),
            cartViewModel = cartViewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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