package de.laurinhummel.SparkSRV.paths.products;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GetProducts implements Route {
    MySQLConnectionHandler handler;
    public GetProducts(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (SessionValidationHandler.validate(request)) {
            return SessionValidationHandler.correct(response);
        }

        Connection connection = handler.getConnection();

        String enterpriseID = null;
        String productID = null;
        try {
            enterpriseID = request.params(":enterprise");
            productID = request.params(":product");
        } catch (Exception ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, 500, "Error while parsing parameter");
        }

        if(productID == null) {
            //LIST ALL PRODUCTS OF ENTERPRISE

            String sqlArgs = "SELECT * FROM `" + Main.names[3] + "` ORDER BY id ASC";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlArgs);
                ResultSet rs = preparedStatement.executeQuery();

                JSONArray ja = new JSONArray();

                while (rs.next()) {
                    ja.put(new JSONObject()
                            .put("validation_enterprise", rs.getString("validation_enterprise"))
                            .put("validation_product", rs.getString("validation_product"))
                            .put("name_product", rs.getString("name_product"))
                            .put("price", rs.getInt("price")));
                }

                rs.close();
                preparedStatement.close();
                SkyLogger.log("User list fetched");
                return JRepCrafter.cancelOperation(response, 200, null).put("products", ja). put("enterprise", enterpriseID);
            } catch (Exception e) {
                SkyLogger.logStack(e);
                return JRepCrafter.cancelOperation(response, 500, "Error while parsing product list");
            }
        } else {
            //SPECIFIC PRODUCT
        }

        return JRepCrafter.cancelOperation(response, 500, "You need to specify at least an enterprise!");
    }
}
