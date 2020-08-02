package com.christinewang;

import io.javalin.http.Handler;
import spark.utils.IOUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.christinewang.Application.*;
import static com.christinewang.CSVLib.getCSVLog;

public class AdminController {
    public static int MIN_PRECINCT = 0;
    //Set to 10 for testing purposes, will be set to real value later.
    public static int MAX_PRECINCT = 10;

    public static Handler upload = ctx -> {
        //Converted to Atomic, because otherwise Java was yelling about final variables in lambdas.
        AtomicBoolean success= new AtomicBoolean(true);
        LOG.info(uploadsalt);
        ctx.uploadedFiles(uploadsalt).forEach(file -> {
            try {
                StringWriter writer = new StringWriter();
                IOUtils.copy(file.getContent(), writer);
                //Gets the contents as a string.
                String fileContent = writer.toString();
                //Parses the contents, and gets a List of the columns.
                List<ArrayList> cols = CSVLib.parseCSV(fileContent);
                //Then refreshes the names, using the first two columns.
                List<Integer> precincts = cols.get(0);
                List<String> names = cols.get(1);
                //LOG.info("csv parsed");
                try {
                    //LOG.info("before changenames");
                    if (!voteService.changeNames(precincts, names)) {
                        throw new IOException("Bad csv format!");
                    }
                    //LOG.info("after changenames");
                } catch (IOException e) {
                    LOG.error("problem in changeNames.");
                }
            } catch (IOException e) {
                LOG.error("upload-precinctnames: cannot read file.");
                success.set(false);
            }
        });
        if (success.get()) {
            ctx.html("Upload complete");
        } else {
            ctx.html("Upload failure! Wrong format?");
        }
    };

    public static Handler csvStaticIsh = ctx -> {
        ctx.status(HTTP_OK);
        ctx.result(getCSVLog()).contentType("text/csv")
                .header("Content-Disposition", "attachment; filename=VoterQueueLog.csv");
    };
}
