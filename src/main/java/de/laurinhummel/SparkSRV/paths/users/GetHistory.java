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

        JSONObject user = handler.getUserData(validation, request, response).getJSONObject("user");
        if(user.getInt("priority") == 0) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "User not found!");


        if(validation != null) sqlArgs.append(" WHERE taker_validation='").append(validation).append("' OR giver_validation='").append(validation).append("'");
        sqlArgs.append(" ORDER BY date DESC");
        if(amount != null) sqlArgs.append(" LIMIT ").append(amount);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs.toString());
            ResultSet rs = preparedStatement.executeQuery();

            JSONArray ja = new JSONArray();

            while (rs.next()) {
                ja.put(new JSONObject()
                        .put("id", rs.getInt("id"))
                        .put("date", rs.getTimestamp("date"))
                        .put("taker_validation", rs.getString("taker_validation"))
                        .put("taker_name", rs.getObject("taker_name"))
                        .put("giver_validation", rs.getString("giver_validation"))
                        .put("giver_name", rs.getObject("giver_name"))
                        .put("message", rs.getString("message"))
                        .put("money", rs.getFloat(switch (user.getInt("priority")) {
                            case 1 -> "money";
                            default -> "money_taxed";
                        })));
            }

            if(ja.isEmpty()) {
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Specified user has no transactions");
            }

            rs.close();
            preparedStatement.close();
            SkyLogger.log("Fetched transaction history for " + (validation == null ? "global" : validation));
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, null).put("transactions", ja).put("bank", user.getFloat("money"));
        } catch (SQLException e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Error while parsing user list");
        }
    }
}
