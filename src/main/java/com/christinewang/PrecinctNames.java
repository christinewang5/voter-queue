package com.christinewang;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

/** A class to handle the names of the precincts.
 *  Grabs the data from a file, "precinctNames.csv"
 *  Can be exported from Excel.
 *  @author John Berberian
 * */
public class PrecinctNames {
    public static ArrayList<String> precinctNames;
    public static int MAX_PRECINCT;
    public static int MIN_PRECINCT;
    public static final String fileName = "precinctNames.csv";

    /** Just here so that I can call the non-static init method.
     * @author John Berberian
     * */
    public PrecinctNames() throws IOException {
        initNames();
    }

    /** Just here so that I can call the non-static init method.
     * @author John Berberian
     * */
    public PrecinctNames(String alternativeName) throws IOException {
        initNames(alternativeName);
    }
    /** Populates precinctNames with the values from precinctNames.csv
     *  Note: precinctNames.csv MUST be on classpath.
     * @author John Berberian
     * */
    public void initNames() throws IOException {
        initNames(fileName);
    }
    /** Populates precinctNames with the values from a given filename.
     * @param alternateName The filename of the csv to use.
     * @author John Berberian
     * */
    public void initNames(String alternateName) throws IOException {
        //Open the file
        File csv = new File(
                getClass().getClassLoader().getResource("precinctNames.csv").getFile()
        );
        //Get a nice reader for it
        FileReader file = new FileReader(csv);
        BufferedReader buff = new BufferedReader(file);
        //And read all the lines into an arraylist
        ArrayList<String> lines = new ArrayList<>();
        while (buff.ready()){
            lines.add(buff.readLine());
        }
        //Sort the list, in case it is out of order, by precinct.
        Comparator<String> sortByPrec= Comparator.comparingInt((String s) -> Integer.parseInt(s.split(",")[0]));
        lines.sort(sortByPrec);
        //Get the maxind and minind, along with the names list.
        //This approach should work, unless they skipped numbers.
        int max_l=-2147483648; //Set to minInt
        int min_l=2147483647; //Set to maxInt.
        precinctNames = new ArrayList<>();
        for (String line : lines) {
            precinctNames.add(line.split(",")[1]);
            int curr_l = Integer.parseInt(line.split(",")[0]);
            max_l = Math.max(curr_l, max_l);
            min_l = Math.min(curr_l, min_l);
        }
        //Set the values!
        MAX_PRECINCT = max_l;
        MIN_PRECINCT = min_l;

        //Pad out the beginning of precinctNames with nonsense, for nonzero min_precinct values.
        for (int i=0;i<MIN_PRECINCT;i++){
            precinctNames.add(0,"SHOULD NEVER BE SEEN");
        }
    }
}
