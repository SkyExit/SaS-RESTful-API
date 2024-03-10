package de.laurinhummel.SparkSRV.handler;

import de.laurinhummel.SparkSRV.Main;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import java.sql.*;

public class MySQLConnectionHandler {
    private Connection connection = null;

    public MySQLConnectionHandler() { }

    private Connection connect() {
        String HOSTNAME = "192.168.0.13";
        String PORT = "3306";
        String username = "laurin";
        String DBNAME = "database";
        String password = "password";
        String url = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/" + DBNAME;

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
            DriverManager.setLoginTimeout(4);
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected!");
        } catch (SQLException e) {
            try {
                System.out.println("Connecting Docker database...");
                connection = DriverManager.getConnection("jdbc:mysql://" + "172.19.0.3" + ":" + PORT + "/" + DBNAME, username, password);
                System.out.println("Database connected!");
            } catch (SQLException ex) {
                SkyLogger.logStack(ex);
                throw new IllegalStateException("Cannot connect the database!", ex);
            }
        }
        createDatabases(connection);
        return connection;
    }

    private void createDatabases(Connection connection) {
        try {
            Main.createWealth(connection);
            Main.createTransactions(connection);
            Main.createEmployee(connection);
            Main.createProducts(connection);
            Main.createLogin(connection);
        } catch (Exception e) { SkyLogger.logStack(e); }
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
        String sqlArgs = "SELECT * FROM `" + Main.names[0] + "` WHERE `validation`='" + searchParameter + "'";

        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONObject jo = new JSONObject();
            jo.put("status", response.status());
            JSONObject ja = new JSONObject();

            if(!rs.next()) {
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Specified user not found")
                        .put("user", new JSONObject().put("priority", 0));
            } else {
                ja.put("id", rs.getInt("id"));
                ja.put("validation", rs.getString("validation"));
                ja.put("name", rs.getString("name") == null ? JSONObject.NULL : rs.getString("name"));
                ja.put("money", rs.getFloat("money"));
                ja.put("priority", rs.getInt("priority"));
                ja.put("owner", rs.getString("owner") == null ? JSONObject.NULL : rs.getString("owner"));
                ja.put("taxed", rs.getBoolean("taxed"));
            }

            jo.put("user", ja);

            rs.close();
            preparedStatement.close();
            SkyLogger.log("User data fetched for " + searchParameter);
            return jo;
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Error while parsing user");
        }
    }

    public JSONObject getProduct(String searchParameter, Request request, Response response) {
        String sqlArgs = "SELECT * FROM `" + Main.names[3] + "` WHERE `validation_product`='" + searchParameter + "'";

        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONObject jo = new JSONObject();
            jo.put("status", response.status());
            JSONObject ja = new JSONObject();

            if(!rs.next()) {
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Specified product not found");
            } else {
                ja.put("id", rs.getInt("id"));
                ja.put("validation_enterprise", rs.getString("validation_enterprise"));
                ja.put("validation_product", rs.getString("validation_product"));
                ja.put("name_product", rs.getString("name_product"));
                ja.put("price", rs.getFloat("price"));
            }

            jo.put("product", ja);

            rs.close();
            preparedStatement.close();
            SkyLogger.log("Product data fetched for " + searchParameter);
            return jo;
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Error while parsing user");
        }
    }
}
