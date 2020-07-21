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

import static com.christinewang.CryptoLib.getRandomString;
import static io.javalin.apibuilder.ApiBuilder.get;

/**
 * Controller for the Voter Queue App.
 * Contains all the routes.
 *
 * @author Christine Wang
 * @author John Berberian
 */
//TODO Add back in the nice error handling from earlier.
    //TODO Add back in the QR page naming.
    //TODO Put csvlog function calls throughout.
    //TODO Make csvlog downloadable from admin panel.
    //TODO Put in Prof. Acemyan's page edits.
public class Application {
    public static Logger LOG = LoggerFactory.getLogger(Application.class);
    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static VoteService voteService;

    public static void main(String[] args) {
        Sql2o sql2o = HerokuUtil.setupDB();
        voteService = new VoteService(sql2o);
        CSVLib.logInit();
        //A random string, to prevent attackers from throwing malicious post requests at us.
        String uploadsalt=getRandomString("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstufwxyz",15);

        Javalin app = Javalin.create(config -> {
            config.enableWebjars();
            config.addStaticFiles("/public");
        })
            .start(HerokuUtil.getHerokuAssignedPort());

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
                ctx.html(CryptoLib.get_adminpage(uploadsalt));
            });

        });

        //Listener for handling uploaded files.
        app.post("/upload-precinctnames", ctx -> {
            //Converted to Atomic, because otherwise Java was yelling about final variables in lambdas.
            AtomicBoolean success= new AtomicBoolean(true);
            LOG.info(uploadsalt);
            ctx.uploadedFiles(uploadsalt).forEach(file -> {
                try {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(file.getContent(), writer);
                    //Gets the contents as a string.
                    String fileContent = writer.toString();
                    //Parses the contents, and gets a List of the columns.
                    List<ArrayList> cols = CSVLib.parseCSV(fileContent);
                    //Then refreshes the names, using the first two columns.
                    List<Integer> precincts = cols.get(0);
                    List<String> names = cols.get(1);
                    //LOG.info("csv parsed");
                    try {
                        //LOG.info("before changenames");
                        if (!voteService.changeNames(precincts, names)) {
                            throw new IOException("Bad csv format!");
                        }
                        //LOG.info("after changenames");
                    } catch (IOException e) {
                        LOG.error("problem in changeNames.");
                    }
                } catch (IOException e) {
                    LOG.error("upload-precinctnames: cannot read file.");
                    success.set(false);
                }
            });
            if (success.get()) {
                ctx.html("Upload complete");
            } else {
                ctx.html("Upload failure! Wrong format?");
            }
        });

        app.error(404, ctx -> ctx.result("Page does not exist."));
        LOG.info("Server started, all routes mapped successfully.\n");
    }
}