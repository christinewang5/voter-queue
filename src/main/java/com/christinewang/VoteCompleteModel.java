package com.christinewang;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class VoteCompleteModel {
    private UUID uuid;
    private int precinct;
    private double waitTime; //Wait time in minutes.

    public double getWaitTime() {
        return waitTime;
    }
    public UUID getUUID(){ return uuid;}

}
