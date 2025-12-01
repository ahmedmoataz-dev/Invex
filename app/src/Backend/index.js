
const express = require("express");
const db = require("mssql");
const app = express();
const {body, validationResult} = require("express-validator");

app.use(express.json());
const port = 123;

app.get('/api/warehouse/', async (req, res) => {
    const data = await db.query('');
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
    
    await db.query(``);

    res.status(201).json({  id: id, 
                            name: name, 
                            governorate: governorate, 
                            city: city, 
                            capacity: capacity, 
                            responsible: responsible
    });
});