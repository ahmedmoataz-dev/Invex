package com.example.invex

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("deals")
    suspend fun getDeals(): Response<List<Deal>>

    @GET("vendors")
    suspend fun getVendors(): List<GetVendor>


    @POST("vendors")
    suspend fun addVendor(@Body vendor: AddVendor): AddVendorResponse


    @GET("manager")
    suspend fun getAllManagers(): List<Manager>

    @POST("manager")
    suspend fun addManager(@Body request: AddManagerRequest): AddManagerResponse

    @GET("warehouse_name")
    suspend fun getWarehousesNames(): List<WarehouseVen>

    @GET("warehouse_manager")
    suspend fun getWarehouseManagers(): List<WarehouseManager>

    @GET("warehouse")
    suspend fun getWarehouses(): List<Warehouse>

    @POST("warehouse")
    suspend fun addWarehouse(@Body request: AddWarehouseRequest): AddWarehouseResponse

    @GET("warehouse/{ware_name}")
    suspend fun getWarehouseDetails(@Path("ware_name") name: String): Array<Array<Any>>


}
