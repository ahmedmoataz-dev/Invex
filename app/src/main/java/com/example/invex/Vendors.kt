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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun VendorsScreen(navController: NavHostController) {
    val WViewModel:WarehousesViewModel=viewModel()
    val viewModel: VendorsViewModel = viewModel()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
    ) {

        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vendors",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF243D64),
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Vendor",
                    tint = Color(0xFF243D64),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
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
                value = viewModel.searchQuery.value,
                onValueChange = { viewModel.updateSearchQuery(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerField ->
                    if (viewModel.searchQuery.value.isEmpty())
                        Text("Search Vendors...", color = Color(0xFF6C7A89))
                    innerField()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vendors List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(viewModel.filteredList) { vendor ->
                VendorCard(vendor)
            }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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

    if (showAddDialog) {
        AddVendorDialog(
             viewModel,
            WViewModel,
             { showAddDialog = false }
        )
    }
}

@Composable
fun AddVendorDialog(
    vendorsViewModel: VendorsViewModel,
    warehousesViewModel: WarehousesViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val warehousesNames = warehousesViewModel.warehousesList.map { it.name }
    val selectedWarehouse = vendorsViewModel.selectedWarehouse

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .width(380.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {

            Text(
                text = "Add New Vendor",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF243D64)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name input
            VendorInputField("Vendor Name", name) { name = it }

            Spacer(modifier = Modifier.height(14.dp))

            // ---------------------
            //   WAREHOUSE DROPDOWN
            // ---------------------
            Text(
                text = "Select Warehouse",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF243D64)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF243D64), RoundedCornerShape(10.dp))
                        .clickable{ expanded = true }
                        .padding(14.dp)
                ) {
                    Text(
                        text = selectedWarehouse ?: "Select Warehouse",
                        color = if (selectedWarehouse == null) Color.Gray else Color(0xFF243D64),
                        fontSize = 16.sp
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    warehousesNames.forEach { warehouseName ->
                        DropdownMenuItem(
                            text = { Text(warehouseName) },
                            onClick = {
                                vendorsViewModel.updateSelectedWarehouse(warehouseName)
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel", color = Color(0xFF6C7A89))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        val warehouse = vendorsViewModel.selectedWarehouse
                        if (name.isBlank()) {
                            errorMessage = "Please enter vendor name!"
                        } else if (vendorsViewModel.selectedWarehouse == null) {
                            errorMessage = "Please select a warehouse!"
                        } else {
                            vendorsViewModel.addVendor(
                                Vendor(
                                    id = (vendorsViewModel.vendorsList.size + 1).toString(),
                                    name = name,
                                    warehouse = warehouse!!
                                )
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))
                ) {
                    Text("Add", color = Color.White)
                }
            }
        }
    }
}


@Composable
fun VendorCard(vendor: Vendor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF243D64).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = vendor.name,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF243D64)
        )
        Text(text = vendor.warehouse, fontWeight = FontWeight.Medium, color = Color(0xFF6C7A89))

    }
}

@Composable
fun VendorInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF243D64)
        )

        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F0))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
