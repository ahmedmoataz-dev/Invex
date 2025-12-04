package com.example.invex

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
data class Shortcut(val icon: Int, val title: String, val subtitle: String)

class HomeViewModel : ViewModel() {

    var shortcuts by mutableStateOf(
        listOf(
            Shortcut(R.drawable.van, "vendors", "Explore your vendors"),
            Shortcut(R.drawable.ic_warehouses, "Warehouses", "Manage your warehouses"),
            Shortcut(R.drawable.ic_companies, "Companies", "View your companies"),
            Shortcut(R.drawable.ic_managers, "Warehouse Managers", "See warehouse managers")
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

/***********************************
 *      COMPANIES VIEWMODEL
 ***********************************/
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

        // 🔹 ليست ثابتة من الكاتيجوريز
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

    var selectedCategoryName by mutableStateOf<String?>(null)   // ← الجديد
    var newCategoryName by mutableStateOf("")                  // ← لو اختار Add New Category
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

    // 🔥 التعديل الوحيد في الفيو مودل
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



/***********************************
 *      WAREHOUSES MANAGERS VIEWMODEL
 ***********************************/
data class WarehouseManagerInfo(
    val managerName: String,
    val warehouseName: String,
    val warehouseLocation: String
)

class WarehousesManagersViewModel(
    private val warehousesViewModel: WarehousesViewModel
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    val warehouseManagersList: List<WarehouseManagerInfo>
        get() = warehousesViewModel.warehousesList.map { warehouse ->
            WarehouseManagerInfo(
                managerName = warehouse.managerName,
                warehouseName = warehouse.name,
                warehouseLocation = warehouse.location
            )
        }.filter {
            it.managerName.contains(searchQuery, ignoreCase = true) ||
                    it.warehouseName.contains(searchQuery, ignoreCase = true) ||
                    it.warehouseLocation.contains(searchQuery, ignoreCase = true)
        }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
}
/***********************************
 *      DEALS VIEWMODEL
 ***********************************/
data class Deal(
    val type: String, // Import / Export
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
    val type: String, // Import / Export
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

/***********************************
 *      DEAL DETAILS VIEWMODEL
 ***********************************/
class DealDetailsViewModel : ViewModel() {

    var dealDetail by mutableStateOf<DealDetail?>(null)
        private set

    // Load deal detail based on company name
    fun loadDealDetail(companyName: String, dealsViewModel: DealsViewModel) {
        dealDetail = dealsViewModel.getDealDetail(companyName)
    }
}
// جزء ال Vendors
class VendorsViewModel : ViewModel() {

    var vendorsList = mutableStateListOf<Vendor>()
        private set

    var searchQuery = mutableStateOf("")
        private set

    // selected warehouse
    var selectedWarehouse by mutableStateOf<String?>(null)

    fun updateSelectedWarehouse(value: String) {
        selectedWarehouse = value
    }

    init {
        vendorsList.addAll(
            listOf(
                Vendor(id = "1", name = "Fresh Supplies", warehouse = "Warehouse A"),
                Vendor(id = "2", name = "Global Traders", warehouse = "Warehouse B"),
                Vendor(id = "3", name = "Al Arab Distribution", warehouse = "Warehouse C"),
                Vendor(id = "4", name = "NextGen Imports", warehouse = "Warehouse A"),
                Vendor(id = "5", name = "Royal Food Co.", warehouse = "Warehouse B")
            )
        )
    }

    // 🔥 FILTER (Search + Warehouse)
    val filteredList: List<Vendor>
        get() {
            val query = searchQuery.value.trim().lowercase()
            val warehouseFilter = selectedWarehouse

            return vendorsList.filter { vendor ->

                // Search by name OR warehouse
                val matchesSearch =
                    vendor.name.lowercase().contains(query) ||
                            vendor.warehouse.lowercase().contains(query)

                // Filter by selected warehouse (if selected)
                val matchesWarehouse =
                    warehouseFilter == null || vendor.warehouse == warehouseFilter

                matchesSearch && matchesWarehouse
            }
        }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addVendor(vendor: Vendor) {
        vendorsList.add(vendor)
    }
}

data class Vendor(
    val id: String,
    val name: String,
    val warehouse: String
)

// جزء اددد ديل
data class AddDealProduct(
    val category: String,
    val name: String,
    val pricePerUnit: Double
)

data class AddDealWarehouse(
    val name: String,
    val categories: List<Pair<String, List<AddDealProduct>>>
)

class AddDealViewModel(
    private val companiesViewModel: CompaniesViewModel,
    private val suppliersViewModel: SupplierDetailsViewModel,
    private val warehousesViewModel: WarehousesViewModel,
    private val vendorsViewModel: VendorsViewModel
) : androidx.lifecycle.ViewModel() {

    var dealType by mutableStateOf("Import")
    val dealTypeOptions = listOf("Import", "Export")

    var selectedCompany by mutableStateOf<String?>(null)
    var selectedVendor by mutableStateOf<String?>(null)
    var selectedWarehouse by mutableStateOf<AddDealWarehouse?>(null)

    private val quantities: SnapshotStateMap<String, Int> = mutableStateMapOf()
    fun getQuantity(key: String) = quantities[key] ?: 0
    fun increaseQuantity(key: String) {
        val q = quantities[key] ?: 0
        quantities[key] = q + 1
    }
    fun decreaseQuantity(key: String) {
        val q = quantities[key] ?: 0
        if (q <= 1) quantities.remove(key) else quantities[key] = q - 1
    }

    // Companies filtered based on deal type
    val filteredCompanies: List<String>
        get() = companiesViewModel.companiesList.filter {
            if (dealType == "Import") it.type == "Supplier" else it.type == "Importer"
        }.map { it.name }

    // When company is selected, set associated warehouse/vendor and load products
    fun onCompanySelected(companyName: String) {
        selectedCompany = companyName

        if (dealType == "Import") {
            // Supplier → choose warehouse
            val supplierCompany = companiesViewModel.companiesList.find { it.name == companyName }
            val warehouseName = supplierCompany?.vendor ?: warehousesViewModel.warehousesList.firstOrNull()?.name
            selectedWarehouse = warehouseName?.let { getWarehouseByName(it) }
            selectedVendor = null
        } else {
            // Export → show vendor and warehouse
            val importerCompany = companiesViewModel.companiesList.find { it.name == companyName }
            val vendorName = importerCompany?.vendor ?: vendorsViewModel.vendorsList.firstOrNull()?.name
            val warehouseName = warehousesViewModel.warehousesList.firstOrNull()?.name
            selectedVendor = vendorName
            selectedWarehouse = warehouseName?.let { getWarehouseByName(it) }
        }

        quantities.clear()
    }

    private fun getWarehouseByName(name: String): AddDealWarehouse? {
        val warehouseDetail = warehousesViewModel.warehousesList.find { it.name == name } ?: return null
        val categories = listOf(
            "Electronics" to listOf(
                AddDealProduct("Electronics","Laptop",1200.0),
                AddDealProduct("Electronics","Mouse",20.0)
            ),
            "Furniture" to listOf(
                AddDealProduct("Furniture","Chair",75.0),
                AddDealProduct("Furniture","Table",150.0)
            ),
            "Food" to listOf(
                AddDealProduct("Food","Apple",1.0),
                AddDealProduct("Food","Chocolate",3.5)
            )
        )
        return AddDealWarehouse(warehouseDetail.name, categories)
    }

    fun computeTotal(): Double {
        var total = 0.0
        selectedWarehouse?.categories?.forEach { (_, products) ->
            products.forEach { p ->
                val key = "${p.category}::${p.name}"
                val qty = quantities[key] ?: 0
                total += p.pricePerUnit * qty
            }
        }
        return total
    }
}

