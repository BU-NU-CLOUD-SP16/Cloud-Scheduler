import com.jcraft.jsch.*;

import java.io.InputStream;

/**
 * Created by chemistry_sourabh on 3/12/16.
 */
public class SshProxy {

    public void executeCommand(String final_host, String command) throws Exception{


        String host="129.10.3.91"; // First level target
        String user="ubuntu";
        int port=22;

        String private_key = "/Users/chemistry_sourabh/.ssh/id_rsa";

        JSch jsch=new JSch();
        jsch.addIdentity(private_key);
        Session session=jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPortForwardingL(2233, final_host, 22);
        session.connect();
        Channel ch = session.openChannel("direct-tcpip");


        Session secondSession = jsch.getSession(user, "localhost", 2233);
        secondSession.setConfig("StrictHostKeyChecking", "no");

        secondSession.connect(); // now we're connected to the secondary system
        Channel channel=secondSession.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);

        channel.setInputStream(null);

        InputStream stdout=channel.getInputStream();

        channel.connect();

        while (true) {
            if(channel.isClosed()){
                System.out.println("exit-status: "+channel.getExitStatus());
                break;
            }
        }
        stdout.close();

        channel.disconnect();

        secondSession.disconnect();
        session.disconnect();
    }
}
