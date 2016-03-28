import static java.lang.Thread.sleep;

/**
 * Created by Praveen on 3/26/2016.
 */
public class Overlord {

    private OverlordPolicyInfo policyConfigHandle;
    private HttpEndPoints httpHandle;
    private OpenStackWrapper openStackHandle;
    private CEAgentList registeredCEAgents;

    public Overlord(){
        policyConfigHandle = new OverlordPolicyInfo();
        httpHandle = new HttpEndPoints(this);
        openStackHandle = new OpenStackWrapper(this);
        registeredCEAgents = new CEAgentList();
    }

    public static void main(String args[]) throws InterruptedException {

        Overlord overlordHandle = new Overlord();

        overlordHandle.getPolicyConfigHandle().LoadPolicyInfo();

        Thread openStackThread = new Thread(overlordHandle.openStackHandle);
        openStackThread.start();

        overlordHandle.getHttpHandle().configureHttpEndPoints();

        while(true) {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public OverlordPolicyInfo getPolicyConfigHandle() {
        return policyConfigHandle;
    }

    public void setPolicyConfigHandle(OverlordPolicyInfo policyConfigHandle) {
        this.policyConfigHandle = policyConfigHandle;
    }

    public HttpEndPoints getHttpHandle() {
        return httpHandle;
    }

    public void setHttpHandle(HttpEndPoints httpHandle) {
        this.httpHandle = httpHandle;
    }

    public OpenStackWrapper getOpenStackHandle() {
        return openStackHandle;
    }

    public void setOpenStackHandle(OpenStackWrapper openStackHandle) {
        this.openStackHandle = openStackHandle;
    }

    public CEAgentList getRegisteredCEAgents() {
        return registeredCEAgents;
    }

    public void setRegisteredCEAgents(CEAgentList registeredCEAgents) {
        this.registeredCEAgents = registeredCEAgents;
    }
}
