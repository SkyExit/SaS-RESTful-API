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
import java.util.HashMap;
import java.util.Map;

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
            enterpriseID = (request.params(":enterprise") == null || request.params(":enterprise").isBlank()) ? null : handler.getUserData(request.params(":enterprise"), request, response)
                    .getJSONObject("user").getString("validation");
            productID = (request.params(":product") == null || request.params(":product").isBlank()) ? null : handler.getProduct(request.params(":product"), request, response)
                    .getJSONObject("product").getString("validation_product");
        } catch (Exception ex) {
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Enterprise or product not found");
        }

        try {
            if(enterpriseID == null) {
                //LIST ALL PRODUCTS
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + Main.names[3] + " ORDER BY id DESC");
                ResultSet rs = preparedStatement.executeQuery();

                //MAP -> Enterprise + JSONArray
                Map<String, JSONArray> map = new HashMap<>();
                JSONObject register = new JSONObject();
                JSONArray reg = new JSONArray();

                while(rs.next()) {
                    try {
                        if(map.containsKey(rs.getString("validation_enterprise"))) {
                            map.get(rs.getString("validation_enterprise")).put(new JSONObject()
                                    .put("validation_enterprise", rs.getString("validation_enterprise"))
                                    .put("validation_product", rs.getString("validation_product"))
                                    .put("name_product", rs.getString("name_product"))
                                    .put("price", rs.getInt("price"))
                            );
                        } else {
                            map.put(rs.getString("validation_enterprise"), new JSONArray().put(new JSONObject()
                                    .put("validation_enterprise", rs.getString("validation_enterprise"))
                                    .put("validation_product", rs.getString("validation_product"))
                                    .put("name_product", rs.getString("name_product"))
                                    .put("price", rs.getInt("price"))
                            ));
                        }
                    } catch (Exception ex) {
                        SkyLogger.logStack(ex);
                    }
                }

                for (Map.Entry<String, JSONArray> entry : map.entrySet()) {
                    //register.put(entry.getKey(), entry.getValue());
                    reg.put(entry.getValue());
                }

                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, "Fetched all products").put("products", reg);
            } else if(productID == null) {
                //LIST ALL PRODUCTS OF ENTERPRISE

                String sqlArgs = "SELECT * FROM `" + Main.names[3] + "` WHERE `validation_enterprise` LIKE '%" + enterpriseID + "'";
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
                    SkyLogger.log("Product list fetched for " + enterpriseID);
                    return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, null).put("products", ja). put("enterprise", enterpriseID);
                } catch (Exception e) {
                    SkyLogger.logStack(e);
                    return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Error while parsing product list");
                }
            } else {
                //SPECIFIC PRODUCT
                return handler.getProduct(productID, request, response);
            }
        } catch (Exception ex) {
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "No results!");
        }
    }
}
