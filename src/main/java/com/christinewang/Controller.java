package com.christinewang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.christinewang.PrecinctNames.MAX_PRECINCT;
import static com.christinewang.PrecinctNames.MIN_PRECINCT;
import static com.christinewang.QRLib.*;
import static spark.Spark.get;
import static spark.Spark.staticFiles;

/**
 * NOTE - will be deprecated
 *
 * Controller for the Voter Queue App.
 * Contains all the routes.
 * @author Christine Wang
 * @author John B.
 */
public class Controller {
    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    private static final int TIME_IN_DAY_IN_S = 86400;
    private static Logger LOG = LoggerFactory.getLogger(Controller.class);
    public static final String WEB_HOST = "http://voter-queue.herokuapp.com";

    public static void main(String[] args) {
        // Set up database
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
                res.cookie("/", "uuid", uuid.toString(), TIME_IN_DAY_IN_S, false, true);
                res.type("application/json");
                res.status(HTTP_OK);
                return "Thanks for checking in! Remember to check out at the end.";
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
                res.status(HTTP_OK);
                if (cookie==null){
                    return "Go to the start_vote page to get a cookie first!";
                }
                if (! isValid(UUID.fromString(cookie),voteService,p)){
                    return "You seem to have an invalid cookie for this precinct.";
                }
                if (hasAlreadyVoted(UUID.fromString(cookie),voteService)) {
                    return "You've already visited this page.";
                }
                String waitTime = String.valueOf(voteService.endVote(UUID.fromString(cookie), p));
                res.type("application/json");
                return "Thanks for checking in! You waited "+waitTime+" minute(s)!";
            } catch (Exception e) {
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
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
                String waitTime = dataToJson(voteService.getWaitTime(p));
                if (waitTime == null || waitTime.equals("null")){
                    return "We currently have no data for that precinct.";
                }
                return "The wait time for that precinct is "+waitTime+" minutes.";
            } catch (Exception e) {
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
                return null;
            }
        });

        get("/get_QR_start/:precinct",(req, res) -> {
            try {
                String precinct = req.params(":precinct");
                int p = Integer.parseInt(precinct);
                String baseUrl = WEB_HOST + "/start_vote/";
                String QR_embed = getStart_Printout(p,baseUrl);
                res.status(HTTP_OK);
                return QR_embed;
            } catch (Exception e){
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
                return null;
            }
        });

        get("/get_QR_end/:precinct",(req, res) -> {
            try {
                String precinct = req.params(":precinct");
                int p = Integer.parseInt(precinct);
                String baseUrl = WEB_HOST + "/end_vote/";
                String QR_embed = getEnd_Printout(p,baseUrl);
                res.status(HTTP_OK);
                return QR_embed;
            } catch (Exception e) {
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
                return null;
            }
        });

        get("/get_QR_wait/:precinct",(req, res) -> {
            try {
                String precinct = req.params(":precinct");
                int p = Integer.parseInt(precinct);
                String baseUrl = WEB_HOST + "/wait_time/";
                String QR_embed = getQR(p, baseUrl, true);
                res.status(HTTP_OK);
                return QR_embed;
            } catch (Exception e){
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
                return null;
            }
        });

        get("/all_QR_start", (req, res) -> {
            try {
                String baseUrl = WEB_HOST + "/start_vote/";
                String accumulateAll = getStart_Printouts(MIN_PRECINCT,MAX_PRECINCT,baseUrl);
                res.status(HTTP_OK);
                return accumulateAll;
            } catch (Exception e){
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
                return null;
            }
        });

        get("/all_QR_end", (req, res) -> {
            try {
                String baseUrl = WEB_HOST + "/end_vote/";
                String accumulateAll = getEnd_Printouts(MIN_PRECINCT,MAX_PRECINCT,baseUrl);
                res.status(HTTP_OK);
                return accumulateAll;
            } catch (Exception e){
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
                return null;
            }
        });

        get("/all_QR_wait", (req, res) -> {
            try {
                String baseURL = WEB_HOST + "/wait_time/";
                String accumulateAll = "";
                for (int p = MIN_PRECINCT; p <= MAX_PRECINCT; p++) {
                    accumulateAll += "<p><strong>QR wait code for precinct " + p + "</strong><p>";
                    accumulateAll += getQR(p, baseURL, true);
                }
                res.status(HTTP_OK);
                return accumulateAll;
            } catch (Exception e){
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
                return null;
            }
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

    /** Check if a uuid is valid to be ended for a given precinct.
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
}
