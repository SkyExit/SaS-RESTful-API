package de.laurinhummel.SparkSRV.paths.users;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;

public class PostCreate implements Route {
    MySQLConnectionHandler handler;
    public PostCreate(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        try {
            Connection connection = handler.getConnection();

            boolean taxed = true;
            DecimalFormat dfZero = new DecimalFormat("0.00");
            float sum = 0.00f;

            //JSON BODY HANDLER
            JSONObject body = JRepCrafter.getRequestBody(request, response);
            if(response.status() != 200) return body;

            try {
                if(!body.has("priority")) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You need to set a priority, e.g. 1 or 2");
                if(body.getInt("priority") == 2) {
                    if(!body.has("name") || body.getString("name").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Enterprises must have a name");
                    if(!body.has("owner") || body.getString("owner").isBlank()) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Enterprises must have an owner");
                    if(handler.getUserData(body.getString("owner"), request, response).getJSONObject("user").getInt("priority") != 1) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Enterprises must be owner by a valid regular user (no enterprise)");
                    if(body.has("taxed") && !body.getBoolean("taxed")) taxed = false;
                    if(body.has("money") && body.getFloat("money") > 0.0f) sum = Float.parseFloat(dfZero.format(body.getFloat("money")).replace(',', '.'));
                } else if(!(body.getInt("priority") == 1)){
                    return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Priority must be 1 or 2");
                } else { taxed = false; }
            } catch (Exception ex) {
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Malformed json body");
            }

            String val = RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());
            String password = "PWD-" + RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());

            while (handler.getUserData(val, request, response).getInt("status") == 200) {
                val = RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());
            }

            boolean isEnterprise = body.getInt("priority") == 2;
            val = (isEnterprise ? "ENT" : "USR") + "-" + val;

            String owner = (body.getInt("priority") == 2) ? ((body.has("owner") && !body.getString("owner").isBlank()) ? body.getString("owner") : null) : null;

            PreparedStatement preparedStmt = connection.prepareStatement("insert into " + Main.names[0] + " (validation, name, money, priority, owner, taxed) values (?, ?, ?, ?, ?, ?)");
            String name = isEnterprise ? body.getString("name") : null;
                preparedStmt.setString(1, val);
                preparedStmt.setString(2, name);
                preparedStmt.setFloat (3, sum);
                preparedStmt.setInt (4, isEnterprise ? 2 : 1);
                preparedStmt.setString(5, owner);
                preparedStmt.setBoolean(6, taxed);
            preparedStmt.execute();

            preparedStmt = connection.prepareStatement("insert into " + Main.names[4] + " (validationID, password, enabled) values (?, ?, ?)");
            preparedStmt.setString(1, val);
            preparedStmt.setString (2, password);
            preparedStmt.setBoolean(3, true);
            preparedStmt.execute();


            SkyLogger.log((name == null ? "User" : "Enterprise") + " created successfully");
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.CREATED, null).put("validationID", val).put("name", name)
                    .put("message", (name == null ? "User" : "Enterprise") + " created successfully").put("password", password)
                    .put("owner", owner).put("taxed", taxed).put("money", sum);
        } catch (Exception e) {
            System.err.println("Got an exception! - create");
            System.err.println(e.getMessage());
            SkyLogger.logStack(e);
            return "Got an exception!";
        }
    }
}
