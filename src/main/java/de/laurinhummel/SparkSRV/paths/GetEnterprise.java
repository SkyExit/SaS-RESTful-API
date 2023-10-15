package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;

public class GetEnterprise implements Route {
    MySQLConnectionHandler handler;
    public GetEnterprise(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String auth = request.headers("Authentication");
        if(auth == null || !auth.equals(Main.APIKEY)) {
            return JRepCrafter.cancelOperation(response, 401, "Invalid or missing API-Key");
        }

        Connection connection = handler.getConnection();

        String searchParameter = request.params(":validation") == null ? "" : request.params(":validation");
        String sqlArgs;
        int priority = 0;

        if(searchParameter.isBlank()) {
            sqlArgs = "SELECT * FROM " + Main.names[2] + " ORDER BY id DESC";
        } else {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + Spark.port() + "/users/" + searchParameter).openConnection();
                urlConnection.setRequestProperty("Authentication", Main.APIKEY);
                if(urlConnection.getResponseCode() != 200) { return JRepCrafter.cancelOperation(response, 404, "Specified user not found"); }
                InputStream inputStream = urlConnection.getInputStream();
                JSONObject userData = new JSONObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                priority = userData.getJSONObject("user").getInt("priority");
            } catch (Exception ex) {
                SkyLogger.logStack(ex);
                return JRepCrafter.cancelOperation(response, 500, "Error while parsing userdata");
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
                        SkyLogger.log(Level.INFO, rs.getString("validation_employee"));
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

                        ja.put(new JSONObject().put("name", entry.getKey())
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
            SkyLogger.log(Level.INFO, "Fetched enterprise register for " + (searchParameter.isBlank() ? "global" : searchParameter));

            return JRepCrafter.successOperation(response, 200).put("data", ja).put("priority", priority);
        } catch (Exception e) {
            SkyLogger.logStack(e);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing user list");
        }
    }
}
