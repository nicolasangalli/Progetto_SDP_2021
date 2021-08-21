package Drone;

import Dronazon.Coordinate;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;


public class MainDrone {

    private static Drone d;

    public static void main(String[] args) {
        Random random = new Random();

        //Drone initialization
        d = new Drone(random.nextInt(10000), random.nextInt(1000)+1000, "http://localhost:8080/");

        System.out.println("Created a new drone:");
        System.out.println(d.getStatus());

        if(addToSmartCity()) { //REST call to ServerAmministratore
            System.out.println(d.getStatus());
        }
    }

    private static boolean addToSmartCity() {
        System.out.println("Try to be added to SmartCity...");

        Client client = Client.create();
        WebResource webResource = client.resource(d.getServerAmmAddress() + "drone/add");
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, d);
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {
            System.out.println("Successfully added to Smart City!\n");

            //update position and other drones list
            String s = response.getEntity(String.class);
            Gson gson = new Gson();
            ArrayList respArray = gson.fromJson(s, ArrayList.class);
            Iterator it = respArray.iterator();
            ArrayList posArr = (ArrayList) it.next();
            Coordinate position = new Coordinate(Integer.parseInt((String) posArr.get(0)), Integer.parseInt((String) posArr.get(1)));
            d.setPosition(position);
            while(it.hasNext()) {
                ArrayList dr = (ArrayList) it.next();
                String[] drone = new String[2];
                drone[0] = (String) dr.get(0);
                drone[1] = (String) dr.get(1);
                d.getDronesList().add(drone);
            }

            return true;
        } else {
            if(response.getStatus() == Response.Status.NOT_ACCEPTABLE.getStatusCode()) {
                System.out.println("NOT added to Smart City: id or port already used!");
            } else {
                System.out.println("Generic error");
            }
            return false;
        }
    }

}
