package com.christinewang;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VoteModel {
    private Sql2o sql2o;

    public VoteModel(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public UUID startVote(int precinct) {
        try (Connection conn = sql2o.beginTransaction()) {
            Date start_time = new Date();
            UUID uuid = UUID.randomUUID();
            conn.createQuery("INSERT INTO vote(vote_uuid, precinct, start_time) VALUES (:vote_uuid, :precinct, :start_time)")
                    .addParameter("vote_uuid", uuid)
                    .addParameter("precinct", precinct)
                    .addParameter("start_time", start_time)
                    .executeUpdate();
            conn.commit();
            return uuid;
        }
    }

    public void endVote(UUID uuid, int precint) {
        return;
    }

    public List<Vote> getAllVotes() {
        try (Connection conn = sql2o.open()) {
            List<Vote> posts = conn.createQuery("select * from vote")
                    .executeAndFetch(Vote.class);
            return posts;
        }
    }
}
