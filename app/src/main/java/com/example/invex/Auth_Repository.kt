package com.example.invex

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository {

    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return RetrofitClient.api.login(request)
    }

    suspend fun getRecentDeals(): List<Deal> {
        val response = RetrofitClient.api.getDeals()

        return if (response.isSuccessful && response.body() != null) {
            response.body()!!.take(7)
        } else {
            emptyList()
        }
    }

    suspend fun getVendors(): List<GetVendor> {
        return RetrofitClient.api.getVendors()
    }

    suspend fun addVendor(vendor: AddVendor): AddVendorResponse {
        return RetrofitClient.api.addVendor(vendor)
    }


    suspend fun getWarehouseManagers(): List<WarehouseManager> {
        return RetrofitClient.api.getWarehouseManagers()
    }

    suspend fun getManagers(query: String? = null): List<Manager> {
        return RetrofitClient.api.getAllManagers()
    }

    suspend fun addManager(request: AddManagerRequest): AddManagerResponse {
        return RetrofitClient.api.addManager(request)
    }

    suspend fun getWarehousesNames(): List<WarehouseVen> {
        return RetrofitClient.api.getWarehousesNames()
    }

    suspend fun getWarehouses(): List<Warehouse> {
        return RetrofitClient.api.getWarehouses()
    }

    suspend fun addWarehouse(request: AddWarehouseRequest): AddWarehouseResponse {
        return RetrofitClient.api.addWarehouse(request)
    }

    suspend fun getWarehouseDetails(name: String): WarehouseDetailsResponse {
        val raw = RetrofitClient.api.getWarehouseDetails(name)

        val infoList = raw.getOrNull(0) as? List<Map<String, Any>> ?: emptyList()
        val infoMap = infoList.firstOrNull()
        val info = if (infoMap != null) {
            Gson().fromJson(Gson().toJson(infoMap), Warehouse::class.java)
        } else {
            Warehouse(
                name = "Unknown",
                governorate = "",
                city = "",
                manager = "",
                capacity = 0,
                itemsCount = 0
            )
        }

        val categoriesList = raw.getOrNull(1) as? List<Map<String, Any>> ?: emptyList()
        val categories = categoriesList.map { catMap ->
            val itemsJsonString = catMap["items"]?.toString() ?: "[]"
            val itemsList = Gson().fromJson(itemsJsonString, Array<WarehouseItem>::class.java).toList()
            val catName = catMap["Cat_Name"]?.toString() ?: "Unnamed Category"
            WarehouseCategory(catName, itemsList)
        }

        return WarehouseDetailsResponse(info, categories)
    }

}
