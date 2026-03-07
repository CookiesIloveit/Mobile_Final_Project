package com.example.threemusketeers

sealed class Screen(val route: String) {
    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")
    object History : Screen("history")
    object Account : Screen("account")

    object Store : Screen("store/{storeId}") {
        fun createRoute(storeId: String) = "store/$storeId"
    }

    object Payment : Screen("payment/{orderId}") {
        fun createRoute(orderId: String) = "payment/$orderId"
    }
}