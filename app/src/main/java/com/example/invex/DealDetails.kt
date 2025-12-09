package com.example.invex

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun DealDetailsScreen(
    navController: NavHostController,
    dealCompany: String
) {
    val viewModel: DealDetailsViewModel = viewModel()
    val viewModelDeal: DealsViewModel = viewModel()


    LaunchedEffect(dealCompany) {
        viewModel.loadDealDetail(dealCompany,viewModelDeal)
    }

    val deal = viewModel.dealDetail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        deal?.let { d ->
            Text(
                text = "Deal Details",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B3B6F),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow(label = "Type", value = d.type)
                    InfoRow(label = "Company", value = d.company, isBold = true)
                    InfoRow(label = "Date", value = d.date)
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF243D64).copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(label = "Warehouse", value = d.warehouseName)
                    InfoRow(label = "Address", value = d.warehouseLocation)

                    if (d.type == "Export") {
                        InfoRow(label = "Vendor", value = d.vendor)
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Products",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B3B6F)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f) // مهم عشان ياخد المساحة المتاحة
            ) {
                items(d.products) { product ->
                    DealProductCardProfessional(product)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val totalPrice = d.products.sumOf { product ->
                val price = product.pricePerUnit.replace("$", "").toDoubleOrNull() ?: 0.0
                price * product.quantity
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                border = BorderStroke(1.dp, Color(0xFF1B3B6F).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Price",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1B3B6F)
                    )
                    Text(
                        text = "$${"%.2f".format(totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1B3B6F)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243D64)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Text(
                    text = "Back",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6C7A89),
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF243D64)
        )
    }
}

@Composable
fun DealProductCardProfessional(product: DealProduct) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFF243D64).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1B3B6F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Category: ${product.category}", fontWeight = FontWeight.Medium, color = Color(0xFF6C7A89))
            Text("Quantity: ${product.quantity}", color = Color(0xFF6C7A89))
            Text("Price per unit: ${product.pricePerUnit}", fontWeight = FontWeight.Medium, color = Color(0xFF243D64))
        }
    }
}
