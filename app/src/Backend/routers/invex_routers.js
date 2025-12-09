
const express = require('express');
const {body} = require("express-validator");

const router = express.Router();

const controllers = require('../controllers/invex_controller'); //controllers

router.post('/api/login/', [
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
], controllers.authen);

router.get('/api/recent-deals', controllers.recentDeals);

router.get('/api/manager/', controllers.getAllManagers);

router.post('/api/manager', [
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
], controllers.addManager);

router.get('/api/warehouse/', controllers.getWarehouses);

router.post('/api/warehouse/', [
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
], controllers.addWarehouse);

router.get('/api/warehouse/:ware_name', controllers.warehouseDetails);

router.get('/api/warehouse_manager/', controllers.warehouseManagers);

router.post('/api/categories/', [
    body('name')
        .trim()
        .notEmpty()
        .withMessage("Name is required")
        .isString()
        .withMessage("Invalid name")
], controllers.addCategory);

router.get('/api/categories/', controllers.getCategories);

router.post('/api/item/', [
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
], controllers.addItem);

router.get('/api/vendors/', controllers.getVendors);

router.post('/api/vendors/', [
    body('vendor')
        .trim()
        .notEmpty()
        .withMessage("Vendor name is required")
        .isAlpha('en-US', {ignore: ' '})
        .withMessage("Vendor name must be in letters"),
    body('warehouse')
        .trim()
        .notEmpty()
        .withMessage("Warehouse name is required"),
    body('type')
        .trim()
        .notEmpty()
        .withMessage("Type is required")
], controllers.addVendor);

router.get('/api/vendors_at_deal/', [
    body('type')
        .trim()
        .notEmpty()
        .withMessage("Deal type is required"),
    body('warehouse')
        .trim()
        .notEmpty()
        .withMessage("Warehouse name is required")
], controllers.vendorsAtDeal);

router.get('/api/warehouse_name', controllers.warehouse_names);

router.get('/api/company/exporter/', controllers.getExporter);

router.get('/api/company/importer/', controllers.getImporter);

router.get('/api/company/:type', controllers.comByType);

router.post('/api/company/', [
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
    body('type')
        .trim()
        .notEmpty()
        .withMessage("Type is required")
], controllers.addCompany);

router.get('/api/supplier-details/:name', controllers.supplierDetails);

router.get('/api/category-items-importer/:name', controllers.categoryItems);

router.post('/api/deal-import/', controllers.dealImport);

router.post('/api/deal-export/', controllers.dealExport);

router.get('/api/deal-details/:id', controllers.dealDetails);

module.exports = {
    router
};