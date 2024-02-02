package de.laurinhummel.SparkSRV.paths.users;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import de.laurinhummel.SparkSRV.paths.PostLogin;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.*;

public class PatchPurchase implements Route {
    MySQLConnectionHandler handler;
    public PatchPurchase(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        String message;

        //JSON REQUEST BODY VALIDATOR
        JSONObject body = JRepCrafter.getRequestBody(request, response);
        if(!body.has("enterprise") || body.getString("enterprise").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a 'enterprise validation ID'");
        if(!body.has("customer") || body.getString("customer").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a 'customer validation ID'");
        if(!body.has("money") || !(body.getInt("money") > 0)) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a transaction greater than 0");
        if(!body.has("password") || body.getString("password").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You need to provide a password for the transaction");
        if(!body.has("message") || body.getString("message").isBlank()) message = null; else message = body.getString("message");

        if(PostLogin.getLoginStatus(connection, body.getString("customer"), body.getString("password")) < 0) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.FORBIDDEN, "Password is incorrect");

        //FETCH BOTH USERS AND CHECK VALIDITY
        JSONObject enterprise = handler.getUserData(body.getString("enterprise"), request, response).getJSONObject("user");
        JSONObject customer = handler.getUserData(body.getString("customer"), request, response).getJSONObject("user");

        if(enterprise.getInt("priority") != 2) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Enterprise doesn't exist");
        if(customer.getInt("priority") != 1) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Customer doesn't exist");

        //MONEY VALIDITY CHECKER
        int entMoney = enterprise.getInt("money");
        int cusMoney = customer.getInt("money");
        int sum = body.getInt("money");

        if(cusMoney >= sum) {
            cusMoney = cusMoney - sum;
            entMoney = entMoney + sum;
        } else {
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.FORBIDDEN, "Not enough money for this payment");
        }

        //UPDATE MONEY ON DATABASE
        try {
            connection.prepareStatement("UPDATE `" + Main.names[0] + "` SET `money`='" + entMoney + "' WHERE `validation`='" + body.getString("enterprise") + "'").execute();
            connection.prepareStatement("UPDATE `" + Main.names[0] + "` SET `money`='" + cusMoney + "' WHERE `validation`='" + body.getString("customer") + "'").execute();
        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "There was an error while pushing data back to database");
        }

        //INSERTING DATA INTO HISTORY
        try {
            String query = "insert into " + Main.names[1] + " (enterprise_validation, enterprise_name, customer_validation, message, money)"
                    + " values (?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, enterprise.getString("validation"));
                preparedStmt.setString(2, enterprise.getString("name"));
                preparedStmt.setString (3, customer.getString("validation"));
                preparedStmt.setString(4, message);
                preparedStmt.setInt (5, sum);

            preparedStmt.execute();

            SkyLogger.log("'" + customer.getString("validation") + "' purchased something from '" + enterprise.getString("name") + "' for " + sum + "$ (" + message + ")");


        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "There was an error while pushing data back to database");
        }

        return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, "Performing transaction was a success");
    }
}
