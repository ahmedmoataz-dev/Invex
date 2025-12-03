
const express = require("express");
const db = require("mssql");
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
    if(data.recordset == []){
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
    
    await db.query(`INSERT INTO INVENTORY VALUES('${id}', '${governorate}', '${city}', ${capacity}, '${responsible}', '${name}')`);

    res.status(201).json({  id: id, 
                            name: name, 
                            governorate: governorate, 
                            city: city, 
                            capacity: capacity, 
                            responsible: responsible
    });
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

    if(data1.recordset == []){
        return res.status(404).json({msg: "There is no warehouses yet"});
    }

    if(data2.recordset == []){
        return res.status(404).json({msg: "There is no items in the warehouse yet"});
    }

    res.json([data1.recordset, data2.recordset]);
});

app.get('/api/warehouse_manager/', async (req, res) => {
    const data = await db.query(`SELECT Responsible, Inv_Name FROM INVENTORY`);

    if(data.recordset == []){
        return res.status(404).json({msg: "There is no Warehouse managers yet"});
    }

    res.status(200).json(data.recordset);
});

app.listen(port, () => {
    console.log(`Listening on port ${port}`);
});