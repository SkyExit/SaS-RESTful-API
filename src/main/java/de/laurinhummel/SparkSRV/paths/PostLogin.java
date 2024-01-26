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
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        //JSON BODY HANDLER
        JSONObject body = JRepCrafter.getRequestBody(request, response);
        if(response.status() != 200) return body;

        if(!body.has("validation") || body.getString("validation").isBlank()) return JRepCrafter.cancelOperation(response, 400, "Please provide an ID");
        if(!body.has("password") || body.getString("password").isBlank()) return JRepCrafter.cancelOperation(response, 400, "Please provide a password");

        String validation = body.getString("validation");
        String password = body.getString("password");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + Main.names[4] + "` WHERE `validationID`='" + validation +
                    "' AND `password`='" + password + "'");
            ResultSet rs = preparedStatement.executeQuery();

            JSONObject jo = new JSONObject();
            jo.put("status", response.status());

            if(!rs.next()) {
                return JRepCrafter.cancelOperation(response, 403, "Invalid username or password").put("login", false);
            }
            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing user");
        }

        long time = Instant.now().toEpochMilli();
        String input = validation + "." + time;
        String encoded = Base64.getEncoder().encodeToString(input.getBytes());

        SkyLogger.log("User " + validation + " logged in at " + Instant.now());
        return JRepCrafter.cancelOperation(response, 200, "kp was du willst").put("token", encoded).put("creation", time).put("login", true);
    }
}
