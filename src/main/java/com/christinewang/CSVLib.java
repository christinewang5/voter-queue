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
import java.util.UUID;

import static com.christinewang.Application.LOG;

/** A library to make cleaned-up csv logs.
 * @author John Berberian
 * @author Christine Wang
 * */

public class CSVLib {
    public static final String csvName = "log.csv";
    public static final Charset charset = Charsets.UTF_8;
    public static CharSink charSink = Files.asCharSink(new File(csvName),charset, FileWriteMode.APPEND);
    public static boolean isFileWriteable;

    /** A function to run a test at the start of the run.
     *  This will set a boolean, isFileWriteable, which will determine
     *  If any of the other logging functions here will do anything.
     * */
    public static void logInit() {
        try {
            //Write a restart message.
            charSink.write("=======RESTART=======\n");
            //It worked! The file must be writeable.
            isFileWriteable = true;
        } catch (IOException e) {
            //It failed. The file is not writeable.
            isFileWriteable=false;
            //Log an error message to the console.
            LOG.error(String.format("CSVLib: Error in init, does %s exist and is it writeable?",csvName));
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
            //Surrounded in try/catch in case some error was overlooked.
            try {
                //Write our status string.
                charSink.write(String.format("%s,START,%d,%s\n", getNow(), precinct, uuid));
            } catch (IOException e) {
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
            //Surrounded in try/catch in case some error was overlooked.
            try {
                //Write our status string.
                charSink.write(String.format("%s,MIGRATE.INCOMPLETE,%d,%d,%s,%s\n", getNow(),
                        precinctOld, precinctNew, uuidOld, uuidNew));
            } catch (IOException e) {
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
            //Surrounded in try/catch in case some error was overlooked.
            try {
                //Write our status string.
                charSink.write(String.format("%s,MIGRATE.COMPLETE,%d,%d,%s,%s\n", getNow(),
                        precinctOld, precinctNew, uuidOld, uuidNew));
            } catch (IOException e) {
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
            //Surrounded in try/catch in case some error was overlooked.
            try {
                //Write our status string.
                charSink.write(String.format("%s,END,%d,%s\n", getNow(), precinct, uuid));
            } catch (IOException e) {
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
            //Surrounded in try/catch in case some error was overlooked.
            try {
                //Write our status string.
                charSink.write(String.format("%s,NOCHANGE.BADUUID,%d,%s\n", getNow(), precinct, uuid));
            } catch (IOException e) {
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
            //Surrounded in try/catch in case some error was overlooked.
            try {
                //Write our status string.
                charSink.write(String.format("%s,NOCHANGE.LOSTUUID,%d,%d,%s\n", getNow(),
                        precinct, precinctRegistered, uuid));
            } catch (IOException e) {
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
            //Surrounded in try/catch in case some error was overlooked.
            try {
                //Write our status string.
                charSink.write(String.format("%s,NOCHAGE.DOUBLESCAN,%d,%s\n", getNow(), precinct, uuid));
            } catch (IOException e) {
                //Hm, something went wrong, and we didn't expect it to. Log it!
                LOG.error(String.format("logNC_DoubleScan: Something is wrong with isFileWriteable," +
                        " or %s became unwriteable.",csvName));
            }
        }
    }

    /** A nice function to get the current time, for CSV logging.
     * @author John Berberian
     * */
    public static String getNow() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}
