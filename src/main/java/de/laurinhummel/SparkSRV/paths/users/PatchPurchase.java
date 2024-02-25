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
import java.text.DecimalFormat;

public class PatchPurchase implements Route {
    MySQLConnectionHandler handler;
    public PatchPurchase(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        String message;
        DecimalFormat dfZero = new DecimalFormat("0.00");

        //JSON REQUEST BODY VALIDATOR
        JSONObject body = JRepCrafter.getRequestBody(request, response);
        if(!body.has("enterprise") || body.getString("enterprise").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a 'enterprise validation ID'");
        if(!body.has("customer") || body.getString("customer").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a 'customer validation ID'");
        if(!body.has("money") || !(body.getFloat("money") > 0)) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a transaction greater than 0");
        if(!body.has("message") || body.getString("message").isBlank()) message = null; else message = body.getString("message");

        //FETCH BOTH USERS AND CHECK VALIDITY
        JSONObject enterprise = handler.getUserData(body.getString("enterprise"), request, response).getJSONObject("user");
        JSONObject customer = handler.getUserData(body.getString("customer"), request, response).getJSONObject("user");

        if(enterprise.getInt("priority") != 2) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Enterprise doesn't exist");
        if(customer.getInt("priority") != 1) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Customer doesn't exist");

        //MONEY VALIDITY CHECKER
        float cusMoney = customer.getFloat("money");
        float price = Float.parseFloat(dfZero.format(body.getFloat("money")).replace(',', '.'));

        if(cusMoney >= price) {
            cusMoney = cusMoney - price;
        } else {
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.FORBIDDEN, "Not enough money for this payment");
        }

        //UPDATE MONEY ON DATABASE
        try {
            connection.prepareStatement("UPDATE `" + Main.names[0] + "` SET `money`=money+" + price + " WHERE `validation`='" + body.getString("enterprise") + "'").execute();
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
                preparedStmt.setString(1, body.getString("enterprise"));
                preparedStmt.setString(2, enterprise.getString("name"));
                preparedStmt.setString (3, customer.getString("validation"));
                preparedStmt.setString(4, message);
                preparedStmt.setFloat (5, price);

            preparedStmt.execute();

            SkyLogger.log("'" + customer.getString("validation") + "' purchased something from '" + body.getString("enterprise") + "' (" + enterprise.getString("name") + ") for " + price + "$ (" + message + ")");


        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "There was an error while pushing data back to database");
        }

        return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, "Performing transaction was a success");
    }
}
