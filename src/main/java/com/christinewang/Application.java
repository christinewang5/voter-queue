package com.christinewang;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.vue.VueComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import java.io.IOException;

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

    public static void main(String[] args) {
        try {
            new PrecinctNames();
        } catch (IOException e) {
            LOG.error("Looks like we may be missing our csv.");
            LOG.error(e.toString());
        }
        Sql2o sql2o = HerokuUtil.setupDB();
        voteService = new VoteService(sql2o);

        Javalin app = Javalin.create(config -> {
            config.enableWebjars();
            config.addStaticFiles("/public");
        })
            .start(HerokuUtil.getHerokuAssignedPort());

        app.routes(() -> {
            get("/", new VueComponent("<wait-time-overview></wait-time-overview>"));
            get("/api/wait_time_overview", VoteController.getWaitTimeOverview);
            get("/start_vote/:precinct", VoteController.startVoteHandler);
            get("/end_vote/:precinct", VoteController.endVoteHandler);
            get("/wait_time/:precinct", VoteController.waitTimeHandler);

            get("/get_QR_start/:precinct", QRController.get_QR_startHandler);
            get("/get_QR_end/:precinct", QRController.get_QR_endHandler);
            get("/get_QR_wait/:precinct", QRController.get_QR_waitHandler);

            get("/all_QR_start", QRController.all_QR_startHandler);
            get("/all_QR_end", QRController.all_QR_endHandler);
            get("/all_QR_wait", QRController.all_QR_waitHandler);


        });

        app.error(404, ctx -> ctx.result("Page does not exist."));
        LOG.info("Server started, all routes mapped successfully.\n");
    }
}
