
const express = require("express");
const db = require("mssql");
const bcrypt = require("bcrypt");
const crypto = require("crypto");
const app = express();
const {body, validationResult} = require("express-validator");

app.use(express.json());
const port = 123;

const config = {
    user: "ahmed",
    password: "1234",
    server: "localhost",
    database: "Invex",
    options: {
        encrypt: false,
        trustServerCertificate: true
    }
};

db.connect(config);

async function checkPassword(password, hashed){
    const result = await bcrypt.compare(password, hashed);
    return result;
}

app.post('/api/login/', [
    body('email')
        .trim()
        .notEmpty()
        .withMessage("Email is required")
        .isEmail()
        .withMessage("Invalid email"),
    body('password')
        .trim()
        .notEmpty()
        .withMessage("Password is required")
        .isLength({min: 6})
        .withMessage("Password must be at least 6 characters")
], async (req, res) => {
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
});

app.get('/api/recent-deals', async (req, res) => {
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
});

app.get('/api/managers', async (req, res) => {
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
});

async function hashPassword(password){
    const hashedPassword = await bcrypt.hash(password, 10);
    return hashedPassword;
}


app.post('/api/manager', [
    body('name')
        .trim()
        .notEmpty()
        .withMessage("Name is required"),
    body('email')
        .trim()
        .notEmpty()
        .withMessage("Email is required")
        .isEmail()
        .withMessage("Invalid email"),
    body('password')
        .trim()
        .notEmpty()
        .withMessage("Password is required")
        .isLength({min: 6})
        .withMessage("Password must be at least 6 characters")
], async (req, res) => {
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

        return res.status(201).json({status: "Manager is successfully added"});

    } catch (error) {
        console.error("Error adding manager: ", error);
        return res.status(500).json({msg: "Server error"});
    }
});

app.get('/api/warehouse/', async (req, res) => {
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
});

app.post('/api/warehouse/', [
    body('name')
        .trim()
        .notEmpty()
        .withMessage("Name is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("Name must contain only letters"),
    body('governorate')
        .trim()
        .notEmpty()
        .withMessage("Governorate is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("Governorate must contain only letters"),
    body('city')
        .trim()
        .notEmpty()
        .withMessage("City is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("City must contain only letters"),
    body('capacity')
        .notEmpty()
        .withMessage("Capacity is required")
        .isNumeric()
        .withMessage("Capacity must be a number")
        .toInt(),
    body('responsible')
        .trim()
        .notEmpty()
        .withMessage("Responsible is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("Responsible must contain only letters")
], async (req, res) => {
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
});

app.get('/api/warehouse/:ware_name', async (req, res) => {
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
                                SELECT 
                                    CATEGORY.Cat_Name,
                                    (
                                        SELECT
                                            ITEM.Item_Name,
                                            ITEM.Item_Salery,
                                            ITEM.Item_Quantity,
                                            COMPANY.Com_Name
                                        FROM ITEM
                                        JOIN DEAL
                                            ON DEAL.Item_ID = ITEM.Item_ID
                                        JOIN COMPANY
                                            ON COMPANY.Contract_ID = DEAL.Contract_ID
                                        JOIN SUPPLIER
                                            ON SUPPLIER.SUPContract_ID = COMPANY.Contract_ID
                                        WHERE ITEM.Cat_ID = CATEGORY.Cat_ID
                                        FOR JSON PATH
                                    ) AS items
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
});

app.get('/api/warehouse_manager/', async (req, res) => {
    const data = await db.query(`SELECT Responsible, Inv_Name, Governorate, City FROM INVENTORY`);

    if(data.recordset.length === 0){
        return res.status(404).json({msg: "There is no Warehouse managers yet"});
    }

    res.status(200).json(data.recordset);
});

app.post('/api/categories/', [
    body('name')
        .trim()
        .notEmpty()
        .withMessage("Name is required")
        .isString()
        .withMessage("Invalid name")
], async (req, res) => {
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
});

app.get('/api/categories/', async (req, res) => {
    const data = await db.query(`SELECT Cat_Name FROM CATEGORY`);

    if(data.recordset.length === 0){
        return res.status(404).json({msg: "There is no categories yet"});
    }

    res.json(data.recordset);
});

app.post('/api/item/', [
    body('supplier')
        .trim()
        .notEmpty()
        .withMessage("Supplier is required"),
    body('category')
        .trim()
        .notEmpty()
        .withMessage("Category is required"),
    body('item')
        .trim()
        .notEmpty()
        .withMessage("Item name is required"),
    body('price')
        .trim()
        .notEmpty()
        .withMessage("Price is required")
        .isFloat({min: 0.01})
        .withMessage("Price must be above positive")        
], async (req, res) => {
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
    await db.query(`INSERT INTO ITEM VALUES ('${id}', '${item}', 0, ${price}, '${cat_id.recordset[0].Cat_ID}', '${sup_id}')`);
    res.json({msg: "Item successfully added"});
});

app.get('/api/vendors/', async (req, res) => {
    const data = await db.query(`
                                    SELECT
                                        VENDOR.Ven_Name,
                                        INVENTORY.Inv_Name,
                                    FROM VENDOR
                                    JOIN INVENTORY
                                        ON VENDOR.Inv_ID = INVENTORY.Inv_ID
                                `);
    
    if(data.recordset.length === 0){
        return res.status(400).json({msg: "There is no Vendors yet"});
    }

    res.json(data.recordset);
});

app.post('/api/vendors/', [
    body('vendor')
        .trim()
        .notEmpty()
        .withMessage("Vendor name is required")
        .isAlpha('en-US', {ignore: ' '})
        .withMessage("Vendor name must be in letters"),
    body('warehouse')
        .trim()
        .notEmpty()
        .withMessage("Warehouse name is required")
], async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({error: errors.array()[0].msg});
    }

    const {vendor, warehouse} = req.body;
    const inv_id = await db.query(`SELECT Inv_ID FROM INVENTORY WHERE Inv_Name = '${warehouse}'`);
    if(inv_id.recordset.length === 0){
        return res.status(404).json({msg: "Warehouse is not existed"});
    }

    const ven_id = crypto.randomUUID();
    await db.query(`INSERT INTO VENDOR VALUES('${ven_id}', '${inv_id.recordset[0].Inv_ID}', '${vendor}')`);
    res.status(201).json({msg: "Vendor added successfully"});
});

app.get('/api/warehouse_name', async (req, res) => {
    const data = await db.query(`SELECT Inv_Name FROM INVENTORY`);
    res.json(data.recordset);
});

app.get('/api/company/supplier/', async (req, res) => {
    const data = await db.query(`SELECT Com_Name, Governorate, City, Street FROM COMPANY WHERE Company_Type = 'exporter'`);
    res.json(data.recordset);
});

app.get('/api/company/importer/', async (req, res) => {
    const data = await db.query(`
                                SELECT 
                                    COMPANY.Com_Name, 
                                    COMPANY.Governorate, 
                                    COMPANY.City, 
                                    COMPANY.Street, 
                                    COMPANY.Com_Phone, 
                                    COMPANY.Com_Email,
                                    VENDOR.Ven_Name 
                                FROM COMPANY
                                JOIN PHARMACIES
                                    ON PHARMACIES.PHContract_ID = COMPANY.Contract_ID
                                JOIN VENDOR
                                    ON VENDOR.Ven_ID = PHARMACIES.Ven_ID
                                WHERE Company_Type = 'exporter'
                            `);
    res.json(data.recordset);
});

app.get('/api/company/:type', async (req, res) => {
    const com_type = req.params.type;
    const data = await db.query(`SELECT Com_Name FROM COMPANY WHERE Company_Type = '${com_type}'`);
    res.json(data.recordset);
});

app.post('/api/company/supplier/', [
    body('name')
        .trim()
        .notEmpty()
        .withMessage("Name is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("Name must contain only letters"),
    body('governorate')
        .trim()
        .notEmpty()
        .withMessage("Governorate is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("Governorate must contain only letters"),
    body('city')
        .trim()
        .notEmpty()
        .withMessage("City is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("City must contain only letters"),
    body('street')
        .trim()
        .notEmpty()
        .withMessage("Street is required")
        .isString()
        .withMessage("Invalid street"),
    body('phone')
        .trim()
        .notEmpty()
        .withMessage("Phone is required")
        .isMobilePhone("ar-EG")
        .withMessage("Invalid phone number"),
    body('email')
        .trim()
        .notEmpty()
        .withMessage("Email is required")
        .isEmail()
        .withMessage("Invalid email")
], async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({msg: errors.array()[0].msg});
    }

    const {name, governorate, city, street, phone, email} = req.body;
    const contract_id = crypto.randomUUID();
    const supplier_id = crypto.randomUUID();
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
            'exporter' 
        )`);
    await db.query(`INSERT INTO SUPPLIER VALUES('${contract_id}','${supplier_id}')`);
    res.json({msg: "Supplier created successfully"});
});

app.post('/api/company/pharma/', [
    body('name')
        .trim()
        .notEmpty()
        .withMessage("Name is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("Name must contain only letters"),
    body('governorate')
        .trim()
        .notEmpty()
        .withMessage("Governorate is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("Governorate must contain only letters"),
    body('city')
        .trim()
        .notEmpty()
        .withMessage("City is required")
        .isAlpha('en-US', { ignore: ' ' })
        .withMessage("City must contain only letters"),
    body('street')
        .trim()
        .notEmpty()
        .withMessage("Street is required")
        .isString()
        .withMessage("Invalid street"),
    body('phone')
        .trim()
        .notEmpty()
        .withMessage("Phone is required")
        .isMobilePhone("ar-EG")
        .withMessage("Invalid phone number"),
    body('email')
        .trim()
        .notEmpty()
        .withMessage("Email is required")
        .isEmail()
        .withMessage("Invalid email"),
    body('vendor')
        .trim()
        .notEmpty()
        .withMessage("Vendor is required")
], async (req, res) => {
    const errors = validationResult(req);
    if(!errors.isEmpty()){
        return res.status(400).json({msg: errors.array()[0].msg});
    }

    const {name, governorate, city, street, phone, email, vendor} = req.body;
    const venid = await db.query(`SELECT Ven_ID FROM VENDOR WHERE Ven_Name = '${vendor}'`);
    if(venid.recordset.length === 0){
        return res.status(404).json({msg: "Vendor is not existed"});
    }

    const contract_id = crypto.randomUUID();
    const Ven_ID = venid.recordset[0].Ven_ID;
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
            'importer' 
        )`);
    await db.query(`INSERT INTO PHARMACIES VALUES('${contract_id}','${Ven_ID}')`);
    res.json({msg: "Pharmacy created successfully"});
});

app.get('/api/supplier-details/:name', async (req, res) => {
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
                                        JOIN SUPPLIER 
                                            ON SUPPLIER.SUPContract_ID = CATEGORY.SUP_ID
                                        JOIN COMPANY
                                            ON COMPANY.Contract_ID = SUPPLIER.SUPContract_ID
                                        WHERE COMPANY.Com_Name = '${name}';
                                    `);

        res.status(200).json(data1.recordset, data2.recordset);

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
});

app.get('/api/category-items-exporter/:name', async (req, res) => {
    try {
        const name = req.params.name;
        if (!name) 
            return res.status(400).json({msg: "Supplier name required"});

        const data = await db.query(`
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
                                        JOIN SUPPLIER 
                                            ON SUPPLIER.SUPContract_ID = CATEGORY.SUP_ID
                                        JOIN COMPANY
                                            ON COMPANY.Contract_ID = SUPPLIER.SUPContract_ID
                                        WHERE COMPANY.Com_Name = '${name}';
                                    `);
        res.status(200).json(data.recordset);
    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
});

app.get('/api/category-items-importer/:name', async (req, res) => {
    try {
        const name = req.params.name;
        if (!name) 
            return res.status(400).json({msg: "Supplier name required"});

        const data1 = await db.query(`
                                        SELECT
                                            VENDOR.Ven_Name,
                                            INVENTORY.Inv_Name
                                        FROM COMPANY
                                        JOIN PHARMACIES
                                            ON COMPANY.Contract_ID = PHARMACIES.PHContract_ID
                                        JOIN VENDOR
                                            ON VENDOR.Ven_ID = PHARMACIES.Ven_ID
                                        JOIN INVENTORY
                                            ON INVENTORY.Inv_ID = VENDOR.Inv_ID
                                        WHERE COMPANY.Com_Name = '${name}'
            `);

        const data2 = await db.query(`
                                        SELECT
                                            CATEGORY.Cat_Name,
                                            (
                                                SELECT
                                                    ITEM.Item_Name,
                                                    ITEM.Item_Salery,
                                                    ITEM.Item_Quantity
                                                FROM ITEM
                                                WHERE ITEM.Cat_ID = CATEGORY.Cat_ID
                                                FOR JSON PATH
                                            ) AS items
                                        FROM CATEGORY
                                        JOIN INV_CAT
                                            ON INV_CAT.Cat_ID = CATEGORY.Cat_ID
                                        JOIN INVENTORY
                                            ON INVENTORY.Inv_ID = INV_CAT.Inv_ID
                                        WHERE INVENTORY.Inv_Name = '${data1.recordset[0].Inv_Name}';
                                    `);

        res.status(200).json({
            vendor_inventory: data1.recordset,
            categories: data2.recordset
        });

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
});

app.post('/api/deal-import/', async (req, res) => {
    try {
        const {com_name, war_name, items, total_price} = req.body;

        if (!com_name || !war_name || !Array.isArray(items) || !items.length)
            return res.status(400).json({msg: "Missing data"});

        const company = await db.query(`SELECT
                                            COMPANY.Contract_ID,
                                            SUPPLIER.SUP_VEN_ID 
                                        FROM COMPANY
                                        JOIN SUPPLIER
                                            ON SUPPLIER.SUPContract_ID = COMPANY.Contract_ID
                                        WHERE COMPANY.Com_Name='${com_name}
                                    '`);
        if (!company.recordset.length)
            return res.status(404).json({msg: "Company not found"});

        const warehouse = await db.query(`SELECT Inv_ID FROM INVENTORY WHERE Inv_Name='${war_name}'`);
        if (!warehouse.recordset.length) 
            return res.status(404).json({msg: "Warehouse not found"});

        const contractId = company.recordset[0].Contract_ID;
        const ven_id = company.recordset[0].SUP_VEN_ID;
        const invId = warehouse.recordset[0].Inv_ID;

        const dealId = crypto.randomUUID();
        const dealDate = new Date().toISOString();

        await db.query(`
            INSERT INTO DEAL
            VALUES ('${dealId}', '${ven_id}', '${contractId}', ${total_price}, '${dealDate}')
        `);

        for (const it of items) {
            const item = await db.query(`SELECT Item_ID, Item_Quantity, Cat_ID FROM ITEM WHERE Item_Name = '${it.name}'`);
            if (!item.recordset.length)
                return res.status(404).json({ status: "not_found", msg: "Item not found: " + it.name });

            const catId = item.recordset[0].Cat_ID;
            const newQty = item.recordset[0].Item_Quantity + it.quantity;
            const itemId = item.recordset[0].Item_ID;

            await db.query(`UPDATE ITEM SET Item_Quantity = ${newQty} WHERE Item_ID = '${itemId}'`);
            await db.query(`INSERT INTO ITEM_DEAL VALUES ('${dealId}', '${itemId}', '${newQty}')`);

            const invcat = await db.query(`SELECT * FROM INV_CAT WHERE Inv_ID = '${invId}' AND Cat_ID = '${catId}'`);
            if (!invcat.recordset.length)
                await db.query(`INSERT INTO INV_CAT VALUES ('${invId}', '${catId}')`);
        }

        return res.status(201).json({status: "success"});

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
});

app.post('/api/deal-export/', async (req, res) => {
    try {
        const {com_name, items, total_price} = req.body;

        if (!com_name || !Array.isArray(items) || !items.length)
            return res.status(400).json({msg: "Missing data"});

        const company = await db.query(`SELECT Contract_ID FROM COMPANY WHERE Com_Name = '${com_name}'`);
        if (!company.recordset.length)
            return res.status(404).json({msg: "Company not found"});

        const contractId = company.recordset[0].Contract_ID;

        const dealId = crypto.randomUUID();
        const dealDate = new Date().toISOString();
        const iddd = await db.query(`
                                        SELECT
                                            Ven_ID
                                        FROM COMPANY
                                        JOIN PHARMACIES
                                            ON COMPANY.Contract_ID = PHARMACIES.PHContract_ID
                                        JOIN VENDOR
                                            ON VENDOR.Ven_ID = PHARMACIES.Ven_ID
                                        WHERE COMPANY.Com_Name = '${com_name}'
            `);

        const ven_id = iddd.recordset[0].Ven_ID;

        await db.query(`
            INSERT INTO DEAL VALUES ('${dealId}', '${ven_id}', '${contractId}', ${total_price}, '${dealDate}')
        `);

        for (const it of items) {
            const item = await db.query(`SELECT Item_ID, Item_Quantity FROM ITEM WHERE Item_Name = '${it.name}'`);
            if (!item.recordset.length)
                return res.status(404).json({msg: `Item not found: ${it.name}`});

            const itemId = item.recordset[0].Item_ID;
            const current = item.recordset[0].Item_Quantity;

            if (current < it.quantity)
                return res.status(400).json({ status: "invalid", msg: "Not enough quantity" });

            await db.query(`UPDATE ITEM SET Item_Quantity=${current - it.quantity} WHERE Item_ID = '${itemId}'`);
            await db.query(`INSERT INTO ITEM_DEAL VALUES ('${dealId}', '${itemId}', '${it.quantity}')`);
        }

        return res.status(201).json({status: "success"});

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
});

app.get('/api/deal-details/:id', async (req, res) => {
    try {
        const id = req.params.id;
        if (!id) 
            return res.status(400).json({ status: "invalid", msg: "Deal ID required" });

        const type = await db.query(`
                                        SELECT
                                            COMPANY.Contract_ID,
                                            COMPANY.Company_Type
                                        FROM COMPANY
                                        JOIN DEAL 
                                            ON COMPANY.Contract_ID = DEAL.Contract_ID
                                        WHERE DEAL.Deal_ID = '${id}'
            `);

        let data1 = {};
        if(type.recordset[0].Company_Type === "importer"){
            data1 = await db.query(`
                                        SELECT
                                            DEAL.Deal_Date,
                                            DEAL.Deal_Cost,
                                            COMPANY.Company_Type,
                                            COMPANY.Com_Name,
                                            INVENTORY.Inv_Name,
                                            INVENTORY.Governorate,
                                            INVENTORY.City,
                                            VENDOR.Ven_Name
                                        FROM DEAL
                                        JOIN COMPANY
                                            ON COMPANY.Contract_ID = DEAL.Contract_ID
                                        JOIN PHARMACIES
                                            ON PHARMACIES.PHContract_ID = COMPANY.Contract_ID
                                        JOIN VENDOR
                                            ON VENDOR.Ven_ID = PHARMACIES.Ven_ID
                                        JOIN INVENTORY
                                            ON INVENTORY.Inv_ID = VENDOR.Inv_ID
                                        WHERE DEAL.Deal_ID = '${id}'
                `);
        }else{
            data1 = await db.query(`
                                        SELECT
                                            DEAL.Deal_Date,
                                            DEAL.Deal_Cost,
                                            COMPANY.Company_Type,
                                            COMPANY.Com_Name,
                                            INVENTORY.Inv_Name,
                                            INVENTORY.Governorate,
                                            INVENTORY.City
                                        FROM DEAL
                                        JOIN COMPANY
                                            ON COMPANY.Contract_ID = DEAL.Contract_ID
                                        JOIN SUPPLIER
                                            ON SUPPLIER.SUPContract_ID = COMPANY.Contract_ID
                                        JOIN ITEM
                                            ON SUPPLIER.SUP_VEN_ID = ITEM.SUP_ID
                                        JOIN CATEGORY
                                            ON CATEGORY.Cat_ID = ITEM.Cat_ID
                                        JOIN INV_CAT
                                            ON INV_CAT.Cat_ID = CATEGORY.Cat_ID
                                        JOIN INVENTORY
                                            ON INVENTORY.Inv_ID = INV_CAT.Inv_ID
                                        WHERE DEAL.Deal_ID = '${id}'
                                `);
            if (data1.recordset.length > 0)
                data1.recordset[0].vendor = "";
        }

        const data2 = await db.query(`
                                        SELECT
                                            ITEM.Item_Name,
                                            CATEGORY.Cat_Name,
                                            ITEM_DEAL.Item_Quantity,
                                            ITEM.Item_Salery
                                        FROM DEAL
                                        JOIN ITEM_DEAL
                                            ON ITEM_DEAL.Deal_ID = DEAL.Deal_ID
                                        JOIN ITEM
                                            ON ITEM.Item_ID = ITEM_DEAL.Item_ID
                                        JOIN CATEGORY
                                            ON CATEGORY.Cat_ID = ITEM.Cat_ID
                                        WHERE DEAL.Deal_ID = '${id}'
                                    `);
            res.json({
                general: data1.recordset,
                items: data2.recordset
            });

    } catch (err) {
        return res.status(500).json({ status: "invalid", msg: "Server error" });
    }
});

app.listen(port, () => {
    console.log(`Listening on port ${port}`);
});