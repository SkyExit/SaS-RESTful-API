package de.laurinhummel.SparkSRV;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
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

        Spark.get("/users/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) throws SQLException {
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
            }
        });

        Spark.get("/users", new Route() {
            @Override
            public Object handle(Request request, Response response) throws SQLException {
                String sqlArgs = "SELECT * FROM `logbuchv1` ORDER BY id ASC";

                try {
                    Connection connection = handler.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
                    ResultSet rs = preparedStatement.executeQuery();
                    StringBuilder sb = new StringBuilder();
                        sb.append("user list: " + "\n");
                    while (rs.next()) {
                        sb.append("id: " + rs.getInt("id") + " - name: " + rs.getString("name") +
                                " - money: " + rs.getInt("money") + "\n");
                    }
                    rs.close();
                    preparedStatement.close();
                    return sb.toString();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "Error in getUserList - Main function";
                }
            }
        });

        Spark.get("/post", new Route() {
            @Override
            public Object handle(Request request, Response response) throws SQLException {
                try {
                    Connection connection = handler.getConnection();
                    createTable(connection);

                    String query = "insert into LogbuchV1 (name, money)"
                            + " values (?, ?)";

                    PreparedStatement preparedStmt = connection.prepareStatement(query);
                    preparedStmt.setString (1, request.queryParams("name"));
                    preparedStmt.setString (2, request.queryParams("money"));

                    preparedStmt.execute();
                    response.status(201); // 201 Created

                    return "User created";
                } catch (Exception e) {
                    System.err.println("Got an exception!");
                    System.err.println(e.getMessage());
                    return "Got an exception!";
                }
            }
        });
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
