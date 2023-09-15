package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
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
    public Object handle(Request request, Response response) throws Exception {
        String sqlArgs = "SELECT * FROM `logbuchv1` ORDER BY id ASC";
        try {
            Connection connection = handler.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONObject jo = new JSONObject();
                jo.put("status", response.status());
            JSONArray ja = new JSONArray();

            while (rs.next()) {
                ja.put(new JSONObject()
                        .put("id", rs.getInt("id"))
                        .put("name", rs.getString("name"))
                        .put("money", rs.getInt("money")));
            }
            jo.put("users", ja);


            rs.close();
            preparedStatement.close();
            return jo;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error in getUserList - Main function";
        }
    }
}
