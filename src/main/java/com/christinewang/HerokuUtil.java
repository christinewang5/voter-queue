package com.christinewang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;
//import sun.rmi.runtime.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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

            LOG.error(e.toString()+" - You are running locally.");
        }

        return sql2o;
    }

    /** A nice data representation.
     * */
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

    /** Check if a uuid is valid for a given precinct.
     * @author John Berberian
     * @param uuid The uuid to check.
     * @param voteService The VoteService connected to the voter_queue database.
     * @param precinct The precinct to check in.
     * @return True if the uuid is valid, false if not.
     * */
    public static boolean isValid(UUID uuid, VoteService voteService, int precinct) {
        //Get all the votes (valid uuids) for that precinct
        List<VoteModel> votes = voteService.getPrecinctVotes(precinct);
        //Search through them...
        for (VoteModel v : votes) {
            //...and if the input uuid matches one, it is valid.
            if (uuid.equals(v.getUUID())) {
                return true;
            }
        }
        //Otherwise, it is invalid.
        return false;
    }

    /** Check if a uuid is valid for any precinct.
     * @author John Berberian
     * @param uuid The uuid to check.
     * @param voteService The VoteService connected to the voter_queue database.
     * @return True if the uuid is valid, false if not.
     * */
    public static boolean isValid(UUID uuid, VoteService voteService) {
        //Get all the votes (valid uuids) for that precinct
        List<VoteModel> votes = voteService.getAllVotes();
        //Search through them...
        for (VoteModel v : votes) {
            //...and if the input uuid matches one, it is valid.
            if (uuid.equals(v.getUUID())) {
                return true;
            }
        }
        //Otherwise, it is invalid.
        return false;
    }

    /** Check if a uuid has already been used to end a vote.
     * @author John Berberian
     * @param uuid The uuid to check.
     * @param voteService The VoteService connected to the voter_queue database.
     * @return True if the uuid has already been used, false if not.
     * */
    public static boolean hasAlreadyVoted(UUID uuid, VoteService voteService) {
        //Get all the complete votes.
        List<VoteCompleteModel> votes = voteService.getAllCompleteVotes();
        //Search through them...
        for (VoteCompleteModel v : votes) {
            //...and if the input uuid matches one, it's already been used.
            if (uuid.equals(v.getUUID())) {
                return true;
            }
        }
        //Otherwise, it's still available.
        return false;
    }

    /** Check if a uuid has already been used to end a vote in a given precinct.
     * @author John Berberian
     * @param uuid The uuid to check.
     * @param voteService The VoteService connected to the voter_queue database.
     * @param precinct The precinct to check in.
     * @return True if the uuid has already been used, false if not.
     * */
    public static boolean hasAlreadyVoted(UUID uuid, VoteService voteService, int precinct) {
        //Get all the complete votes.
        List<VoteCompleteModel> votes = voteService.getPrecinctCompleteVotes(precinct);
        //Search through them...
        for (VoteCompleteModel v : votes) {
            //...and if the input uuid matches one, it's already been used.
            if (uuid.equals(v.getUUID())) {
                return true;
            }
        }
        //Otherwise, it's still available.
        return false;
    }

}
