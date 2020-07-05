package com.christinewang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;

import java.io.IOException;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.staticFiles;

/**
 * Controller for the Voter Queue App.
 * Contains all the routes.
 */
public class Controller {
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static Logger LOG = LoggerFactory.getLogger(Controller.class);

    public static void main(String[] args) {
        String dbHost = "localhost";
        int dbPort = 5432;
        String database = "voter_queue";
        String dbUsername = "voter";
        String dbPassword = "voting-rocks";

        Sql2o sql2o = new Sql2o("jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + database,
                dbUsername, dbPassword, new PostgresQuirks() {
            {
                // make sure we use default UUID converter.
                converters.put(UUID.class, new UUIDConverter());
            }
        });

        staticFiles.location("/public");

        VoteService voteService = new VoteService(sql2o);

        // insert a voter entry
        get("/start_vote/:precinct", (req, res) -> {
            try {
                String precinct = req.params(":precinct");
                int p = Integer.parseInt(precinct);
                UUID uuid = voteService.startVote(p);
                res.cookie("/", "uuid", uuid.toString(), 3600, false, true);
                res.type("application/json");
                res.status(HTTP_OK);
                return uuid;
            } catch (Exception e) {
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
                return null; // TODO - custom error message
            }
        });

        get("/end_vote/:precinct", (req, res) -> {
            try {
                String precinct = req.params(":precinct");
                int p = Integer.parseInt(precinct);
                String cookie = req.cookie("uuid");
                System.out.println(cookie);
                voteService.endVote(UUID.fromString(cookie), p);
                res.type("application/json");
                res.status(HTTP_OK);
                return "Thanks for checking in!";
            } catch (Exception e) {
                LOG.error(e.toString());
                res.status(HTTP_OK);
                return null;
            }
        });

        // TODO - calculate average, change to moving average/waited average
        get("/wait_time/:precinct", (req, res) -> {
            try {
                String precinct = req.params(":precinct");
                int p = Integer.parseInt(precinct);
                res.type("application/json");
                res.status(HTTP_OK);
                return dataToJson(voteService.getWaitTime(p));
            } catch (Exception e) {
                LOG.error(e.toString());
                res.status(HTTP_OK);
                return null;
            }
        });

        // TODO - remove this, for debugging
        get("/getAll", (req, res) -> {
            res.status(HTTP_OK);
            res.type("application/json");
            return dataToJson(voteService.getAllVotes());
        });

        // TODO - remove this, for debugging
        get("/getComplete", (req, res) -> {
            res.status(HTTP_OK);
            res.type("application/json");
            return dataToJson(voteService.getAllCompleteVotes());
        });

        // TODO - create QR code front end
        get("/create", (req, res) -> {
            res.status(HTTP_OK);
            res.type("application/json");
            return dataToJson(voteService.getAllVotes());
        });
    }

    public static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValueAsString(data);
            return mapper.writeValueAsString(data);
        } catch (IOException e) {
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }
}
