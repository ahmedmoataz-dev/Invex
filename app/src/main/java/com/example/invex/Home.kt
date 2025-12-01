package com.example.invex

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

val PrimaryColor = Color(0xFF243D64)
val SecondaryColor = Color(0xFF6C7A89)
val BackgroundColor = Color(0xFFF5F5F5)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {

    val shortcuts = viewModel.shortcuts
    val recentDealsList = viewModel.recentDealsList

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(20.dp)
    ) {
        Text(
            text = "Invex",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Shortcuts",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryColor
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
                            "Items" -> navController.navigate("items")
                            "Warehouses" -> navController.navigate("warehouses") {
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                restoreState = true
                            }
                            "Companies" -> navController.navigate("companies"){
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                restoreState = true
                            }
                            "Warehouse Managers" -> navController.navigate("managers"){
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                restoreState = true
                            }
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
            color = PrimaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(recentDealsList) { deal ->
                RecentDealCard(deal.company, deal.cost, deal.date)
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
            .border(BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
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
                color = PrimaryColor
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = SecondaryColor
            )
        }
    }
}

@Composable
fun RecentDealCard(company: String, cost: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f)), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = "Associated Item",
                fontSize = 13.sp,
                color = SecondaryColor
            )

            Text(
                text = company,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = PrimaryColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Cost: $cost   |   Date: $date",
                fontSize = 14.sp,
                color = SecondaryColor.copy(alpha = 0.7f)
            )
        }
    }
}
