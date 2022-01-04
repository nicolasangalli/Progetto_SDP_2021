import Libraries.Coordinate;
import Libraries.Drone;
import Libraries.TopologyDrone;
import Sensor.MeasurementBuffer;
import Sensor.PM10Simulator;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;


public class MainDrone {

    private static io.grpc.Server server; //for gRPC call
    private static NetworkServiceImpl networkServiceImpl; //for gRPC call
    private static Drone d;
    private static Client client; //for REST call
    private static WebResource webResource; //for REST call
    private static String restResponse; //REST call response
    private static Gson gson;

    public static void main(String[] args) {
        gson = new Gson();
        Random random = new Random();
        int serverPort = random.nextInt(1000) + 1000;

        //Drone initialization
        d = new Drone(random.nextInt(10000), "localhost", serverPort, "http://localhost:8080/");

        //Server gRPC initialization
        networkServiceImpl = new NetworkServiceImpl();
        server = ServerBuilder.forPort(serverPort)
                            .addService(networkServiceImpl)
                            .build();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        networkServiceImpl.setD(d);

        //REST client initialization
        client = Client.create();
        webResource = client.resource(d.getServerAmmAddress() + "drone/add");

        System.out.println("New drone generated:");
        System.out.println("id = " + d.getId());
        System.out.println("port = " + d.getPort() + "\n");


        if(addToSmartCity()) { //REST call to ServerAmministratore
            initializeNetworkTopology();
            startThreads();
        } else {
            System.out.println("Shutdown...");
            server.shutdownNow();
        }

        //Server gRPC termination
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean addToSmartCity() {
        System.out.println("Try to be added to SmartCity...");

        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, d); //REST call
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {
            System.out.println("Successfully added to SmartCity!\n");
            restResponse = response.getEntity(String.class);
            return true;
        } else {
            if(response.getStatus() == Response.Status.NOT_ACCEPTABLE.getStatusCode()) {
                System.out.println("NOT added to SmartCity: id already used!");
            } else {
                System.out.println("Generic error");
            }
            return false;
        }
    }

    private static void initializeNetworkTopology() {
        ArrayList<Object> respArray = gson.fromJson(restResponse, ArrayList.class);
        Iterator it = respArray.iterator();

        System.out.println("Set drone position...");
        int x = (int) (double) it.next();
        int y = (int) (double) it.next();
        Coordinate position = new Coordinate(x, y);
        d.setPosition(position);
        System.out.println("Drone position: (" + d.getPosition().getX() + "," + d.getPosition().getY() + ")\n");


        System.out.println("Network ring generation...");
        ArrayList<TopologyDrone> dronesList = new ArrayList<>();
        dronesList.add(new TopologyDrone(d.getId(), d.getIp(), d.getPort(), d.getPosition()));
        while(it.hasNext()) {
            String s = it.next().toString();
            TopologyDrone otherDrone = gson.fromJson(s, TopologyDrone.class);
            dronesList.add(otherDrone);
        }
        d.getNetworkTopology().setDronesList(dronesList);

        if(d.getNetworkTopology().getDronesList().size() == 1) {
            d.setMasterId(d.getId());
            d.setMaster(true);
            System.out.println("This drone is the only one in the network, elected as master");
        } else {
            System.out.println("Comunicate to all other drones my insertion in the network...");
            dronesList = d.getNetworkTopology().getDronesList();
            for(TopologyDrone otherDrone : dronesList) {
                if(otherDrone.getId() != d.getId()) {
                    System.out.println("Try to communicate with drone with id = " + otherDrone.getId());
                    final ManagedChannel channel = ManagedChannelBuilder.forTarget(otherDrone.getIp() + ":" + otherDrone.getPort())
                            .usePlaintext(true)
                            .build();

                    NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                    NetworkService.NewDrone request = NetworkService.NewDrone.newBuilder()
                            .setId(d.getId())
                            .setIp(d.getIp())
                            .setPort(d.getPort())
                            .setX(d.getPosition().getX())
                            .setY(d.getPosition().getY())
                            .build();
                    NetworkService.Master response = stub.addNewDrone(request);
                    d.setMasterId(response.getMasterId());
                    System.out.println("Drone with id = " + otherDrone.getId() + " say: OK! The drone master id = " + d.getMasterId());
                    channel.shutdownNow();
                }
            }
        }
        System.out.println("Network ring generated!\n");
    }

    private static void startThreads() {

        //debug
        //Random random = new Random();
        //d.setMaster(false);
        //d.setMasterId(-1);
        //d.setBattery(random.nextInt(100));

        Console console = new Console(d);
        console.start();
        System.out.println("Console thread started");
        //NetworkChecker networkChecker = new NetworkChecker(d);
        //networkChecker.start();
        //System.out.println("Network checker thread started");

        MeasurementBuffer myBuffer = new MeasurementBuffer(d);
        PM10Simulator pm10Simulator = new PM10Simulator(myBuffer);
        pm10Simulator.start();

        if(d.getMaster()) {
            MQTTSubscription mqttSubscription = new MQTTSubscription(d);
            mqttSubscription.start();

            SendGlobalStats sendGlobalStats = new SendGlobalStats(d);
            sendGlobalStats.start();
        }

    }

}
