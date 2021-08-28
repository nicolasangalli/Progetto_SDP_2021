package ServerAmministratore;

import Libraries.Coordinate;
import Libraries.DroneSmartCity;
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
    public Response addNewDrone(DroneSmartCity dsc) {
        System.out.println("Drone " + dsc.getId() + " ask to be added to SmartCity...");

        if(SmartCity.getInstance().checkAvailable(dsc.getId())) {
            //add drone to SmartCity
            Coordinate position = SmartCity.getInstance().addNewDrone(dsc);
            System.out.println("Drone " + dsc.getId() + " added to SmartCity!");

            //return the position and the other drones in SmartCity
            ArrayList<Object> retList = SmartCity.getInstance().getDrones(dsc.getId(), position.getX(), position.getY());
            Gson gson = new Gson();
            String resp = gson.toJson(retList);
            return Response.ok(resp).build();
        } else {
            System.out.println("Drone " + dsc.getId() + " NOT added to SmartCity (id already in use)!");
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }

    @Path("remove")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDrone(int id) {
        System.out.println("Drone " + id + " ask to be removed to SmartCity...");
        if(!SmartCity.getInstance().checkAvailable(id)) {
            //remove drone from SmartCity
            SmartCity.getInstance().removeDrone(id);
            System.out.println("Drone " + id + " removed from SmartCity!");
            return Response.ok().build();
        } else {
            System.out.println("Drone " + id + " NOT in SmartCity!");
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }

    @Path("stats")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats() {
        System.out.println("Drone master requested stats...");

        ArrayList<DroneSmartCity> retList = SmartCity.getInstance().getStats();
        Gson gson = new Gson();
        String resp = gson.toJson(retList);
        return Response.ok(resp).build();
    }

}
