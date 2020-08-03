package com.christinewang;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class VoteCompleteModel {
    private UUID uuid;
    private int precinct;
    private double waitTime; //Wait time in minutes.
    private String name;

    public VoteCompleteModel(int precinct, double waitTime, String name) {
        this.precinct=precinct;
        this.waitTime=waitTime;
        this.name=name;
    }
    public String getName() {return name;}
    public void setName(String newName) {name=newName;}
    public double getWaitTime() {
        return waitTime;
    }
    public int getPrecinct() {return precinct;}
    public UUID getUUID(){ return uuid;}

}
