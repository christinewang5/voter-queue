package com.christinewang;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.christinewang.Application.LOG;

/**
 * VoteService contains the functions to interact with the database.
 */
public class VoteService {
    private Sql2o sql2o;

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

    public Integer getWaitTime(int precinct) throws Exception {
        try (Connection conn = sql2o.open()) {
            List<Integer> waitTime = conn.createQuery("SELECT AVG(waitTime) FROM complete_vote WHERE precinct=:precinct")
                .addParameter("precinct", precinct)
                .executeAndFetch(Integer.class);
            if (waitTime.isEmpty()) throw new Exception("No data for precinct.");
            return waitTime.get(0);
        }
    }

    // TODO - remove this later, for debugging
    public List<VoteCompleteModel> getAllCompleteVotes() {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteCompleteModel> votes = conn.createQuery("SELECT * FROM complete_vote")
                .executeAndFetch(VoteCompleteModel.class);
            conn.commit();
            return votes;
        }
    }

    // TODO - remove this later, for debugging
    public List<VoteModel> getAllVotes() {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteModel> votes = conn.createQuery("SELECT * FROM vote")
                .executeAndFetch(VoteModel.class);
            conn.commit();
            return votes;
        }
    }

    public List<VoteCompleteModel> getWaitTimeOverview() {
        try (Connection conn = sql2o.open()) {
            List<VoteCompleteModel> waitTimes = conn.createQuery("SELECT precinct, AVG(waitTime) AS waitTime FROM complete_vote GROUP BY precinct ORDER BY precinct")
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
        try (Connection conn = sql2o.open()) {
            List<VoteModel> votes = conn.createQuery("SELECT * FROM vote WHERE precinct=:precinct")
                .addParameter("precinct", precinct)
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
        try (Connection conn = sql2o.open()) {
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
        try (Connection conn = sql2o.open()) {
            List<Integer> precincts = conn.createQuery("SELECT precinct FROM vote WHERE uuid=:uuid")
                    .addParameter("uuid",uuid)
                    .executeAndFetch(Integer.class);
            if (precincts.size()>1) {
                LOG.error(String.format("Error in getPrecinct: %d precincts found for uuid %s, choosing first.",
                        precincts.size(),uuid));
            }
            return precincts.get(0);
        }
    }
}
