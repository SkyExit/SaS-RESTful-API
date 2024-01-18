package de.laurinhummel.SparkSRV.paths.products;

import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;

public class PatchProducts implements Route {
    MySQLConnectionHandler handler;
    public PatchProducts(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (SessionValidationHandler.validate(request)) {
            return SessionValidationHandler.correct(response);
        }
        Connection connection = handler.getConnection();

        JSONObject body = JRepCrafter.getRequestBody(request, response);
        if(response.status() != 200) return body;

        String enterprise = "";  //NOT NULL
        String val_product = null; //CAN BE NULL ON CREATION
        String name_product = null; //NOT NULL ON CREATION - CAN BE NULL ON PRICE CHANGE
        Integer price = null; //NOT NULL ON CREATION - CAN BE NULL ON NAME CHANGE
        Boolean remove = null; //NEVER TRUE EXCEPT FOR REMOVAL

        try {
            enterprise = body.getString("val_enterprise");
            if(enterprise.isBlank()) throw new NullPointerException();
            if(handler.getUserData(enterprise, request, response).getInt("status") != 200) throw new NullPointerException();
        } catch (Exception exception) {
            return JRepCrafter.cancelOperation(response, 500, "You have to provide an enterprise validation ID");
        }

        if(body.has("val_product") && !body.get("val_product").toString().isBlank()) val_product = body.getString("val_product");
        if(body.has("name_product") && !body.get("name_product").toString().isBlank()) name_product = body.getString("name_product");
        if(body.has("price") && !body.get("price").toString().isBlank()) price = Integer.parseInt(body.get("price").toString());
        if(body.has("remove") && !body.get("remove").toString().isBlank()) remove = Boolean.parseBoolean(body.get("remove").toString());

        if(price != null && price < 0) return JRepCrafter.cancelOperation(response, 500, "The price hast to be greater than 0!");

        //SkyLogger.log(enterprise + " " + val_product + " " + name_product + " " + price + " " + remove);


        //CREATE PRODUCT
        if(val_product == null && name_product != null && price != null && Boolean.FALSE.equals(remove)) {

        }

        return true;
    }
}