package de.laurinhummel.SparkSRV.handler;

import org.json.JSONObject;
import spark.Response;

public class JRepCrafter {
    public static JSONObject cancelOperation(Response response, int statusCode, String message) {
        response.status(statusCode);
        return new JSONObject().put("status", response.status())
                .put("message", message);
    }
}
