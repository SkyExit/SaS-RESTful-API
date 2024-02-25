package de.laurinhummel.SparkSRV.paths.enterprises;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import de.laurinhummel.SparkSRV.paths.PostLogin;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;
import java.sql.*;
import java.text.DecimalFormat;

public class PatchEmployee implements Route {
    MySQLConnectionHandler handler;
    public PatchEmployee(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();
        DecimalFormat dfZero = new DecimalFormat("0.00");

        //JSON REQUEST BODY VALIDATOR
        JSONObject body = JRepCrafter.getRequestBody(request, response);
        if(!body.has("enterprise") || body.getString("enterprise").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a 'enterprise validation ID'");
        if(!body.has("employee") || body.getString("employee").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a 'employee validation ID'");
        if(!body.has("salary") || body.getFloat("salary") < 0) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a salary greater than 0");

        //VALIDATE USER
        JSONObject customer = handler.getUserData(body.getString("employee"), request, response).getJSONObject("user");
        JSONObject enterprise = handler.getUserData(body.getString("enterprise"), request, response).getJSONObject("user");

        if(customer.getInt("priority") != 1) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "User doesn't exist");
        if(enterprise.getInt("priority") != 2) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Enterprise doesn't exist");

        float salary = Float.parseFloat(dfZero.format(body.getFloat("salary")).replace(',', '.'));

        //PATCH USER
        try {
            String sqlArgs = "SELECT * FROM `" + Main.names[2] + "` WHERE `validation_enterprise`='" + body.getString("enterprise") + "' AND `validation_employee`='" + body.getString("employee") + "';";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sqlArgs);

            if(!rs.isBeforeFirst()) {
                //User is new
                String query = "insert into " + Main.names[2] + " (validation_enterprise, validation_employee, salary, name_enterprise)"
                        + " values (?, ?, ?, ?)";

                PreparedStatement preparedStmt = connection.prepareStatement(query);
                    preparedStmt.setString(1, body.getString("enterprise"));
                    preparedStmt.setString(2, body.getString("employee"));
                    preparedStmt.setFloat(3, salary);
                    preparedStmt.setString(4, enterprise.getString("name"));

                preparedStmt.execute();
            } else {
                //edit user status
                rs.next();

                if(rs.getFloat("salary") == salary) {
                    //KICK
                    connection.prepareStatement("DELETE FROM " + Main.names[2] + " WHERE `validation_enterprise`='" + body.getString("enterprise") + "' AND `validation_employee`='" + body.getString("employee") + "'").execute();
                } else {
                    //CHANGE SALARY
                    connection.prepareStatement("UPDATE `" + Main.names[2] + "` SET `salary`='" + salary + "' WHERE `validation_enterprise`='" + body.getString("enterprise") +
                            "' AND `validation_employee`='" + body.getString("employee") + "'").execute();
                }
            }

            rs.close();
            statement.close();
        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "There was an error while parsing user data");
        }

        SkyLogger.log(body.getString("employee") + " changed status at " + body.getString("enterprise"));
        return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, "Changing employment status was a success");
    }
}
