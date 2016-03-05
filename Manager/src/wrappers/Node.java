package wrappers;

import java.util.ArrayList;

/**
 * Created by chemistry_sourabh on 3/2/16.
 */

/* This class describes the data which describes the node in a cluster. It has an ArrayList which contains all the
essential data of the node  */
public class Node {

    private ArrayList data;

    public ArrayList getData() {
        return data;
    }

    public void setData(ArrayList data) {
        this.data = data;
    }
}
