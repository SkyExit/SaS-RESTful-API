package de.laurinhummel.SparkSRV.handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionHandler {
    private Connection connection = null;

    public MySQLConnectionHandler() { }

    private Connection connect() {
        String HOSTNAME = "localhost";
        String PORT = "3306";
        String username = "root";
        String DBNAME = "sas-spark-api";
        String password = "root";
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
            connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        } finally {
            System.out.println("Database connected!");
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
