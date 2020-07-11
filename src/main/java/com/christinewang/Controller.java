package com.christinewang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;
import spark.servlet.SparkApplication;

import java.io.IOException;
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
public class Controller {
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int TIME_IN_DAY_IN_S = 86400;
    private static Logger LOG = LoggerFactory.getLogger(Controller.class);
    public static final String WEB_HOST = "localhost";
    public static final int WEB_PORT = 4567;
    private static final int MIN_PRECINCT = 0;
    //Set to 10 for testing purposes, will be set to real value later.
    private static final int MAX_PRECINCT = 10;

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
                res.cookie("/", "uuid", uuid.toString(), TIME_IN_DAY_IN_S, false, true);
                res.type("application/json");
                res.status(HTTP_OK);
                return "Thanks for checking in! Remember to check out at the end";
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
                if (cookie==null){
                    return "Go to the start_vote page to get a cookie first!";
                }
                String waitTime = String.valueOf(voteService.endVote(UUID.fromString(cookie), p));
                res.type("application/json");
                res.status(HTTP_OK);
                return "Thanks for checking in! You waited "+waitTime+" minutes!";
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
                return dataToJson(voteService.getWaitTime(p));
            } catch (Exception e) {
                LOG.error(e.toString());
                res.status(HTTP_BAD_REQUEST);
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

        get("/get_QR_start/:precinct",(req, res) -> {
            try {
                String precinct = req.params(":precinct");
                int p = Integer.parseInt(precinct);
                String baseUrl = WEB_HOST + ":" + WEB_PORT + "/start_vote/";
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
                String baseUrl = WEB_HOST + ":" + WEB_PORT + "/end_vote/";
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
                String baseUrl = WEB_HOST + ":" + WEB_PORT + "/wait_time/";
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
                String baseUrl = WEB_HOST + ":" + WEB_PORT + "/start_vote/";
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
                String baseUrl = WEB_HOST + ":" + WEB_PORT + "/end_vote/";
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
                String baseURL = WEB_HOST + ":" + WEB_PORT + "/wait_time/";
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
}
