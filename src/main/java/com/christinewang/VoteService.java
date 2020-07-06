package com.christinewang;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    public void endVote(UUID uuid, int precinct) throws Exception {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteModel> voteModels = conn.createQuery("SELECT * FROM vote WHERE uuid=:uuid")
                    .addParameter("uuid", uuid)
                    .executeAndFetch(VoteModel.class);
            if (voteModels.isEmpty()) throw new Exception("No check in data. Failed to checkout.");
            Date startTime = voteModels.get(0).getStartTime();
            long waitTimeInMs = new Date().getTime() - startTime.getTime();
            long waitTime = TimeUnit.MINUTES.convert(waitTimeInMs, TimeUnit.MILLISECONDS);
//            System.out.printf("startTime: %d\n", startTime.getTime());
//            System.out.printf("waitTime: %d\n", waitTime);
            conn.createQuery("INSERT INTO complete_vote(uuid, precinct, waitTime) VALUES (:uuid, :precinct, :waitTime)")
                    .addParameter("uuid", uuid)
                    .addParameter("precinct", precinct)
                    .addParameter("waitTime", waitTime)
                    .executeUpdate();
            conn.commit();
        }
        return;
    }

    public Integer getWaitTime(int precinct) throws Exception{
        try (Connection conn = sql2o.open()) {
            List<Integer> waitTime = conn.createQuery("SELECT AVG(waitTime) FROM complete_vote WHERE precinct=:precinct")
                    .addParameter("precinct", precinct)
                    .executeAndFetch(Integer.class);
            if (waitTime.isEmpty()) throw new Exception("No data for precinct.");
            return waitTime.get(0);
        }
    }

    // TODO - remove this later, for debugging
    public List<VoteModel> getAllCompleteVotes() {
        try (Connection conn = sql2o.beginTransaction()) {
            List<VoteModel> votes = conn.createQuery("SELECT * FROM complete_vote")
                    .executeAndFetch(VoteModel.class);
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
}
