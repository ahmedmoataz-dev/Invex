package com.example.invex

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun ManagersScreen(navController: NavHostController) {
    val viewModel: ManagersViewModel = viewModel()
    var showAddDialog by remember { mutableStateOf(false) }

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
                text = "Managers",
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
                    contentDescription = "Add Manager",
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

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(viewModel.filteredList) { manager ->
                ManagerCard(manager)
            }
        }
    }

    if (showAddDialog) {
        AddManagerDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}
@Composable
fun AddManagerDialog(viewModel: ManagersViewModel, onDismiss: () -> Unit) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
            Text(
                text = "Add New Manager",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF243D64)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields
            ManagerInputField("Name", name,{ name = it })
            Spacer(modifier = Modifier.height(8.dp))
            ManagerInputField("Email", email,{ email = it })
            Spacer(modifier = Modifier.height(8.dp))
            ManagerInputField("Password", password,{ password = it })

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel", color = Color(0xFF6C7A89))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMessage = "Please fill all fields!"
                        } else {
                            val newManager = Manager(
                                id = (viewModel.managersList.size + 1).toString(),
                                name = name,
                                email = email,
                                password = password
                            )
                            viewModel.addManager(newManager)
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
fun ManagerCard(manager: Manager) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF243D64).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Name: ${manager.name}", fontWeight = FontWeight.Bold, color = Color(0xFF243D64))
        Text("Email: ${manager.email}", fontWeight = FontWeight.Medium, color = Color(0xFF6C7A89))
        Text("Password: ${manager.password}", fontWeight = FontWeight.Medium, color = Color(0xFF6C7A89))
    }
}

@Composable
fun ManagerInputField(
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
