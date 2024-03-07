package de.laurinhummel.SparkSRV.paths.enterprises;

import de.laurinhummel.SparkSRV.handler.JRepCrafter;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import de.laurinhummel.SparkSRV.handler.SessionValidationHandler;
import de.laurinhummel.SparkSRV.handler.SkyLogger;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

public class GetTaxes implements Route {
    MySQLConnectionHandler handler;
    public GetTaxes(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if(SessionValidationHandler.validate(request)) { return SessionValidationHandler.correct(response); }

        String enterpriseID;
        try { enterpriseID = request.params(":validation");
        } catch (Exception ex) {
            SkyLogger.logStack(ex);
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "Error while parsing parameter");
        }

        JSONObject enterprise = handler.getUserData(enterpriseID, request, response).getJSONObject("user");
        if(enterprise.getInt("priority") != 2) return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.BAD_REQUEST, "This is not an enterprise!");

        try {
            return JRepCrafter.cancelOperation(response, JRepCrafter.ResCode.OK, "There you go!").put("taxed", enterprise.getBoolean("taxed"))
                    .put("taxes", enterprise.getBoolean("taxed") ? 20.0f : JSONObject.NULL);
        } catch (Exception e) {
            SkyLogger.logStack(e);
            return false;
        }

    }
}
