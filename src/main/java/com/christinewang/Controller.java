package com.christinewang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.zxing.WriterException;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;

import java.io.IOException;
import java.util.UUID;

import static com.christinewang.QRLib.createQR_b64;
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
    private static final String WEB_HOST = "localhost";
    private static final int WEB_PORT = 4567;
    private static final int MIN_PRECINCT = 0;
    //Set to 10 for testing purposes, will be set to real value later.
    private static final int MAX_PRECINCT = 10;

    public static void main(String[] args) {
        // TODO - separate DB code out
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
            res.status(HTTP_OK);
            String precinct = req.params(":precinct");
            int p = Integer.parseInt(precinct);
            UUID uuid = voteService.startVote(p);
            // TODO - remove maxAge  of cookie
            res.cookie("/", "uuid", uuid.toString(), 3600, false, true);
            return uuid;
        });

        get("/end_vote/:precinct", (req, res) -> {
            String precinct = req.params(":precinct");
            int p = Integer.parseInt(precinct);
            String cookie = req.cookie("uuid");
            if (cookie==null){
                return "Go to the start_vote page to get a cookie first!";
            }
            System.out.println(cookie);
            String waitTime = String.valueOf(voteService.endVote(UUID.fromString(cookie), p));
            res.status(HTTP_OK);
            // TODO - make this look better
            return "Thanks for checking in! You waited "+waitTime+" minutes!";
        });

        // TODO - calculate average, change to moving average
        get("/wait_time/:precinct", (req, res) -> {
            res.status(HTTP_OK);
            String precinct = req.params(":precinct");
            int p = Integer.parseInt(precinct);
            res.type("application/json");
            return dataToJson(voteService.getWaitTime(p));
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
            res.status(HTTP_OK);
            String precinct = req.params(":precinct");
            int p = Integer.parseInt(precinct);
            String baseUrl = WEB_HOST+":"+WEB_PORT+"/start_vote/";
            String QR_embed = getQR(p, baseUrl, true);
            return QR_embed;
        });

        get("/get_QR_end/:precinct",(req, res) -> {
            res.status(HTTP_OK);
            String precinct = req.params(":precinct");
            int p = Integer.parseInt(precinct);
            String baseUrl = WEB_HOST+":"+WEB_PORT+"/end_vote/";
            String QR_embed = getQR(p, baseUrl, true);
            return QR_embed;
        });

        get("/get_QR_wait/:precinct",(req, res) -> {
            res.status(HTTP_OK);
            String precinct = req.params(":precinct");
            int p = Integer.parseInt(precinct);
            String baseUrl = WEB_HOST+":"+WEB_PORT+"/wait_time/";
            String QR_embed = getQR(p, baseUrl, true);
            return QR_embed;
        });

        get("/all_QR_start", (req, res) -> {
            res.status(HTTP_OK);
            String baseURL = WEB_HOST+":"+WEB_PORT+"/start_vote/";
            String accumulateAll="";
            for (int p=MIN_PRECINCT;p<=MAX_PRECINCT;p++){
                accumulateAll += "<p><strong>QR start code for precinct "+p+"</strong><p>";
                accumulateAll += getQR(p, baseURL, true);
            }
            return accumulateAll;
        });

        get("/all_QR_end", (req, res) -> {
            res.status(HTTP_OK);
            String baseURL = WEB_HOST+":"+WEB_PORT+"/end_vote/";
            String accumulateAll="";
            for (int p=MIN_PRECINCT;p<=MAX_PRECINCT;p++){
                accumulateAll += "<p><strong>QR end code for precinct "+p+"</strong><p>";
                accumulateAll += getQR(p, baseURL, true);
            }
            return accumulateAll;
        });

        get("/all_QR_wait", (req, res) -> {
            res.status(HTTP_OK);
            String baseURL = WEB_HOST+":"+WEB_PORT+"/wait_time/";
            String accumulateAll="";
            for (int p=MIN_PRECINCT;p<=MAX_PRECINCT;p++){
                accumulateAll += "<p><strong>QR wait code for precinct "+p+"</strong><p>";
                accumulateAll += getQR(p, baseURL, true);
            }
            return accumulateAll;
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
    /** Returns an HTML img QR code for urlBase/precinct.
     * @author John Berberian
     * @param precinct The precinct that the QR code should be for.
     * @param urlBase The base URL, such that the final url is "urlBase/precinct".
     * @param hasBreak Specifies whether or not the image should be followed by a br.
     * @return HTML img string of base64-encoded QR code of "urlBase/precinct".
     * */
    public static String getQR(int precinct, String urlBase, boolean hasBreak)
            throws IOException, WriterException {
        if (urlBase.charAt(urlBase.length()-1)!='/'){
            urlBase += "/";
        }
        String full_URL = urlBase+precinct;
        String b64_enc = createQR_b64(full_URL);
        String html_img = "<img src=\"data:image/png;base64,"+b64_enc+"\" alt=\"QR code for precinct "+precinct+"\">";
        if (hasBreak){
            html_img += "<br>";
        }
        return html_img;
    }
}
