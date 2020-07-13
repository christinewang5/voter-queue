package com.christinewang;

import io.javalin.http.Handler;

import java.util.UUID;

import static com.christinewang.Application.voteService;
import static com.christinewang.Application.LOG;


// TODO - add test
public class VoteController {

    public static Handler startVoteHandler = ctx -> {
        int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
        UUID uuid = voteService.startVote(precinct);
        ctx.cookieStore("uuid", uuid);
        ctx.result("Thanks for checking in! Remember to check out at the end.");

        LOG.info("start vote handler");
        LOG.info(String.format("precinct: %d\n", precinct));
        LOG.info(String.format("uuid start: %s \n", uuid));
    };

    public static Handler endVoteHandler = ctx -> {
        int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
        String val = ctx.cookieStore("uuid");
        UUID uuid  = UUID.fromString(val);
        long waitTime = voteService.endVote(uuid, precinct);
        ctx.result("Thanks for checking in! You waited "+waitTime+" minute(s)!");


        LOG.info("end vote handler") ;
        LOG.info(String.format("precinct: %d\n",precinct));
        LOG.info(String.format("uuid end: %s\n", uuid));
    };

    public static Handler waitTimeHandler = ctx -> {
        int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i > 0 && i < 10).get();
        int waitTime = voteService.getWaitTime(precinct);
        ctx.result(String.format("Wait Time For Precinct %d  - %d",  precinct, waitTime));
    };
}
