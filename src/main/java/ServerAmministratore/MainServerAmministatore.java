package ServerAmministratore;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainServerAmministatore {

    public static void main(String[] args) {
        try {
            HttpServer serverAdm = HttpServerFactory.create("http://localhost:8080/");
            System.out.println("ServerAmministratore started...");
            serverAdm.start();
            System.out.println("ServerAmministratore running on http://localhost:8080/");

            //run the MQTT broker
            String cmd = "brew services restart mosquitto";
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(cmd);
            pr.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = br.readLine();
            while(line != null) {
                System.out.println(line);
                line = br.readLine();
            }
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }
    }

}
