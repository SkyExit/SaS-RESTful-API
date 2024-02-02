package de.laurinhummel.SparkSRV.paths.users;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetUsers implements Route {
    MySQLConnectionHandler handler;
    public GetUsers(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        String sqlArgs = "SELECT * FROM `" + Main.names[0] + "` ORDER BY id ASC";
        try {
            Connection connection = handler.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONArray ja = new JSONArray();

            while (rs.next()) {
                ja.put(new JSONObject()
                        .put("id", rs.getInt("id"))
                        .put("validation", rs.getString("validation"))
                        .put("name", rs.getString("name") == null ? JSONObject.NULL : rs.getString("name"))
                        .put("money", rs.getInt("money"))
                        .put("priority", rs.getInt("priority")));
            }

            rs.close();
            preparedStatement.close();
            SkyLogger.log("User list fetched");
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, null).put("users", ja);
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Error while parsing user list");
        }
    }
}
