package com.finbaby.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val iconMap = mapOf(
    "shopping_bag" to Icons.Filled.ShoppingBag,
    "restaurant" to Icons.Filled.Restaurant,
    "local_gas_station" to Icons.Filled.LocalGasStation,
    "home_work" to Icons.Filled.HomeWork,
    "receipt_long" to Icons.Filled.ReceiptLong,
    "medical_services" to Icons.Filled.MedicalServices,
    "shopping_cart" to Icons.Filled.ShoppingCart,
    "school" to Icons.Filled.School,
    "cleaning_services" to Icons.Filled.CleaningServices,
    "account_balance" to Icons.Filled.AccountBalance,
    "movie" to Icons.Filled.Movie,
    "directions_car" to Icons.Filled.DirectionsCar,
    "spa" to Icons.Filled.Spa,
    "phone_android" to Icons.Filled.PhoneAndroid,
    "payments" to Icons.Filled.Payments,
)

fun resolveIcon(iconName: String): ImageVector {
    return iconMap[iconName] ?: Icons.Filled.Category
}

@Composable
fun CategoryIcon(
    iconName: String,
    color: Long,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp
) {
    val bgColor = Color(color)
    val icon = resolveIcon(iconName)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor.copy(alpha = 0.15f))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconName,
            modifier = Modifier.size(iconSize),
            tint = bgColor
        )
    }
}
