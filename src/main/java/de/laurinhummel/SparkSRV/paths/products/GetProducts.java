package de.laurinhummel.SparkSRV.paths.products;

import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
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

        String enterpriseID;
        String productID;
        try {
            enterpriseID = request.params(":enterprise");
            productID = request.params(":product");
        } catch (Exception ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing parameter");
        }



        return true;
    }
}
