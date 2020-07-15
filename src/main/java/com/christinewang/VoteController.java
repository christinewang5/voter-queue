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
import static com.christinewang.PrecinctNames.precinctNames;
import static com.christinewang.PrecinctNames.MAX_PRECINCT;
import static com.christinewang.PrecinctNames.MIN_PRECINCT;
import static com.christinewang.HerokuUtil.*;


// TODO - add test
public class VoteController {

    public static Handler startVoteHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            UUID uuid;
            try {
                //Try to see if they already have a cookie.
                String val = ctx.cookieStore("uuid");
                uuid = UUID.fromString(val);
            } catch (Exception e){
                //If not, pretend they had an impossible cookie.
                uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
            }
            //Give them a new uuid.
            UUID uuid2 = voteService.startVote(precinct);
            ctx.cookieStore("uuid", uuid2);
            //And give the http an ok.
            ctx.status(HTTP_OK);
            //Make a wait string.
            String waitString = "The current wait time for precinct "+precinct+", "+precinctNames.get(precinct)+" is: " +
                    voteService.getWaitTime(precinct)+" minute(s).";
            //Unless we have no data.
            if (voteService.getWaitTime(precinct)==null ||
                    (""+voteService.getWaitTime(precinct)).equals("null")) {
                LOG.warn(String.format("No data for precinct %d",precinct));
                waitString = "We currently have no data for precinct "+precinct+", "+precinctNames.get(precinct)+".\n"+
                    "You could be the one to change that!";
            }
            //If the person has already voted, log it.
            if (hasAlreadyVoted(uuid,voteService)){
                ctx.result("It looks like you've already gone through.\n" +
                        "You can go again, unless you're trying to mess up our data.\n\n" +
                        waitString);
                LOG.info(String.format("already completed, going again: %s -> %s", uuid, uuid2));
            }
            //If the person has already scanned, lot it.
            else if (isValid(uuid, voteService, precinct)){
                ctx.result("You scanned this before, but you're here again.\n" +
                        "That's fine, we'll just pretend that you initially arrived now.\n\n" +
                        waitString);
                LOG.info(String.format("didn't complete, going again: %s -> %s",uuid,uuid2));
            }
            //If the person has already scanned in a different precinct, log it.
            else if (isValid(uuid, voteService)){
                ctx.result("Hm, you seem to have transferred from another precinct.\n" +
                        "That's fine, we'll just pretend you started here.\n\n"+
                        waitString);
                LOG.info(String.format("transferred from other precinct: %s -> %s",uuid, uuid2));
            }
            //And then our nice normal case.
            else {
                ctx.result("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);

                LOG.info("start vote handler");
            }
            LOG.info(String.format("precinct: %d", precinct));
            LOG.info(String.format("uuid start: %s \n", uuid2));
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
                LOG.info(String.format("precinct: %d", precinct));
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
