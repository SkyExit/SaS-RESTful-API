package de.laurinhummel.SparkSRV.paths.users;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
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
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        try {
            Connection connection = handler.getConnection();

            String val = RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());

            while (handler.getUserData(val, request, response).getInt("status") == 200) {
                val = RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());
            }

            //JSON BODY HANDLER
            JSONObject body = JRepCrafter.getRequestBody(request, response);
            if(response.status() != 200) return body;

            try {
                if(!body.has("priority") || !body.has("password") || body.getString("password").isBlank()) return JRepCrafter.cancelOperation(response, 400, "You need to set password and priority");
                if(!ArrayUtils.contains(new int[]{1,2}, body.getInt("priority"))) return JRepCrafter.cancelOperation(response, 400, "Priority must be 1 or 2");
                if(body.getInt("priority") >= 2 && (!body.has("name") || body.getString("name").isBlank())) return JRepCrafter.cancelOperation(response, 400, "Enterprises must have a name");
            } catch (Exception ex) {
                return JRepCrafter.cancelOperation(response, 400, "Malformed json body");
            }

            //return body.getString("name") + " " + body.getString("password") + " " + body.getInt("priority");

            boolean isEnterprise = body.getInt("priority") == 2;
            val = (isEnterprise ? "ENT" : "USR") + "-" + val;

            PreparedStatement preparedStmt = connection.prepareStatement("insert into " + Main.names[0] + " (validation, name, money, priority) values (?, ?, ?, ?)");
            String name = isEnterprise ? body.getString("name") : null;
                preparedStmt.setString(1, val);
                preparedStmt.setString(2, name);
                preparedStmt.setString (3, "0");
                preparedStmt.setString (4, isEnterprise ? "2" : "1");
            preparedStmt.execute();

            preparedStmt = connection.prepareStatement("insert into " + Main.names[4] + " (validationID, password, enabled) values (?, ?, ?)");
            preparedStmt.setString(1, val);
            preparedStmt.setString (2, body.getString("password"));
            preparedStmt.setBoolean(3, true);
            preparedStmt.execute();


            SkyLogger.log((name == null ? "User" : "Enterprise") + " created successfully");
            return JRepCrafter.cancelOperation(response, 201, null).put("validationID", val).put("name", name).put("message", (name == null ? "User" : "Enterprise") + " created successfully");
        } catch (Exception e) {
            System.err.println("Got an exception! - create");
            System.err.println(e.getMessage());
            SkyLogger.logStack(e);
            return "Got an exception!";
        }
    }
}
