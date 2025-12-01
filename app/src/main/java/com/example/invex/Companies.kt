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
fun CompaniesScreen(navController: NavHostController) {
    val viewModel: CompaniesViewModel = viewModel()
    var showAddDialog by remember { mutableStateOf(false) }

    // خريطة حالة التوسيع للكاردات
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
                onClick = { showAddDialog = true },
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
            TypeButton("Supplier", viewModel) {
                expandedStates = mapOf() // إعادة تعيين حالة الكارد عند تغيير النوع
            }
            TypeButton("Exporter", viewModel) {
                expandedStates = mapOf()
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
                CompanyCard(company, expandedStates) { id, value ->
                    expandedStates = expandedStates.toMutableMap().also { it[id] = value }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCompanyDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
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
    onExpandChange: (String, Boolean) -> Unit
) {
    val expanded = expandedStates[company.id] ?: false

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF243D64).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { onExpandChange(company.id, !expanded) }
            .padding(16.dp)
    ) {
        Text("Name: ${company.name}", fontWeight = FontWeight.Bold, color = Color(0xFF243D64))
        Text(
            "Address: ${company.governorate}, ${company.city}, ${company.street}",
            color = Color(0xFF6C7A89)
        )

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Phone: ${company.phone}", color = Color(0xFF243D64))
            Text("Email: ${company.email}", color = Color(0xFF243D64))
            Text("License No.: ${company.licenseNumber}", color = Color(0xFF243D64))
        }
    }
}
@Composable
fun AddCompanyDialog(viewModel: CompaniesViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var governorate by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(viewModel.selectedType.value) }
    var errorMessage by remember { mutableStateOf("") }

    // Data class صغيرة لتمثيل الحقول
    data class FieldState(
        val label: String,
        val value: String,
        val onValueChange: (String) -> Unit
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add Company", fontWeight = FontWeight.Bold, color = Color(0xFF243D64)) },
        text = {
            Column {
                // تعريف الحقول
                val fields = listOf(
                    FieldState("Company Name", name) { name = it },
                    FieldState("Governorate", governorate) { governorate = it },
                    FieldState("City", city) { city = it },
                    FieldState("Street", street) { street = it },
                    FieldState("Phone", phone) { phone = it },
                    FieldState("Email", email) { email = it },
                    FieldState("License Number", licenseNumber) { licenseNumber = it }
                )

                // عرض الحقول
                fields.forEach { field ->
                    TextField(
                        value = field.value,
                        onValueChange = field.onValueChange,
                        label = { Text(field.label, color = Color(0xFF243D64)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Company Type", fontWeight = FontWeight.Medium, color = Color(0xFF243D64))
                Row {
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

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || governorate.isBlank() || city.isBlank() || street.isBlank() ||
                        phone.isBlank() || email.isBlank() || licenseNumber.isBlank()
                    ) {
                        errorMessage = "Please fill all fields!"
                    } else {
                        viewModel.addCompany(
                            Company(
                                id = (viewModel.companiesList.size + 1).toString(),
                                name = name,
                                governorate = governorate,
                                city = city,
                                street = street,
                                phone = phone,
                                email = email,
                                type = type,
                                licenseNumber = licenseNumber
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}
