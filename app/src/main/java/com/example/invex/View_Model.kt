package com.example.invex

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

/***********************************
 *            LOGIN VIEWMODEL
 ***********************************/
class LoginViewModel : ViewModel() {
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var passwordVisible = mutableStateOf(false)

    fun togglePasswordVisibility() {
        passwordVisible.value = !passwordVisible.value
    }

    fun login() {
        // TODO: Implement login logic later
    }
}

/***********************************
 *            HOME VIEWMODEL
 ***********************************/
data class Deal(val company: String, val cost: String, val date: String)
data class Shortcut(val icon: Int, val title: String, val subtitle: String)

class HomeViewModel : ViewModel() {

    var shortcuts by mutableStateOf(
        listOf(
            Shortcut(R.drawable.ic_items, "Items", "Explore your items"),
            Shortcut(R.drawable.ic_warehouses, "Warehouses", "Manage your warehouses"),
            Shortcut(R.drawable.ic_companies, "Companies", "View your companies"),
            Shortcut(R.drawable.ic_managers, "Warehouse Managers", "See warehouse managers")
        )
    )
        private set

    var recentDealsList by mutableStateOf(
        listOf(
            Deal("Company A", "$1500", "2024-07-26"),
            Deal("Company B", "$2200", "2024-07-25"),
            Deal("Company C", "$800", "2024-07-24")
        )
    )
        private set
}

/***********************************
 *       WAREHOUSES LIST VIEWMODEL
 ***********************************/
data class Warehouse(
    val name: String,
    val location: String,
    val fillPercent: Float,
    val managerName: String
)

class WarehousesViewModel : ViewModel() {
    var warehousesList by mutableStateOf(
        listOf(
            Warehouse("Warehouse A", "Cairo, Downtown", 0.7f, "Ahmed"),
            Warehouse("Warehouse B", "Alexandria, Smouha", 0.4f, "Sara"),
            Warehouse("Warehouse C", "Giza, Dokki", 0.9f, "Omar")
        )
    )
        private set

    var searchQuery by mutableStateOf("")
        private set

    var showAddDialog by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")


    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun openAddDialog() {
        showAddDialog = true
        errorMessage = ""
    }

    fun closeAddDialog() {
        showAddDialog = false
        errorMessage = ""
    }

    fun addWarehouse(
        name: String,
        governorate: String,
        city: String,
        capacity: String,
        managerName: String
    ) {
        if (name.isBlank() || governorate.isBlank() || city.isBlank() || capacity.isBlank() || managerName.isBlank()) {
            errorMessage = "Please fill all fields!"
            return
        }
        val itemsCount = capacity.toIntOrNull() ?: 0
        val location = "$governorate, $city"
        val fill = itemsCount.toFloat() / 100f
        warehousesList = warehousesList + Warehouse(name, location, fill, managerName)
        closeAddDialog()
    }

    val filteredList: List<Warehouse>
        get() = warehousesList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true) ||
                    it.managerName.contains(searchQuery, ignoreCase = true)
        }
}

/***********************************
 *      WAREHOUSE DETAILS VIEWMODEL
 ***********************************/
data class Product(
    val name: String,
    val price: Double,
    val quantity: Int,
    val supplier: String
)

data class Category(
    val name: String,
    val products: List<Product>
)

data class WarehouseDetail(
    val name: String,
    val capacity: Int,
    val fillPercent: Float,
    val manager: String,
    val categories: List<Category>
)

class WarehouseDetailsViewModel : ViewModel() {

    private val _warehouseDetails = mutableStateOf<WarehouseDetail?>(null)
    val warehouseDetails: State<WarehouseDetail?> = _warehouseDetails

    fun loadWarehouse(name: String) {
        _warehouseDetails.value = WarehouseDetail(
            name = name,
            capacity = 1000,
            fillPercent = 0.65f,
            manager = "Ahmed Moataz",
            categories = listOf(
                Category(
                    "Electronics",
                    listOf(
                        Product("Laptop", 1200.0, 5, "Dell"),
                        Product("Mouse", 20.0, 10, "Logitech")
                    )
                ),
                Category(
                    "Furniture",
                    listOf(
                        Product("Chair", 75.0, 20, "Ikea"),
                        Product("Table", 150.0, 5, "Ikea")
                    )
                ),
                Category(
                    "Food",
                    listOf(
                        Product("Chocolate", 3.5, 100, "Nestle"),
                        Product("Apple", 1.0, 50, "LocalFarm")
                    )
                )
            )
        )
    }
}

/***********************************
 *      MANAGERS VIEWMODEL
 ***********************************/
data class Manager(
    val id: String,
    val name: String,
    val email: String,
    val password: String
)

class ManagersViewModel : ViewModel() {

    var managersList by mutableStateOf(
        listOf(
            Manager("1", "Ahmed", "ahmed@example.com", "1234"),
            Manager("2", "Sara", "sara@example.com", "abcd"),
            Manager("3", "Omar", "omar@example.com", "pass")
        )
    )
        private set

    var searchQuery by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    val filteredList: List<Manager>
        get() = managersList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }

    fun addManager(manager: Manager) {
        managersList = managersList + manager
    }
}
// جزء الشركااات
// Data Class للشركة
data class Company(
    val id: String,
    val name: String,
    val governorate: String,
    val city: String,
    val street: String,
    val phone: String,
    val email: String,
    val type: String,
    val licenseNumber: String
)

class CompaniesViewModel : ViewModel() {

    // القائمة الكاملة لكل الشركات
    var companiesList = mutableStateListOf<Company>()
        private set

    // النوع المختار حاليا
    var selectedType = mutableStateOf("Supplier")
        private set

    // قيمة البحث
    var searchQuery = mutableStateOf("")

    // أنواع الشركات
    val companyTypes = listOf("Supplier", "Exporter")

    // رسالة خطأ عند إضافة شركة
    var errorMessage by mutableStateOf("")

    // حالة عرض الديالوج
    var showAddDialog by mutableStateOf(false)

    // قائمة مفلترة حسب النوع والبحث
    val filteredList: List<Company>
        get() = companiesList.filter { company ->
            (company.type == selectedType.value) &&
                    (company.name.contains(searchQuery.value, ignoreCase = true)
                            || company.city.contains(searchQuery.value, ignoreCase = true)
                            || company.governorate.contains(searchQuery.value, ignoreCase = true))
        }

    // تحديث النوع المختار
    fun updateSelectedType(type: String) {
        selectedType.value = type
    }

    // تحديث قيمة البحث
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    // إضافة شركة جديدة
    fun addCompany(company: Company) {
        companiesList.add(company)
    }

    // فتح/إغلاق الديالوج
    fun openAddDialog() {
        showAddDialog = true
        errorMessage = ""
    }

    fun closeAddDialog() {
        showAddDialog = false
        errorMessage = ""
    }

    // مثال على شركات جاهزة عند بداية التطبيق
    init {
        companiesList.addAll(
            listOf(
                Company(
                    id = "1",
                    name = "Alpha Suppliers",
                    governorate = "Cairo",
                    city = "Nasr City",
                    street = "Street 1",
                    phone = "01000000001",
                    email = "alpha@example.com",
                    type = "Supplier",
                    licenseNumber = "SUP123"
                ),
                Company(
                    id = "2",
                    name = "Beta Exporters",
                    governorate = "Giza",
                    city = "Dokki",
                    street = "Street 2",
                    phone = "01000000002",
                    email = "beta@example.com",
                    type = "Exporter",
                    licenseNumber = "EXP456"
                )
            )
        )
    }
}
