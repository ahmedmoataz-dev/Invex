package com.example.invex

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(),
    dealsViewModel: DealsViewModel = viewModel()
) {

    val shortcuts = homeViewModel.shortcuts
    val dealsList = dealsViewModel.dealsList // هنا نستخدم DealDetail مباشرة

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
    ) {
        Text(
            text = "Invex",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF243D64),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Shortcuts",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF243D64)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            items(shortcuts) { shortcut ->
                ShortcutCard(
                    icon = shortcut.icon,
                    title = shortcut.title,
                    subtitle = shortcut.subtitle,
                    onClick = {
                        when (shortcut.title) {
                            "vendors" -> navController.navigate("vendors")
                            "Warehouses" -> navController.navigate("warehouses")
                            "Companies" -> navController.navigate("companies")
                            "Warehouse Managers" -> navController.navigate("warehousesManagers")
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Recent Deals",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF243D64)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(dealsViewModel.filteredList) { deal ->
                val detail = dealsViewModel.getDealDetail(deal.company)
                if (detail != null) {
                    DealCard(detail, navController)
                }
            }
        }
    }
}

@Composable
fun ShortcutCard(icon: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .height(110.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable { onClick() },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF243D64)
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF6C7A89)
            )
        }
    }
}
@Composable
fun DealCardHome(dealDetail: DealDetail, navController: NavHostController) {
    val totalPrice = dealDetail.products.sumOf { product ->
        val price = product.pricePerUnit.replace("$", "").toDoubleOrNull() ?: 0.0
        price * product.quantity
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable {
                navController.navigate("dealDetails/${dealDetail.company}")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dealDetail.type,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF243D64)
            )
            Text(
                text = dealDetail.company,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF243D64)
            )
            Text(
                text = dealDetail.date,
                fontSize = 14.sp,
                color = Color(0xFF6C7A89)
            )
            // Show vendor only if Export
            if (dealDetail.type.equals("Export", ignoreCase = true)) {
                Text(
                    text = "Vendor: ${dealDetail.vendor}",
                    fontSize = 14.sp,
                    color = Color(0xFF6C7A89)
                )
            }
        }

        Text(
            text = "$${"%.2f".format(totalPrice)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF243D64)
        )
    }
}