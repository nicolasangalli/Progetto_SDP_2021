package ClientAmministratore;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import java.io.InputStreamReader;
import java.util.Scanner;


public class MainClientAmministratore {

    public static void main(String[] args) {
        String commandsList = "List of possible commands:\n\n" +
                            "drones -> get the list of drones\n" +
                            "avgDelivery -> get the average number of deliveries between to timestamps\n" +
                            "help -> show this commands list\n" +
                            "quit / q / exit -> quit from this tool";

        System.out.println("Welcome to Amministratore line tool");
        System.out.println(commandsList);
        System.out.print("\nInsert a command: ");
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        while(true) {
            String input = scanner.nextLine();
            if(input.trim().equalsIgnoreCase("drones")) {
                getDronesList();
            } else if(input.trim().equalsIgnoreCase("avgDelivery")) {
                //...
            } else if(input.trim().equalsIgnoreCase("help")) {
                System.out.println(commandsList);
            } else if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                break;
            }
            System.out.print("Insert a command: ");
        }
    }

    private static void getDronesList() {
        //Ask to ServerAmministratore the list of drones in SmartCity
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/adm/drones");
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        String dronesList = response.getEntity(String.class);
        System.out.println(dronesList);
    }

}
