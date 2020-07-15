package com.christinewang;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/** An object to hold a just-started vote.
 * @author Christine Wang
 * @author John Berberian
 * */
@Data
public class VoteModel {
    private UUID uuid;
    private int precinct;
    private Date startTime;
    private String name; //Name of the precinct

    public Date getStartTime() {
        return startTime;
    }
    public UUID getUUID(){ return uuid;}
    public String getName() { return name; }
    public void setName(String newName) { name = newName; }
    public int getPrecinct() { return precinct; }
}
