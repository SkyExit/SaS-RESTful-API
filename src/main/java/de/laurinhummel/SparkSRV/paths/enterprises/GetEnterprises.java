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

        String searchParameter = request.params(":validation") == null ? "" : request.params(":validation");
        String sqlArgs;
        int priority = 0;

        if(searchParameter.isBlank()) {
            sqlArgs = "SELECT * FROM " + Main.names[2] + " ORDER BY id DESC";
        } else {
            try {
                JSONObject userData = handler.getUserData(searchParameter, request, response);
                priority = userData.getJSONObject("user").getInt("priority");
            } catch (Exception ex) {
                return JRepCrafter.cancelOperation(response, 500, "Database is empty");
            }

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
                case 0 -> {
                    //MAP -> Enterprise + (Employee, Employed)
                    Map<String, Map<String, Boolean>> map = new HashMap<String, Map<String, Boolean>>();

                    while(rs.next()) {
                        try {
                            if(map.containsKey(rs.getString("validation_enterprise"))) {
                                map.get(rs.getString("validation_enterprise")).put(rs.getString("validation_employee"), rs.getBoolean("employed"));
                            } else {
                                Map<String, Boolean> innerMap = new HashMap<String, Boolean>();
                                innerMap.put(rs.getString("validation_employee"), rs.getBoolean("employed"));
                                map.put(rs.getString("validation_enterprise"), innerMap);
                            }
                        } catch (Exception ex) {
                            SkyLogger.logStack(ex);
                        }
                    }

                    for (Map.Entry<String, Map<String, Boolean>> entry : map.entrySet()) {
                        Map<String, Boolean> employees = entry.getValue();

                        JSONArray array = new JSONArray();
                        for(Map.Entry<String, Boolean> member : employees.entrySet()) {
                            array.put(new JSONObject()
                                    .put("name", member.getKey())
                                    .put("employed", member.getValue()));
                        }

                        ja.put(new JSONObject().put("enterprise", entry.getKey())
                                .put("members", array));
                    }
                }
                case 1 -> {
                    while (rs.next()) {
                        ja.put(new JSONObject()
                                .put("name", rs.getString("validation_enterprise"))
                                .put("employed", rs.getBoolean("employed")));
                    }
                }
                case 2,3 -> {
                    while (rs.next()) {
                        ja.put(new JSONObject()
                                .put("name", rs.getString("validation_employee"))
                                .put("employed", rs.getBoolean("employed")));
                    }
                }
            }

            if(ja.isEmpty()) {
                return JRepCrafter.cancelOperation(response, 404, "Specified " + ((priority == 1) ? "user is not employed" : "enterprise has no employees"));
            }

            rs.close();
            preparedStatement.close();
            SkyLogger.log(SkyLogger.Level.INFO, "Fetched enterprise register for " + (searchParameter.isBlank() ? "global" : searchParameter));

            return JRepCrafter.successOperation(response, 200).put("data", ja).put("priority", priority);
        } catch (Exception e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing user list");
        }
    }
}