package com.example.threemusketeers

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Account : Screen("account")
    object Payment : Screen("payment/{orderId}") {
        fun createRoute(orderId: String) = "payment/$orderId"
    }
}