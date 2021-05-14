package ServerAmministratore;

import Drone.Drone;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("drone")
public class DroneInterface {
    @Path("welcome")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getWelcome() {
        return Response.ok("Welcome to SmartCity!").build();
    }

    @Path("add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewDrone(Drone d) {
        String dronesList = "";
        if(SmartCity.getInstance().checkAvailableID(d)) {
            SmartCity.getInstance().addNewDrone(d);
        } else {
            dronesList = "WARNING: drone not accepted - id already used\n";
        }
        dronesList = SmartCity.getInstance().prettyPrinter(d);
        return Response.ok(dronesList).build();
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
