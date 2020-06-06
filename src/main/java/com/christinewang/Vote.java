package com.christinewang;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class Vote {
    private UUID vote_uuid;
    private int precinct;
    private Date start_time;
}
