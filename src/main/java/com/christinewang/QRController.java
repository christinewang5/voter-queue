package com.christinewang;

import io.javalin.http.Handler;

import static com.christinewang.Application.LOG;
import static com.christinewang.Application.HTTP_OK;
import static com.christinewang.Application.HTTP_BAD_REQUEST;
import static com.christinewang.QRLib.*;
import static com.christinewang.QRLib.getEnd_Printout;

public class QRController {
    public static final String WEB_HOST = "https://voter-queue.herokuapp.com";
    //Deprecated because
    //public static final int WEB_PORT = 4567;
    public static final int MIN_PRECINCT = 0;
    //Set to 10 for testing purposes, will be set to real value later.
    public static final int MAX_PRECINCT = 10;

    public static Handler get_QR_startHandler = ctx -> {
        try {
            int precinct = ctx.pathParam("precinct", Integer.class).check(i -> i >= MIN_PRECINCT && i <= MAX_PRECINCT).get();
            String baseUrl = WEB_HOST + "/start_vote/";
            String QR_embed = getStart_Printout(precinct, baseUrl);
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
            String QR_embed = getEnd_Printout(precinct, baseUrl);
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
            String QR_embed = getQR(precinct, baseUrl, true);
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
            String accumulateAll = getStart_Printouts(MIN_PRECINCT, MAX_PRECINCT, baseUrl);
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
            String accumulateAll = getEnd_Printouts(MIN_PRECINCT, MAX_PRECINCT, baseUrl);
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
                accumulateAll += getQR(p, baseURL, true);
            }
            ctx.status(HTTP_OK);
            ctx.html(accumulateAll);
        } catch (Exception e) {
            LOG.error(e.toString());
            ctx.status(HTTP_BAD_REQUEST);
        }
    };
}
