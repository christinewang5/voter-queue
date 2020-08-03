package com.christinewang;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;

import static com.christinewang.Application.LOG;
import static com.christinewang.Application.voteService;

/** A library to make cleaned-up csv logs.
 * @author John Berberian
 * @author Christine Wang
 * */

public class CSVLib {
    public static final String csvName = "csv_log";
    //public static final Charset charset = Charsets.UTF_8;
    //public static CharSink charSink = Files.asCharSink(new File(csvName),charset, FileWriteMode.APPEND);
    public static boolean isFileWriteable;

    /** A function to run a test at the start of the run.
     *  This will set a boolean, isFileWriteable, which will determine
     *  If any of the other logging functions here will do anything.
     * */
    public static void logInit() {
        if
            //Write a restart message.
        (voteService.CSVLogEvent(getNow(),"=======RESTART=======")){
            //It worked! The file must be writeable.
            isFileWriteable = true;
        } else {
            //It failed. The file is not writeable.
            isFileWriteable=false;
            //Log an error message to the console.
            LOG.error(String.format("CSVLib: Error in init, does %s exist and is it writeable?",csvName));
        }
    }

    /** Logs a reset of the precinct names.
     * This would mean a change to the precinct_names table.
     * */
    public static void logNameReset() {
        //Only run if the starting check passed.
        if (isFileWriteable) {
            //Internally, surrounded in try/catch in case some error was overlooked.
            if
                //Write our status string.
            (voteService.CSVLogEvent(getNow(),"RESETNAMES")){
            } else {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logNameReset: Something is wrong with isFileWriteable, or %s " +
                        "became unwriteable.",csvName));
            }
        }
    }

    /** Logs the start of a uuid in the CSV.
     * @author John Berberian
     * @param uuid The uuid to log the start of.
     * @param precinct The precinct that the uuid belongs to.
     * */
    public static void logStart(int precinct, UUID uuid) {
        //Only run if the starting check passed.
        if (isFileWriteable) {
            //Internally, surrounded in try/catch in case some error was overlooked.
            if
                //Write our status string.
            (voteService.CSVLogEvent(getNow(),"START", uuid, precinct)){
            } else {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logStart: Something is wrong with isFileWriteable, or %s " +
                        "became unwriteable.",csvName));
            }
        }
    }

    /** Logs the migration of an incomplete uuid.
     * @author John Berberian
     * @param precinctOld The precinct that we are migrating from.
     * @param precinctNew The precinct that we are migrating to.
     * @param uuidOld The uuid that we are migrating from.
     * @param uuidNew The uuid that we are migrating to.
     * */
    public static void logIncompleteMigrate(int precinctOld,int precinctNew, UUID uuidOld, UUID uuidNew) {
        //Only run if the starting check passed.
        if (isFileWriteable) {
            //Internally, surrounded in try/catch in case some error was overlooked.
            if
                //Write our status string.
            (voteService.CSVLogEvent(getNow(),"MIGRATE.INCOMPLETE", uuidOld, uuidNew,
                        precinctOld, precinctNew)){
            } else {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logIncompleteMigrate: Something is wrong with isFileWriteable," +
                        " or %s became unwriteable.",csvName));
            }
        }
    }

    /** Logs the migration of a completed uuid.
     * @author John Berberian
     * @param precinctOld The precinct that we are migrating from.
     * @param precinctNew The precinct that we are migrating to.
     * @param uuidOld The uuid that we are migrating from.
     * @param uuidNew The uuid that we are migrating to.
     * */
    public static void logCompleteMigrate(int precinctOld,int precinctNew, UUID uuidOld, UUID uuidNew) {
        //Only run if the starting check passed.
        if (isFileWriteable) {
            //Internally, surrounded in try/catch in case some error was overlooked.
            if
                //Write our status string.
                (voteService.CSVLogEvent( getNow(),"MIGRATE.COMPLETE",
                    uuidOld, uuidNew, precinctOld, precinctNew)){
            } else {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logCompleteMigrate: Something is wrong with isFileWriteable," +
                        " or %s became unwriteable.",csvName));
            }
        }
    }

    /** Logs the end of a uuid in the CSV.
     * @author John Berberian
     * @param uuid The uuid to log the end of.
     * @param precinct The precinct that the uuid belongs to.
     * */
    public static void logEnd(int precinct, UUID uuid) {
        //Only run if the starting check passed.
        if (isFileWriteable) {
            //Internally, surrounded in try/catch in case some error was overlooked.
            if
                //Write our status string.
                (voteService.CSVLogEvent( getNow(),"END", uuid, precinct)){
            } else {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logEnd: Something is wrong with isFileWriteable, or %s " +
                        "became unwriteable.",csvName));
            }
        }
    }

    /** Logs an end request that referenced an invalid UUID.
     * Note: This records an event that caused NO CHANGE in the db state, hence the NC prefix.
     * @author John Berberian
     * @param uuid The invalid uuid.
     * @param precinct The precinct from which this request came.
     * */
    public static void logNC_BadUUID(int precinct, UUID uuid) {
        //Only run if the starting check passed.
        if (isFileWriteable) {
            //Internally, surrounded in try/catch in case some error was overlooked.
            if
                //Write our status string.
                (voteService.CSVLogEvent(getNow(), "NOCHANGE.BADUUID", uuid, precinct)){
            } else {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logNC_BadUUID: Something is wrong with isFileWriteable," +
                        " or %s became unwriteable.",csvName));
            }
        }
    }

    /** Logs an end request that referenced a UUID from another precinct.
     * Note: This records an event that caused NO CHANGE in the db state, hence the NC prefix.
     * @author John Berberian
     * @param uuid The uuid from another precinct.
     * @param precinctRegistered The precinct that the uuid belongs to.
     * @param precinct The precinct that the uuid attempted to end in.
     * */
    public static void logNC_LostUUID(int precinct, int precinctRegistered, UUID uuid) {
        //Only run if the starting check passed.
        if (isFileWriteable) {
            //Internally, surrounded in try/catch in case some error was overlooked.
            if
                //Write our status string.
                (voteService.CSVLogEvent(getNow(), "NOCHANGE.LOSTUUID",
                         uuid, precinct, precinctRegistered)){
            } else {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logNC_LostUUID: Something is wrong with isFileWriteable," +
                        " or %s became unwriteable.",csvName));
            }
        }
    }

    /** Logs an end request that referenced an already-ended UUID.
     * Note: This records an event that caused NO CHANGE in the db state, hence the NC prefix.
     * @author John Berberian
     * @param uuid The uuid that attempted to double-end.
     * @param precinct The precinct that the uuid belongs to.
     * */
    public static void logNC_DoubleScan(int precinct, UUID uuid) {
        //Only run if the starting check passed.
        if (isFileWriteable) {
            //Internally, surrounded in try/catch in case some error was overlooked.
            if
                //Write our status string.
                (voteService.CSVLogEvent(getNow(), "" +
                    "NOCHAGE.DOUBLESCAN", uuid, precinct)){
            } else {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logNC_DoubleScan: Something is wrong with isFileWriteable," +
                        " or %s became unwriteable.",csvName));
            }
        }
    }

    /** A nice function to get the current time, for CSV logging.
     *  Now does uselessly little, but keeping it anyway.
     * @author John Berberian
     * */
    public static Date getNow() {
        return (new Date());
    }

    /** Parses a CSV, will convert quoted ints to ints, e.g. "1" -> 1.
     * @param fileContent The full file dump, including newline characters.
     * @return a List of Lists, representing the columns of the file.
     * */
    public static List<ArrayList> parseCSV(String fileContent) {
        ArrayList<ArrayList> columns = new ArrayList<ArrayList>();
        String regex="\n";
        if (fileContent.contains("\r\n")) {
            regex="\r\n";
        }
        String[] rows = fileContent.split(regex);
        boolean isInt=false;
        for (String row : rows) {
            int i=0;
            for (String elem : row.split(",")) {
                if (elem!=null && !elem.equals("")) {
                    try {
                        int k = Integer.parseInt(elem);
                        isInt = true;
                    } catch (NumberFormatException e) {
                        isInt = false;
                    }
                    if (columns.size() <= i) {
                        if (isInt) {
                            columns.add(new ArrayList<Integer>());
                        } else {
                            columns.add(new ArrayList<String>());
                        }
                    }
                    if (isInt) {
                        columns.get(i).add(Integer.parseInt(elem));
                    } else {
                        columns.get(i).add(elem);
                    }
                }
                i++;
            }
        }
        return columns;
    }

    /** Gets a string representation of the CSV log.
     * @return A string representation of the CSV file, with newlines
     * separating lines, and commas separating values in each line.
     * */
    public static String getCSVLog() {
        List<CSVEvent> events = voteService.getCSVEvents();
        StringBuilder lines = new StringBuilder();
        events.forEach((event) -> lines.append(event.toString()+"\n"));
        return lines.toString().trim();
    }

    /** Gets a string representation of all the CSV log's event since a certain Date.
     * @param epoch The date to start looking for events from.
     * @return A string representation of the CSV file, with newlines
     * separating lines, and commas separating values in each line.
     * */
    public static String getCSVLog(Date epoch) {
        List<CSVEvent> events = voteService.getCSVEvents(epoch);
        StringBuilder lines = new StringBuilder();
        events.forEach((event) -> lines.append(event.toString()+"\n"));
        return lines.toString().trim();
    }
}
