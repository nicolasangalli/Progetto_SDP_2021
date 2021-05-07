package Drone;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import java.util.Random;


public class MainDrone {

    private static Drone d;

    public static void main(String[] args) {
        Random random = new Random();
        d = new Drone(random.nextInt(10000));
        connectionRequest();
    }

    private static void connectionRequest() {
        //Ask ServerAmministratore to add this drone to SmartCity
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/drone/add");
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, d);
        String otherDrones = response.getEntity(String.class);
        System.out.println(otherDrones);

        //rimozione del drone dalla SmartCity
        /*
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        webResource = client.resource("http://localhost:8080/drone/remove");
        response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, d);
        String deleteResp = response.getEntity(String.class);
        System.out.println(deleteResp);
        */
    }

}
