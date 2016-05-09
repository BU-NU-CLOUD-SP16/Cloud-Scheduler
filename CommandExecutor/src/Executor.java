import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static spark.Spark.port;
import static spark.Spark.post;

/**
 * Created by chemistry_sourabh on 4/28/16.
 */
public class Executor {

    public static void main(String[] args) {


        LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();


        port(8000);
        post("/execute",((request, response) -> {

            System.out.println("Got "+request.body());
            if (request.body().equalsIgnoreCase("running"))
            {
                return "yes";
            }

            if (request.body().equalsIgnoreCase("done"))
            {
                if (commandQueue.isEmpty())
                    return "yes";
                else
                    return "no";
            }


            commandQueue.add(request.body());

            return "";

        }));


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String command = commandQueue.take();
                        Process p;
                        if (command.contains("sed")) {
                            String s[] = command.split(" ");
                            String commands[] = {"sudo", "sed", "-i", "1s/^/" + s[1] + " mesos-hdfs-master\\n /", "/etc/hosts"};
                            p = Runtime.getRuntime().exec(commands);
                            p.waitFor();
                        } else {
                            p = Runtime.getRuntime().exec(command);
                            if (command.toLowerCase().startsWith("nohup"))
                                p.waitFor(1, TimeUnit.SECONDS);
                            else
                                p.waitFor();
                        }
                        System.out.println("Executed "+command);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t.setDaemon(true);
        t.start();

    }



}
