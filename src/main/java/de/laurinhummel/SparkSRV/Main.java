package de.laurinhummel.SparkSRV;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        //Spark.get("/hello", (request, response) -> "Hello World");
        //TransactionHandler.validateUserBalance(1);

        Spark.get("/users/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) throws SQLException {
                System.out.println(1);
                USRObject user = getDao().queryForId(request.params(":id"));
                System.out.println(2);
                if (user != null) {
                    return "Username: " + user.getName(); // or JSON? :-)
                } else {
                    response.status(404); // 404 Not found
                    return "User not found";
                }
            }
        });

        Spark.get("/post", new Route() {
            @Override
            public Object handle(Request request, Response response) throws SQLException {
                String name = request.queryParams("name");
                int money = Integer.parseInt(request.queryParams("money"));

                USRObject user = new USRObject();
                user.setName(name);
                user.setMoney(money);

                getDao().create(user);

                response.status(201); // 201 Created

                return true;
            }
        });
    }

    private static Dao<USRObject,String> getDao() throws SQLException {
        try {
            String databaseUrl = "jdbc:mysql://db4free.net:3306/sasrestapi?user=sparkapi&password=spark12345"; //"jdbc:mysql://localhost/spark";
            ConnectionSource connectionSource;

            connectionSource = new JdbcConnectionSource(databaseUrl);
            ((JdbcConnectionSource)connectionSource).setUsername("sparkapi");
            ((JdbcConnectionSource)connectionSource).setPassword("spark12345");

            Dao<USRObject,String> userDao = DaoManager.createDao(connectionSource, USRObject.class);
            TableUtils.createTableIfNotExists(connectionSource, USRObject.class);

            return userDao;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
