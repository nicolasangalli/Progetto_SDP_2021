package Drone;

import Libraries.Coordinate;
import Libraries.DroneSmartCity;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;


public class MainDrone {

    private static Drone d;
    private static Client client; //for REST call
    private static WebResource webResource; //for REST call

    public static void main(String[] args) {
        //Drone initialization
        Random random = new Random();
        d = new Drone(random.nextInt(10000), random.nextInt(1000)+1000, "http://localhost:8080/");

        client = Client.create();
        webResource = client.resource(d.getServerAmmAddress() + "drone/add");

        System.out.println("Created a new drone!");
        System.out.println("id: " + d.getId());
        System.out.println("port: " + d.getPort());

        if(addToSmartCity()) { //REST call to ServerAmministratore
            System.out.println("position: (" + d.getPosition().getX() + "," + d.getPosition().getY() + ")");
            //ordering drones list (network structure)
            Collections.sort(d.getDronesList(), new Comparator<DroneSmartCity>() {
                @Override
                public int compare(DroneSmartCity dsc1, DroneSmartCity dsc2) {
                    if(dsc1.getId() > dsc2.getId()) {
                        return 1;
                    }
                    if(dsc1.getId() < dsc2.getId()) {
                        return -1;
                    }
                    return 0;
                }
            });


        }
    }

    private static boolean addToSmartCity() {
        System.out.println("Try to be added to SmartCity...");

        DroneSmartCity droneSmartCity = new DroneSmartCity(d.getId(), "localhost", d.getPort()); //input for REST call
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, droneSmartCity); //REST call
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {
            System.out.println("Successfully added to Smart City!");

            String r = response.getEntity(String.class);
            Gson gson = new Gson();
            ArrayList<Object> respArray = gson.fromJson(r, ArrayList.class);
            Iterator it = respArray.iterator();

            //update drone position
            int x = (int) (double) it.next();
            int y = (int) (double) it.next();
            Coordinate position = new Coordinate(x, y);
            d.setPosition(position);

            //set drones list
            while(it.hasNext()) {
                String s = it.next().toString();
                DroneSmartCity dsc = gson.fromJson(s, DroneSmartCity.class);
                d.getDronesList().add(dsc);
            }
            return true;
        } else {
            if(response.getStatus() == Response.Status.NOT_ACCEPTABLE.getStatusCode()) {
                System.out.println("NOT added to Smart City: id already used!");
            } else {
                System.out.println("Generic error");
            }
            return false;
        }
    }

}
