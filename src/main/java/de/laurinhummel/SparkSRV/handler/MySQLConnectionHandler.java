package de.laurinhummel.SparkSRV.handler;

import de.laurinhummel.SparkSRV.Main;
import org.json.JSONObject;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
            url = "jdbc:sqlite:"+ Path.of("./").toRealPath().resolve("database.db");
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

    public JSONObject requestGetApi(Response response, String path, String args) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + Spark.port() + "/" + path + "/" + args).openConnection();
            urlConnection.setRequestProperty("Authentication", Main.APIKEY);
            if(urlConnection.getResponseCode() != 200) { return JRepCrafter.cancelOperation(response, 404, "Specified user not found - handler"); }
            InputStream inputStream = urlConnection.getInputStream();
            return new JSONObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            SkyLogger.logStack(e);
        }
        return null;
    }
}
