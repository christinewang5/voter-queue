package com.christinewang;

import io.javalin.http.Handler;

import static com.christinewang.AdminController.MAX_PRECINCT;
import static com.christinewang.AdminController.MIN_PRECINCT;
import static com.christinewang.Application.*;
import static com.christinewang.HerokuUtil.LOCAL_PORT;
import static com.christinewang.QRLib.*;
import static com.christinewang.QRLib.getEnd_Printout;

public class QRController {
    public static final String WEB_HOST = isRunningLocally ?"http://localhost:"+LOCAL_PORT:"https://voter-queue.herokuapp.com";

    public static Handler get_QR_startHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            String baseUrl = WEB_HOST + "/start_vote/";
            String QR_embed = getStart_Printout(precinct, baseUrl, voteService);
            ctx.status(HTTP_OK);
            ctx.html(QR_embed);
        } catch (Exception e) {
            LOG.error(e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };

    public static Handler get_QR_endHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            String baseUrl = WEB_HOST + "/end_vote/";
            String QR_embed = getEnd_Printout(precinct, baseUrl, voteService);
            ctx.status(HTTP_OK);
            ctx.html(QR_embed);
        } catch (Exception e) {
            LOG.error(e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };

    public static Handler get_QR_waitHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();

            String baseUrl = WEB_HOST + "/wait_time/";
            String QR_embed = getQR(precinct, baseUrl, true,2);
            ctx.status(HTTP_OK);
            ctx.html(QR_embed);
        } catch (Exception e) {
            LOG.error(e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };

    public static Handler all_QR_startHandler = ctx -> {
        try {
            String baseUrl = WEB_HOST + "/start_vote/";
            String accumulateAll = getStart_Printouts(MIN_PRECINCT, MAX_PRECINCT, baseUrl, voteService);
            ctx.status(HTTP_OK);
            ctx.html(accumulateAll);
        } catch (Exception e) {
            LOG.error(e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };

    public static Handler all_QR_endHandler = ctx -> {
        try {
            String baseUrl = WEB_HOST + "/end_vote/";
            String accumulateAll = getEnd_Printouts(MIN_PRECINCT, MAX_PRECINCT, baseUrl, voteService);
            ctx.status(HTTP_OK);
            ctx.html(accumulateAll);
        } catch (Exception e) {
            LOG.error(e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };

    public static Handler all_QR_waitHandler = ctx -> {
        try {
            String baseURL = WEB_HOST + "/wait_time/";
            String accumulateAll = "";
            for (int p = MIN_PRECINCT; p <= MAX_PRECINCT; p++) {
                accumulateAll += "<p><strong>QR wait code for precinct " + p + "</strong><p>";
                accumulateAll += getQR(p, baseURL, true, 2);
            }
            ctx.status(HTTP_OK);
            ctx.html(accumulateAll);
        } catch (Exception e) {
            LOG.error(e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };
}
