package com.christinewang;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.sql.SQLDataException;
import java.util.Arrays;
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
            String waitString;
            try {
                waitString = "The current wait time for precinct " + precinct + ", " + voteService.getName(precinct) + " is: " +
                        String.format("%.2f",voteService.getWaitTime(precinct).getWaitTime()) + " minute(s).";
            }
            //Unless we have no data.
            /*if (voteService.getWaitTime(precinct)==null ||
                    (""+voteService.getWaitTime(precinct)).equals("null")) {*/
            catch (SQLDataException e){
                LOG.warn(String.format("No data for precinct %d\n\n", precinct));
                waitString = "We currently have no data for precinct " + precinct + ", " + voteService.getName(precinct) + ".\n" +
                        "You could be the one to change that!";
            }
            //If the person has already voted in this precinct, log it.
            if (hasAlreadyVoted(uuid,voteService,precinct)) {
                //But give them no indication that anything is wrong.
                ctx.html("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);
                /*ctx.html("It looks like you've already gone through.\n" +
                        "You can go again, unless you're trying to mess up our data.\n\n" +
                        waitString);*/
                LOG.info(String.format("already completed, going again: %s -> %s", uuid, uuid2));
                CSVLib.logCompleteMigrate(precinct,precinct,uuid,uuid2);
            }
            //If the person has already voted in a different precinct, log it.
            else if (hasAlreadyVoted(uuid,voteService)){
                //But give them no indication that anything is wrong.
                ctx.html("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);
                /*ctx.html("It looks like you've already gone through.\n" +
                        "You can go again, unless you're trying to mess up our data.\n\n" +
                        waitString);*/
                int precinctOld = voteService.getPrecinct(uuid);
                LOG.info(String.format("already completed, transferred from precinct %d -> %d: %s -> %s",
                        precinctOld, precinct, uuid, uuid2));
                CSVLib.logCompleteMigrate(precinctOld,precinct,uuid,uuid2);
            }
            //If the person has already scanned, log it.
            else if (isValid(uuid, voteService, precinct)){
                //But give them no indication that anything is wrong.
                ctx.html("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);
                /*ctx.html("You scanned this before, but you're here again.\n" +
                        "That's fine, we'll just pretend that you initially arrived now.\n\n" +
                        waitString);*/
                LOG.info(String.format("didn't complete, going again: %s -> %s",uuid,uuid2));
                CSVLib.logIncompleteMigrate(precinct,precinct,uuid,uuid2);
            }
            //If the person has already scanned in a different precinct, log it.
            else if (isValid(uuid, voteService)){
                //But give them no indication that anything is wrong.
                ctx.html("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);
                /*ctx.html("Hm, you seem to have transferred from another precinct.\n" +
                        "That's fine, we'll just pretend you started here.\n\n"+
                        waitString);*/
                int precinctOld = voteService.getPrecinct(uuid);
                LOG.info(String.format("didn't complete, transferred from precinct %d -> %d: " +
                        "%s -> %s",precinctOld,precinct,uuid, uuid2));
                CSVLib.logIncompleteMigrate(precinctOld,precinct,uuid,uuid2);
            }
            //And then our nice normal case.
            else {
                ctx.html("Thanks for checking in! Remember to check out at the end.\n\n" +
                        waitString);

                LOG.info("start vote handler");
            }
            LOG.info(String.format("precinct: %d", precinct));
            LOG.info(String.format("uuid start: %s \n", uuid2));
            CSVLib.logStart(precinct,uuid2);
        }
        //If something unforeseen goes wrong, log it.
        catch (Exception e) {
            //Don't give them any page.
            ctx.status(HTTP_BAD_REQUEST);
            //And log it, incl. the location, StartVoteHandler.
            LOG.error("StartVoteHandler: "+e.toString());
            e.printStackTrace();
        }

    };

    public static Handler endVoteHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            boolean noCookie;
            boolean badCookie;
            //This had to be init'd to something.
            //If it remains null, that is handled later.
            UUID uuid = null;

            //UUID.fromString will throw NullPointerException if there is no cookie
            //ctx.cookieStore will throw Exception if the JSON/base64 is invalid.
            try {
                String val = ctx.cookieStore("uuid");
                uuid = UUID.fromString(val);
                //Everything went fine, so no error-flags.
                noCookie = false;
                badCookie = false;
            } catch (NullPointerException e) {
                //NullPointer, therefore noCookie.
                noCookie = true;
                badCookie=false;
            } catch (Exception e){
                //Bad JSON/base64, therefore badCookie.
                noCookie=false;
                badCookie=true;
            }
            //If someone fed us a bad/malicious cookie, log it.
            if (badCookie) {
                //But give them no indication that anything is wrong.
                ctx.status(HTTP_OK);
                ctx.html("Thanks for checking in!");
                //Try to clear the cookies though.
                boolean cleared;
                try {
                    ctx.clearCookieStore();
                    cleared=true;
                } catch (Exception e) {
                    cleared=false;
                }
                //Log it, including success/failure of clearing.
                String clearStatus = (cleared) ? "cleared cookieStore" : "failed to clear cookieStore";
                LOG.info("Possibly malicious: Bad cookie, likely bad JSON or bad base64, " +
                        clearStatus+".\n");
            }
            //If a person has no cookie, log it.
            else if (noCookie || uuid==null){
                //But give them no indication that anything is wrong.
                ctx.status(HTTP_OK);
                //Actually, I'm torn on this one. Should we let the user
                // know that they forgot to scan the start?
                ctx.html("Thanks for checking in!");
                //ctx.html("Please scan the starting QR before the ending QR.");
                //Log it!
                LOG.info(String.format("Uncookied user requested end vote, precinct %d.\n",precinct));
            }
            //If a person gives us a valid cookie with an invalid uuid, log it.
            else if (! isValid(uuid,voteService)){
                //But give them no indication that anything is wrong.
                ctx.status(HTTP_OK);
                ctx.html("Thanks for checking in!");
                //ctx.html("You seem to have an invalid cookie for this precinct.");
                //Log it!
                LOG.info(String.format("Invalid uuid %s requested end vote, precinct %d.\n",
                        uuid, precinct));
                CSVLib.logNC_BadUUID(precinct,uuid);
            }
            //If a person gives us a valid cookie with a valid uuid from another precinct, log it.
            else if (! isValid(uuid,voteService,precinct)){
                //But give them no indication that anything is wrong.
                ctx.status(HTTP_OK);
                ctx.html("Thanks for checking in!");
                //ctx.html("You seem to have an invalid cookie for this precinct.");
                //Log it!
                int precinctOld = voteService.getPrecinct(uuid);
                LOG.info(String.format("Lost uuid %s requested end vote, %d -> %d.\n",
                        uuid, precinctOld, precinct));
                CSVLib.logNC_LostUUID(precinct,precinctOld,uuid);
            }
            //If a person tries to vote twice, log it.
            else if (hasAlreadyVoted(uuid,voteService)) {
                //But give them no indication that anything is wrong.
                ctx.status(HTTP_OK);
                ctx.html("Thanks for checking in!");
                //ctx.html("You've already visited this page.");
                //Log it!
                LOG.info(String.format("Repeated request to end vote by valid uuid %s, precinct %d.\n",
                        uuid, precinct));
                CSVLib.logNC_DoubleScan(precinct,uuid);
            } else {
                //Otherwise, everything looks good!
                long waitTime = voteService.endVote(uuid, precinct);
                ctx.html("Thanks for checking in! You waited " + waitTime + " minute(s)!");
                ctx.status(HTTP_OK);
                CSVLib.logEnd(precinct,uuid);

                //I don't understand why we would do that.
                //We want to prevent double-counting of a single voter.
                //ctx.clearCookieStore();

                //But still, log it.
                LOG.info("end vote handler");
                LOG.info(String.format("precinct: %d", precinct));
                LOG.info(String.format("uuid end: %s\n", uuid));
            }
        }
        //If something unforeseen goes wrong, log it.
        catch (Exception e) {
            //Don't give them any page.
            ctx.status(HTTP_BAD_REQUEST);
            //And log it, incl. the location, EndVoteHandler.
            LOG.error("EndVoteHandler: "+e.toString());
        }
    };

    public static Handler waitTimeHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            String waitTime = dataToJson(voteService.getWaitTime(precinct));
            if (waitTime==null || waitTime.equals("null")){
                ctx.status(HTTP_OK);
                ctx.html(String.format("We have no data for precinct %d",precinct));
                LOG.warn(String.format("No data for precinct %d\n",precinct));
            } else {
                ctx.html(String.format("Wait Time For Precinct %d  - %s", precinct, waitTime));
                ctx.status(HTTP_OK);
                ctx.html("Wait Time For Precinct " + precinct + ": " + voteService.getWaitTime(precinct) + " minute(s)");
            }
        }
        //If something unforeseen goes wrong, log it.
        catch (Exception e) {
            //Don't give them any page.
            ctx.status(HTTP_BAD_REQUEST);
            //And log it, incl. the location, WaitTimeHandler.
            LOG.error("WaitTimeHandler: "+e.toString());
        }
    };

    public static Handler getWaitTimeOverview = ctx -> {
        List<VoteCompleteModel> waitTimes = voteService.getWaitTimeOverview();
        ctx.json(waitTimes);
    };
}
