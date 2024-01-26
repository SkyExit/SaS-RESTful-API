package de.laurinhummel.SparkSRV;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import de.laurinhummel.SparkSRV.paths.*;
import de.laurinhummel.SparkSRV.paths.enterprises.GetEnterprises;
import de.laurinhummel.SparkSRV.paths.enterprises.PatchEmployee;
import de.laurinhummel.SparkSRV.paths.products.GetProducts;
import de.laurinhummel.SparkSRV.paths.products.PatchProducts;
import de.laurinhummel.SparkSRV.paths.users.*;
import spark.Spark;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {
        MySQLConnectionHandler handler = new MySQLConnectionHandler();

        final Properties properties = new Properties();
        properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));

        SkyLogger.log(SkyLogger.Level.INFO, "Spark has ignited");
        SkyLogger.log(SkyLogger.Level.INFO, "Version: " + properties.getProperty("version"));

        Spark.port(5260);

        /*
        try {
            Spark.secure("data/KeyStore.jks", "QMS3ti9xvjqR", null, null);
        } catch (Exception ex) {
            SkyLogger.logStack(ex);
        }
         */

        Spark.staticFiles.location("/assets");
        Spark.staticFiles.header("Access-Control-Allow-Origin", "*");

        Spark.before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "*");
            res.type("application/json");
        });

        Spark.options("*/*", (req, res) -> {
            String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        Spark.get("/users/:validation", new GetUser(handler));
        Spark.get("/users/:validation/", new GetUser(handler));

        Spark.get("/users", new GetUsers(handler));
        Spark.get("/users/", new GetUsers(handler));

        Spark.post("/create", new PostCreate(handler));
        Spark.post("/create/", new PostCreate(handler));

        Spark.patch("/transaction", new PatchTransaction(handler));
        Spark.patch("/transaction/", new PatchTransaction(handler));

        Spark.get("/history/:validation", new GetHistory(handler));
        Spark.get("/history/:validation/", new GetHistory(handler));
        Spark.get("/history", new GetHistory(handler));
        Spark.get("/history/", new GetHistory(handler));

        Spark.patch("/employee", new PatchEmployee(handler));
        Spark.patch("/employee/", new PatchEmployee(handler));

        Spark.get("/enterprises/:validation", new GetEnterprises(handler));
        Spark.get("/enterprises/:validation/", new GetEnterprises(handler));
        Spark.get("/enterprises", new GetEnterprises(handler));
        Spark.get("/enterprises/", new GetEnterprises(handler));

        Spark.post("/login", new PostLogin(handler));
        Spark.post("/login/", new PostLogin(handler));

        Spark.get("/products/:enterprise/:product", new GetProducts(handler));
        Spark.get("/products/:enterprise/:product/", new GetProducts(handler));
        Spark.get("/products/:enterprise", new GetProducts(handler));
        Spark.get("/products/:enterprise/", new GetProducts(handler));
        Spark.get("/products", new GetProducts(handler));
        Spark.get("/products/", new GetProducts(handler));

        Spark.patch("/products", new PatchProducts(handler));
        Spark.patch("/products/", new PatchProducts(handler));
    }

    public static String[] names = new String[]{"sas_wealth_v3", "sas_transactions_v3", "sas_employee_v2", "sas_products_v1", "sas_login_v1"};

    public static String APIKEY = "40263hv-0824ff933a-4014ff9345-d7c0402";

    public static void createWealth(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + names[0] + "(" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(50) DEFAULT NULL," +
                "validation VARCHAR(20) NOT NULL," +
                "money INTEGER NOT NULL," +
                "priority INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

    public static void createTransactions(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + names[1] + "(" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "validation_active VARCHAR(20) NOT NULL," +
                "name_active VARCHAR(50) NOT NULL," +
                "validation_passive VARCHAR(20)," +
                "name_passive VARCHAR(50) DEFAULT NULL," +
                "money INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

    public static void createEmployee(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + names[2] + "(" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "validation_enterprise VARCHAR(20) NOT NULL," +
                "validation_employee VARCHAR(20) NOT NULL," +
                "employed BOOLEAN DEFAULT TRUE," +
                "salary INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

    public static void createProducts(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + names[3] + "(" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "validation_enterprise VARCHAR(20) NOT NULL," +
                "validation_product VARCHAR(20) NOT NULL," +
                "name_product VARCHAR(60) NOT NULL," +
                "price INTEGER NOT NULL DEFAULT 0" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

    public static void createLogin(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + names[4] + "(" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "validationID VARCHAR(20) NOT NULL," +
                "password VARCHAR(20) NOT NULL," +
                "enabled BOOLEAN DEFAULT TRUE" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }
}
