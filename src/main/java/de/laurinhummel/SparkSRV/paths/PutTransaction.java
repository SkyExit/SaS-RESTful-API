package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.USRObjectV2;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PutTransaction implements Route {
    MySQLConnectionHandler handler;
    public PutTransaction(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        Connection connection = handler.getConnection();
        Main.createBlockchain(connection);

        String validationActive;
        String validationPassive;
        int money;

        try {
            JSONObject body = new JSONObject(request.body());
            validationActive = body.getString("validation_active");
            validationPassive = body.getString("validation_passive");
            money = body.getInt("money");
        } catch (JSONException ex) {
            response.status(500);
            ex.printStackTrace();
            System.out.println("err");
            return "Error while parsing JSON - GetUser";
        }

        try {
            String sqlArgs = "SELECT * FROM `logbuchv2` WHERE `validation`='" + validationActive + "' OR `validation`='" + validationPassive + "' ORDER BY `priority` DESC";
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlArgs);

            JSONObject jo = new JSONObject();
                jo.put("status", response.status());
            JSONArray ja = new JSONArray();

            rs.last();
            if(rs.getRow() != 2) {
                response.status(404);
                return new JSONObject().put("response", response.status())
                        .put("status", "One or more users not found");
            }

            USRObjectV2 active;
            USRObjectV2 passive;

            rs.first();
            if(rs.getString("validation").equals(validationActive)) {
                active = putDATA(rs);
                rs.next();
                passive = putDATA(rs);
            } else {
                passive = putDATA(rs);
                rs.next();
                active = putDATA(rs);
            }

            System.out.println(active.getPriority() + " " + passive.getPriority());
            if(active.getPriority() <= passive.getPriority()) {
                response.status(401);
                return new JSONObject().put("response", response.status())
                        .put("status", "You don't have the permission to execute this transaction");
            }

            rs.close();
            statement.close();
            return jo;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "null";
    }

    private USRObjectV2 putDATA(ResultSet rs) throws SQLException {
        return new USRObjectV2(rs.getInt("id"), rs.getString("validation"), rs.getString("name"),
                rs.getInt("money"), rs.getInt("priority"));
    }
}
