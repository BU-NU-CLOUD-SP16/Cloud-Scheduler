import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */

/* This class describes the data that will be passed to the plugin. Data will be passed as a ArrayList of Arraylists
   where each list is a row of data from db */
public class Data {

    private String query;
    private ArrayList<String[]> data;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<String []> getData() {
        return data;
    }

    public void setData(ArrayList<String[]> data) {
        this.data = data;
    }


    @Override
    public String toString() {
        return "Data{" +
                "query='" + query + '\'' +
                ", data=" + data +
                '}';
    }
}
