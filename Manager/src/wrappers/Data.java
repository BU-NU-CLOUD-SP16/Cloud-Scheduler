package wrappers;

import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */

/* This class describes the data that will be passed to the plugin. Data will be passed as a ArrayList of Arraylists
   where each list is a row of data from db */
public class Data {

    private String query;
    private ArrayList<ArrayList> data;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<ArrayList> getData() {
        return data;
    }

    public void setData(ArrayList<ArrayList> data) {
        this.data = data;
    }
}
