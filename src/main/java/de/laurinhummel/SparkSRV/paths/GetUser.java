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
import java.util.logging.Level;

public class GetUser implements Route {
    MySQLConnectionHandler handler;
    public GetUser(MySQLConnectionHandler handler) { this.handler = handler; }

    public static JSONObject getUserData(String validationID, MySQLConnectionHandler handler, Request request, Response response) {
        String sqlArgs = "SELECT * FROM `" + Main.names[0] + "` WHERE `validation`='" + validationID + "'";

        try {
            Connection connection = handler.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONObject jo = new JSONObject();
            jo.put("status", response.status());
            JSONObject ja = new JSONObject();

            if(!rs.next()) {
                return JRepCrafter.cancelOperation(response, 404, "Specified user not found");
            } else {
                ja.put("id", rs.getInt("id"));
                ja.put("validation", rs.getString("validation"));
                ja.put("name", rs.getString("name") == null ? JSONObject.NULL : rs.getString("name"));
                ja.put("money", rs.getInt("money"));
                ja.put("priority", rs.getInt("priority"));
            }

            jo.put("user", ja);

            rs.close();
            preparedStatement.close();
            SkyLogger.log(Level.INFO, "User data fetched for " + validationID);
            return jo;
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing user");
        }
    }

    @Override
    public Object handle(Request request, Response response) {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        String validationID;
        try {
            validationID = request.params(":validation");
        } catch (Exception ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing parameter");
        }

        return getUserData(validationID, handler, request, response);
    }
}
