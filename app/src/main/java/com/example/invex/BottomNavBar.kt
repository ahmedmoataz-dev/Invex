package com.example.invex

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: Int
)

@Composable
fun BottomNavBar(navController: NavHostController) {

    val items = listOf(
        BottomNavItem("Home", "home", R.drawable.ic_home_nav),
        BottomNavItem("Managers", "managers", R.drawable.ic_managers_nav),
        BottomNavItem("Warehouses", "warehouses", R.drawable.ic_warehouses_nav),
        BottomNavItem("Companies", "companies", R.drawable.ic_companies_nav),
        BottomNavItem("Deals", "deals", R.drawable.ic_deals_nav)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painterResource(item.icon),
                        contentDescription = item.title,
                        tint = if (selected) Color.Black else Color(0xFF243D64)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selected) Color.Black else Color(0xFF243D64)
                    )
                }
            )
        }
    }
}
