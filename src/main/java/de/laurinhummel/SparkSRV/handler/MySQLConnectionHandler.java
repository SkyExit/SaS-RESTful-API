package de.laurinhummel.SparkSRV.handler;

import de.laurinhummel.SparkSRV.Main;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.IOException;

import java.nio.file.Path;
import java.sql.*;

public class MySQLConnectionHandler {
    private Connection connection = null;

    public MySQLConnectionHandler() { }

    private Connection connect() {
        //String HOSTNAME = "sql229.your-server.de";
        //String PORT = "3306";
        //String username = "laurin_1";
        //String DBNAME = "laurin_db1";
        //String password = "yDeSn57NMdwnMk7C";
        //String url = false ? "jdbc:mysql://u33515_X8lzuzNisT:sdK8uQdT3tL7KsAg%5Ej%2BYb%5E!D@161.97.78.70:3306/s33515_SaS-RESTFUL-API" : "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/" + DBNAME;
        String url = "";
        try {
            url = "jdbc:sqlite:"+ Path.of("./data/").toRealPath().resolve("database.db");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println("Loading driver...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded!");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath! - custom2", e);
        }

        System.out.println("Database: " + url);
        System.out.println("Connecting database...");
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Database connected!");
            createDatabases(connection);
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    private void createDatabases(Connection connection) throws SQLException {
        Main.createWealth(connection);
        Main.createTransactions(connection);
        Main.createEmployee(connection);
    }

    public boolean isConnected() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection.isValid(5);
            }
        } catch (SQLException exception) {
            SkyLogger.logStack(exception);
        }
        return false;
    }

    public Connection getConnection() {
        if(isConnected()) {
            return connection;
        } else {
            return connect();
        }
    }

    public JSONObject getUserData(String searchParameter, Request request, Response response) {
        String sqlArgs = "SELECT * FROM `" + Main.names[0] + "` WHERE `validation` LIKE '%" + searchParameter + "'";

        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONObject jo = new JSONObject();
            jo.put("status", response.status());
            JSONObject ja = new JSONObject();

            if(!rs.next()) {
                return JRepCrafter.cancelOperation(response, 404, "Specified user not found");
            } else {
                ja.put("id", rs.getInt("id"));
                ja.put("validation", rs.getString("validation"));
                ja.put("name", rs.getString("name") == null ? JSONObject.NULL : rs.getString("name"));
                ja.put("money", rs.getInt("money"));
                ja.put("priority", rs.getInt("priority"));
            }

            jo.put("user", ja);

            rs.close();
            preparedStatement.close();
            SkyLogger.log("User data fetched for " + searchParameter);
            return jo;
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing user");
        }
    }
}
