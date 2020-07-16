package com.christinewang;

import java.util.Date;
import java.util.UUID;

public class NameModel {
    private int precinct;
    private String name; //Name of the precinct
    
    public String getName() { return name; }
    public void setName(String newName) { name = newName; }
    public int getPrecinct() { return precinct; }
}
