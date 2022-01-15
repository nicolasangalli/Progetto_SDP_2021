package ClientAmministratore;

import Libraries.TopologyDrone;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;


public class MainClientAmministratore {

    private static Scanner scanner;
    private static Client client;
    private static WebResource webResource;
    private static ClientResponse response;
    private static String restResponse;
    private static Gson gson;

    public static void main(String[] args) {
        client = Client.create();
        gson = new Gson();

        String commandsList = "List of possible commands:\n\n" +
                            "drones -> get the list of drones\n" +
                            "stats -> get the last n global stats\n" +
                            "delivery -> get the average number of deliveries between two timestamps\n" +
                            "distance > get the average number of kms covered between two timestamps\n" +
                            "help -> show this commands list\n" +
                            "quit / q / exit -> quit from this tool\n";

        System.out.println("Welcome to Amministratore line tool");
        System.out.println(commandsList);
        System.out.print("Insert a command: ");
        scanner = new Scanner(new InputStreamReader(System.in));
        while(true) {
            String input = scanner.nextLine();
            if(input.trim().equalsIgnoreCase("drones")) {
                getDronesList();
            } else if(input.trim().equalsIgnoreCase("stats")) {
                getGlobalStats();
            } else if(input.trim().equalsIgnoreCase("delivery")) {
                getDeliveries();
            } else if(input.trim().equalsIgnoreCase("distance")) {
                getDistances();
            } else if(input.trim().equalsIgnoreCase("help")) {
                System.out.println(commandsList);
                System.out.print("Insert a command: ");
            } else if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                break;
            }
        }
    }

    private static void getDronesList() {
        webResource = client.resource("http://localhost:8080/adm/drones");
        response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        restResponse = response.getEntity(String.class);
        ArrayList<Object> retList = gson.fromJson(restResponse, ArrayList.class);
        if(retList.size() > 0) {
            Iterator it = retList.iterator();
            while(it.hasNext()) {
                String s = it.next().toString();
                TopologyDrone td = gson.fromJson(s, TopologyDrone.class);
                System.out.println(td.getStatus());
            }
        } else {
            System.out.println("SmartCity is empty\n");
        }
        System.out.print("Insert a command: ");
    }

    private static void getGlobalStats() {
        System.out.print("Number of global stats to receive: ");
        String n = scanner.next();
        webResource = client.resource("http://localhost:8080/adm/globalStats");
        response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, n.trim());
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {
            restResponse = response.getEntity(String.class);
            ArrayList<Object> retList = gson.fromJson(restResponse, ArrayList.class);
            Iterator it = retList.iterator();
            while(it.hasNext()) {
                String s = it.next().toString();
                System.out.println(s.substring(1, s.length()-1));
            }
        } else {
            System.out.println("not a number");
        }
        System.out.print("\nInsert a command: ");
    }

    private static void getDeliveries() {
        System.out.print("Insert first timestamp: ");
        String t1 = scanner.nextLine();

        System.out.print("Insert second timestamp: ");
        String t2 = scanner.nextLine().trim();

        webResource = client.resource("http://localhost:8080/adm/deliveries");
        String[] timestamps = {t1, t2};
        response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, gson.toJson(timestamps));
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {
            restResponse = response.getEntity(String.class);
            System.out.println("Average deliveries: " + restResponse);
        }

        System.out.print("\nInsert a command: ");
    }

    private static void getDistances() {
        System.out.print("Insert first timestamp: ");
        String t1 = scanner.nextLine();

        System.out.print("Insert second timestamp: ");
        String t2 = scanner.nextLine().trim();

        webResource = client.resource("http://localhost:8080/adm/distances");
        String[] timestamps = {t1, t2};
        response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, gson.toJson(timestamps));
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {
            restResponse = response.getEntity(String.class);
            System.out.println("Average kms covered: " + restResponse);
        }

        System.out.print("\nInsert a command: ");
    }

}
