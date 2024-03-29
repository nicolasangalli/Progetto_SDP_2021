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
    public static Client client; //for REST call
    private static WebResource webResource; //for REST call
    private static String restResponse; //REST call response
    private static Gson gson;
    public static ArrayList<ArrayList<Integer>> candidates; //for bestAvailableDrone thread

    //Threads
    public static Console console;
    public static NetworkChecker networkChecker;
    public static PM10Simulator pm10Simulator;
    public static MQTTSubscription mqttSubscription;
    public static SendGlobalStats sendGlobalStats;
    public static Delivery delivery;
    public static PrintInfo printInfo;

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

            ArrayList<ParallelCommunication> parallelThreads = new ArrayList<>();
            for(TopologyDrone otherDrone: dronesList) {
                if(otherDrone.getId() != d.getId()) {
                    ParallelCommunication parallelCommunication = new ParallelCommunication(d, otherDrone, "greeting");
                    parallelThreads.add(parallelCommunication);
                }
            }
            for(ParallelCommunication pc : parallelThreads) {
                pc.start();
            }
            for(ParallelCommunication pc : parallelThreads) {
                try {
                    pc.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Network ring generated!\n");
    }

    private static void startThreads() {
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

        printInfo = new PrintInfo(d);
        printInfo.start();
        System.out.println("PrintInfo thread started");

        if(d.getMaster()) {
            mqttSubscription = new MQTTSubscription(d);
            mqttSubscription.start();
            System.out.println("MQTTSubscription thread started");

            sendGlobalStats = new SendGlobalStats(d);
            sendGlobalStats.start();
            System.out.println("SendGlobalStats thread started");
        }

        System.out.println("");

    }

    public static void removeFromSmartCity(int id) {
        WebResource webResource = client.resource(d.getServerAmmAddress() + "drone/remove");
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).delete(ClientResponse.class, Integer.toString(id));
        if(response.getStatus() == Response.Status.NOT_ACCEPTABLE.getStatusCode()) {
            System.out.println("Drone " + id + " already removed");
        }
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

                    NetworkProtoGrpc.NetworkProtoStub stub = NetworkProtoGrpc.newStub(channel);
                    NetworkService.Order request = NetworkService.Order.newBuilder()
                            .setOrderId(order.getId())
                            .setX1(order.getStartPoint().getX())
                            .setY1(order.getStartPoint().getY())
                            .setX2(order.getFinishPoint().getX())
                            .setY2(order.getFinishPoint().getY())
                            .build();
                    try {
                        stub.deliverOrder(request, new StreamObserverCallback(channel));
                    } catch(StatusRuntimeException sre) {
                        System.out.println("Can't assign order " + order.getId());
                    }
                } else {
                    MainDrone.delivery = new Delivery(d, order);
                    MainDrone.delivery.start();
                }
            }
        }

    }

    private static int bestAvailableDrone(Drone d, Order order, boolean itSelf) {
        candidates = new ArrayList<>();
        ArrayList<TopologyDrone> dronesList = d.getNetworkTopology().getDronesList();
        ArrayList<ParallelCommunication> parallelThreads = new ArrayList<>();
        for(TopologyDrone td : dronesList) {
            if(td.getId() != d.getId()) {
                ParallelCommunication parallelCommunication = new ParallelCommunication(d, td, "bestAvailableDrone", order);
                parallelThreads.add(parallelCommunication);
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

        for(ParallelCommunication pc : parallelThreads) {
            pc.start();
        }
        for(ParallelCommunication pc : parallelThreads) {
            try {
                pc.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                mqttSubscription.mqttClient.disconnectForcibly();
                mqttSubscription.mqttClient.close();
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
        removeFromSmartCity(d.getId());
        System.exit(0);
    }

    public static void rechargingRequest(Drone d) {
        d.setDelivering(true);
        d.setRequestRecharging(true);
        d.setTimestamp(System.currentTimeMillis());

        ArrayList<TopologyDrone> dronesList = d.getNetworkTopology().getDronesList();
        ArrayList<ParallelCommunication> parallelThreads = new ArrayList<>();

        for(TopologyDrone td : dronesList) {
            if(td.getId() != d.getId()) {
                ParallelCommunication parallelCommunication = new ParallelCommunication(d, td, "recharge", d.getTimestamp());
                parallelThreads.add(parallelCommunication);
            }
        }

        for(ParallelCommunication pc : parallelThreads) {
            pc.start();
        }
        for(ParallelCommunication pc : parallelThreads) {
            try {
                pc.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        d.setRequestRecharging(false);
        d.setRecharging(true);

        Recharge recharge = new Recharge(d);
        recharge.start();
        System.out.println("Recharge thread started");
        try {
            recharge.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        d.setRecharging(false);

        if(d.getMaster() == false) {
            TopologyDrone master = d.getNetworkTopology().getDroneWithId(d.getMasterId());
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(master.getIp() + ":" + master.getPort())
                    .usePlaintext(true)
                    .build();

            NetworkProtoGrpc.NetworkProtoStub stub = NetworkProtoGrpc.newStub(channel);
            NetworkService.DronePosition request = NetworkService.DronePosition.newBuilder()
                    .setId(d.getId())
                    .setX(d.getPosition().getX())
                    .setY(d.getPosition().getY())
                    .build();
            try {
                stub.newDronePosition(request, new StreamObserverCallback(channel));
            } catch (StatusRuntimeException sre) {
                System.out.println("Drone " + master.getId() + " not reachable");
            }
        }

        if(d.getQueue().size() > 0) {
            dronesList = d.getNetworkTopology().getDronesList();
            parallelThreads = new ArrayList<>();
            for(TopologyDrone td : dronesList) {
                if(d.inQueue(td.getId())) {
                    ParallelCommunication parallelCommunication = new ParallelCommunication(d, td, "rechargeOk");
                    parallelThreads.add(parallelCommunication);
                }
            }

            for(ParallelCommunication pc : parallelThreads) {
                pc.start();
            }

            d.getQueue().clear();
        }

        System.out.println("Recharged ok");
        d.setDelivering(false);
    }

}
