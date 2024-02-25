package de.laurinhummel.SparkSRV.paths.enterprises;

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
import java.util.*;

public class GetEnterprises implements Route {
    MySQLConnectionHandler handler;
    public GetEnterprises(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        if(request.params(":validation").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You need to specify an enterprise or a user");

        String searchParameter = request.params(":validation");
        String sqlArgs;
        int priority = 0;

        if(searchParameter.isBlank()) {
            sqlArgs = "SELECT * FROM " + Main.names[2] + " ORDER BY id DESC";
        } else {
            priority = handler.getUserData(searchParameter, request, response).getJSONObject("user").getInt("priority");
            if(priority <= 0) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "User not found");

            sqlArgs = switch (priority) {
                case 1 -> "SELECT * FROM " + Main.names[2] + " WHERE validation_employee='" + searchParameter + "' ORDER BY id DESC";
                case 2,3 -> "SELECT * FROM " + Main.names[2] + " WHERE validation_enterprise='" + searchParameter + "' ORDER BY id DESC";
                default -> null;
            };
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONArray ja = new JSONArray();
            switch (priority) {
                case 1 -> {
                    while (rs.next()) {
                        ja.put(new JSONObject()
                                .put("validation", rs.getString("validation_enterprise"))
                                .put("salary", rs.getInt("salary"))
                                .put("name_enterprise", rs.getString("name_enterprise")));
                    }
                }
                case 2,3 -> {
                    while (rs.next()) {
                        ja.put(new JSONObject()
                                .put("validation", rs.getString("validation_employee"))
                                .put("salary", rs.getInt("salary"))
                                .put("name_enterprise", rs.getString("name_enterprise")));
                    }
                }
            }

            if(ja.isEmpty()) {
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Specified " + ((priority == 1) ? "user is not employed" : "enterprise has no employees"));
            }

            rs.close();
            preparedStatement.close();
            SkyLogger.log(SkyLogger.Level.INFO, "Fetched enterprise register for " + (searchParameter.isBlank() ? "global" : searchParameter));

            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, null).put("data", ja).put("priority", priority);
        } catch (Exception e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Error while parsing user list");
        }
    }
}