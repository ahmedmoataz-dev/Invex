package com.example.invex

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AddDealScreen(navController: NavController) {
    var dealType by remember { mutableStateOf("Import") }
    var selectedCompany by remember { mutableStateOf<String?>(null) }
    var selectedWarehouse by remember { mutableStateOf<String?>(null) }
    var selectedVendor by remember { mutableStateOf<String?>(null) }

    val companies = listOf("Company A", "Company B", "Company C")
    val warehouses = listOf("Warehouse X", "Warehouse Y")
    val vendors = listOf("Vendor 1", "Vendor 2")
    val categories = listOf(
        "Electronics" to listOf("Laptop" to 1200.0, "Mouse" to 20.0),
        "Furniture" to listOf("Chair" to 75.0, "Table" to 150.0)
    )
    val quantities = remember { mutableStateMapOf<String, Int>() }

    fun getQuantity(key: String) = quantities[key] ?: 0
    fun increaseQuantity(key: String) { quantities[key] = getQuantity(key) + 1 }
    fun decreaseQuantity(key: String) { if (getQuantity(key) > 0) quantities[key] = getQuantity(key) -1 }
    fun computeTotal(): Double {
        var total = 0.0
        categories.forEach { (_, items) -> items.forEach { (name, price) -> total += price * getQuantity(name) } }
        return total
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {

        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

            // Title + Back
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Add New Deal", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF243D64))
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))) {
                    Text("Back", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deal Type
            Text("Type:", fontSize = 18.sp, color = Color(0xFF243D64))
            Row {
                listOf("Import","Export").forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end=16.dp)) {
                        RadioButton(selected = dealType==type, onClick = {
                            dealType = type
                            selectedCompany = null
                            selectedWarehouse = null
                            selectedVendor = null
                        }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF243D64)))
                        Text(type,color=Color(0xFF243D64))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Company Dropdown
            var expandedCompany by remember { mutableStateOf(false) }
            Text("Select Company", fontSize=16.sp,color=Color(0xFF243D64))
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF243D64).copy(alpha=0.3f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                Text(selectedCompany ?: "Select Company", modifier = Modifier.clickable { expandedCompany=true }, color=Color(0xFF243D64))
                DropdownMenu(expanded=expandedCompany,onDismissRequest={expandedCompany=false}) {
                    companies.forEach { c -> DropdownMenuItem(text={Text(c)}, onClick={selectedCompany=c; expandedCompany=false}) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Warehouse
            if (dealType=="Import" && selectedCompany!=null) {
                var expandedWarehouse by remember { mutableStateOf(false) }
                Text("Select Warehouse", fontSize=16.sp,color=Color(0xFF243D64))
                Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF243D64).copy(alpha=0.3f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Text(selectedWarehouse ?: "Select Warehouse", modifier = Modifier.clickable { expandedWarehouse=true }, color=Color(0xFF243D64))
                    DropdownMenu(expanded=expandedWarehouse,onDismissRequest={expandedWarehouse=false}) {
                        warehouses.forEach { w -> DropdownMenuItem(text={Text(w)}, onClick={selectedWarehouse=w; expandedWarehouse=false}) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Export Vendor + Warehouse
            if (dealType=="Export" && selectedCompany!=null) {
                selectedVendor = vendors.firstOrNull()
                selectedWarehouse = warehouses.firstOrNull()
                Text("Vendor: ${selectedVendor}", fontSize=16.sp,color=Color(0xFF243D64))
                Text("Warehouse: ${selectedWarehouse}", fontSize=16.sp,color=Color(0xFF243D64))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Products LazyColumn scrollable
            if (selectedCompany!=null) {
                Text("Products:", fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color(0xFF243D64))
                LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    categories.forEach { (catName, items) ->
                        item { Text(catName,fontSize=16.sp,fontWeight=FontWeight.Bold,color=Color(0xFF243D64)) }
                        items(items){ (name, price) ->
                            Row(verticalAlignment=Alignment.CenterVertically, modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)) {
                                Text("$name - $$price", modifier=Modifier.weight(1f), color=Color(0xFF243D64))
                                IconButton(onClick={decreaseQuantity(name)}){ Text("-", color=Color(0xFF243D64)) }
                                Text("${getQuantity(name)}", color=Color(0xFF243D64))
                                IconButton(onClick={increaseQuantity(name)}){ Text("+", color=Color(0xFF243D64)) }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Fixed Total + Confirm
        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(Color.White)
            .padding(16.dp)) {
            Text("Total: $${computeTotal()}", fontSize=20.sp,fontWeight=FontWeight.Bold,color=Color(0xFF243D64))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick={ /* do nothing for now */ }, modifier=Modifier.fillMaxWidth(), colors=ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))) {
                Text("Confirm Deal", color=Color.White)
            }
        }
    }
}

