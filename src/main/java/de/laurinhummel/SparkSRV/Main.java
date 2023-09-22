package de.laurinhummel.SparkSRV;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.paths.GetUser;
import de.laurinhummel.SparkSRV.paths.GetUsers;
import de.laurinhummel.SparkSRV.paths.PostCreate;
import de.laurinhummel.SparkSRV.paths.PutTransaction;
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

        Spark.get("/user", new GetUser(handler));

        Spark.get("/users", new GetUsers(handler));

        Spark.post("/create", new PostCreate(handler));

        Spark.put("/transaction", new PutTransaction(handler));
    }

    public static void createLogbuch(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS logbuchv2 (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "validation VARCHAR(10) NOT NULL," +
                "name VARCHAR(50) NOT NULL," +
                "money INTEGER NOT NULL," +
                "priority INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }

    public static void createBlockchain(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS blockchainv1 (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "validation_active VARCHAR(10) NOT NULL," +
                "name_active VARCHAR(50) NOT NULL," +
                "validation_passive VARCHAR(10) NOT NULL," +
                "name_passive VARCHAR(50) NOT NULL," +
                "money INTEGER NOT NULL" + ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
        stmt.close();
    }
}
