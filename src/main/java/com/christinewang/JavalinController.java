package com.christinewang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static com.christinewang.QRLib.*;
import static spark.Spark.get;
import static spark.Spark.staticFiles;

/**
 * Controller for the Voter Queue App.
 * Contains all the routes.
 * @author Christine Wang
 * @author John B.
 */
// TODO - add error handling
public class JavalinController {
    private static Logger LOG = LoggerFactory.getLogger(Controller.class);

    public static void main(String[] args) {
        // Set up database
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

        VoteService voteService = new VoteService(sql2o);

        Javalin app = Javalin.create().start(getHerokuAssignedPort());

        app.get("/", ctx -> ctx.result("Welcome to voter queue"));
        app.get("/start_vote/:precinct", ctx -> {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
            System.out.printf("precinct: %d\n",precinct);
            UUID uuid = voteService.startVote(precinct);
            ctx.cookieStore("uuid", uuid);
            System.out.printf("uuid start: %s \n", uuid);
            ctx.result("Thanks for checking in! Remember to check out at the end.");
        });

        app.get("/end_vote/:precinct", ctx -> {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
            String val = ctx.cookieStore("uuid");
            System.out.printf("cookie end: %s \n", val);
            UUID uuid  = UUID.fromString(val);
            long waitTime = voteService.endVote(uuid, precinct);
            ctx.result("Thanks for checking in! You waited "+waitTime+" minute(s)!");
        });

        app.get("/wait_time/:precinct", ctx -> {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
            int waitTime = voteService.getWaitTime(precinct);
            ctx.result(String.format("Wait Time For Precinct %d  - %d",  precinct, waitTime));
        });
    }
    private static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }
        return 7000;
    }
}
