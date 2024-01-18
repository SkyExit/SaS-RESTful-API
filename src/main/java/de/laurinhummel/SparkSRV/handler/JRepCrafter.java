package de.laurinhummel.SparkSRV.handler;

import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class JRepCrafter {
    public static JSONObject cancelOperation(Response response, int statusCode, String message) {
        response.status(statusCode);
        return new JSONObject().put("status", response.status())
                .put("message", message);
    }

    public static JSONObject successOperation(Response response, int statusCode) {
        response.status(statusCode);
        return new JSONObject().put("status", response.status())
                .put("status", statusCode);
    }

    public static JSONObject getRequestBody(Request request, Response response) {
        try {
            return new JSONObject(request.body());
        } catch (JSONException ex) {
            response.status(500);
            SkyLogger.logStack(ex);
            return cancelOperation(response, 500, "Error while parsing JSON body");
        }
    }
}