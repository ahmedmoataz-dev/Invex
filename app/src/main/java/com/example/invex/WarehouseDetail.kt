package com.example.invex

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun WarehouseDetailsScreen(
    warehouseName: String,
    navController: NavHostController,
    viewModel: WarehouseDetailsViewModel = viewModel()
) {
    LaunchedEffect(warehouseName) { viewModel.loadDetails(warehouseName) }

    when (val state = viewModel.state.value) {
        is WarehouseDetailsState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WarehouseDetailsState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.message}", color = Color.Red)
            }
        }
        is WarehouseDetailsState.Success -> {
            val warehouse = state.data.info
            val categories = state.data.categories
            val fillPercent = if (warehouse.capacity > 0) warehouse.itemsCount.toFloat() / warehouse.capacity else 0f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(20.dp)
            ) {
                Text(warehouse.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF243D64))
                Spacer(Modifier.height(8.dp))
                Text("Manager: ${warehouse.manager}", fontSize = 16.sp, color = Color(0xFF6C7A89))
                Spacer(Modifier.height(12.dp))
                Text("Capacity: ${warehouse.capacity} items", fontSize = 16.sp)
                Text("Total Items: ${warehouse.itemsCount}", fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = fillPercent.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = Color(0xFF243D64),
                    trackColor = Color(0xFFB0C4DE).copy(alpha = 0.3f)
                )

                Spacer(Modifier.height(16.dp))
                Text("Categories:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF243D64))
                Spacer(Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(categories) { category ->
                        CategoryCard(category)
                    }

                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.CenterEnd) {
                            Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))) {
                                Text("Back", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: WarehouseCategory) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Text(category.categoryName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF243D64))
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            if (category.items.isEmpty()) Text("No products available", color = Color.Gray)
            else category.items.forEach { ProductRow(it) }
        }
    }
}

@Composable
fun ProductRow(product: WarehouseItem) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("Name: ${product.name}", fontWeight = FontWeight.Medium)
        Text("Price: \$${product.price}", fontSize = 14.sp, color = Color.DarkGray)
        Text("Quantity: ${product.quantity}", fontSize = 14.sp, color = Color.DarkGray)
        Text("Supplier: ${product.company}", fontSize = 14.sp, color = Color.DarkGray)
        Divider(Modifier.padding(vertical = 4.dp))
    }
}
