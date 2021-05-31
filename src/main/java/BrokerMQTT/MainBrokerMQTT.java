package BrokerMQTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainBrokerMQTT {

    public static void main(String[] args) {
        try {
            String cmd = "brew services restart mosquitto";
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(cmd);
            pr.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = br.readLine();
            while (line != null) {
                System.out.println(line);
                line = br.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

}
