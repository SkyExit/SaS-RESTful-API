package de.laurinhummel.SparkSRV;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import paths.GetUsers;
import paths.PostCreate;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        //Spark.get("/hello", (request, response) -> "Hello World");
        //TransactionHandler.validateUserBalance(1);

        MySQLConnectionHandler handler = new MySQLConnectionHandler();
        Spark.port(8080);

        Spark.get("/users/:id", (request, response) -> {
            /*
            USRObject user = getDao().queryForId(request.params(":id"));k
            if (user != null) {
                return "Username: " + user.getName(); // or JSON? :-)
            } else {
                response.status(404); // 404 Not found
                return "User not found";
            }

             */
            return false;
        });

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
