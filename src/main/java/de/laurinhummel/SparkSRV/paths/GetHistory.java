package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GetHistory implements Route {
    MySQLConnectionHandler handler;
    public GetHistory(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String auth = request.headers("Authentication");
        if(auth == null || !auth.equals(Main.APITOKEN)) {
            return JRepCrafter.cancelOperation(response, 401, "Invalid or missing API-Key");
        }

        Connection connection = handler.getConnection();
        Main.createTransactions(connection);

        String validationID;
        try {
            validationID = request.params(":validation");
        } catch (Exception ex) {
            ex.printStackTrace();
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing parameter");
        }



        return null;
    }
}
