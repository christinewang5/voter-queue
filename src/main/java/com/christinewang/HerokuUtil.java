package com.christinewang;

import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class HerokuUtil {

    public static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }
        return 7000;
    }

    /**
     * Set up database, connect to heroku database server.
     * @return The Sql2o connection
     */
    public static Sql2o setupDB() {
        URI dbUri = null;
        try {
            dbUri = new URI(System.getenv("DATABASE_URL"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String dbUsername = dbUri.getUserInfo().split(":")[0];
        String dbPassword = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        Sql2o sql2o = new Sql2o(dbUrl,
            dbUsername, dbPassword, new PostgresQuirks() {
            {
                converters.put(UUID.class, new UUIDConverter());
            }
        });
        return sql2o;
    }
}
