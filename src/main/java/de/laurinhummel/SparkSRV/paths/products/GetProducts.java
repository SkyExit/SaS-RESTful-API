package de.laurinhummel.SparkSRV.paths.products;

import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;

public class GetProducts implements Route {
    MySQLConnectionHandler handler;
    public GetProducts(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (SessionValidationHandler.validate(request)) {
            return SessionValidationHandler.correct(response);
        }

        Connection connection = handler.getConnection();

        return true;
    }
}
