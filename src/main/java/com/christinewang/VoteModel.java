package com.christinewang;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class VoteModel {
    private UUID uuid;
    private int precinct;
    private Date startTime;

    public Date getStartTime() {
        return startTime;
    }
}
