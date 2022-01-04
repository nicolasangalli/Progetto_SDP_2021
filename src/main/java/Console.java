import Libraries.Coordinate;
import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


public class Console extends Thread {

    private Drone d;

    public Console(Drone d) {
        this.d = d;
    }

    public void run() {
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        while(true) {
            String input = scanner.nextLine();
            if(input.trim().equalsIgnoreCase("stats")) {

            } else if(input.trim().equalsIgnoreCase("print")) {
                System.out.println(d.getNetworkTopology().getNetworkDrones());
            } else if(input.trim().equalsIgnoreCase("status")) {
                System.out.println(d.getStatus());
            } else if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                System.out.println("quitting...");
            } else if(input.trim().equalsIgnoreCase("master")) {
                masterElection(d);
            } else if(input.trim().equalsIgnoreCase("order")) {
                assignOrder(d);
            } else if(input.trim().equalsIgnoreCase("mqtt")) {
                MQTTSubscription mqttSubscription = new MQTTSubscription(d);
                mqttSubscription.start();
            } else if(input.trim().equalsIgnoreCase("pos")) {
                Random r = new Random();
                Coordinate pos = new Coordinate(r.nextInt(10), r.nextInt(10));
                d.setPosition(pos);
                TopologyDrone td = d.getNetworkTopology().getDroneWithId(d.getId());
                td.setPosition(pos);
            }
        }
    }

    private static void masterElection(Drone d) {
        d.setParticipant(true);

        TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                .usePlaintext(true)
                .build();

        NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
        NetworkService.ElectionMsg request = NetworkService.ElectionMsg.newBuilder()
                .setId(d.getId())
                .setBattery(d.getBattery())
                .build();
        stub.election(request);
        channel.shutdownNow();
    }

    private static void assignOrder(Drone d) {
        if(d.getOrdersList().size() > 0) {
            Order order = d.getOrdersList().get(0);
            d.getOrdersList().remove(0);
            int idCandidate = bestAvailableDrone(d, order);
            if(idCandidate == -1) { //no drones available, reinsert the order in the list
                d.getOrdersList().add(order);
            } else {
                TopologyDrone drone = d.getNetworkTopology().getDroneWithId(idCandidate);

                final ManagedChannel channel = ManagedChannelBuilder.forTarget(drone.getIp() + ":" + drone.getPort())
                        .usePlaintext(true)
                        .build();

                NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                NetworkService.Order request = NetworkService.Order.newBuilder()
                        .setOrderId(order.getId())
                        .setX(order.getFinishPoint().getX())
                        .setY(order.getFinishPoint().getY())
                        .build();
                stub.deliverOrder(request);
                channel.shutdownNow();
            }
        }

    }

    private static int bestAvailableDrone(Drone d, Order order) {
        ArrayList<ArrayList<Integer>> candidates = new ArrayList<>();
        ArrayList<TopologyDrone> dronesList = d.getNetworkTopology().getDronesList();
        for(TopologyDrone td : dronesList) {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
                    .usePlaintext(true)
                    .build();

            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.HelloRequest request = NetworkService.HelloRequest.newBuilder()
                    .setId(d.getId())
                    .build();
            NetworkService.DroneDeliveryInfo response = stub.freeDrone(request);

            if(response.getDelivery() == false) {
                ArrayList<Integer> candidate = new ArrayList<>();
                candidate.add(response.getId());
                int distance = (int) Math.sqrt(Math.pow(order.getStartPoint().getX() - response.getX(), 2) + Math.pow(order.getStartPoint().getY() - response.getY(),2));
                candidate.add(distance);
                candidate.add(response.getBattery());

                candidates.add(candidate);
            }

            channel.shutdownNow();
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

}
