package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;
import java.sql.*;

public class PatchEmployee implements Route {
    MySQLConnectionHandler handler;
    public PatchEmployee(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        String validationEnterprise;
        String validationEmployee;

        //JSON REQUEST BODY VALIDATOR
        try {
            JSONObject body = new JSONObject(request.body());
            validationEnterprise = body.getString("enterprise");
            validationEmployee = body.getString("employee");
        } catch (JSONException ex) {
            response.status(500);
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing JSON body");
        }

        //FETCH USER
        try {
            JSONObject employee = handler.getUserData(validationEmployee, request, response);
            if(employee.getInt("status") != 200) {
                return JRepCrafter.cancelOperation(response, 404, "Specified user not found");
            }
        } catch (Exception ex) {
            SkyLogger.logStack(ex);
            return null;
        }


        //PATCH USER
        try {
            String sqlArgs = "SELECT * FROM `" + Main.names[2] + "` WHERE `validation_enterprise`='" + validationEnterprise + "' AND `validation_employee`='" + validationEmployee + "';";
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlArgs);

            if(!rs.isBeforeFirst()) {
                //User is new
                String query = "insert into " + Main.names[2] + " (validation_enterprise, validation_employee, employed)"
                        + " values (?, ?, ?)";

                PreparedStatement preparedStmt = connection.prepareStatement(query);
                    preparedStmt.setString(1, validationEnterprise);
                    preparedStmt.setString(2, validationEmployee);
                    preparedStmt.setBoolean(3, true);

                preparedStmt.execute();
            } else {
                //edit user status
                if(rs.getBoolean("employed")) {
                    connection.prepareStatement("UPDATE `" + Main.names[2] + "` SET `employed`='" + 0 + "' WHERE `validation_enterprise`='" + validationEnterprise +
                            "' AND `validation_employee`='" + validationEmployee + "'").execute();
                } else {
                    connection.prepareStatement("UPDATE `" + Main.names[2] + "` SET `employed`='" + 1 + "' WHERE `validation_enterprise`='" + validationEnterprise +
                            "' AND `validation_employee`='" + validationEmployee + "'").execute();
                }
            }

            rs.close();
            statement.close();
        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "There was an error while parsing user data");
        }

        SkyLogger.log(validationEmployee + " changed status at " + validationEnterprise);
        return JRepCrafter.cancelOperation(response, 200, "Performing transaction was a success");
    }
}
