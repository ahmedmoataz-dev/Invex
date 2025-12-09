package com.example.invex

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun WarehousesManagersScreen(navController: NavHostController) {
    val viewModel: WarehouseManagerViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadManagers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
    ) {
        Text(
            text = "Warehouses Managers",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF243D64),
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(
                    BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.2f)),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (viewModel.searchQuery.isEmpty()) {
                        Text("Search Warehouses Managers...", color = Color(0xFF6C7A89))
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is WarehouseManagerState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is WarehouseManagerState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = (state as WarehouseManagerState.Error).message,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is WarehouseManagerState.Success -> {
                val managers = viewModel.filteredList
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(managers) { manager ->
                        WarehouseManagerCard(
                            managerName = manager.name,
                            warehouseName = manager.warehouse,
                            warehouseLocation = "${manager.governorate}, ${manager.city}"
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Button(
                                onClick = { navController.popBackStack() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))
                            ) {
                                Text("Back", color = Color.White)
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun WarehouseManagerCard(
    managerName: String,
    warehouseName: String,
    warehouseLocation: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF243D64).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Manager: $managerName", fontWeight = FontWeight.Bold, color = Color(0xFF243D64))
        Text("Warehouse: $warehouseName", fontWeight = FontWeight.Medium, color = Color(0xFF243D64))
        Text("Address: $warehouseLocation", fontWeight = FontWeight.Medium, color = Color(0xFF6C7A89))
    }
}
