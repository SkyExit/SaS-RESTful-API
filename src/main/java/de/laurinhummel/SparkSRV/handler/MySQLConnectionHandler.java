package de.laurinhummel.SparkSRV.handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionHandler {
    private Connection connection = null;

    public MySQLConnectionHandler() { }

    private Connection connect() {
        String HOSTNAME = "sql229.your-server.de";
        String PORT = "3306";
        String username = "laurin_1";
        String DBNAME = "laurin_db1";
        String password = "yDeSn57NMdwnMk7C";
        String url = false ? "jdbc:mysql://u33515_X8lzuzNisT:sdK8uQdT3tL7KsAg%5Ej%2BYb%5E!D@161.97.78.70:3306/s33515_SaS-RESTFUL-API" :
                "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/" + DBNAME;

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
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected!");
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    public boolean isConnected() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection.isValid(5);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
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
}
