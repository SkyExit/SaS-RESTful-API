package de.laurinhummel.SparkSRV.handler;

import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class JRepCrafter {
    public enum ResCode {
        OK(200),
        CREATED(201),
        ACCEPTED(202),
        BAD_REQUEST(400),               //MALFORMED JSON BODY
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        LOCKED(423),
        INTERNAL_SERVER_ERROR(500),
        NOT_IMPLEMENTED(501);

        public final int code;
        ResCode(int code) { this.code = code; }
    }


    public static JSONObject cancelOperation(Response response, ResCode statusCode, String message) {
        response.status(statusCode.code);
        return new JSONObject().put("status", response.status())
                .put("message", message).put("daniel", 200);
    }

    public static JSONObject getRequestBody(Request request, Response response) {
        try {
            response.status(200);
            return new JSONObject(request.body());
        } catch (JSONException ex) {
            response.status(500);
            SkyLogger.logStack(ex);
            return cancelOperation(response, ResCode.INTERNAL_SERVER_ERROR, "Error while parsing JSON body");
        }
    }
}