package com.christinewang;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.vue.VueComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;
import spark.utils.IOUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.christinewang.AdminController.*;
import static com.christinewang.CryptoLib.*;
import static com.christinewang.HerokuUtil.LOCAL_PORT;
import static io.javalin.apibuilder.ApiBuilder.get;

/**
 * Controller for the Voter Queue App.
 * Contains all the routes.
 *
 * @author Christine Wang
 * @author John Berberian
 */

public class Application {
    public static Logger LOG = LoggerFactory.getLogger(Application.class);
    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static VoteService voteService;
    //A random string, to prevent attackers from throwing malicious post requests at us.
    public static String uploadsalt=getRandomString("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstufwxyz",16);
    public static List<String> startURLs;
    public static List<String> endURLs;
    public static boolean isRunningLocally;

    public static void main(String[] args) {
        Sql2o sql2o = HerokuUtil.setupDB();
        voteService = new VoteService(sql2o);
        String csvPath=getRandomString("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstufwxyz",100);
        CSVLib.logInit();
        MAX_PRECINCT = voteService.getMaxPrecinct();
        MIN_PRECINCT = voteService.getMinPrecinct();
        endURLs = get_EndURLs();
        startURLs = get_StartURLs();

        int port = HerokuUtil.getHerokuAssignedPort();
        isRunningLocally = port==LOCAL_PORT;
        Javalin app = Javalin.create(config -> {
            config.enableWebjars();
            config.addStaticFiles("/public");
        })
            .start(port);

        app.routes(() -> {
            get("/", new VueComponent("<wait-time-overview></wait-time-overview>"));
            get("/start_vote/:precinct", new VueComponent("<start-vote-view></start-vote-view>"));
            get("/end_vote/:precinct", new VueComponent("<end-vote-view></end-vote-view>"));


            get("/api/wait_time_overview", VoteController.getWaitTimeOverview);
            get("/api/start_vote/:precinct", VoteController.startVoteHandler);
            get("/api/end_vote/:precinct", VoteController.endVoteHandler);

            get("/wait_time/:precinct", VoteController.waitTimeHandler);

            get("/get_QR_start/:precinct", QRController.get_QR_startHandler);
            get("/get_QR_end/:precinct", QRController.get_QR_endHandler);
            get("/get_QR_wait/:precinct", QRController.get_QR_waitHandler);

            get("/all_QR_start", QRController.all_QR_startHandler);
            get("/all_QR_end", QRController.all_QR_endHandler);
            get("/all_QR_wait", QRController.all_QR_waitHandler);

            get("/admin", ctx -> {
                ctx.html(CryptoLib.get_adminpage(uploadsalt,csvPath));
            });
            get("/get_csv/"+csvPath+"/VoterQueueLog.csv",AdminController.csvStaticIsh);

        });

        //Listener for handling uploaded files.
        app.post("/upload-precinctnames", upload);

        app.error(404, ctx -> ctx.result("Page does not exist."));
        LOG.info("Server started, all routes mapped successfully.\n");
    }
}