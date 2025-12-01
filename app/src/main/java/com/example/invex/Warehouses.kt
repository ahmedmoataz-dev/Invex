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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
@Composable
fun WarehousesScreen(
    navController: NavHostController,
    viewModel: WarehousesViewModel = viewModel()
) {
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
                text = "Warehouses",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF243D64),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = { viewModel.openAddDialog() },
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Warehouse",
                    tint = Color(0xFF243D64),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                value = viewModel.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (viewModel.searchQuery.isEmpty())
                        Text("Search Warehouses...", color = Color(0xFF6C7A89))
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(viewModel.filteredList) { warehouse ->
                WarehouseCard(warehouse = warehouse, onClick = {
                    navController.navigate("warehouseDetails/${warehouse.name}")
                })
            }
        }
    }
// ده الديالووووج
    if (viewModel.showAddDialog) {
        var name by remember { mutableStateOf("") }
        var governorate by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("") }
        var capacity by remember { mutableStateOf("") }
        var managerName by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(450.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = "Add New Warehouse",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF243D64)
                )

                Spacer(modifier = Modifier.height(16.dp))
                InputField("Warehouse Name", name, { name = it })
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InputField("Governorate", governorate, { governorate = it }, modifier = Modifier.weight(1f))
                    InputField("City", city, { city = it }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                InputField("Capacity (items)", capacity, { capacity = it }, keyboardType = KeyboardType.Number)
                Spacer(modifier = Modifier.height(8.dp))
                InputField("Manager Name", managerName, { managerName = it })

                if (viewModel.errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(viewModel.errorMessage, color = Color.Red, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.closeAddDialog() }) {
                        Text("Cancel", color = Color(0xFF6C7A89))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || governorate.isBlank() || city.isBlank() || capacity.isBlank() || managerName.isBlank()) {
                                viewModel.errorMessage = "Please fill all fields!"
                            } else {
                                viewModel.addWarehouse(name, governorate, city, capacity, managerName)
                                viewModel.closeAddDialog()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF243D64))
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F0))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun WarehouseCard(warehouse: Warehouse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(Color(0xFFEFF4FA), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Text(warehouse.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF243D64))
        Spacer(modifier = Modifier.height(4.dp))
        Text(warehouse.location, fontSize = 14.sp, color = Color(0xFF6C7A89))
        Spacer(modifier = Modifier.height(4.dp))
        Text("Manager: ${warehouse.managerName}", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF243D64))
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = warehouse.fillPercent,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = Color(0xFF243D64),
            trackColor = Color(0xFFB0C4DE).copy(alpha = 0.4f)
        )
    }
}
