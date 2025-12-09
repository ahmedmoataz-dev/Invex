
const db = require("mssql");
const bcrypt = require("bcrypt");
const crypto = require("crypto");
const {validationResult} = require("express-validator");

async function checkPassword(password, hashed){
    const result = await bcrypt.compare(password, hashed);
    return result;
};

const authen = async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({status: errors.array()[0].msg});
    }

    try{
        const {email, password} = req.body;
        const userDatabase = await db.query(`
                                                SELECT 
                                                    Man_Password 
                                                FROM MANAGER 
                                                WHERE Man_Email = '${email}'
                                            `);

        if(userDatabase.recordset.length === 0){
            return res.status(400).json({status: "Wrong email, Please try again"});
        }

        const storedPassword = userDatabase.recordset[0].Man_Password;
        const correctPassword = await checkPassword(password, storedPassword);

        if(correctPassword){
            return res.json({status: "successful login"});
        }else{
            return res.status(400).json({status: "failed login"});
        }
    } catch(error){
        console.error("Error during login:", error);
        return res.status(500).json({msg: "Server error"});
    }
};

const recentDeals = async (req, res) => {
    try {
        const result = await db.query(`
            SELECT
                DEAL.Deal_ID,
                COMPANY.Company_Type,
                COMPANY.Com_Name,
                DEAL.Deal_Cost, 
                DEAL.Deal_Date
            FROM DEAL
            JOIN COMPANY
                ON COMPANY.Contract_ID = DEAL.Contract_ID
            ORDER BY DEAL.Deal_Date DESC
        `);

        if (result.recordset.length === 0) {
            return res.status(404).json({message: "No Recent Deals found"});
        }

        res.json(result.recordset);

    } catch (error) {
        console.error("Error fetching recent deals:", error);
        return res.status(500).json({error: error.message});
    }
};

const getAllManagers = async (req, res) => {
    try {
        const result = await db.query(`
            SELECT 
                Man_Name, 
                Man_Email 
            FROM MANAGER
            ORDER BY Man_Name
        `);

        if (result.recordset.length === 0) {
            return res.status(404).json({message: "No managers found"});
        }

        res.json(result.recordset);
    } catch (error) {
        console.error("Error fetching managers:", error);
        return res.status(500).json({error: error.message});
    }
};

async function hashPassword(password){
    const hashedPassword = await bcrypt.hash(password, 10);
    return hashedPassword;
};

const addManager = async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({msg: errors.array()[0].msg});
    }

    try{
        const {name, email, password} = req.body;

        const existing = await db.query(`
            SELECT * 
            FROM MANAGER 
            WHERE Man_Email = '${email}'
        `);

        if(existing.recordset.length > 0){
            return res.status(400).json({msg: "Email already exists"});
        }

        const hashedPassword = await hashPassword(password);
        const id = crypto.randomUUID();
        await db.query(`
            INSERT INTO MANAGER 
            VALUES ('${id}', '${name}', '${hashedPassword}', '${email}')
        `);

        return res.status(201).json({msg: "Manager is successfully added"});

    } catch (error) {
        console.error("Error adding manager: ", error);
        return res.status(500).json({msg: "Server error"});
    }
};

const getWarehouses = async (req, res) => {
    const data = await db.query(`
                                SELECT 
                                    INVENTORY.Inv_Name, 
                                    INVENTORY.Governorate, 
                                    INVENTORY.City, 
                                    INVENTORY.Responsible, 
                                    INVENTORY.Capacity, 
                                    SUM(ITEM.Item_Quantity) AS Total_Quantity 
                                FROM INVENTORY 
                                JOIN INV_CAT 
                                    ON INVENTORY.Inv_ID = INV_CAT.Inv_ID 
                                JOIN CATEGORY 
                                    ON CATEGORY.Cat_ID = INV_CAT.Cat_ID 
                                JOIN ITEM 
                                    ON ITEM.Cat_ID = CATEGORY.Cat_ID 
                                GROUP BY 
                                    INVENTORY.Inv_ID, 
                                    INVENTORY.Inv_Name, 
                                    INVENTORY.Governorate, 
                                    INVENTORY.City, 
                                    INVENTORY.Responsible, 
                                    INVENTORY.Capacity
                                `);
    if(data.recordset.length === 0){
        return res.status(404).json({msg: "There is no warehouses yet"});
    }

    res.json(data.recordset);
};

const warehouseDetails = async (req, res) => {
    const ware_name = req.params.ware_name;
    const data1 = await db.query(`
                                SELECT 
                                    INVENTORY.Inv_Name, 
                                    INVENTORY.Governorate, 
                                    INVENTORY.City, 
                                    INVENTORY.Responsible, 
                                    INVENTORY.Capacity, 
                                    SUM(ITEM.Item_Quantity) AS Total_Quantity 
                                FROM INVENTORY 
                                JOIN INV_CAT 
                                    ON INVENTORY.Inv_ID = INV_CAT.Inv_ID 
                                JOIN CATEGORY 
                                    ON CATEGORY.Cat_ID = INV_CAT.Cat_ID 
                                JOIN ITEM 
                                    ON ITEM.Cat_ID = CATEGORY.Cat_ID 
                                WHERE INVENTORY.Inv_Name = '${ware_name}' 
                                GROUP BY 
                                    INVENTORY.Inv_ID, 
                                    INVENTORY.Inv_Name, 
                                    INVENTORY.Governorate, 
                                    INVENTORY.City, 
                                    INVENTORY.Responsible, 
                                    INVENTORY.Capacity
                                `);
    
    const data2 = await db.query(`
    SELECT CATEGORY.Cat_Name, 
           JSON_QUERY((
               SELECT ITEM.Item_Name,
                      ITEM.Item_Salery,
                      ITEM.Item_Quantity,
                      COMPANY.Com_Name
               FROM ITEM
               JOIN ITEM_DEAL
                   ON ITEM.Item_ID = ITEM_DEAL.Item_ID
               JOIN DEAL
                   ON DEAL.Deal_ID = ITEM_DEAL.Deal_ID
               JOIN COMPANY
                   ON COMPANY.Contract_ID = DEAL.Contract_ID
               WHERE ITEM.Cat_ID = CATEGORY.Cat_ID
               FOR JSON PATH
           )) AS items
    FROM CATEGORY
    JOIN INV_CAT
        ON CATEGORY.Cat_ID = INV_CAT.Cat_ID
    JOIN INVENTORY
        ON INVENTORY.Inv_ID = INV_CAT.Inv_ID
    WHERE INVENTORY.Inv_Name = '${ware_name}'
    FOR JSON PATH
`);

    if(data1.recordset.length === 0){
        return res.status(404).json({msg: "There is no warehouses yet"});
    }

    if(data2.recordset == []){
        return res.status(404).json({msg: "There is no items in the warehouse yet"});
    }

    res.json([data1.recordset, data2.recordset]);
};

const warehouseManagers = async (req, res) => {
    const data = await db.query(`SELECT Responsible, Inv_Name, Governorate, City FROM INVENTORY`);

    if(data.recordset.length === 0){
        return res.status(404).json({msg: "There is no Warehouse managers yet"});
    }

    res.status(200).json(data.recordset);
};

const addCategory = async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({msg: errors.array()[0].msg});
    }

    const {name} = req.body;
    const id = crypto.randomUUID();
    const data = await db.query(`SELECT Cat_Name FROM CATEGORY WHERE Cat_Name = '${name}'`);

    if(data.recordset.length !== 0){
        return res.status(400).json({msg: "Already existed"});
    }

    await db.query(`INSERT INTO CATEGORY VALUES ('${id}', '${name}')`);
    res.json({msg: "Category is added successfully"});
};

const getCategories = async (req, res) => {
    const data = await db.query(`SELECT Cat_Name FROM CATEGORY`);

    if(data.recordset.length === 0){
        return res.status(404).json({msg: "There is no categories yet"});
    }

    res.json(data.recordset);
};

const addItem = async (req, res) => {
    const {supplier, category, item, price} = req.body;
    const sup = await db.query(`SELECT SUPPLIER.SUPContract_ID AS SUPContract_ID FROM COMPANY JOIN SUPPLIER ON SUPPLIER.SUPContract_ID = COMPANY.Contract_ID WHERE COMPANY.Com_Name = '${supplier}'`);
    const cat = await db.query(`SELECT Cat_ID FROM CATEGORY WHERE Cat_Name = '${category}'`);
    if(cat.recordset.length === 0){
        return res.status(400).json({msg: "Category is not existed"});
    }
    if(sup.recordset.length === 0){
        return res.status(400).json({msg: "Supplier is not existed"});
    }

    const id = crypto.randomUUID();
    const cat_id = cat.recordset[0].Cat_ID;
    const sup_id = sup.recordset[0].SUPContract_ID;
    await db.query(`INSERT INTO ITEM VALUES ('${id}', '${item}', 0, ${price}, '${cat_id}', '${sup_id}')`);
    res.json({msg: "Item successfully added"});
};

const getVendors = async (req, res) => {
    const data = await db.query(`
                                    SELECT
                                        VENDOR.Ven_Name AS Ven_Name,
                                        INVENTORY.Inv_Name AS Inv_Name,
                                        VENDOR.Ven_Type AS Ven_Type
                                    FROM VENDOR
                                    JOIN INVENTORY
                                        ON VENDOR.Inv_ID = INVENTORY.Inv_ID
                                `);
    
    res.json(data.recordset);
};

const addVendor = async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({error: errors.array()[0].msg});
    }

    const {vendor, warehouse, type} = req.body;
    const inv_id = await db.query(`SELECT Inv_ID FROM INVENTORY WHERE Inv_Name = '${warehouse}'`);
    if(inv_id.recordset.length === 0){
        return res.status(404).json({msg: "Warehouse is not existed"});
    }

    const ven_id = crypto.randomUUID();
    await db.query(`INSERT INTO VENDOR VALUES('${ven_id}', '${inv_id.recordset[0].Inv_ID}', '${vendor}', '${type}')`);
    if(type === "exporter"){
        await db.query(`INSERT INTO PHA_VEN VALUES('${ven_id}')`);
    }else{
        await db.query(`INSERT INTO SUP_VEN VALUES('${ven_id}')`);
    }
    res.status(201).json({msg: "Vendor added successfully"});
};

const vendorsAtDeal = async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({msg: errors.array()[0].msg});
    }

    const {type, warehouse} = req.query;
    const inv = await db.query(`SELECT Inv_ID FROM INVENTORY WHERE Inv_Name = '${warehouse}'`);
    if(inv.recordset.length === 0){
        return res.status(404).json({msg: "Warehouse does not exist"});
    }

    const inv_id = inv.recordset[0].Inv_ID;
    const neededType = type.toLowerCase() === "exporter" ? "importer" : "exporter";

    const vendors = await db.query(`
                                        SELECT 
                                            Ven_Name 
                                        FROM VENDOR 
                                        WHERE Inv_ID = '${inv_id}' AND Ven_Type = '${neededType}'
    `);

    res.status(200).json(vendors.recordset);
};

const warehouse_names = async (req, res) => {
    const data = await db.query(`SELECT Inv_Name FROM INVENTORY`);
    res.json(data.recordset);
};

const getExporter = async (req, res) => {
    const data = await db.query(`SELECT Com_Name, Governorate, City, Street FROM COMPANY WHERE Company_Type = 'exporter'`);
    res.json(data.recordset);
};

const getImporter = async (req, res) => {
    const data = await db.query(`
                                SELECT 
                                    Com_Name, 
                                    Governorate, 
                                    City, 
                                    Street, 
                                    Com_Phone, 
                                    Com_Email
                                FROM COMPANY
                                WHERE Company_Type = 'importer'
                            `);
    res.json(data.recordset);
};

const comByType = async (req, res) => {
    const com_type = req.params.type;
    const data = await db.query(`SELECT Com_Name FROM COMPANY WHERE Company_Type = '${com_type}'`);
    res.json(data.recordset);
};

const addCompany = async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({msg: errors.array()[0].msg});
    }

    const {name, governorate, city, street, phone, email, type} = req.body;
    const contract_id = crypto.randomUUID();
    await db.query(`
        INSERT INTO COMPANY 
        VALUES(
            '${contract_id}', 
            '${name}', 
            '${governorate}', 
            '${city}', 
            '${street}',
            '${phone}',
            '${email}',
            '${type}' 
        )`);

    res.status(201).json({msg: "Company has been created successfully"});
};

const supplierDetails = async (req, res) => {
    try {
        const name = req.params.name;
        if (!name) 
            return res.status(400).json({msg: "Supplier name required"});

        const data1 = await db.query(`
                                        SELECT 
                                            COMPANY.Com_Name,
                                            COMPANY.Governorate,
                                            COMPANY.City,
                                            COMPANY.Street,
                                            COMPANY.Com_Phone,
                                            COMPANY.Com_Email,
                                        FROM COMPANY
                                        WHERE COMPANY.Com_Name = '${name}'
                                    `);
        const data2 = await db.query(`
                                        SELECT
                                            CATEGORY.Cat_Name,
                                            (
                                                SELECT
                                                    ITEM.Item_Name,
                                                    ITEM.Item_Salery
                                                FROM ITEM
                                                WHERE ITEM.Cat_ID = CATEGORY.Cat_ID
                                                FOR JSON PATH
                                            ) AS items
                                        FROM CATEGORY
                                        JOIN ITEM
                                            ON ITEM.Cat_ID = CATEGORY.Cat_ID
                                        JOIN ITEM_COM
                                            ON ITEM_COM.Item_ID = ITEM.Item_ID
                                        JOIN COMPANY
                                            ON COMPANY.Contract_ID = ITEM_COM.Contract_ID
                                        WHERE COMPANY.Com_Name = '${name}';
                                    `);

        res.status(200).json([data1.recordset, data2.recordset]);

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
};

const categoryItems = async (req, res) => {
    try {
        const name = req.params.name;
        if (!name) 
            return res.status(400).json({msg: "importer name required"});

        const data2 = await db.query(`
                                        SELECT
                                            CATEGORY.Cat_Name,
                                            (
                                                SELECT
                                                    ITEM.Item_Name,
                                                    ITEM.Item_Salery
                                                FROM ITEM
                                                WHERE ITEM.Cat_ID = CATEGORY.Cat_ID
                                                FOR JSON PATH
                                            ) AS items
                                        FROM CATEGORY
                                        JOIN ITEM
                                            ON ITEM.Cat_ID = CATEGORY.Cat_ID
                                        JOIN ITEM_COM
                                            ON ITEM_COM.Item_ID = ITEM.Item_ID
                                        JOIN COMPANY
                                            ON COMPANY.Contract_ID = ITEM_COM.Contract_ID
                                        WHERE COMPANY.Com_Name = '${name}';
                                    `);

        res.status(200).json(data2.recordset);

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
};

const dealImport = async (req, res) => {
    try {
        const {com_name, war_name, vendor_name, items, total_price} = req.body;

        if (!com_name || !war_name || !Array.isArray(items) || !items.length || !vendor_name)
            return res.status(400).json({msg: "Missing data"});

        const company = await db.query(`SELECT
                                            Contract_ID
                                        FROM COMPANY
                                        WHERE Com_Name='${com_name}'
                                    `);
        if (!company.recordset.length)
            return res.status(404).json({msg: "Company not found"});

        const contractId = company.recordset[0].Contract_ID;
        
        const vendor = await db.query(`
            SELECT Ven_ID
            FROM VENDOR
            WHERE Ven_Name = '${vendor_name}'
        `);
        
        const ven_id = vendor.recordset[0].Ven_ID;

        const warehouse = await db.query(`SELECT Inv_ID FROM INVENTORY WHERE Inv_Name='${war_name}'`);
        if (!warehouse.recordset.length) 
            return res.status(404).json({msg: "Warehouse not found"});

        const invId = warehouse.recordset[0].Inv_ID;
        const dealId = crypto.randomUUID();
        const dealDate = new Date().toISOString();

        await db.query(`
            INSERT INTO DEAL (Deal_ID, Ven_ID, Contract_ID, Deal_Cost, Deal_Date)
            VALUES ('${dealId}', '${ven_id}', '${contractId}', ${total_price}, '${dealDate}')
        `);

        for (const it of items) {
            const item = await db.query(`SELECT Item_ID, Item_Quantity, Cat_ID FROM ITEM WHERE Item_Name = '${it.name}'`);
            if (!item.recordset.length)
                return res.status(404).json({msg: "Item not found: " + it.name});

            const catId = item.recordset[0].Cat_ID;
            const newQty = item.recordset[0].Item_Quantity + it.quantity;
            const itemId = item.recordset[0].Item_ID;

            await db.query(`UPDATE ITEM SET Item_Quantity = ${newQty} WHERE Item_ID = '${itemId}'`);
            await db.query(`INSERT INTO ITEM_DEAL VALUES ('${dealId}', '${itemId}', ${newQty})`);

            const invcat = await db.query(`SELECT * FROM INV_CAT WHERE Inv_ID = '${invId}' AND Cat_ID = '${catId}'`);
            if (!invcat.recordset.length)
                await db.query(`INSERT INTO INV_CAT VALUES ('${invId}', '${catId}')`);
        }

        return res.status(201).json({status: "success"});

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: err });
    }
};

const dealExport = async (req, res) => {
    try {
        const {com_name, ware_name, vendor_name, items, total_price} = req.body;

        if (!com_name || !Array.isArray(items) || !items.length || !ware_name || !vendor_name)
            return res.status(400).json({msg: "Missing data"});

        const company = await db.query(`SELECT Contract_ID FROM COMPANY WHERE Com_Name = '${com_name}'`);
        if (!company.recordset.length)
            return res.status(404).json({msg: "Company not found"});

        const contractId = company.recordset[0].Contract_ID;

        const vendor = await db.query(`
            SELECT Ven_ID
            FROM VENDOR
            WHERE Ven_Name = '${vendor_name}'
        `);
        
        const vendor_id = vendor.recordset[0].Ven_ID;

        const warehouse = await db.query(`SELECT Inv_ID FROM INVENTORY WHERE Inv_Name='${ware_name}'`);
        if (!warehouse.recordset.length) 
            return res.status(404).json({msg: "Warehouse not found"});

        const dealId = crypto.randomUUID();
        const dealDate = new Date().toISOString();

        await db.query(`
            INSERT INTO DEAL (Deal_ID, Ven_ID, Contract_ID, Deal_Cost, Deal_Date)
            VALUES ('${dealId}', ${vendor_id}, '${contractId}', ${total_price}, '${dealDate}')
        `);

        for (const it of items) {
            const item = await db.query(`SELECT Item_ID, Item_Quantity FROM ITEM WHERE Item_Name = '${it.name}'`);
            if (!item.recordset.length)
                return res.status(404).json({msg: `Item not found: ${it.name}`});

            const itemId = item.recordset[0].Item_ID;
            const current = item.recordset[0].Item_Quantity;

            if (current < it.quantity)
                return res.status(400).json({msg: "Not enough quantity"});

            await db.query(`UPDATE ITEM SET Item_Quantity=${current - it.quantity} WHERE Item_ID = '${itemId}'`);
            await db.query(`INSERT INTO ITEM_DEAL VALUES ('${dealId}', '${itemId}', '${it.quantity}')`);
        }

        return res.status(201).json({status: "success"});

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: err });
    }
};

const dealDetails = async (req, res) => {
    try {
        const id = req.params.id;
        if (!id) 
            return res.status(400).json({ status: "invalid", msg: "Deal ID required" });

        const data = await db.query(`
                                        SELECT
                                            COMPANY.Company_Type,
                                            COMPANY.Com_Name,
                                            DEAL.Deal_Date,
                                            INVENTORY.Inv_Name,
                                            INVENTORY.Governorate,
                                            INVENTORY.City,
                                            VENDOR.Ven_Name,
                                            (
                                                SELECT
                                                    ITEM.Item_Name,
                                                    CATEGORY.Cat_Name,
                                                    ITEM.Item_Quantity,
                                                    ITEM.Item_Salery
                                                FROM ITEM_DEAL
                                                JOIN ITEM 
                                                    ON ITEM.Item_ID = ITEM_DEAL.Item_ID
                                                JOIN CATEGORY
                                                    ON CATEGORY.Cat_ID = ITEM.Cat_ID
                                                WHERE ITEM_DEAL.Deal_ID = DEAL.Deal_ID   
                                                FOR JSON PATH
                                            ) AS items,
                                            DEAL.Deal_Cost
                                        FROM DEAL
                                        JOIN COMPANY
                                            ON COMPANY.Contract_ID = DEAL.Contract_ID
                                        JOIN VENDOR
                                            ON VENDOR.Ven_ID = DEAL.Ven_ID
                                        JOIN INVENTORY 
                                            ON INVENTORY.Inv_ID = VENDOR.Inv_ID
                                        WHERE DEAL.Deal_ID = '${id}';
            `)

        res.json(data.recordset);

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
};

const addWarehouse = async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({msg: errors.array()[0].msg});
    }

    const id = crypto.randomUUID();
    const {name, governorate, city, capacity, responsible} = req.body;
    
    await db.query(`
                        INSERT INTO INVENTORY 
                        VALUES('${id}', '${governorate}', '${city}', ${capacity}, '${responsible}', '${name}')`);

    res.status(201).json({msg: "Inventory created successfully"});
};

module.exports = {
    authen,
    recentDeals,
    getAllManagers,
    addManager,
    getWarehouses,
    warehouseDetails,
    warehouseManagers,
    addCategory,
    getCategories,
    addItem,
    getVendors,
    addVendor,
    vendorsAtDeal,
    warehouse_names,
    getExporter,
    getImporter,
    comByType,
    addCompany,
    supplierDetails,
    categoryItems,
    dealImport,
    dealExport,
    dealDetails,
    addWarehouse
}