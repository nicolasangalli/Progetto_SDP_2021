package ServerAmministratore;

import Drone.Drone;
import com.google.gson.Gson;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;


@Path("drone")
public class DroneInterface {
    @Path("add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewDrone(Drone d) {
        System.out.println("Drone " + d.getId() + " ask to be added to SmartCity");
        if(SmartCity.getInstance().checkAvailable(d.getId(), d.getPort())) {
            //add drone to SmartCity
            SmartCity.getInstance().addNewDrone(d);
            System.out.println("Drone " + d.getId() + " added to SmartCity!");

            //return the position and the other drones in SmartCity
            ArrayList<String[]> dronesList = SmartCity.getInstance().getDrones(d.getId());
            Gson gson = new Gson();
            String resp = gson.toJson(dronesList);
            return Response.ok(resp).build();
        } else {
            System.out.println("Drone " + d.getId() + " NOT added to SmartCity (id or port already in use)!");
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }

}
