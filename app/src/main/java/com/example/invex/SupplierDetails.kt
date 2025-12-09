package com.example.invex

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun SupplierDetailsScreen(
    supplierName: String,
    navController: NavHostController,
    viewModel: SupplierDetailsViewModel = viewModel()
) {
    val companiesViewModel: CompaniesViewModel = viewModel()
    LaunchedEffect(supplierName) {
        viewModel.loadSupplier(supplierName,companiesViewModel)
    }

    val supplierInfo = viewModel.currentSupplierInfo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = viewModel.currentSupplierName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF243D64)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Address: ${supplierInfo?.address ?: ""}", color = Color(0xFF243D64))
        Text("Phone: ${supplierInfo?.phone ?: ""}", color = Color(0xFF243D64))
        Text("Email: ${supplierInfo?.email ?: ""}", color = Color(0xFF243D64))

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.showAddCategoryDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64))
        ) {
            Text("Add Category", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(supplierInfo?.categories ?: emptyList()) { category ->
                CategoryCard(category, viewModel)
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

    if (viewModel.showAddCategoryDialog) {
        AddCategoryDialog(viewModel)
    }

    if (viewModel.showAddItemDialog) {
        AddItemDialog(viewModel)
    }
}


@Composable
fun CategoryCard(category: SupplierCategory, viewModel: SupplierDetailsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = category.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    viewModel.selectedCategoryId = category.id
                    viewModel.showAddItemDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64)),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Add Item", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        category.items.forEach { item ->
            Text("- ${item.name}: ${item.price} EGP", color = Color(0xFF243D64))
        }
    }
}

@Composable
fun AddCategoryDialog(viewModel: SupplierDetailsViewModel) {

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .width(350.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {

            Text(
                text = "Add Category",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF243D64)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF243D64)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(viewModel.selectedCategoryName ?: "Select...")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SupplierDetailsViewModel.predefinedCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            expanded = false
                            viewModel.selectedCategoryName = category
                        }
                    )
                }
            }


            if (viewModel.selectedCategoryName == "Add New Category") {
                Spacer(modifier = Modifier.height(12.dp))

                AppInputField(
                    label = "New Category Name",
                    value = viewModel.newCategoryName,
                    onValueChange = { viewModel.newCategoryName = it }
                )
            }


            if (viewModel.categoryError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(viewModel.categoryError, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.showAddCategoryDialog = false }) {
                    Text("Cancel", color = Color(0xFF6C7A89))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        viewModel.addCategory()
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
fun AddItemDialog(viewModel: SupplierDetailsViewModel) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(350.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "Add New Item",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF243D64)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppInputField("Item Name", itemName,{ itemName = it })
            Spacer(modifier = Modifier.height(12.dp))
            AppInputField(
                "Price",
                itemPrice,
                onValueChange = { itemPrice = it },
                keyboardType = KeyboardType.Number
            )

            if (viewModel.itemError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(viewModel.itemError, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.showAddItemDialog = false }) {
                    Text("Cancel", color = Color(0xFF6C7A89))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        viewModel.newItemName = itemName
                        viewModel.newItemPrice = itemPrice
                        viewModel.addItem()
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
fun AppInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
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
