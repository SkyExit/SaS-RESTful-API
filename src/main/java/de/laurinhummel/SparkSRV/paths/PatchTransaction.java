package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.USRObjectV2;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
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
        response.type("application/json");
        Connection connection = handler.getConnection();
        Main.createBlockchain(connection);

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
            ex.printStackTrace();
            return "Error while parsing JSON - GetUser";
        }

        //FETCH BOTH USERS AND CHECK VALIDITY
        try {
            String sqlArgs = "SELECT * FROM `logbuchv2` WHERE `validation`='" + validationActive + "' OR `validation`='" + validationPassive + "' ORDER BY `priority` DESC";
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlArgs);

            rs.last();
            if(rs.getRow() != 2) {
                response.status(404);
                return new JSONObject().put("response", response.status())
                        .put("status", "One or more users not found");
            }

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
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.status(500);
            return new JSONObject().put("response", response.status())
                    .put("status", "There was an error parsing your command");
        }

        //MONEY VALIDITY CHECKER
        if(passive.getMoney() >= money) {
            System.out.println("ac: " + active.getMoney() + "   pas: " + passive.getMoney());
            passive.setMoney(passive.getMoney() - money);
            active.setMoney(active.getMoney() + money);
            System.out.println("ac: " + active.getMoney() + "   pas: " + passive.getMoney());
        } else {
            response.status(403);
            return new JSONObject().put("response", response.status())
                    .put("status", "He doesn't have enough money for this transaction");
        }

        //UPDATE MONEY ON DATABASE
        try {
            connection.prepareStatement("UPDATE `logbuchv2` SET `money`='" + active.getMoney() + "' WHERE `validation`='" + active.getValidation() + "'").execute();
            connection.prepareStatement("UPDATE `logbuchv2` SET `money`='" + passive.getMoney() + "' WHERE `validation`='" + passive.getValidation() + "'").execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.status(500);
            return new JSONObject().put("response", response.status())
                    .put("status", "There was an error while pushing back to database");
        }

        //INSERTING DATA INTO BLOCKCHAIN
        /*
        try {
            String query = "insert into blockchainv1 (validation_active, name_active, money, priority)"
                    + " values (?, ?, ?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, val);
                preparedStmt.setString (2, request.queryParams("name"));
                preparedStmt.setString (3, "0");
                preparedStmt.setString (4, "1");

            preparedStmt.execute();
            response.status(201); // 201 Created

            Logger.getGlobal().log(Level.INFO, "USR created: " + request.queryParams("name") + "(p1) - " + val);
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.status(500);
            return new JSONObject().put("response", response.status())
                    .put("status", "There was an error while pushing back to database");
        }
        -
         */

        response.status(200);
        return new JSONObject().put("response", response.status())
                .put("status", "Updating balances was a success");
    }

    private USRObjectV2 putDATA(ResultSet rs) throws SQLException {
        return new USRObjectV2(rs.getInt("id"), rs.getString("validation"), rs.getString("name"),
                rs.getInt("money"), rs.getInt("priority"));
    }
}
