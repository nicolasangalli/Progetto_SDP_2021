package ServerAmministratore;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("adm")
public class AmministratoreInterface {
    @Path("drones")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDronesList() {
        String dronesList = SmartCity.getInstance().prettyPrinter();
        return Response.ok(dronesList).build();
    }

}
