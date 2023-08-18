package de.laurinhummel.SparkSRV.handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionHandler {
    public MySQLConnectionHandler() {

    }

    public Connection connect() {
        Connection conn;
        String HOSTNAME = "localhost";
        String PORT = "3306";
        String username = "root";
        String DBNAME = "sas-spark-api";
        String password = "root";
        String url = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/" + DBNAME;

        System.out.println("Loading driver...");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Driver loaded!");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }

        System.out.println("Database: " + url);
        System.out.println("Connecting database...");
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        } finally {
            System.out.println("Database connected!");
        }
    }
}
