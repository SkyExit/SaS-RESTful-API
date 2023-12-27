package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.apache.commons.lang3.RandomStringUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class PostCreate implements Route {
    MySQLConnectionHandler handler;
    public PostCreate(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        try {
            Connection connection = handler.getConnection();

            String val = RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());

            while (handler.getUserData(val, request, response).getInt("status") == 200) {
                val = RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());
            }

            String query = "insert into " + Main.names[0] + " (validation, name, money, priority)"
                    + " values (?, ?, ?, ?)";

            boolean user = (request.queryParams("name") == null || request.queryParams("name").isBlank());
            val = (user ? "USR" : "ENT") + "-" + val;

            PreparedStatement preparedStmt = connection.prepareStatement(query);
            String name = user ? null : request.queryParams("name");
                preparedStmt.setString(1, val);
                preparedStmt.setString(2, name);
                preparedStmt.setString (3, "0");
                preparedStmt.setString (4, name == null ? "1" : "2");

            preparedStmt.execute();


            SkyLogger.log(Level.INFO, (name == null ? "User" : "Enterprise") + " created successfully");
            return JRepCrafter.successOperation(response, 201).put("validationID", val).put("name", name).put("message", (name == null ? "User" : "Enterprise") + " created successfully");
        } catch (Exception e) {
            System.err.println("Got an exception! - create");
            System.err.println(e.getMessage());
            SkyLogger.logStack(e);
            return "Got an exception!";
        }
    }
}
