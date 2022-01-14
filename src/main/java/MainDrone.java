import Libraries.Coordinate;
import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import Sensor.MeasurementBuffer;
import Sensor.PM10Simulator;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.*;
import org.eclipse.paho.client.mqttv3.MqttException;
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

    //Threads
    public static Console console;
    public static NetworkChecker networkChecker;
    public static PM10Simulator pm10Simulator;
    public static MQTTSubscription mqttSubscription;
    public static SendGlobalStats sendGlobalStats;
    public static Delivery delivery;

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

        console = new Console(d);
        console.start();
        System.out.println("Console thread started");

        networkChecker = new NetworkChecker(d);
        networkChecker.start();
        System.out.println("NetworkChecker thread started");

        MeasurementBuffer myBuffer = new MeasurementBuffer(d);
        pm10Simulator = new PM10Simulator(myBuffer);
        pm10Simulator.start();
        System.out.println("PM10Simulator thread started");

        if(d.getMaster()) {
            mqttSubscription = new MQTTSubscription(d);
            mqttSubscription.start();
            System.out.println("MQTTSubscription thread started");

            sendGlobalStats = new SendGlobalStats(d);
            sendGlobalStats.start();
            System.out.println("SendGlobalStats thread started");
        }

    }

    public static void removeFromSmartCity() {
        WebResource webResource = client.resource(d.getServerAmmAddress() + "drone/remove");
        webResource.type(MediaType.APPLICATION_JSON).delete(Integer.toString(d.getId()));
    }

    public static void assignOrder(Drone d, boolean itSelf) {
        if(d.getOrdersList().size() > 0) {
            Order order = d.getOrdersList().get(0);
            d.getOrdersList().remove(0);
            int idCandidate = bestAvailableDrone(d, order, itSelf);
            if(idCandidate == -1) { //no drones available, reinsert the order in the list
                if(itSelf) {
                    d.getOrdersList().add(order);
                }
            } else {
                if(idCandidate != d.getId()) {
                    TopologyDrone drone = d.getNetworkTopology().getDroneWithId(idCandidate);

                    final ManagedChannel channel = ManagedChannelBuilder.forTarget(drone.getIp() + ":" + drone.getPort())
                            .usePlaintext(true)
                            .build();

                    NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                    NetworkService.Order request = NetworkService.Order.newBuilder()
                            .setOrderId(order.getId())
                            .setX1(order.getStartPoint().getX())
                            .setY1(order.getStartPoint().getY())
                            .setX2(order.getFinishPoint().getX())
                            .setY2(order.getFinishPoint().getY())
                            .build();
                    stub.deliverOrder(request);
                    channel.shutdownNow();
                } else {
                    MainDrone.delivery = new Delivery(d, order);
                    MainDrone.delivery.start();
                }
            }
        }

    }

    private static int bestAvailableDrone(Drone d, Order order, boolean itSelf) {
        ArrayList<ArrayList<Integer>> candidates = new ArrayList<>();
        ArrayList<TopologyDrone> dronesList = d.getNetworkTopology().getDronesList();
        for(TopologyDrone td : dronesList) {
            if(td.getId() != d.getId()) {
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
                        .usePlaintext(true)
                        .build();

                NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                NetworkService.HelloRequest request = NetworkService.HelloRequest.newBuilder()
                        .setId(d.getId())
                        .build();
                NetworkService.DroneDeliveryInfo response = stub.freeDrone(request);

                if (response.getDelivery() == false) {
                    ArrayList<Integer> candidate = new ArrayList<>();
                    candidate.add(response.getId());
                    int distance = (int) Math.sqrt(Math.pow(order.getStartPoint().getX() - response.getX(), 2) + Math.pow(order.getStartPoint().getY() - response.getY(), 2));
                    candidate.add(distance);
                    candidate.add(response.getBattery());

                    candidates.add(candidate);
                }

                td.setPosition(new Coordinate(response.getX(), response.getY()));

                channel.shutdownNow();
            } else {
                if (d.getDelivering() == false && itSelf == true) {
                    ArrayList<Integer> candidate = new ArrayList<>();
                    candidate.add(d.getId());
                    int distance = (int) Math.sqrt(Math.pow(order.getStartPoint().getX() - d.getPosition().getX(), 2) + Math.pow(order.getStartPoint().getY() - d.getPosition().getY(), 2));
                    candidate.add(distance);
                    candidate.add(d.getBattery());

                    candidates.add(candidate);
                }
            }
        }

        int minDistance = Integer.MAX_VALUE;
        int maxBattery = -1;
        int idCandidate = -1;
        ArrayList<Integer> candidate = new ArrayList<>();
        for(ArrayList<Integer> ali : candidates) {
            if(ali.get(1) < minDistance) {
                minDistance = ali.get(1);
            }
        }
        for(ArrayList<Integer> ali : candidates) {
            if(ali.get(2) > maxBattery && ali.get(1) == minDistance) {
                maxBattery = ali.get(2);
            }
        }
        for(ArrayList<Integer> ali : candidates) {
            if(ali.get(0) > idCandidate && ali.get(1) == minDistance && ali.get(2) == maxBattery) {
                idCandidate = ali.get(0);
            }
        }

        return idCandidate;
    }

    public static void explicitExit(Drone d) {
        //console.interrupt();
        networkChecker.running = false;
        try {
            networkChecker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(d.getMaster()) {
            try {
                mqttSubscription.mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        if(delivery != null && d.getDelivering()) {
            try {
                delivery.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(d.getMaster()) {
            while(d.getOrdersList().size() > 0) {
                assignOrder(d, false);
            }
            pm10Simulator.stopMeGently();
            sendGlobalStats.sending = false;
            try {
                sendGlobalStats.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        removeFromSmartCity();
        System.exit(0);
    }

}
