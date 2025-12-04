package com.example.invex

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("login") {
            popUpTo("onboarding") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Invex",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(80.dp))

        Image(
            painter = painterResource(id = R.drawable.myy_icon),
            contentDescription = "Onboarding photo",
            modifier = Modifier.size(400.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Manage your inventory efficiently", fontSize = 20.sp)
        Text("Track items, deals, and vendors seamlessly.", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(60.dp))

        CircularProgressIndicator(
            color = Color(0xFF243D64),
            strokeWidth = 4.dp,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(80.dp))
    }
}
