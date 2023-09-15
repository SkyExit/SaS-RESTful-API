package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
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
        String sqlArgs = "SELECT * FROM `logbuchv1` WHERE id=" + request.params(":id");

        try {
            Connection connection = handler.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
            ResultSet rs = preparedStatement.executeQuery();

            JSONObject jo = new JSONObject();
                jo.put("status", response.status());
            JSONObject ja = new JSONObject();

            if(!rs.next()) {
                response.status(404);
                return  new JSONObject().put("response", response.status());
            } else {
                ja.put("id", rs.getInt("id"));
                ja.put("name", rs.getString("name"));
                ja.put("money", rs.getInt("money"));
            }

            jo.put("user", ja);

            rs.close();
            preparedStatement.close();
            return jo;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error in getUserList - Main function";
        }
    }
}
