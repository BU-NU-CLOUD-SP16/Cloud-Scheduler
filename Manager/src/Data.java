import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */

/* This class describes the data that will be passed to the plugin. Data will be passed as a ArrayList of Arraylists
   where each list is a row of data from db */
public class Data {

    private ArrayList data;

    public ArrayList getData() {
        return data;
    }

    public void setData(ArrayList data) {
        this.data = data;
    }
}
