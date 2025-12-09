package com.example.invex

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun DealsScreen(navController: NavHostController) {
    val dealsViewModel: DealsViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Deals",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF243D64),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = { navController.navigate("addDeal") },
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Deal",
                    tint = Color(0xFF243D64),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = dealsViewModel.searchQuery,
                onValueChange = { dealsViewModel.updateSearchQuery(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (dealsViewModel.searchQuery.isEmpty()) {
                        Text("Search Deals...", color = Color(0xFF6C7A89))
                    }
                    innerTextField()
                }
            )
        }

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
fun DealCard(dealDetail: DealDetail, navController: NavHostController) {
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
        }

        Text(
            text = "$${"%.2f".format(totalPrice)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF243D64)
        )
    }
}
