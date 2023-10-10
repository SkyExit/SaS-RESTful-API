package de.laurinhummel.SparkSRV;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import de.laurinhummel.SparkSRV.paths.*;
import spark.Spark;

import java.sql.*;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {
        MySQLConnectionHandler handler = new MySQLConnectionHandler();

        SkyLogger.log(Level.INFO, "Spark has ignited");

        Spark.port(5260);

        Spark.staticFiles.location("/assets");
        Spark.staticFiles.header("Access-Control-Allow-Origin", "*");

        Spark.before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "*");
            res.type("application/json");
        });

        Spark.options("/*", (req, res) -> {
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
    }

    public static final String APITOKEN = "40263hv-0824ff933a-4014ff9345-d7c0402";

    public static void createWealth(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS sas_wealth_v2 (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(50) NOT NULL," +
                "validation VARCHAR(10) NOT NULL," +
                "money INTEGER NOT NULL," +
                "priority INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

    public static void createTransactions(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS sas_transactions_v2 (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "validation_active VARCHAR(10) NOT NULL," +
                "name_active VARCHAR(50) NOT NULL," +
                "validation_passive VARCHAR(10)," +
                "name_passive VARCHAR(50) NOT NULL," +
                "money INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }
}
