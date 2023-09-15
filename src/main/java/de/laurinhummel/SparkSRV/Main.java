package de.laurinhummel.SparkSRV;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.paths.GetUser;
import de.laurinhummel.SparkSRV.paths.GetUsers;
import de.laurinhummel.SparkSRV.paths.PostCreate;
import spark.Spark;

import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        //Spark.get("/hello", (request, response) -> "Hello World");
        //TransactionHandler.validateUserBalance(1);

        MySQLConnectionHandler handler = new MySQLConnectionHandler();
        Spark.port(8080);

        Spark.get("/users/:id", new GetUser(handler));

        Spark.get("/users", new GetUsers(handler));

        Spark.post("/create", new PostCreate(handler));
    }

    public static void createTable(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS logbuchv1 (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(50) NOT NULL," +
                "money INTEGER NOT NULL" +
                ")";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
    }
}
