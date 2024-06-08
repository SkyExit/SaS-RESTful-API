package de.laurinhummel.SparkSRV.paths.users;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.*;
import java.text.DecimalFormat;

public class PatchPayment implements Route {
    MySQLConnectionHandler handler;
    public PatchPayment(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        String message;
        DecimalFormat dfZero = new DecimalFormat("0.00");

        //JSON REQUEST BODY VALIDATOR
        JSONObject body = JRepCrafter.getRequestBody(request, response);
        if(!body.has("taker") || body.getString("taker").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a 'takers validation ID'");
        if(!body.has("giver") || body.getString("giver").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a 'givers validation ID'");
        if(!body.has("money") || !(body.getFloat("money") > 0)) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You must provide a transaction greater than 0");
        if(!body.has("message") || body.getString("message").isBlank()) message = null; else message = body.getString("message");

        //FETCH BOTH USERS AND CHECK VALIDITY
        JSONObject taker = handler.getUserData(body.getString("taker"), request, response).getJSONObject("user");
        JSONObject giver = handler.getUserData(body.getString("giver"), request, response).getJSONObject("user");

        if(taker.getInt("priority") == 0) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Taker doesn't exist");
        if(giver.getInt("priority") == 0) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Giver doesn't exist");



        //MONEY VALIDITY CHECKER
        float giverMoney = giver.getFloat("money");
        float sum = Float.parseFloat(dfZero.format(body.getFloat("money")).replace(',', '.'));

        if(giverMoney >= sum) {
            giverMoney = giverMoney - sum;
        } else {
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.FORBIDDEN, "Not enough money for this payment");
        }

        //UPDATE MONEY ON DATABASE
        try {
            connection.prepareStatement("UPDATE `" + Main.names[0] + "` SET `money`=money+" + sum + " WHERE `validation`='" + body.getString("taker") + "'").execute();
            connection.prepareStatement("UPDATE `" + Main.names[0] + "` SET `money`='" + giverMoney + "' WHERE `validation`='" + body.getString("giver") + "'").execute();
        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "There was an error while pushing data back to database");
        }

        //INSERTING DATA INTO HISTORY
        try {
            String query = "insert into " + Main.names[1] + " (taker_validation, taker_name, giver_validation, giver_name, message, money_taxed, money)"
                    + " values (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, body.getString("taker"));
            preparedStmt.setString(2, taker.get("name").equals(JSONObject.NULL) ? null : taker.getString("name"));
            preparedStmt.setString (3, body.getString("giver"));
            preparedStmt.setString(4, giver.get("name").equals(JSONObject.NULL) ? null : giver.getString("name"));
            preparedStmt.setString(5, message);
            preparedStmt.setFloat (6, sum);
            preparedStmt.setFloat (7, sum);

            preparedStmt.execute();

            SkyLogger.log("'" + giver.getString("validation") + "' gave " + sum + "$ to '" + taker.get("name") + " (" + message + ")");


        } catch (Exception ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "There was an error while pushing data back to database");
        }
        return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, "Performing transaction was a success");
    }
}
