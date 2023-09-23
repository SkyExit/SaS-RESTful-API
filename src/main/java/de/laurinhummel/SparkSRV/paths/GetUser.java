package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetUser implements Route {
    MySQLConnectionHandler handler;
    public GetUser(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String auth = request.headers("Authentication");
        if(auth == null || !auth.equals(Main.APITOKEN)) {
            return JRepCrafter.cancelOperation(response, 401, "Invalid or missing API-Key");
        }

        response.type("application/json");
        String validationID;

        try {
            validationID = request.params(":validation");
        } catch (Exception ex) {
            response.status(500);
            ex.printStackTrace();
            return "Error while parsing JSON - GetUser";
        }

        String sqlArgs = "SELECT * FROM `logbuchv2` WHERE `validation`='" + validationID + "'";

        try {
            Connection connection = handler.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONObject jo = new JSONObject();
                jo.put("status", response.status());
            JSONObject ja = new JSONObject();

            if(!rs.next()) {
                response.status(404);
                return new JSONObject().put("response", response.status());
            } else {
                ja.put("id", rs.getInt("id"));
                ja.put("validation", rs.getString("validation"));
                ja.put("name", rs.getString("name"));
                ja.put("money", rs.getInt("money"));
                ja.put("priority", rs.getInt("priority"));
            }

            jo.put("user", ja);

            rs.close();
            preparedStatement.close();
            return jo;
        } catch (SQLException e) {
            e.printStackTrace();
            response.status(500);
            return "Error in getUserList - Main function";
        }
    }
}
