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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun CompaniesScreen(navController: NavHostController) {
    val viewModel: CompaniesViewModel = viewModel()
    val vendorsViewModel: VendorsViewModel = viewModel() // ⚡ نجيب بيانات الفيندورز

    var expandedStates by remember { mutableStateOf(mapOf<String, Boolean>()) }

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
                text = "Companies",
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
                    contentDescription = "Add Company",
                    tint = Color(0xFF243D64),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Type Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TypeButton("Supplier", viewModel) { expandedStates = mapOf() }
            TypeButton("Importer", viewModel) { expandedStates = mapOf() }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
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
                value = viewModel.searchQuery.value,
                onValueChange = { viewModel.updateSearchQuery(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (viewModel.searchQuery.value.isEmpty())
                        Text("Search Companies...", color = Color(0xFF6C7A89))
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(viewModel.filteredList) { company ->
                CompanyCard(
                    company = company,
                    expandedStates = expandedStates,
                    navController = navController,
                    vendorsViewModel = vendorsViewModel
                ) { id, value ->
                    expandedStates = expandedStates.toMutableMap().also { it[id] = value }
                }
            }
        }
    }

    if (viewModel.showAddDialog) {
        AddCompanyDialog(viewModel = viewModel, vendorsViewModel = vendorsViewModel)
    }
}

@Composable
fun TypeButton(type: String, viewModel: CompaniesViewModel, onClickAction: () -> Unit) {
    Button(
        onClick = {
            viewModel.updateSelectedType(type)
            onClickAction()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (viewModel.selectedType.value == type) Color(0xFF243D64)
                else Color.Gray
        )
    ) {
        Text(type, color = Color.White)
    }
}

@Composable
fun CompanyCard(
    company: Company,
    expandedStates: Map<String, Boolean>,
    navController: NavHostController,
    vendorsViewModel: VendorsViewModel,
    onExpandChange: (String, Boolean) -> Unit
) {
    val expanded = expandedStates[company.id] ?: false
    val vendorName = if (company.type == "Importer") {
        vendorsViewModel.vendorsList.find { it.name == company.name }?.name ?: ""
    } else ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF243D64).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable {
                if (company.type == "Supplier") {
                    navController.navigate("supplierDetails/${company.name}")
                } else {
                    onExpandChange(company.id, !expanded)
                }
            }
            .padding(16.dp)
    ) {
        Text("Name: ${company.name}", fontWeight = FontWeight.Bold, color = Color(0xFF243D64))
        Text(
            "Address: ${company.governorate}, ${company.city}, ${company.street}",
            color = Color(0xFF6C7A89)
        )

        if (expanded && company.type != "Supplier") {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Phone: ${company.phone}", color = Color(0xFF243D64))
            Text("Email: ${company.email}", color = Color(0xFF243D64))
            Text("Vendor: ${company.vendor}", color = Color(0xFF243D64))

        }
    }
}

@Composable
fun AddCompanyDialog(viewModel: CompaniesViewModel, vendorsViewModel: VendorsViewModel) {

    var name by remember { mutableStateOf("") }
    var governorate by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(viewModel.selectedType.value) }
    var selectedVendor by remember { mutableStateOf<String?>(null) }
    var expandedVendorDropdown by remember { mutableStateOf(false) }

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
                text = "Add New Company",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF243D64)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CompanyInputField("Company Name", name,{ name = it })
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompanyInputField("Governorate", governorate, { governorate = it }, modifier = Modifier.weight(1f))
                CompanyInputField("City", city, { city = it }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            CompanyInputField("Street", street,{ street = it })
            Spacer(modifier = Modifier.height(8.dp))
            CompanyInputField("Phone", phone,{ phone = it })
            Spacer(modifier = Modifier.height(8.dp))
            CompanyInputField("Email", email,{ email = it })
            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(12.dp))

            Text("Company Type", fontWeight = FontWeight.Medium, color = Color(0xFF243D64))
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                viewModel.companyTypes.forEach { t ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        RadioButton(
                            selected = type == t,
                            onClick = { type = t },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF243D64))
                        )
                        Text(t, color = Color(0xFF243D64))
                    }
                }
            }

            // Vendor dropdown لو النوع Importer
            if (type == "Importer") {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Vendor", fontWeight = FontWeight.Medium, color = Color(0xFF243D64))
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    Text(
                        text = selectedVendor ?: "Select vendor",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedVendorDropdown = true }
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                    DropdownMenu(
                        expanded = expandedVendorDropdown,
                        onDismissRequest = { expandedVendorDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        vendorsViewModel.vendorsList.forEach { vendor ->
                            DropdownMenuItem(
                                text = { Text(vendor.name) },
                                onClick = {
                                    selectedVendor = vendor.name
                                    expandedVendorDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            if (viewModel.errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(viewModel.errorMessage, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.closeAddDialog() }) {
                    Text("Cancel", color = Color(0xFF6C7A89))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (type == "Importer" && selectedVendor == null) {
                            viewModel.errorMessage = "Please select a vendor"
                        } else {
                            viewModel.addCompany(
                                name,
                                governorate,
                                city,
                                street,
                                phone,
                                email,
                                type,
                                selectedVendor
                            )
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

@Composable
fun CompanyInputField(
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
