/**
 * <h1>ClusterElasticityAgentException</h1>
 *
 * @author Praveen
 * @version 1.0
 * @since 2016-03-02
 */

import java.lang.Exception;

public class ClusterElasticityAgentException extends Exception {
    //Parameterless Constructor

    /**
     * <h1>ClusterElasticityAgentException</h1>
     * Constructor without any initialization.
     */
    public ClusterElasticityAgentException() {
    }

    /**
     * <h1>ClusterElasticityAgentException</h1>
     * Constructor that accepts a message.
     * @param message
     */
    public ClusterElasticityAgentException(String message) {
        super(message);
    }
}
