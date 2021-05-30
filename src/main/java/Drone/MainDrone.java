package Drone;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;


public class MainDrone {

    private static Drone d;

    public static void main(String[] args) {
        Random random = new Random();
        d = new Drone(random.nextInt(10000), random.nextInt(1000)+1000, "http://localhost:8080/");

        System.out.println("Created a new drone:");
        System.out.println(d.getStatus());

        if(addToSmartCity()) {
            //add this to a thread
            Scanner scanner = new Scanner(new InputStreamReader(System.in));
            while(true) {
                System.out.print("Insert a command: ");
                String input = scanner.nextLine();
                if(input.trim().equalsIgnoreCase("stats")) {
                    getStats();
                } else if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                    webExit();
                    break;
                }
            }
        }
    }

    private static boolean addToSmartCity() {
        System.out.println("Try to be added to SmartCity...");

        Client client = Client.create();
        WebResource webResource = client.resource(d.getServerAmmAddress() + "drone/add");
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, d);
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {
            System.out.println("Successfully added to Smart City!\n");
            return true;
        } else {
            if(response.getStatus() == Response.Status.NOT_ACCEPTABLE.getStatusCode()) {
                System.out.println("NOT added to Smart City: id or port already used!");
            } else {
                System.out.println("Generic error");
            }
            return false;
        }
        //String otherDrones = response.getEntity(String.class);
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
