package com.example.invex

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)
data class LoginResponse(
    val status: String
)

data class GetVendor(
    @SerializedName("Ven_Name")
    val name: String,

    @SerializedName("Inv_Name")
    val warehouse: String,

    @SerializedName("Ven_Type")
    val type: String
)
data class AddVendor(
    @SerializedName("vendor")
    val name: String,

    @SerializedName("warehouse")
    val warehouse: String,

    @SerializedName("type")
    val type: String
)

data class AddVendorResponse(
    @SerializedName("msg")
    val message: String
)
data class WarehouseVen( @SerializedName("Inv_Name") val name: String)

data class WarehouseManager(
    @SerializedName("Responsible")
    val name: String,
    @SerializedName("Inv_Name")
    val warehouse: String,
    @SerializedName("Governorate")
    val governorate: String,
    @SerializedName("City")
    val city: String
)


data class Manager(
    @SerializedName("Man_Name")
    val name: String,
    @SerializedName("Man_Email")
    val email: String
)

data class AddManagerRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class AddManagerResponse(
    @SerializedName("msg")
    val message: String
)

data class Warehouse(
    @SerializedName("Inv_Name") val name: String,
    @SerializedName("Governorate") val governorate: String,
    @SerializedName("City") val city: String,
    @SerializedName("Responsible") val manager: String,
    @SerializedName("Capacity") val capacity: Int,
    @SerializedName("Total_Quantity") val itemsCount: Int
)
data class WarehouseItem(
    @SerializedName("Item_Name") val name: String,
    @SerializedName("Item_Salery") val price: Double,
    @SerializedName("Item_Quantity") val quantity: Int,
    @SerializedName("Com_Name") val company: String
)
data class WarehouseCategory(
    @SerializedName("Cat_Name") val categoryName: String,
    @SerializedName("items") val items: List<WarehouseItem>
)
data class WarehouseDetailsResponse(
    val info: Warehouse,
    val categories: List<WarehouseCategory>
)
data class WarehouseNameRequest(val name: String)


data class AddWarehouseRequest(
    @SerializedName("name") val name: String,
    @SerializedName("governorate") val governorate: String,
    @SerializedName("city") val city: String,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("responsible") val manager: String
)

data class AddWarehouseResponse(
    @SerializedName("msg") val message: String
)
