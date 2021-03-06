package com.christinewang;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.sql.SQLDataException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.christinewang.Application.*;

/**
 * VoteService contains the functions to interact with the database.
 */
public class VoteService {
    private Sql2o sql2o;
    //Sets the current epoch to whenever the server started.
    private Date epoch = new Date();

    //These will be used if the database cannot be read.
    public static final int DEFAULT_MINPREC=0;
    public static final int DEFAULT_MAXPREC=10;

    public VoteService(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public UUID startVote(int precinct) {
        try (Connection conn = sql2o.beginTransaction()) {
            UUID uuid = UUID.randomUUID();
            conn.createQuery("INSERT INTO vote(uuid, precinct, startTime) VALUES (:uuid, :precinct, :startTime)")
                .addParameter("uuid", uuid)
                .addParameter("precinct", precinct)
                .addParameter("startTime", new Date())
                .executeUpdate();
            conn.commit();
            return uuid;
        }
    }

    public long endVote(UUID uuid, int precinct) throws Exception {
        long waitTime = 0;
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteModel> voteModels = conn.createQuery("SELECT * FROM vote WHERE uuid=:uuid")
                .addParameter("uuid", uuid)
                .executeAndFetch(VoteModel.class);
            if (voteModels.isEmpty()) throw new Exception("No check in data. Failed to checkout.");
            Date startTime = voteModels.get(0).getStartTime();
            long waitTimeInMs = new Date().getTime() - startTime.getTime();
            waitTime = TimeUnit.MINUTES.convert(waitTimeInMs, TimeUnit.MILLISECONDS);
            conn.createQuery("INSERT INTO complete_vote(uuid, precinct, waitTime) VALUES (:uuid, :precinct, :waitTime)")
                .addParameter("uuid", uuid)
                .addParameter("precinct", precinct)
                .addParameter("waitTime", waitTime)
                .executeUpdate();
            conn.commit();
        }
        return waitTime;
    }

    public VoteCompleteModel getWaitTime(int precinct) throws Exception {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteCompleteModel> waitTime = conn.createQuery("SELECT * FROM " +
                        "(" +
                        "SELECT a.precinct,avg(waittime) AS waittime FROM " +
                            "(SELECT * FROM complete_vote WHERE precinct=:precinct) a " +
                        "JOIN " +
                        "(SELECT * FROM vote WHERE starttime>=:epoch) b " +
                            "ON a.uuid=b.uuid GROUP BY a.precinct" +
                        ") a " +
                    "JOIN " +
                        "(SELECT name FROM precinct_names WHERE precinct=:precinct) b " +
                    "ON 1=1")
                    .addParameter("precinct", precinct)
                    .addParameter("epoch", epoch)
                    .executeAndFetch(VoteCompleteModel.class);
            conn.commit();
            if (waitTime.isEmpty()) throw new SQLDataException("No data for precinct.");
            return waitTime.get(0);
        }
    }

    // TODO - remove this later, for debugging
    public List<VoteCompleteModel> getAllCompleteVotes() {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteCompleteModel> votes = conn.createQuery("SELECT a.uuid,a.precinct,waittime FROM " +
                        "(SELECT * FROM complete_vote) a " +
                    "JOIN " +
                        "(SELECT * FROM vote WHERE startTime>=:epoch) b " +
                    "ON a.uuid=b.uuid")
                    .addParameter("epoch",epoch)
                    .executeAndFetch(VoteCompleteModel.class);
            conn.commit();
            return votes;
        }
    }

    // TODO - remove this later, for debugging
    public List<VoteModel> getAllVotes() {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteModel> votes = conn.createQuery("SELECT * FROM vote WHERE startTime>=:epoch")
                    .addParameter("epoch",epoch)
                    .executeAndFetch(VoteModel.class);
            conn.commit();
            return votes;
        }
    }

    public List<VoteCompleteModel> getWaitTimeOverview() {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteCompleteModel> waitTimes = conn.createQuery("SELECT a.precinct,waittime,name FROM " +
                        "(" +
                        "SELECT a.precinct,ROUND(avg(waitTime),2) AS waitTime FROM " +
                            "(SELECT * FROM complete_vote) a " +
                        "JOIN " +
                            "(SELECT * FROM vote WHERE starttime>=:epoch) b " +
                        "ON a.uuid=b.uuid GROUP BY a.precinct ORDER BY a.precinct" +
                        ") a " +
                    "JOIN " +
                        "(SELECT * FROM precinct_names) b " +
                    "ON a.precinct=b.precinct;")
                    .addParameter("epoch",epoch)
                    .executeAndFetch(VoteCompleteModel.class);
            return waitTimes;
        }
    }

    /** A method to get all votes for a given precinct.
     * @author John Berberian
     * @param precinct The precinct in question, by number.
     * @return A List of VoteModel, representing the votes for the precinct
     */
    public List<VoteModel> getPrecinctVotes(int precinct) {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteModel> votes = conn.createQuery("SELECT * FROM vote WHERE precinct=:precinct AND starttime>=:epoch")
                    .addParameter("precinct", precinct)
                    .addParameter("epoch", epoch)
                    .executeAndFetch(VoteModel.class);
            return votes;
        }
    }

    /** A method to get all complete votes for a given precinct.
     * @author John Berberian
     * @param precinct The precinct in question, by number.
     * @return A List of VoteCompleteModel, representing
     * the complete votes for the precinct
     */
    public List<VoteCompleteModel> getPrecinctCompleteVotes(int precinct) {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteCompleteModel> votes = conn.createQuery("SELECT * FROM complete_vote WHERE precinct=:precinct")
                .addParameter("precinct", precinct)
                .executeAndFetch(VoteCompleteModel.class);
            return votes;
        }
    }

    /** A method to get the precinct of a given uuid.
     * @author John Berberian
     * @param uuid The uuid to find the precinct of.
     * @return The precinct number.
     * */
    public int getPrecinct(UUID uuid) {
        try (Connection conn = sql2o.beginTransaction()) {
            List<Integer> precincts = conn.createQuery("SELECT precinct FROM vote WHERE uuid=:uuid AND starttime>=:epoch")
                    .addParameter("uuid",uuid)
                    .addParameter("epoch",epoch)
                    .executeAndFetch(Integer.class);
            conn.commit();
            if (precincts.size()>1) {
                LOG.error(String.format("Error in getPrecinct: %d precincts found for uuid %s, choosing first.",
                        precincts.size(),uuid));
            }
            return precincts.get(0);
        }
    }

    /** A method to get the name of a given precinct.
     * @author John Berberian
     * @param precinct The precinct to get the name of.
     * @return The precinct's name.
     * */
    public String getName(int precinct) {
        try (Connection conn = sql2o.beginTransaction()) {
            List<String> names = conn.createQuery("SELECT name FROM precinct_names WHERE precinct=:precinct")
                    .addParameter("precinct",precinct)
                    .executeAndFetch(String.class);
            conn.commit();
            if (names.size()>1) {
                LOG.error(String.format("Error in getName: %d names found for precinct %s, choosing first.",
                        names.size(),precinct));
            }
            return names.get(0);
        }
    }

    /** Gets a list of precincts and names, in NameModels.
     * @return The List< NameModel > of names.
     * */
    public List<NameModel> getPrecinctNameList() {
        try (Connection conn = sql2o.beginTransaction()) {
            List<NameModel> names = conn.createQuery("SELECT * FROM precinct_names")
                    .executeAndFetch(NameModel.class);
            conn.commit();
            return names;
        }
    }

    /** Logs an event in the csv_log table.
     * @param timeStamp The time the event occurred.
     * @param eventName A string representing what happened.
     * @param uuid1 The first uuid involved (meaning of this depends on eventName)
     * @param uuid2 The second uuid involved (meaning of this depends on eventName)
     * @param precinct1 The first precinct involved (meaning of this depends on eventName)
     * @param precinct2 The second precinct involved (meaning of this depends on eventName)
     * @return True for success, false for failure.
     * */
    public boolean CSVLogEvent(Date timeStamp, String eventName, UUID uuid1, UUID uuid2, int precinct1, int precinct2) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery("INSERT INTO csv_log(timeStamp, eventName, uuid1, uuid2, precinct1, precinct2) VALUES " +
                    "(:timeStamp, :eventName, :uuid1, :uuid2, :precinct1, :precinct2)")
                    .addParameter("timeStamp", timeStamp)
                    .addParameter("eventName",eventName)
                    .addParameter("uuid1",uuid1)
                    .addParameter("uuid2",uuid2)
                    .addParameter("precinct1", precinct1)
                    .addParameter("precinct2", precinct2)
                    .executeUpdate();
            conn.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Logs an event in the csv_log table.
     * @param timeStamp The time the event occurred.
     * @param eventName A string representing what happened.
     * @param uuid1 The first uuid involved (meaning of this depends on eventName)
     * @param precinct1 The first precinct involved (meaning of this depends on eventName)
     * @return True for success, false for failure.
     * */
    public boolean CSVLogEvent(Date timeStamp, String eventName, UUID uuid1, int precinct1) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery("INSERT INTO csv_log(timeStamp, eventName, uuid1, precinct1) VALUES " +
                    "(:timeStamp, :eventName, :uuid1, :precinct1)")
                    .addParameter("timeStamp", timeStamp)
                    .addParameter("eventName",eventName)
                    .addParameter("uuid1",uuid1)
                    .addParameter("precinct1", precinct1)
                    .executeUpdate();
            conn.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Logs an event in the csv_log table.
     * @param timeStamp The time the event occurred.
     * @param eventName A string representing what happened.
     * @param uuid1 The first uuid involved (meaning of this depends on eventName)
     * @param precinct1 The first precinct involved (meaning of this depends on eventName)
     * @param precinct2 The second precinct involved (meaning of this depends on eventName)
     * @return True for success, false for failure.
     * */
    public boolean CSVLogEvent(Date timeStamp, String eventName, UUID uuid1, int precinct1, int precinct2) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery("INSERT INTO csv_log(timeStamp, eventName, uuid1, precinct1, precinct2) VALUES " +
                    "(:timeStamp, :eventName, :uuid1, :precinct1, :precinct2)")
                    .addParameter("timeStamp", timeStamp)
                    .addParameter("eventName",eventName)
                    .addParameter("uuid1",uuid1)
                    .addParameter("precinct1", precinct1)
                    .addParameter("precinct2", precinct2)
                    .executeUpdate();
            conn.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Logs an event in the csv_log table.
     * @param timeStamp The time the event occurred.
     * @param eventName A string representing what happened.
     * @return True for success, false for failure.
     * */
    public boolean CSVLogEvent(Date timeStamp, String eventName) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery("INSERT INTO csv_log(timeStamp, eventName) VALUES " +
                    "(:timeStamp, :eventName)")
                    .addParameter("timeStamp", timeStamp)
                    .addParameter("eventName",eventName)
                    .executeUpdate();
            conn.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Gets all the CSV events in the DB.
     * @return A List of CSVEvents.
     * */
    public List<CSVEvent> getCSVEvents() {
        try (Connection conn = sql2o.beginTransaction()) {
            List<CSVEvent> events = conn.createQuery("SELECT * FROM csv_log")
                    .executeAndFetch(CSVEvent.class);
            return events;
        } catch (Exception e) {
            LOG.error("In getCSVEvents: error getting events!");
            return new ArrayList<CSVEvent>();
        }
    }

    /** Gets the CSV events in the DB since a certain date.
     * @param temp_epoch A date, such that we should only return events
     *              that occurred after that date.
     * @return A List of CSVEvents.
     * */
    public List<CSVEvent> getCSVEvents(Date temp_epoch) {
        try (Connection conn = sql2o.beginTransaction()) {
            List<CSVEvent> events = conn.createQuery("SELECT * FROM csv_log WHERE timeStamp>=:epoch")
                    .addParameter("epoch", temp_epoch)
                    .executeAndFetch(CSVEvent.class);
            return events;
        } catch (Exception e) {
            LOG.error("In getCSVEvents: error getting events!");
            return new ArrayList<CSVEvent>();
        }
    }

    /** Function to change the precinct_names table.
     * Also resets the epoch that everything runs on.
     * Effectively a non-destructive wipe of our database.
     * */
    public boolean changeNames(List<Integer> precincts,List<String> names) {
        //LOG.info("beginning");
        try {
            //LOG.info("started");
            //Remove the previous precinct names
            try (Connection conn = sql2o.beginTransaction()) {
                conn.createQuery("DELETE FROM precinct_names").executeUpdate();
                conn.commit();
            } catch (Exception e) {
                return false;
            }
            //LOG.info("emptied table");
            //Make a table to hold the new values
            /*try (Connection conn = sql2o.beginTransaction()) {
                conn.createQuery("CREATE TABLE precinct_names(precinct INT, name TEXT)")
                        .executeUpdate();
                conn.commit();
            } catch (Exception e) {
                return false;
            }*/
            //LOG.info("recreated table");
            //And add the new values in.
            try (Connection conn = sql2o.beginTransaction()) {
                Query q = conn.createQuery("INSERT INTO precinct_names(precinct, name) VALUES " +
                        "(:precinct, :name)");
                for (int i = 0; i < names.size(); i++) {
                            q.addParameter("precinct", precincts.get(i))
                            .addParameter("name", names.get(i))
                            .addToBatch();
                    //LOG.info("Added element");
                }
                q.executeBatch();
                //LOG.info("Added elements");
                conn.commit();
                //LOG.info("Committed");
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        //Reset the epoch
        epoch=new Date();
        CSVLib.logNameReset();
        startURLs = CryptoLib.get_StartURLs();
        endURLs = CryptoLib.get_EndURLs();
        return true;
    }

    /** Gets the max precinct number in the db.
     * @return The result of "SELECT MAX(precinct) FROM precinct_names", or
     * a default value, defined in this class.
     * */
    public int getMaxPrecinct() {
        try (Connection conn = sql2o.beginTransaction()) {
            Query q = conn.createQuery("SELECT MAX(precinct) FROM precinct_names");
            Integer maxprec = q.executeAndFetch(Integer.class).get(0);
            return maxprec;
        } catch (Exception e) {
            LOG.error(String.format("Could not get max precinct! Defaulting to %d.",DEFAULT_MAXPREC));
            return DEFAULT_MAXPREC;
        }
    }

    /** Gets the min precinct number in the db.
     * @return The result of "SELECT MIN(precinct) FROM precinct_names", or
     * a default value, defined in this class.
     * */
    public int getMinPrecinct() {
        try (Connection conn = sql2o.beginTransaction()) {
            Query q = conn.createQuery("SELECT MIN(precinct) FROM precinct_names");
            Integer minprec = q.executeAndFetch(Integer.class).get(0);
            return minprec;
        } catch (Exception e) {
            LOG.error(String.format("Could not get min precinct! Defaulting to %d.",DEFAULT_MINPREC));
            return DEFAULT_MINPREC;
        }
    }

    public String getPassHash_b64(String username) {
        try (Connection conn = sql2o.beginTransaction()) {
            Query q = conn.createQuery("SELECT hash_b64 FROM creds WHERE username=:username")
                    .addParameter("username", username);
            String passhash = q.executeAndFetch(String.class).get(0);
            return passhash;
        } catch (Exception e) {
            LOG.error("getPassHash_b64: "+e.toString());
            return "ERROR";
        }
    }

    public String getSalt_b64(String username) {
        try (Connection conn = sql2o.beginTransaction()) {
            Query q = conn.createQuery("SELECT salt_b64 FROM creds WHERE username=:username")
                    .addParameter("username", username);
            String salt = q.executeAndFetch(String.class).get(0);
            return salt;
        } catch (Exception e) {
            LOG.error("getSalt_b64: "+e.toString());
            return "ERROR";
        }
    }

    public byte[] getPassHash(String username) {
        String hash_b64 = getPassHash_b64(username);
        if (hash_b64==null) {
            return new byte[32];
        }
        byte[] hash = Base64.getDecoder().decode(hash_b64);
        return hash;
    }

    public byte[] getSalt(String username) {
        String salt_b64 = getSalt_b64(username);
        if (salt_b64==null) {
            return new byte[32];
        }
        byte[] salt = Base64.getDecoder().decode(salt_b64);
        return salt;
    }

    public boolean changeHash(String username, String hash_b64, String salt_b64) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery("DELETE FROM creds WHERE username=:username")
                    .addParameter("username", username).executeUpdate();
            conn.commit();
        } catch (Exception e) {
            LOG.error("changeHash: In delete section: "+e.toString());
            return false;
        } try (Connection conn = sql2o.beginTransaction()) {
            Query q = conn.createQuery("INSERT INTO creds(username, hash_b64, salt_b64) VALUES " +
                    "(:username, :hash_b64, :salt_b64)")
                    .addParameter("username", username)
                    .addParameter("hash_b64", hash_b64)
                    .addParameter("salt_b64", salt_b64);
            q.executeUpdate();
            conn.commit();
            return true;
        } catch (Exception e) {
            LOG.error("changeHash: In insert section: "+e.toString());
            return false;
        }
    }
}
