package com.christinewang;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.christinewang.Application.voteService;
import static com.christinewang.Application.LOG;
import static com.christinewang.Application.HTTP_OK;
import static com.christinewang.Application.HTTP_BAD_REQUEST;
import static com.christinewang.Controller.dataToJson;


// TODO - add test
public class VoteController {

    public static Handler startVoteHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
            UUID uuid = voteService.startVote(precinct);
            ctx.cookieStore("uuid", uuid);
            ctx.status(HTTP_OK);
            ctx.result("Thanks for checking in! Remember to check out at the end.");

            LOG.info("start vote handler");
            LOG.info(String.format("precinct: %d\n", precinct));
            LOG.info(String.format("uuid start: %s \n", uuid));
        } catch (Exception e) {
            ctx.status(HTTP_BAD_REQUEST);

            LOG.error(e.toString());
        }

    };

    public static Handler endVoteHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
            String val = ctx.cookieStore("uuid");
            UUID uuid = UUID.fromString(val);
            long waitTime = voteService.endVote(uuid, precinct);
            ctx.result("Thanks for checking in! You waited " + waitTime + " minute(s)!");
            ctx.status(HTTP_OK);
            ctx.clearCookieStore();

            LOG.info("end vote handler");
            LOG.info(String.format("precinct: %d\n", precinct));
            LOG.info(String.format("uuid end: %s\n", uuid));
        } catch (Exception e) {
            ctx.status(HTTP_BAD_REQUEST);
            LOG.error(e.toString());
        }
    };

    public static Handler waitTimeHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
            String waitTime = dataToJson(voteService.getWaitTime(precinct));
            if (waitTime==null || waitTime.equals("null")){
                ctx.status(HTTP_OK);
                ctx.result(String.format("We have no data for precinct %d",precinct));
            } else {
                ctx.result(String.format("Wait Time For Precinct %d  - %s", precinct, waitTime));
                ctx.status(HTTP_OK);
                ctx.result("Wait Time For Precinct: " + precinct + voteService.getWaitTime(precinct) + " mins");
            }
        } catch (Exception e) {
            LOG.error(e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };

    public static Handler getWaitTimeOverview = ctx -> {
        List<VoteCompleteModel> waitTimes = voteService.getWaitTimeOverview();
        ctx.json(waitTimes);
    };
}
