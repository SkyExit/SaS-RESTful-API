package de.laurinhummel.SparkSRV;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.paths.GetUser;
import de.laurinhummel.SparkSRV.paths.GetUsers;
import de.laurinhummel.SparkSRV.paths.PostCreate;
import de.laurinhummel.SparkSRV.paths.PatchTransaction;
import spark.Spark;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws SQLException {
        Logger log = Logger.getGlobal();
            log.log(Level.INFO, "SparkSRV started");

        MySQLConnectionHandler handler = new MySQLConnectionHandler();
        Spark.port(5260);

        Spark.get("/users/:validation", new GetUser(handler));

        Spark.get("/users", new GetUsers(handler));

        Spark.post("/create", new PostCreate(handler));

        Spark.patch("/transaction", new PatchTransaction(handler));
    }

    public static final String APITOKEN = "40263hv-0824ff933a-4014ff9345-d7c0402";

    public static void createWealth(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS sas_wealth_v1 (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "validation VARCHAR(10) NOT NULL," +
                "money INTEGER NOT NULL," +
                "priority INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

    public static void createTransactions(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS sas_transactions_v1 (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "validation_active VARCHAR(10) NOT NULL," +
                "validation_passive VARCHAR(10) NOT NULL," +
                "money INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }
}
