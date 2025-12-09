
const express = require("express");
const db = require("mssql");
const cors = require("cors");
const app = express();

app.use(cors({
    origin: "http://localhost:54537",
    credentials: true
}));
app.use(express.json());
const port = 8080;

const invexRouters = require('./routers/invex_routers');

app.use('/', invexRouters); // Middleware for routes

let config = require('./config/db_config');

db.connect(config); 



app.listen(port, () => {
    console.log(`Listening on port ${port}`);
});