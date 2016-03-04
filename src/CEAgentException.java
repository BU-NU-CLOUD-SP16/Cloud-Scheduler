/**
 * Created by Praveen on 3/2/2016.
 */

import java.lang.Exception;

public class CEAgentException extends Exception {
    //Parameterless Constructor
    public CEAgentException() {
    }

    //Constructor that accepts a message
    public CEAgentException(String message) {
        super(message);
    }
}
