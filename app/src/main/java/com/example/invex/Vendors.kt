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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun VendorsScreen(
    navController: NavHostController,
    viewModel: VendorViewModel = viewModel(),
    warehouseViewModel: WarehouseViewModel = viewModel()
) {
    val vendorState by viewModel.vendorState.collectAsState()
    val warehouseState by warehouseViewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadVendors()
        warehouseViewModel.loadWarehouses()
    }

    val warehouses = (warehouseState as? WarehouseState.Success)?.data ?: emptyList()

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
                text = "Vendors",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF243D64),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(70.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Vendor",
                    tint = Color(0xFF243D64),
                    modifier = Modifier.size(50.dp)
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
                .border(
                    BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.2f)),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerField ->
                    if (searchQuery.isEmpty())
                        Text("Search Vendors...", color = Color(0xFF6C7A89))
                    innerField()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (vendorState) {
            is VendorState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is VendorState.Success -> {
                val vendors = (vendorState as VendorState.Success).data.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.warehouse.contains(searchQuery, ignoreCase = true) ||
                            it.type.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(vendors) { vendor ->
                        VendorCard(vendor)
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
            is VendorState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (vendorState as VendorState.Error).message,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> {}
        }
    }

    if (showAddDialog) {
        AddVendorDialog(
            viewModel = viewModel,
            warehouses = warehouses,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun VendorCard(vendor: GetVendor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF243D64).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = vendor.type, fontWeight = FontWeight.Bold, color = Color(0xFF243D64))
        Text(text = vendor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF243D64))
        Text(text = vendor.warehouse, fontWeight = FontWeight.Medium, color = Color(0xFF6C7A89))
    }
}

@Composable
fun AddVendorDialog(
    viewModel: VendorViewModel,
    warehouses: List<WarehouseVen>,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedWarehouse by remember { mutableStateOf<WarehouseVen?>(null) }
    var type by remember { mutableStateOf("Import Vendor") }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isDataLoaded = warehouses.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text("Add Vendor", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF243D64))
            Spacer(modifier = Modifier.height(16.dp))

            VendorInputField("Vendor Name", name) { name = it }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Warehouse", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF243D64))
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0))
                    .clickable { if (isDataLoaded) expanded = true }
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Text(
                    text = selectedWarehouse?.name ?: if (isDataLoaded) "Select Warehouse" else "Loading...",
                    color = if (selectedWarehouse == null) Color.Gray else Color.Black
                )

                if (isDataLoaded) {
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        warehouses.forEach { warehouse ->
                            DropdownMenuItem(
                                text = { Text(warehouse.name) },
                                onClick = {
                                    selectedWarehouse = warehouse
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                listOf("Import Vendor", "Export Vendor").forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                        RadioButton(
                            selected = type == option,
                            onClick = { type = option },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF243D64), unselectedColor = Color.Gray)
                        )
                        Text(option, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) Text(errorMessage, color = Color.Red)

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onDismiss() }) { Text("Cancel", color = Color(0xFF6C7A89)) }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        if (name.isBlank() || selectedWarehouse == null) {
                            errorMessage = "Please fill all fields!"
                        } else {
                            viewModel.addVendor(
                                name, selectedWarehouse!!.name, type,
                                onSuccess = { onDismiss() },
                                onError = { msg -> errorMessage = msg }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))
                ) { Text("Add", color = Color.White) }

            }
        }
    }
}

@Composable
fun VendorInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF243D64))
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
