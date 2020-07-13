package com.christinewang;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

import static io.javalin.apibuilder.ApiBuilder.get;

import io.javalin.plugin.rendering.vue.VueComponent;

/**
 * Controller for the Voter Queue App.
 * Contains all the routes.
 *
 * @author Christine Wang
 * @author John B.
 */
public class Application {
    public static Logger LOG = LoggerFactory.getLogger(Controller.class);
    public static VoteService voteService;

    public static void main(String[] args) {
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
        });

        app.error(404, ctx -> ctx.result("Page does not exist."));
    }
}
