package de.laurinhummel.SparkSRV.paths.products;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;

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
            JSONObject ent = handler.getUserData(enterprise, request, response);
            if(ent.getInt("status") != 200) throw new NullPointerException();
            enterprise = ent.getJSONObject("user").getString("validation");
        } catch (Exception exception) {
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "You have to provide an enterprise validation ID");
        }

        if(body.has("val_product") && !body.get("val_product").toString().isBlank()) val_product = body.getString("val_product");
        if(body.has("name_product") && !body.get("name_product").toString().isBlank()) name_product = body.getString("name_product");
        if(body.has("price") && !body.get("price").toString().isBlank()) price = Integer.parseInt(body.get("price").toString());
        if(body.has("remove") && !body.get("remove").toString().isBlank()) remove = Boolean.parseBoolean(body.get("remove").toString());

        if(price != null && price < 0) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "The price hast to be greater than 0!");

        //SkyLogger.log(enterprise + " " + val_product + " " + name_product + " " + price + " " + remove);


        //CREATE PRODUCT
        if(val_product == null && name_product != null && price != null && Boolean.FALSE.equals(remove)) {
            val_product = "PRD-" + RandomStringUtils.random(10, 0, 0, true, true, null, new SecureRandom());

            String query = "insert into " + Main.names[3] + " (validation_enterprise, validation_product, name_product, price)"
                    + " values (?, ?, ?, ?)";
            try {
                PreparedStatement preparedStmt = connection.prepareStatement(query);
                    preparedStmt.setString(1, enterprise);
                    preparedStmt.setString(2, val_product);
                    preparedStmt.setString (3, name_product);
                    preparedStmt.setInt (4, price);

                preparedStmt.execute();

                SkyLogger.log("Product" + val_product + " created successfully");
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.CREATED, null).put("product", new JSONObject()
                    .put("val_enterprise", enterprise)
                    .put("val_product", val_product)
                    .put("name_product", name_product)
                    .put("price", price)
                );
            } catch (Exception e) {
                System.err.println("Got an exception! - create");
                System.err.println(e.getMessage());
                SkyLogger.logStack(e);
                return "Got an exception!";
            }
        }

        JSONObject product = handler.getProduct(val_product, request, response);
        if(product.getInt("status") != 200 || !product.getJSONObject("product").getString("validation_enterprise").equals(enterprise)) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Product was not found");

        //EDIT PRODUCT DATA
        if(val_product != null && (name_product != null || price != null) && Boolean.FALSE.equals(remove)) {
            try {
                StringBuilder sql = new StringBuilder("UPDATE " + Main.names[3] + " SET ");
                if(name_product != null && !name_product.isBlank()) sql.append("`name_product`='").append(name_product).append("'");
                if(name_product != null && !name_product.isBlank() && price != null) sql.append(", ");
                if(price != null) sql.append("`price`='").append(price).append("'");
                sql.append(" WHERE `validation_enterprise`='").append(enterprise).append("'").append(" AND `validation_product`='").append(val_product).append("'");

                connection.prepareStatement(sql.toString()).execute();

                SkyLogger.log("Product (" + val_product + ") changed successfully (" + name_product + ", " + price + ")");
            } catch (Exception e) {
                SkyLogger.logStack(e);
            }

            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, "Product updated successfully").put("product", new JSONObject()
                    .put("val_enterprise", enterprise)
                    .put("val_product", val_product)
                    .put("name_product", name_product)
                    .put("price", price)
            );
        }

        //DELETE PRODUCT
        if(val_product != null && Boolean.TRUE.equals(remove)) {
            try {
                connection.prepareStatement("DELETE FROM " + Main.names[3] + " WHERE `validation_enterprise`='" + enterprise + "' AND `validation_product`='" + val_product + "'").execute();
            } catch (Exception e) {
                SkyLogger.logStack(e);
                return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.NOT_FOUND, "Error while deleting product (" + val_product + ")");
            }

            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, "Removing product successful (" + val_product + ")");
        }

        return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.INTERNAL_SERVER_ERROR, "Fallout - PatchProducts");
    }
}