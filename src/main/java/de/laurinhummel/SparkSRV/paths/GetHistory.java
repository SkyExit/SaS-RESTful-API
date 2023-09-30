package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
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
        String auth = request.headers("Authentication");
        if(auth == null || !auth.equals(Main.APITOKEN)) {
            return JRepCrafter.cancelOperation(response, 401, "Invalid or missing API-Key");
        }

        Connection connection = handler.getConnection();
        Main.createTransactions(connection);

        String searchParameter = request.params(":validation") == null ? "" : request.params(":validation");
        String sqlArgs = "";

        if(searchParameter.isBlank()) {
            sqlArgs = "SELECT * FROM sas_transactions_v2 ORDER BY date DESC";
        } else {
            sqlArgs = "SELECT * FROM sas_transactions_v2 WHERE validation_active='" + searchParameter + "' OR validation_passive='" + searchParameter + "' ORDER BY date DESC";
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
            return JRepCrafter.successOperation(response, 200).put("transactions", ja);
        } catch (SQLException e) {
            e.printStackTrace();
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing user list");
        }
    }
}
