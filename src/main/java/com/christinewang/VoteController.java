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
import static com.christinewang.QRController.MAX_PRECINCT;
import static com.christinewang.QRController.MIN_PRECINCT;
import static com.christinewang.HerokuUtil.*;


// TODO - add test
public class VoteController {

    public static Handler startVoteHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
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
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            String val = ctx.cookieStore("uuid");
            UUID uuid = UUID.fromString(val);
            if (val==null){
                ctx.status(HTTP_OK);
                ctx.result("Go to the start_vote page to get a cookie first!");
            }
            if (! isValid(uuid,voteService,precinct)){
                ctx.status(HTTP_OK);
                ctx.result("You seem to have an invalid cookie for this precinct.");
            }
            if (hasAlreadyVoted(uuid,voteService)) {
                ctx.status(HTTP_OK);
                ctx.result("You've already visited this page.");
            } else {
                long waitTime = voteService.endVote(uuid, precinct);
                ctx.result("Thanks for checking in! You waited " + waitTime + " minute(s)!");
                ctx.status(HTTP_OK);

                //I don't understand why we would do that.
                //We want to prevent double-counting of a single voter.
                //ctx.clearCookieStore();

                LOG.info("end vote handler");
                LOG.info(String.format("precinct: %d\n", precinct));
                LOG.info(String.format("uuid end: %s\n", uuid));
            }
        } catch (Exception e) {
            ctx.status(HTTP_BAD_REQUEST);
            LOG.error(e.toString());
        }
    };

    public static Handler waitTimeHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            String waitTime = dataToJson(voteService.getWaitTime(precinct));
            if (waitTime==null || waitTime.equals("null")){
                ctx.status(HTTP_OK);
                ctx.result(String.format("We have no data for precinct %d",precinct));
            } else {
                ctx.result(String.format("Wait Time For Precinct %d  - %s", precinct, waitTime));
                ctx.status(HTTP_OK);
                ctx.result("Wait Time For Precinct " + precinct + ": " + voteService.getWaitTime(precinct) + " minute(s)");
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
