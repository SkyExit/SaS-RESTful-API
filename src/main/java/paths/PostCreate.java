package paths;

import de.laurinhummel.SparkSRV.Main;
import de.laurinhummel.SparkSRV.handler.MySQLConnectionHandler;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PostCreate implements Route {
    MySQLConnectionHandler handler;
    public PostCreate(MySQLConnectionHandler handler) { this.handler = handler; }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        try {
            Connection connection = handler.getConnection();
            Main.createTable(connection);

            String query = "insert into LogbuchV1 (name, money)"
                    + " values (?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString (1, request.queryParams("name"));
            preparedStmt.setString (2, request.queryParams("money"));

            preparedStmt.execute();
            response.status(201); // 201 Created

            return "User created";
        } catch (Exception e) {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
            return "Got an exception!";
        }
    }
}
