package com.christinewang;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/** A class that represents a single event in the csv_log table.
 * */
public class CSVEvent {
    Date timeStamp;
    String eventName;
    UUID uuid1;
    UUID uuid2;
    int precinct1=-1;
    int precinct2=-1;

    public String toString(){
        String dateString = timeStamp==null ? "":new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(timeStamp);
        String eventString = (eventName==null || eventName=="") ? "":eventName;
        String uuid1String = uuid1==null ? "":uuid1.toString();
        String uuid2String = uuid2==null ? "":uuid2.toString();
        String prec1String = precinct1==-1 ? "":""+precinct1;
        String prec2String = precinct2==-1 ? "":""+precinct2;

        return dateString+","+eventString+","+uuid1String+","+uuid2String+
                ","+prec1String+","+prec2String;
    }
}
