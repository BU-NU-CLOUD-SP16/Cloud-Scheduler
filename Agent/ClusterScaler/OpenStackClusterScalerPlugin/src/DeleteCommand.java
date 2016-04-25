/**
 * <h1>DeleteCommand</h1>
 * Getters and Setters for the Delete
 * Command in the OpenStack.
 *
 * @author Sourabh
 * @version 1.0
 * @since 2016-04-10
 */
public class DeleteCommand extends OpenStackCommand {

    private String id;

    /**
     * <h1>getId</h1>
     * @return String
     * The id of the deleted Node
     */
    public String getId() {
        return id;
    }

    /**
     * <h1>setId</h1>
     * @param id String
     */
    public void setId(String id) {
        this.id = id;
    }
}
