package com.christinewang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;

import java.io.IOException;
import java.util.UUID;

import static spark.Spark.*;

/**
 * Hello world!
 */
public class Controller {
    private static final int HTTP_BAD_REQUEST = 400;

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

        VoteModel voteModel = new VoteModel(sql2o);

        // insert a voter entry
        post("/start_vote/:precinct", (req, res) -> {
            String precinct = req.params(":precinct");
            int p = Integer.valueOf(precinct);
            UUID uuid = voteModel.startVote(p);
            res.status(HTTP_BAD_REQUEST);
            return uuid;
        });

        // get all votes
        get("/", (req, res) -> {
            res.status(200);
            res.type("application/json");
            return dataToJson(voteModel.getAllVotes());
        });
    }
    public static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(data);
        } catch (IOException e){
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }
}
