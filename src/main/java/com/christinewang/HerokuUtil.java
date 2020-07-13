package com.christinewang;

import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;
//import sun.rmi.runtime.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static com.christinewang.Application.LOG;

public class HerokuUtil {

    public static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }
        LOG.error("Heroku Port is null.");
        return 7000;
    }

    /**
     * Set up database, connect to heroku database server.
     * @return The Sql2o connection
     */
    public static Sql2o setupDB() {
        URI dbUri = null;
        Sql2o sql2o;
        try {
            dbUri = new URI(System.getenv("DATABASE_URL"));String dbUsername = dbUri.getUserInfo().split(":")[0];
            String dbPassword = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

            sql2o = new Sql2o(dbUrl,
                dbUsername, dbPassword, new PostgresQuirks() {
                {
                    converters.put(UUID.class, new UUIDConverter());
                }
            });
        } catch (URISyntaxException | NullPointerException e) {
            // catch errors and use local database
            String dbHost = "localhost";
            int dbPort = 5432;
            String database = "voter_queue";
            String dbUsername = "voter";
            String dbPassword = "voting-rocks";

            sql2o = new Sql2o("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + database,
                dbUsername, dbPassword, new PostgresQuirks() {
                {
                    // make sure we use default UUID converter.
                    converters.put(UUID.class, new UUIDConverter());
                }
            });

            LOG.error(e.toString());
        }

        return sql2o;
    }
}
