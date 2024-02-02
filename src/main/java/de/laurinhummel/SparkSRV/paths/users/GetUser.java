package de.laurinhummel.SparkSRV.paths.users;

import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import spark.Request;
import spark.Response;
import spark.Route;

public class GetUser implements Route {
    MySQLConnectionHandler handler;
    public GetUser(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        String validationID;
        try {
            validationID = request.params(":validation");
        } catch (Exception ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Error while parsing parameter");
        }

        return handler.getUserData(validationID, request, response);
    }
}
