package de.laurinhummel.SparkSRV;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class Main {
    public static void main(String[] args) {
        //Spark.get("/hello", (request, response) -> "Hello World");

        Spark.get("/users/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) {
                return  "User: username=test, email=test@test.net";
            }
        });
    }
}
