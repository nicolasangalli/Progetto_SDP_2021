package ServerAmministratore;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;


public class MainServerAmministatore {

    public static void main(String[] args) {
        try {
            HttpServer serverAdm = HttpServerFactory.create("http://localhost:8080/");
            System.out.println("ServerAmministratore started...");
            serverAdm.start();
            System.out.println("ServerAmministratore running on http://localhost:8080/");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
