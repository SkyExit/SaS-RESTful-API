package de.laurinhummel.SparkSRV.paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import java.sql.Connection;

public class PutTransaction implements Route {
    MySQLConnectionHandler handler;
    public PutTransaction(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Connection connection = handler.getConnection();
        Main.createBlockchain(connection);

        String validationActive;
        String validationPassive;
        int money;

        try {
            JSONObject body = new JSONObject(request.body());
            validationActive = body.getString("validation_active");
            validationPassive = body.getString("validation_passive");
            money = body.getInt("money");


        } catch (JSONException ex) {
            response.status(500);
            ex.printStackTrace();
            System.out.println("err");
            return "Error while parsing JSON - GetUser";
        }

        try {
            /*Ü
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPut httpPut = new HttpPut("http://localhost:5260/user/");
            StringEntity params = new StringEntity("{ \"validation\": \"" + validationActive + "\"}");
            httpPut.addHeader("content-type", "application/json");
            httpPut.setEntity(params);
            HttpResponse httpResponse = httpClient.execute(httpPut);

            System.out.println("res:" + httpResponse.toString());
             */

            /*
            URL url = new URL ("http://localhost:5260/user");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("PUT");
            con.setDoOutput(true);
            String jsonInputString = "{\"validation\": \"" + validationActive + "\" }";

            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder stringBuilder = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    stringBuilder.append(responseLine.trim());
                }
                System.out.println(stringBuilder.toString());
            }
             */

            var client = HttpClient.newHttpClient();
            var message = "{ \"validation\": \"rVUoPYJ0ps\" }";

            var uri = new URI("http://localhost:5260/user");

                    var httpRequest = HttpRequest.newBuilder(uri).
                    PUT(BodyPublishers.ofString(message))
                    .header("Content-Type", "application/json").
                    build();


            var httpResponse = client.send(httpRequest, BodyHandlers.discarding());

            //var locationHeader = httpResponse.headers().firstValue("Location").get();
            System.out.println(httpResponse);

        } catch (Exception ex) {
            System.out.println("error");
            ex.printStackTrace();
        }

        return "null";
    }
}
