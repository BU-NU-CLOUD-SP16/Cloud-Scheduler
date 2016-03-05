/**
 * Created by Praveen on 3/2/2016.
 */

import java.lang.Exception;

public class ClusterElasticityAgentException extends Exception {
    //Parameterless Constructor
    public ClusterElasticityAgentException() {
    }

    //Constructor that accepts a message
    public ClusterElasticityAgentException(String message) {
        super(message);
    }
}
