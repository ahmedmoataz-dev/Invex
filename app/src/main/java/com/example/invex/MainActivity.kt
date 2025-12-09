package com.example.invex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.invex.ui.theme.InvexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InvexApp()
        }
    }
}
@Composable
fun InvexApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf("home","managers", "warehouses", "companies", "deals")

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarScreens) {
                BottomNavBar(navController)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "Onboarding",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("Onboarding") { OnboardingScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("home") { HomeScreenContent(navController) }
            composable("managers") { ManagersScreen(navController) }
            composable("vendors"){VendorsScreen(navController)}
            composable("warehouses") { WarehousesScreen(navController) }
            composable("companies") { CompaniesScreen(navController) }
            composable(
                route = "supplierDetails/{supplierName}"
            ) { backStackEntry ->
                val supplierName = backStackEntry.arguments?.getString("supplierName") ?: ""

                SupplierDetailsScreen(
                    supplierName = supplierName,
                    navController = navController
                )
            }


            composable("deals") { DealsScreen(navController) }
            composable("addDeal"){AddDealScreen(navController)}
            composable("dealDetails/{dealCompany}") { backStackEntry ->
                val dealCompany = backStackEntry.arguments?.getString("dealCompany") ?: ""
                DealDetailsScreen(navController, dealCompany)
            }
            composable("warehousesManagers"){WarehousesManagersScreen(navController)}

            composable("warehouseDetails/{warehouseName}") { backStackEntry ->
                val warehouseName = backStackEntry.arguments?.getString("warehouseName") ?: ""
                WarehouseDetailsScreen(warehouseName, navController)
            }
        }
    }
}
