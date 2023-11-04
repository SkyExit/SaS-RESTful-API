package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import spark.Request;
import spark.Response;
import spark.Route;

public class GetTest implements Route {
    MySQLConnectionHandler handler;
    public GetTest(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        return "null 123";
    }
}
