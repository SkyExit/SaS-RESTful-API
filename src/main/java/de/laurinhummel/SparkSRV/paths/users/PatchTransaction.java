package de.laurinhummel.SparkSRV.paths.users;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.USRObjectV2;
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

public class PatchTransaction implements Route {
    MySQLConnectionHandler handler;
    public PatchTransaction(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        Connection connection = handler.getConnection();

        String validationActive;
        String validationPassive;
        int money;

        USRObjectV2 active;
        USRObjectV2 passive;

        //JSON REQUEST BODY VALIDATOR
        try {
            JSONObject body = new JSONObject(request.body());
            validationActive = body.getString("validation_active");
            validationPassive = body.getString("validation_passive");
            money = body.getInt("money");
        } catch (JSONException ex) {
            response.status(500);
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing JSON body");
        }

        //FETCH BOTH USERS AND CHECK VALIDITY
        try {
            String sqlArgs = "SELECT * FROM `" + Main.names[0] + "` WHERE `validation`='" + validationActive + "' OR `validation`='" + validationPassive + "' ORDER BY `priority` DESC";
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlArgs);

            if(rs.isBeforeFirst()) { rs.next(); }
            if(rs.getString("validation").equals(validationActive)) {
                active = putDATA(rs);
                if(!rs.next()) { return JRepCrafter.cancelOperation(response, 404, "Specified user(s) not found"); }
                passive = putDATA(rs);
            } else {
                passive = putDATA(rs);
                if(!rs.next()) { return JRepCrafter.cancelOperation(response, 404, "Specified user(s) not found"); }
                active = putDATA(rs);
            }
            if(rs.next()) { return JRepCrafter.cancelOperation(response, 406, "Too many users found in this request"); }

            if(active.getPriority() <= passive.getPriority()) {
                return JRepCrafter.cancelOperation(response, 401, "You don't have permissions to perform this transaction");
            }

            rs.close();
            statement.close();
        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "There was an error while parsing multiple users data");
        }

        //MONEY VALIDITY CHECKER
        if(passive.getMoney() >= money) {
            passive.setMoney(passive.getMoney() - money);
            active.setMoney(active.getMoney() + money);
        } else {
            return JRepCrafter.cancelOperation(response, 403, "He doesn't have enough money for this purchase");
        }

        //UPDATE MONEY ON DATABASE
        try {
            connection.prepareStatement("UPDATE `" + Main.names[0] + "` SET `money`='" + active.getMoney() + "' WHERE `validation`='" + active.getValidation() + "'").execute();
            connection.prepareStatement("UPDATE `" + Main.names[0] + "` SET `money`='" + passive.getMoney() + "' WHERE `validation`='" + passive.getValidation() + "'").execute();
        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "There was an error while pushing data back to database");
        }

        //INSERTING DATA INTO HISTORY
        try {
            String query = "insert into " + Main.names[1] + " (validation_active, name_active, validation_passive, name_passive, money)"
                    + " values (?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, active.getValidation());
                preparedStmt.setString(2, active.getName());
                preparedStmt.setString (3, passive.getValidation());
                preparedStmt.setString(4, passive.getName());
                preparedStmt.setString (5, String.valueOf(money));

            preparedStmt.execute();

            if(money > 0) {
                SkyLogger.log("'" + passive.getName() + "' moved " + money + "€ to '" + active.getName() + "'");
            } else {
                SkyLogger.log("'" + active.getName() + "' moved " + (money*(-1)) + "€ to '" + passive.getName() + "'");
            }


        } catch (SQLException ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "There was an error while pushing data back to database");
        }

        return JRepCrafter.cancelOperation(response, 200, "Performing transaction was a success");
    }

    private USRObjectV2 putDATA(ResultSet rs) throws SQLException {
        return new USRObjectV2(rs.getInt("id"), rs.getString("validation"), rs.getString("name"),
                rs.getInt("money"), rs.getInt("priority"));
    }
}
