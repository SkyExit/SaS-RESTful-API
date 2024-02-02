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
        StringBuilder sqlArgs = new StringBuilder().append("SELECT * FROM ").append(Main.names[1]);
        String validation;
        Integer amount;

        try {
            validation = (request.params(":validation") == null || request.params(":validation").isBlank()) ? null : request.params(":validation");
            amount = (request.params(":amount") == null || request.params(":amount").isBlank()) ? null : Integer.parseInt(request.params(":amount"));
        } catch (Exception ex) { return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Malformed request"); }

        if(validation != null) sqlArgs.append(" WHERE enterprise_validation='").append(validation).append("' OR customer_validation='").append(validation).append("'");
        sqlArgs.append(" ORDER BY date DESC");
        if(amount != null) sqlArgs.append(" LIMIT ").append(amount);

        try {
            Main.createWealth(connection);

            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs.toString());
            ResultSet rs = preparedStatement.executeQuery();

            JSONArray ja = new JSONArray();

            while (rs.next()) {
                ja.put(new JSONObject()
                        .put("id", rs.getInt("id"))
                        .put("date", rs.getTimestamp("date"))
                        .put("enterprise_validation", rs.getString("enterprise_validation"))
                        .put("enterprise_name", rs.getString("enterprise_name"))
                        .put("customer_validation", rs.getString("customer_validation"))
                        .put("message", rs.getString("message"))
                        .put("money", rs.getInt("money")));
            }

            if(ja.isEmpty()) {
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Specified user has no transactions");
            }

            rs.close();
            preparedStatement.close();
            SkyLogger.log("Fetched transaction history for " + (validation == null ? "global" : validation));
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, null).put("transactions", ja);
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Error while parsing user list");
        }
    }
}
