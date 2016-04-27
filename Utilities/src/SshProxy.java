import com.jcraft.jsch.*;

import java.io.InputStream;

/**
 * Created by chemistry_sourabh on 3/12/16.
 */
public class SshProxy {

    private String privateKey;
    private String host;
    private int port;
    private Session session;
    private Session secondSession;
    private Channel channel;

    public SshProxy(String host,int port,String privateKey) {
        this.privateKey = privateKey;
        this.host = host;
        this.port = port;
    }

    public int executeCommand(String finalHost, String command) throws Exception{

        int exitStatus = 1;

        String user="ubuntu";

        JSch jsch=new JSch();
        jsch.addIdentity(privateKey);
//        session=jsch.getSession(user, host, 22);
//        session.setConfig("StrictHostKeyChecking", "no");
//        session.setConfig("UserKnownHostsFile","/dev/null");
//        session.setPortForwardingL(port, finalHost, 22);
//        session.connect();
//        Channel ch = session.openChannel("direct-tcpip");


        secondSession = jsch.getSession(user, finalHost, 22);
        secondSession.setConfig("StrictHostKeyChecking", "no");
        secondSession.setConfig("UserKnownHostsFile","/dev/null");
        secondSession.connect(); // now we're connected to the secondary system
        channel =secondSession.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);

        channel.setInputStream(null);

        InputStream stdout=channel.getInputStream();

        channel.connect();

        while (true) {
            if(channel.isClosed()){
                exitStatus = 0;
                break;
            }
        }
        stdout.close();

        channel.disconnect();

        secondSession.disconnect();
//        session.disconnect();

        return exitStatus;
    }

    public  void closeSessions()
    {
        if(channel != null)
        {
            channel.disconnect();
        }
        secondSession.disconnect();
//        session.disconnect();
    }
}
