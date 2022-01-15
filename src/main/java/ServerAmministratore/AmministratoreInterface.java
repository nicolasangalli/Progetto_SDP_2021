package ServerAmministratore;

import Libraries.GlobalStat;
import Libraries.TopologyDrone;
import com.google.gson.Gson;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;


@Path("adm")
public class AmministratoreInterface {
    @Path("drones")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDronesList() {
        ArrayList<TopologyDrone> retList = SmartCity.getInstance().getDronesList();
        Gson gson = new Gson();
        String resp = gson.toJson(retList);
        return Response.ok(resp).build();
    }

    @Path("globalStats")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGlobalStats(String s) {
        try {
            int n = Integer.parseInt(s);
            ArrayList<GlobalStat> retList = SmartCity.getInstance().getGlobalStats(n);
            Gson gson = new Gson();
            String resp = gson.toJson(retList);
            return Response.ok(resp).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }

    @Path("deliveries")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deliveries(String[] timestamps) {
        double avg = SmartCity.getInstance().getAvgDeliveries(timestamps);
        return Response.ok(avg).build();
    }

    @Path("distances")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response distances(String[] timestamps) {
        double avg = SmartCity.getInstance().getAvgDistances(timestamps);
        return Response.ok(avg).build();
    }

}
