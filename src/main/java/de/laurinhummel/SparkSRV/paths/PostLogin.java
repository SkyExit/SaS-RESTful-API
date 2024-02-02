package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Base64;

public class PostLogin implements Route {
    MySQLConnectionHandler handler;
    public PostLogin(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (SessionValidationHandler.validate(request)) {
            return SessionValidationHandler.correct(response);
        }

        Connection connection = handler.getConnection();

        //JSON BODY HANDLER
        JSONObject body = JRepCrafter.getRequestBody(request, response);
        if (response.status() != 200) return body;

        if (!body.has("validation") || body.getString("validation").isBlank())
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Please provide an ID");
        if (!body.has("password") || body.getString("password").isBlank())
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Please provide a password");

        String validation = body.getString("validation");
        String password = body.getString("password");

        int priority;
        try {
            JSONObject jo = new JSONObject();
            jo.put("status", response.status());
            priority = getLoginStatus(connection, validation, password);
            if (priority < 1) {
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.FORBIDDEN, "Invalid username or password").put("login", false).put("priority", priority);
            }
        } catch (Exception e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Error while parsing user");
        }

        long time = Instant.now().toEpochMilli();
        String input = validation + "." + time;
        String encoded = Base64.getEncoder().encodeToString(input.getBytes());

        SkyLogger.log("User " + validation + " logged in at " + Instant.now());
        return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, null).put("token", encoded).put("creation", time).put("login", true).put("priority", priority);
    }


    public static int getLoginStatus(Connection connection, String validationID, String password) {
        try {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT priority, validationID, password, enabled FROM " + Main.names[0] + " INNER JOIN " + Main.names[4] + " ON validationID = validation WHERE validationID='" + validationID + "'");
                    ResultSet rs = preparedStatement.executeQuery();
            if(!rs.next()) { return 0; }
            if(!rs.getString("password").equals(password)) return -1;
            int p = rs.getInt("priority");
            rs.close();
            return p;
        } catch (Exception e) {
            SkyLogger.logStack(e);
            return 0;
        }
    }
}
