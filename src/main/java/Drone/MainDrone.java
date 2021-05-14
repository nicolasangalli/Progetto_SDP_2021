package Drone;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;


public class MainDrone {

    private static Drone d;

    public static void main(String[] args) {
        Random random = new Random();
        d = new Drone(random.nextInt(10000));
        connectionRequest();

        System.out.print("\nInsert a command: ");
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        while(true) {
            String input = scanner.nextLine();
            if(input.trim().equalsIgnoreCase("stats")) {
                getStats();
            } else if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                webExit();
                break;
            }
            System.out.print("Insert a command: ");
        }
    }

    private static void connectionRequest() {
        //enter to the drones web...

        //Ask ServerAmministratore to add this drone to SmartCity
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/drone/add");
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, d);
        String otherDrones = response.getEntity(String.class);
        System.out.println(otherDrones);
    }

    private static void webExit() {
        //exit from the drones web...

        //Ask ServerAmministratore to remove this drone from SmartCity
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/drone/remove");
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, d);
        String deleteResp = response.getEntity(String.class);
        System.out.println(deleteResp);
    }

    private static void getStats() {
        //Ask to ServerAmministratore the SmartCity stats
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/drone/stats");
        ClientResponse response = webResource.get(ClientResponse.class);
        String stats = response.getEntity(String.class);
        System.out.println(stats);
    }

}
