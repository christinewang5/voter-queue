package com.christinewang;

import com.fasterxml.jackson.core.JsonParseException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.apache.commons.lang3.ObjectUtils;

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
                LOG.warn(String.format("No data for precinct %d\n",precinct));
                waitString = "We currently have no data for precinct "+precinct+", "+precinctNames.get(precinct)+".\n"+
                    "You could be the one to change that!";
            }
            //If the person has already voted, log it.
            if (hasAlreadyVoted(uuid,voteService)){
                ctx.result("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);
                /*ctx.result("It looks like you've already gone through.\n" +
                        "You can go again, unless you're trying to mess up our data.\n\n" +
                        waitString);*/
                LOG.info(String.format("already completed, going again: %s -> %s", uuid, uuid2));
            }
            //If the person has already scanned, lot it.
            else if (isValid(uuid, voteService, precinct)){
                ctx.result("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);
                /*ctx.result("You scanned this before, but you're here again.\n" +
                        "That's fine, we'll just pretend that you initially arrived now.\n\n" +
                        waitString);*/
                LOG.info(String.format("didn't complete, going again: %s -> %s",uuid,uuid2));
            }
            //If the person has already scanned in a different precinct, log it.
            else if (isValid(uuid, voteService)){
                ctx.result("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);
                /*ctx.result("Hm, you seem to have transferred from another precinct.\n" +
                        "That's fine, we'll just pretend you started here.\n\n"+
                        waitString);*/
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

            LOG.error("StartVoteHandler: "+e.toString());
        }

    };

    public static Handler endVoteHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            boolean noCookie;
            boolean badCookie;
            UUID uuid = null;

            //UUID.fromString will throw NullPointerException
            //if passed a no-cookie.
            try {
                String val = ctx.cookieStore("uuid");
                uuid = UUID.fromString(val);
                noCookie = false;
                badCookie=false;
            } catch (NullPointerException e) {
                noCookie = true;
                badCookie=false;
            } catch (Exception e){
                //LOG.error("Ignore the JSON: "+e.toString());
                noCookie=false;
                badCookie=true;
            }
            if (badCookie) {
                ctx.status(HTTP_OK);
                ctx.result("Thanks for checking in!");
                boolean cleared;
                try {
                    ctx.clearCookieStore();
                    cleared=true;
                } catch (Exception e) {
                    cleared=false;
                }
                String clearStatus = (cleared) ? "cleared cookieStore" : "failed to clear cookieStore";
                LOG.info("Possibly malicious: Bad cookie, likely bad JSON or bad base64, " +
                        clearStatus+".\n");
            }
            else if (noCookie || uuid==null){
                ctx.status(HTTP_OK);
                ctx.result("Thanks for checking in!");
                //ctx.result("Please scan the starting QR before the ending QR.");
                LOG.info(String.format("Uncookied user requested end vote, precinct %d.\n",precinct));
            }
            else if (! isValid(uuid,voteService,precinct)){
                ctx.status(HTTP_OK);
                ctx.result("Thanks for checking in!");
                //ctx.result("You seem to have an invalid cookie for this precinct.");
                LOG.info(String.format("Invalid uuid %s requested end vote, precinct %d.\n",uuid,precinct));
            }
            else if (hasAlreadyVoted(uuid,voteService)) {
                ctx.status(HTTP_OK);
                ctx.result("Thanks for checking in!");
                //ctx.result("You've already visited this page.");
                LOG.info(String.format("Repeated request to end vote by valid uuid %s, precinct %d.\n",uuid,precinct));
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
            LOG.error("EndVoteHandler: "+e.toString());
        }
    };

    public static Handler waitTimeHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            String waitTime = dataToJson(voteService.getWaitTime(precinct));
            if (waitTime==null || waitTime.equals("null")){
                ctx.status(HTTP_OK);
                ctx.result(String.format("We have no data for precinct %d",precinct));
                LOG.warn(String.format("No data for precinct %d\n",precinct));
            } else {
                ctx.result(String.format("Wait Time For Precinct %d  - %s", precinct, waitTime));
                ctx.status(HTTP_OK);
                ctx.result("Wait Time For Precinct " + precinct + ": " + voteService.getWaitTime(precinct) + " minute(s)");
            }
        } catch (Exception e) {
            LOG.error("WaitTimeHandler: "+e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };

    public static Handler getWaitTimeOverview = ctx -> {
        List<VoteCompleteModel> waitTimes = voteService.getWaitTimeOverview();
        for (VoteCompleteModel v : waitTimes) {
            v.setName(precinctNames.get(v.getPrecinct()));
        }
        ctx.json(waitTimes);
    };
}
