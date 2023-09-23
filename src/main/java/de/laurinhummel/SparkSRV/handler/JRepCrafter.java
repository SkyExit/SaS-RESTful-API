package de.laurinhummel.SparkSRV.handler;

import de.laurinhummel.SparkSRV.Main;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class JRepCrafter {
    public static JSONObject cancelOperation(Response response, int statusCode, String message) {
        response.status(statusCode);
        return new JSONObject().put("status", response.status())
                .put("message", message);
    }
}
