package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import org.apache.commons.lang3.RandomStringUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostCreate implements Route {
    MySQLConnectionHandler handler;
    public PostCreate(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String auth = request.headers("Authentication");
        if(auth == null || !auth.equals(Main.APITOKEN)) {
            return JRepCrafter.cancelOperation(response, 401, "Invalid or missing API-Key");
        }

        try {
            Connection connection = handler.getConnection();
            Main.createLogbuch(connection);

            String val = RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());

            String query = "insert into logbuchv2 (validation, name, money, priority)"
                    + " values (?, ?, ?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, val);
                preparedStmt.setString (2, request.queryParams("name"));
                preparedStmt.setString (3, "0");
                preparedStmt.setString (4, "1");

            preparedStmt.execute();

            Logger.getGlobal().log(Level.INFO, "USR created: " + request.queryParams("name") + "(p1) - " + val);

            return JRepCrafter.cancelOperation(response, 201, "User created successfully");
        } catch (Exception e) {
            System.err.println("Got an exception! - create");
            System.err.println(e.getMessage());
            e.printStackTrace();
            return "Got an exception!";
        }
    }
}
