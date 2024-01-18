package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.time.Instant;
import java.util.Base64;

public class PostLogin implements Route {
    MySQLConnectionHandler handler;
    public PostLogin(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        JSONObject body = JRepCrafter.getRequestBody(request, response);
        if(response.status() != 200) return body;

        String validation = body.getString("validation");
        JSONObject user;
        try {
            user = handler.getUserData(validation, request, response);
        } catch (Exception ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing JSON body");
        }

        if(user.getInt("status") != 200) {
            return JRepCrafter.cancelOperation(response, 404, "Specified user not found").put("token", JSONObject.NULL).put("creation", JSONObject.NULL);
        } else {
            long time = Instant.now().toEpochMilli();
            String input = validation + "." + time;
            String encoded = Base64.getEncoder().encodeToString(input.getBytes());

            SkyLogger.log("Fetched login data for " + validation);
            return JRepCrafter.cancelOperation(response, 200, "kp was du willst").put("token", encoded).put("creation", time);
        }
    }
}
