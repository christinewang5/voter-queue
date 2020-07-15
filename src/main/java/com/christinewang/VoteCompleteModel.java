package com.christinewang;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/** An object to hold a completed vote.
 * @author Christine Wang
 * @author John Berberian
 * */
@Data
public class VoteCompleteModel {
    private UUID uuid;
    private int precinct;
    private double waitTime; //Wait time in minutes.
    private String name; //Name of the precinct

    public double getWaitTime() {
        return waitTime;
    }
    public UUID getUUID(){ return uuid; }
    public String getName() { return name; }
    public void setName(String newName) { name = newName; }
    public int getPrecinct() { return precinct; }
}
