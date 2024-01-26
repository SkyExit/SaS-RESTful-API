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

import java.sql.*;

public class GetHistory implements Route {
    MySQLConnectionHandler handler;
    public GetHistory(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        String searchParameter = request.params(":validation") == null ? "" : request.params(":validation");
        String sqlArgs;

        if(searchParameter.isBlank()) {
            sqlArgs = "SELECT * FROM " + Main.names[1] + " ORDER BY date DESC";
        } else {
            sqlArgs = "SELECT * FROM " + Main.names[1] + " WHERE validation_active='" + searchParameter + "' OR validation_passive='" + searchParameter + "' ORDER BY date DESC";
        }

        try {
            Main.createWealth(connection);

            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONArray ja = new JSONArray();

            while (rs.next()) {
                ja.put(new JSONObject()
                        .put("id", rs.getInt("id"))
                        .put("date", rs.getTimestamp("date"))
                        .put("validation_active", rs.getString("validation_active"))
                        .put("name_active", rs.getString("name_active"))
                        .put("validation_passive", rs.getString("validation_passive"))
                        .put("name_passive", rs.getString("name_passive"))
                        .put("money", rs.getInt("money")));
            }

            if(ja.isEmpty()) {
                return JRepCrafter.cancelOperation(response, 404, "Specified user has no transactions");
            }

            rs.close();
            preparedStatement.close();
            SkyLogger.log("Fetched transaction history for " + (searchParameter.isBlank() ? "global" : searchParameter));
            return JRepCrafter.cancelOperation(response, 200, null).put("transactions", ja);
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing user list");
        }
    }
}
