package de.laurinhummel.SparkSRV.handler;

import de.laurinhummel.SparkSRV.Main;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.stream.Stream;

public class MySQLConnectionHandler {
    private Connection connection = null;

    public MySQLConnectionHandler() { }

    private static Boolean isRunningInsideDocker() {

        try (Stream< String > stream =
                     Files.lines(Paths.get("/proc/1/cgroup"))) {
            return stream.anyMatch(line -> line.contains("/docker"));
        } catch (IOException e) {
            return false;
        }
    }

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
            DriverManager.setLoginTimeout(7);
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected!");
            createDatabases(connection);
            return connection;
        } catch (SQLException e) {
            try {
                System.out.println("Connecting Docker database...");
                connection = DriverManager.getConnection("jdbc:mysql://" + "172.19.0.2" + ":" + PORT + "/" + DBNAME, username, password);
                System.out.println("Database connected!");
                createDatabases(connection);
                return connection;
            } catch (SQLException ex) {
                SkyLogger.logStack(ex);
                throw new IllegalStateException("Cannot connect the database!", ex);
            }
        }
    }

    private void createDatabases(Connection connection) throws SQLException {
        Main.createWealth(connection);
        Main.createTransactions(connection);
        Main.createEmployee(connection);
        Main.createProducts(connection);
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
