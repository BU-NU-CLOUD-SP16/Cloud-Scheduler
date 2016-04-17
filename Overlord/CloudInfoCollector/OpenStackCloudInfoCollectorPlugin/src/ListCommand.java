/**
 * Created by chemistry_sourabh on 4/10/16.
 */
public class ListCommand extends OpenStackCommand {

    private String name;
    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
