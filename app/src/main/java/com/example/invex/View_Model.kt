package com.example.invex

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response


sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val data: LoginResponse?) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var passwordVisible = mutableStateOf(false)

    var loginErrorMessage = mutableStateOf<String?>(null)

    private val repo = AuthRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun togglePasswordVisibility() {
        passwordVisible.value = !passwordVisible.value
    }

    fun login(navController: NavController) {
        loginErrorMessage.value = null

        if (!validateLogin()) return

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val response = repo.login(LoginRequest(email.value, password.value))

                if (response.isSuccessful) {
                    loginErrorMessage.value = null
                    _loginState.value = LoginState.Success(response.body())
                    navController.navigate("home")
                } else {
                    loginErrorMessage.value = "Invalid email or password"
                    _loginState.value = LoginState.Error("Login failed")
                }

            } catch (e: Exception) {
                loginErrorMessage.value = "Network error: ${e.message}"
                _loginState.value = LoginState.Error(e.message ?: "Error")
            }
        }
    }
    private fun validateLogin(): Boolean {
        if (email.value.isBlank()) {
            loginErrorMessage.value = "Email is required"
            return false
        }

        if (!email.value.contains("@") || !email.value.contains(".com")) {
            loginErrorMessage.value = "Enter a valid email"
            return false
        }

        if (password.value.isBlank()) {
            loginErrorMessage.value = "Password is required"
            return false
        }

        return true
    }

}


data class Shortcut(val icon: Int, val title: String, val subtitle: String)
sealed class DealState {
    data object Idle : DealState()
    data object Loading : DealState()
    data class Success(val data: List<Deal>) : DealState()
    data class Error(val message: String) : DealState()
}
class HomeViewModel : ViewModel() {
    var shortcuts by mutableStateOf(
        listOf(
            Shortcut(R.drawable.van, "vendors", "Explore your vendors"),
            Shortcut(R.drawable.ic_managers, "Warehouse Managers", "See warehouse managers")
        )
    )
        private set
    private val repo = AuthRepository()

    private val _dealState = MutableStateFlow<DealState>(DealState.Idle)
    val dealState: StateFlow<DealState> = _dealState

    fun loadRecentDeals() {
        _dealState.value = DealState.Loading

        viewModelScope.launch {
            try {
                val recent = repo.getRecentDeals()
                _dealState.value = DealState.Success(recent)

            } catch (e: Exception) {
                _dealState.value = DealState.Error(e.message ?: "Error loading deals")
            }
        }
    }
}



sealed class WarehouseStateTotal {
    object Idle : WarehouseStateTotal()
    object Loading : WarehouseStateTotal()
    data class Success(val data: List<Warehouse>) : WarehouseStateTotal()
    data class Error(val message: String) : WarehouseStateTotal()
}

class WarehouseViewModelTotal(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _state = MutableStateFlow<WarehouseStateTotal>(WarehouseStateTotal.Idle)
    val state: StateFlow<WarehouseStateTotal> = _state

    var searchQuery by mutableStateOf("")
    var errorMessage by mutableStateOf("")
    var showAddDialog by mutableStateOf(false)
    var warehousesList = mutableStateOf(listOf<Warehouse>())

    val filteredList: List<Warehouse>
        get() = warehousesList.value.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.manager.contains(searchQuery, ignoreCase = true)
        }

    fun loadWarehouses() {
        _state.value = WarehouseStateTotal.Loading
        viewModelScope.launch {
            try {
                val warehouses = repo.getWarehouses()
                warehousesList.value = warehouses
                _state.value = WarehouseStateTotal.Success(warehouses)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = WarehouseStateTotal.Error(e.message ?: "Failed to load warehouses")
            }
        }
    }

    fun addWarehouse(
        name: String,
        governorate: String,
        city: String,
        manager: String,
        capacity: Int,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = repo.addWarehouse(
                    AddWarehouseRequest(name, governorate, city, capacity, manager)
                )
                if (response.message.contains("success", ignoreCase = true)) {
                    loadWarehouses()
                    onSuccess()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to add warehouse"
            }
        }
    }

    fun openAddDialog() {
        showAddDialog = true
        errorMessage = ""
    }

    fun closeAddDialog() {
        showAddDialog = false
        errorMessage = ""
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
}


sealed class WarehouseDetailsState {
    object Loading : WarehouseDetailsState()
    data class Success(val data: WarehouseDetailsResponse) : WarehouseDetailsState()
    data class Error(val message: String) : WarehouseDetailsState()
}

class WarehouseDetailsViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _state = mutableStateOf<WarehouseDetailsState>(WarehouseDetailsState.Loading)
    val state: State<WarehouseDetailsState> = _state

    fun loadDetails(name: String) {
        viewModelScope.launch {
            _state.value = WarehouseDetailsState.Loading
            try {
                val response = repo.getWarehouseDetails(name)
                _state.value = WarehouseDetailsState.Success(response)
            } catch (e: Exception) {
                _state.value = WarehouseDetailsState.Error(e.message ?: "Failed to load warehouse details")
            }
        }
    }

}


sealed class ManagerState {
    object Idle : ManagerState()
    object Loading : ManagerState()
    data class Success(val data: List<Manager>) : ManagerState()
    data class Error(val message: String) : ManagerState()
}

class ManagerViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _state = MutableStateFlow<ManagerState>(ManagerState.Idle)
    val state: StateFlow<ManagerState> = _state

    var searchQuery by mutableStateOf("")
    var errorMessage by mutableStateOf("")
    var showAddDialog by mutableStateOf(false)

    fun loadManagers() {
        _state.value = ManagerState.Loading
        viewModelScope.launch {
            try {
                val managers = repo.getManagers(searchQuery.ifBlank { null })
                _state.value = ManagerState.Success(managers)
            } catch (e: Exception) {
                _state.value = ManagerState.Error(e.message ?: "Failed to load managers")
            }
        }
    }

    fun addManager(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = repo.addManager(AddManagerRequest(name, email, password))
                if (response.message.contains("success", ignoreCase = true)) {
                    loadManagers()
                    onSuccess()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to add manager"
            }
        }
    }

    fun openAddDialog() {
        showAddDialog = true
        errorMessage = ""
    }

    fun closeAddDialog() {
        showAddDialog = false
        errorMessage = ""
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        loadManagers()
    }
}



data class Company(
    val id: String,
    val name: String,
    val governorate: String,
    val city: String,
    val street: String,
    val phone: String,
    val email: String,
    val type: String,
    val vendor: String?
)

class CompaniesViewModel : ViewModel() {

    var companiesList = mutableStateListOf<Company>()
        private set

    var selectedType = mutableStateOf("Supplier")
        private set

    var searchQuery = mutableStateOf("")

    val companyTypes = listOf("Supplier", "Importer")

    var errorMessage by mutableStateOf("")

    var showAddDialog by mutableStateOf(false)
        private set

    val filteredList: List<Company>
        get() = companiesList.filter { company ->
            (company.type == selectedType.value) &&
                    (company.name.contains(searchQuery.value, ignoreCase = true)
                            || company.city.contains(searchQuery.value, ignoreCase = true)
                            || company.governorate.contains(searchQuery.value, ignoreCase = true))
        }

    fun updateSelectedType(type: String) {
        selectedType.value = type
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun openAddDialog() {
        showAddDialog = true
        errorMessage = ""
    }

    fun closeAddDialog() {
        showAddDialog = false
        errorMessage = ""
    }

    fun addCompany(
        name: String,
        governorate: String,
        city: String,
        street: String,
        phone: String,
        email: String,
        type: String,
        vendor: String?
    ) {
        if (
            name.isBlank() || governorate.isBlank() || city.isBlank() ||
            street.isBlank() || phone.isBlank() || email.isBlank()
        ) {
            errorMessage = "Please fill all fields!"
            return
        }

        val newCompany = Company(
            id = (companiesList.size + 1).toString(),
            name = name,
            governorate = governorate,
            city = city,
            street = street,
            phone = phone,
            email = email,
            type = type,
            vendor = vendor
        )

        companiesList.add(newCompany)
        closeAddDialog()
    }

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
                    vendor = "ahmed"
                ),
                Company(
                    id = "2",
                    name = "Beta Exporters",
                    governorate = "Giza",
                    city = "Dokki",
                    street = "Street 2",
                    phone = "01000000002",
                    email = "beta@example.com",
                    type = "Importer",
                    vendor = "Fresh Supplies"
                )
            )
        )
    }
}
// Supplier view model
data class SupplierItem(
    val id: String,
    var name: String,
    var price: Double
)

data class SupplierCategory(
    val id: String,
    var name: String,
    val items: SnapshotStateList<SupplierItem> = mutableStateListOf()
)

data class SupplierInfo(
    var name: String,
    var address: String = "",
    var phone: String = "",
    var email: String = "",
    val categories: SnapshotStateList<SupplierCategory> = mutableStateListOf()
)

class SupplierDetailsViewModel : ViewModel() {

    companion object {
        val suppliersData = mutableMapOf<String, SupplierInfo>()

        val predefinedCategories = listOf(
            "Electronics",
            "Food",
            "Drinks",
            "Clothes",
            "Furniture",
            "Tools",
            "Add New Category"
        )
    }

    var currentSupplierName by mutableStateOf("")
    var currentSupplierInfo by mutableStateOf<SupplierInfo?>(null)

    var showAddCategoryDialog by mutableStateOf(false)
    var showAddItemDialog by mutableStateOf(false)

    var selectedCategoryName by mutableStateOf<String?>(null)
    var newCategoryName by mutableStateOf("")
    var selectedCategoryId by mutableStateOf<String?>(null)
    var newItemName by mutableStateOf("")
    var newItemPrice by mutableStateOf("")

    var categoryError by mutableStateOf("")
    var itemError by mutableStateOf("")

    fun loadSupplier(name: String, companiesViewModel: CompaniesViewModel) {
        currentSupplierName = name
        val company = companiesViewModel.companiesList.find { it.name == name }

        val supplierInfo = suppliersData.getOrPut(name) {
            SupplierInfo(
                name = name,
                address = company?.street ?: "",
                phone = company?.phone ?: "",
                email = company?.email ?: ""
            )
        }

        if (company != null) {
            supplierInfo.address = company.street
            supplierInfo.phone = company.phone
            supplierInfo.email = company.email
        }

        currentSupplierInfo = supplierInfo
    }

    fun addCategory() {
        val supplierInfo = currentSupplierInfo ?: return

        val selectedName = selectedCategoryName

        if (selectedName.isNullOrBlank()) {
            categoryError = "Please select a category!"
            return
        }

        val finalName =
            if (selectedName == "Add New Category") newCategoryName
            else selectedName

        if (finalName.isBlank()) {
            categoryError = "Category name cannot be empty!"
            return
        }

        val newCat = SupplierCategory(
            id = (supplierInfo.categories.size + 1).toString(),
            name = finalName,
            items = mutableStateListOf()
        )

        supplierInfo.categories.add(newCat)

        selectedCategoryName = null
        newCategoryName = ""
        categoryError = ""
        showAddCategoryDialog = false
    }

    fun addItem() {
        // نفس كودك بدون تغيير
        val supplierInfo = currentSupplierInfo ?: return

        val catId = selectedCategoryId
        if (catId.isNullOrBlank()) {
            itemError = "No category selected!"
            return
        }
        if (newItemName.isBlank() || newItemPrice.isBlank()) {
            itemError = "All fields are required!"
            return
        }
        val price = newItemPrice.toDoubleOrNull()
        if (price == null) {
            itemError = "Price must be a number!"
            return
        }

        val category = supplierInfo.categories.find { it.id == catId } ?: run {
            itemError = "Category not found!"
            return
        }

        val newItem = SupplierItem(
            id = (category.items.size + 1).toString(),
            name = newItemName,
            price = price
        )

        category.items.add(newItem)

        newItemName = ""
        newItemPrice = ""
        selectedCategoryId = null
        itemError = ""
        showAddItemDialog = false
    }
}


sealed class WarehouseManagerState {
    object Idle : WarehouseManagerState()
    object Loading : WarehouseManagerState()
    data class Success(val data: List<WarehouseManager>) : WarehouseManagerState()
    data class Error(val message: String) : WarehouseManagerState()
}

class WarehouseManagerViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<WarehouseManagerState>(WarehouseManagerState.Idle)
    val state: StateFlow<WarehouseManagerState> = _state

    var errorMessage by mutableStateOf("")
    var searchQuery by mutableStateOf("")
        private set

    private var allManagers: List<WarehouseManager> = emptyList()

    val filteredList: List<WarehouseManager>
        get() = if (searchQuery.isBlank()) allManagers
        else allManagers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.warehouse.contains(searchQuery, ignoreCase = true) ||
                    it.governorate.contains(searchQuery, ignoreCase = true) ||
                    it.city.contains(searchQuery, ignoreCase = true)
        }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun loadManagers() {
        _state.value = WarehouseManagerState.Loading
        viewModelScope.launch {
            try {
                allManagers = repo.getWarehouseManagers()
                _state.value = WarehouseManagerState.Success(allManagers)
            } catch (e: Exception) {
                _state.value = WarehouseManagerState.Error(e.message ?: "Failed to load managers")
            }
        }
    }

}



data class Deal(
    val type: String,
    val company: String,
    val date: String,
    val cost: String
)
data class DealProduct(
    val category: String,
    val name: String,
    val quantity: Int,
    val pricePerUnit: String
)

data class DealDetail(
    val type: String,
    val company: String,
    val date: String,
    val warehouseName: String,
    val warehouseLocation: String,
    val vendor: String,
    val products: List<DealProduct>
)

class DealsViewModel : ViewModel() {

    private val dealDetailsMap = mutableMapOf<String, DealDetail>(
        "Company A" to DealDetail(
            type = "Import",
            company = "Company A",
            date = "2025-12-01",
            warehouseName = "Warehouse A",
            warehouseLocation = "Cairo, Downtown",
            vendor = "Ahmed Vendor",
            products = listOf(
                DealProduct("Electronics", "Laptop", 5, "$1200"),
                DealProduct("Electronics", "Mouse", 10, "$25"),
                DealProduct("Furniture", "Chair", 20, "$75")
            )
        ),
        "Company B" to DealDetail(
            type = "Export",
            company = "Company B",
            date = "2025-11-30",
            warehouseName = "Warehouse B",
            warehouseLocation = "Alexandria, Smouha",
            vendor = "Sara Vendor",
            products = listOf(
                DealProduct("Electronics", "Monitor", 3, "$300"),
                DealProduct("Furniture", "Table", 5, "$150")
            )
        )
    )

    var dealsList by mutableStateOf(
        dealDetailsMap.values.map { d ->
            Deal(
                d.type,
                d.company,
                d.date,
                d.products.sumOf { p ->
                    val price = p.pricePerUnit.replace("$", "").toDoubleOrNull() ?: 0.0
                    price * p.quantity
                }.let { "$$it" }
            )
        }
    )
        private set

    var searchQuery by mutableStateOf("")
        private set

    val filteredList: List<Deal>
        get() = dealsList.filter {
            it.type.contains(searchQuery, ignoreCase = true) ||
                    it.company.contains(searchQuery, ignoreCase = true) ||
                    it.date.contains(searchQuery, ignoreCase = true)
        }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun addDeal(deal: Deal) {
        dealsList = dealsList + deal
    }

    fun getDealDetail(company: String): DealDetail? {
        return dealDetailsMap[company]
    }
}


class DealDetailsViewModel : ViewModel() {

    var dealDetail by mutableStateOf<DealDetail?>(null)
        private set

    fun loadDealDetail(companyName: String, dealsViewModel: DealsViewModel) {
        dealDetail = dealsViewModel.getDealDetail(companyName)
    }
}
// جزء ال Vendors
sealed class VendorState {
    object Idle : VendorState()
    object Loading : VendorState()
    data class Success(val data: List<GetVendor>) : VendorState()
    data class Error(val message: String) : VendorState()
}

class VendorViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _vendorState = MutableStateFlow<VendorState>(VendorState.Idle)
    val vendorState: StateFlow<VendorState> = _vendorState

    var searchQuery by mutableStateOf("")
    var errorMessage by mutableStateOf("")

    fun loadVendors() {
        _vendorState.value = VendorState.Loading
        viewModelScope.launch {
            try {
                val vendors = repo.getVendors()
                _vendorState.value = VendorState.Success(vendors)
            } catch (e: Exception) {
                _vendorState.value = VendorState.Error(e.message ?: "Error loading vendors")
            }
        }
    }

    fun addVendor(
        name: String,
        warehouse: String,
        type: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repo.addVendor(AddVendor(name, warehouse, type))
                if (response.message.contains("success", ignoreCase = true)) {
                    loadVendors()
                    onSuccess()
                } else {
                    onError(response.message)
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add vendor")
            }
        }
    }


}

sealed class WarehouseState {
    object Idle : WarehouseState()
    object Loading : WarehouseState()
    data class Success(val data: List<WarehouseVen>) : WarehouseState()
    data class Error(val message: String) : WarehouseState()
}

class WarehouseViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _state = MutableStateFlow<WarehouseState>(WarehouseState.Idle)
    val state: StateFlow<WarehouseState> = _state

    fun loadWarehouses() {
        _state.value = WarehouseState.Loading
        viewModelScope.launch {
            try {
                val warehouses = repo.getWarehousesNames()
                _state.value = WarehouseState.Success(warehouses)
            } catch (e: Exception) {
                _state.value = WarehouseState.Error(e.message ?: "Failed to load warehouses")
            }
        }
    }
}


//// جزء اددد ديل
//data class AddDealProduct(
//    val category: String,
//    val name: String,
//    val pricePerUnit: Double
//)
//
//data class AddDealWarehouse(
//    val name: String,
//    val categories: List<Pair<String, List<AddDealProduct>>>
//)
//
//class AddDealViewModel(
//    private val companiesViewModel: CompaniesViewModel,
//    private val suppliersViewModel: SupplierDetailsViewModel,
//    private val warehousesViewModel: WarehousesViewModel,
//    private val vendorsViewModel: VendorsViewModel
//) : androidx.lifecycle.ViewModel() {
//
//    var dealType by mutableStateOf("Import")
//    val dealTypeOptions = listOf("Import", "Export")
//
//    var selectedCompany by mutableStateOf<String?>(null)
//    var selectedVendor by mutableStateOf<String?>(null)
//    var selectedWarehouse by mutableStateOf<AddDealWarehouse?>(null)
//
//    private val quantities: SnapshotStateMap<String, Int> = mutableStateMapOf()
//    fun getQuantity(key: String) = quantities[key] ?: 0
//    fun increaseQuantity(key: String) {
//        val q = quantities[key] ?: 0
//        quantities[key] = q + 1
//    }
//    fun decreaseQuantity(key: String) {
//        val q = quantities[key] ?: 0
//        if (q <= 1) quantities.remove(key) else quantities[key] = q - 1
//    }
//
//    // Companies filtered based on deal type
//    val filteredCompanies: List<String>
//        get() = companiesViewModel.companiesList.filter {
//            if (dealType == "Import") it.type == "Supplier" else it.type == "Importer"
//        }.map { it.name }
//
//    // When company is selected, set associated warehouse/vendor and load products
//    fun onCompanySelected(companyName: String) {
//        selectedCompany = companyName
//
//        if (dealType == "Import") {
//            // Supplier → choose warehouse
//            val supplierCompany = companiesViewModel.companiesList.find { it.name == companyName }
//            val warehouseName = supplierCompany?.vendor ?: warehousesViewModel.warehousesList.firstOrNull()?.name
//            selectedWarehouse = warehouseName?.let { getWarehouseByName(it) }
//            selectedVendor = null
//        } else {
//            // Export → show vendor and warehouse
//            val importerCompany = companiesViewModel.companiesList.find { it.name == companyName }
//            val vendorName = importerCompany?.vendor ?: vendorsViewModel.vendorsList.firstOrNull()?.name
//            val warehouseName = warehousesViewModel.warehousesList.firstOrNull()?.name
//            selectedVendor = vendorName
//            selectedWarehouse = warehouseName?.let { getWarehouseByName(it) }
//        }
//
//        quantities.clear()
//    }
//
//    private fun getWarehouseByName(name: String): AddDealWarehouse? {
//        val warehouseDetail = warehousesViewModel.warehousesList.find { it.name == name } ?: return null
//        val categories = listOf(
//            "Electronics" to listOf(
//                AddDealProduct("Electronics","Laptop",1200.0),
//                AddDealProduct("Electronics","Mouse",20.0)
//            ),
//            "Furniture" to listOf(
//                AddDealProduct("Furniture","Chair",75.0),
//                AddDealProduct("Furniture","Table",150.0)
//            ),
//            "Food" to listOf(
//                AddDealProduct("Food","Apple",1.0),
//                AddDealProduct("Food","Chocolate",3.5)
//            )
//        )
//        return AddDealWarehouse(warehouseDetail.name, categories)
//    }
//
//    fun computeTotal(): Double {
//        var total = 0.0
//        selectedWarehouse?.categories?.forEach { (_, products) ->
//            products.forEach { p ->
//                val key = "${p.category}::${p.name}"
//                val qty = quantities[key] ?: 0
//                total += p.pricePerUnit * qty
//            }
//        }
//        return total
//    }
//}

