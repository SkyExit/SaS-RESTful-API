package de.laurinhummel.SparkSRV.handler;

import de.laurinhummel.SparkSRV.Main;
import spark.Request;
import spark.Response;

public class SessionValidationHandler {
    private static SessionAccessTypes type;

    public enum SessionAccessTypes {
        FINE,
        INVALIDE_API_KEY,
        INVALIDE_SESSION_ID
    }

    public static boolean validate(Request request) {
        String auth = request.headers("Authentication");
        if(auth == null || !auth.equals(Main.APIKEY)) {
            type = SessionAccessTypes.INVALIDE_API_KEY;
            return true;
        }
        type = SessionAccessTypes.FINE;
        return false;
    }

    public static Object correct(Response response) {
        return switch (type) {
            case INVALIDE_API_KEY -> JRepCrafter.cancelOperation(response, 401, "Invalid or missing API-Key");
            case INVALIDE_SESSION_ID -> JRepCrafter.cancelOperation(response, 401, "Invalid Session ID");
            default -> null;
        };
    }
}
