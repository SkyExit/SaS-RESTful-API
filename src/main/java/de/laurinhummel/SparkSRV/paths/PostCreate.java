package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.apache.commons.lang3.RandomStringUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PostCreate implements Route {
    MySQLConnectionHandler handler;
    public PostCreate(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) {
        String auth = request.headers("Authentication");
        if(auth == null || !auth.equals(Main.APITOKEN)) {
            return JRepCrafter.cancelOperation(response, 401, "Invalid or missing API-Key");
        }

        try {
            Connection connection = handler.getConnection();
            Main.createWealth(connection);

            String val = RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());

            String query = "insert into sas_wealth_v2 (validation, name, money, priority)"
                    + " values (?, ?, ?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
            String name = request.queryParams("name") == null || request.queryParams("name").isBlank() ? "not set" : request.queryParams("name");
                preparedStmt.setString(1, val);
                preparedStmt.setString(2, name);
                preparedStmt.setString (3, "0");
                preparedStmt.setString (4, "1");

            preparedStmt.execute();


            return JRepCrafter.successOperation(response, 201).put("validationID", val).put("name", name).put("message", "User created successfully");
        } catch (Exception e) {
            System.err.println("Got an exception! - create");
            System.err.println(e.getMessage());
            SkyLogger.logStack(e);
            return "Got an exception!";
        }
    }
}
