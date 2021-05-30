package ServerAmministratore;

import Dronazon.Coordinate;
import Drone.Drone;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;


@Path("drone")
public class DroneInterface {
    @Path("add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewDrone(Drone d) {
        System.out.println("Drone " + d.getId() + " ask to be added to SmartCity");
        if(SmartCity.getInstance().checkAvailable(d)) {
            Random random = new Random();
            Coordinate position = new Coordinate(random.nextInt(10), random.nextInt(10));
            d.setPosition(position);
            SmartCity.getInstance().addNewDrone(d);

            System.out.println("Drone " + d.getId() + " added to SmartCity!");
            String dronesList = SmartCity.getInstance().prettyPrinter(d);
            return Response.ok(dronesList).build();
        } else {
            System.out.println("Drone " + d.getId() + " NOT added to SmartCity (id or port already in use)!");
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }

    @Path("remove")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeDrone(Drone d) {
        SmartCity.getInstance().removeDrone(d);
        return Response.ok("Drone removed from SmartCity!").build();
    }

    @Path("stats")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats() {
        String stats = SmartCity.getInstance().getStats();
        return Response.ok(stats).build();
    }

}
