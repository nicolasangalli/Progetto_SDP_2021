import Libraries.Coordinate;
import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;


public class ParallelCommunication extends Thread {

    private Drone d;
    private TopologyDrone td;
    private String type;
    private Order order;
    private int idDeletedDrone;
    private long timestamp;

    public ParallelCommunication(Drone d, TopologyDrone td, String type) {
        this.d = d;
        this.td = td;
        this.type = type;
        this.order = null;
        this.idDeletedDrone = -1;
        this.timestamp = 0;
    }

    public ParallelCommunication(Drone d, TopologyDrone td, String type, Order order) {
        this.d = d;
        this.td = td;
        this.type = type;
        this.order = order;
        this.idDeletedDrone = -1;
        this.timestamp = 0;
    }

    public ParallelCommunication(Drone d, TopologyDrone td, String type, int idDeletedDrone) {
        this.d = d;
        this.td = td;
        this.type = type;
        this.order = null;
        this.idDeletedDrone = idDeletedDrone;
        this.timestamp = 0;
    }

    public ParallelCommunication(Drone d, TopologyDrone td, String type, long timestamp) {
        this.d = d;
        this.td = td;
        this.type = type;
        this.order = null;
        this.idDeletedDrone = -1;
        this.timestamp = timestamp;
    }

    public void run() {
        if(type.equals("greeting")) {
            System.out.println("Try to communicate with drone with id = " + td.getId());
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
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
            try {
                NetworkService.Master response = stub.addNewDrone(request);
                d.setMasterId(response.getMasterId());
                System.out.println("Drone with id = " + td.getId() + " say: OK! The drone master id = " + d.getMasterId());
            } catch (StatusRuntimeException sre) {
                System.out.println("Drone " + td.getId() + " not reachable");
                MainDrone.removeFromSmartCity(td.getId());
            }
            channel.shutdownNow();
        } else if(type.equals("bestAvailableDrone")) {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
                    .usePlaintext(true)
                    .build();

            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.HelloRequest request = NetworkService.HelloRequest.newBuilder()
                    .setId(d.getId())
                    .build();

            try {
                NetworkService.DroneDeliveryInfo response = stub.freeDrone(request);

                if (response.getDelivery() == false) {
                    ArrayList<Integer> candidate = new ArrayList<>();
                    candidate.add(response.getId());
                    int distance = (int) Math.sqrt(Math.pow(order.getStartPoint().getX() - response.getX(), 2) + Math.pow(order.getStartPoint().getY() - response.getY(), 2));
                    candidate.add(distance);
                    candidate.add(response.getBattery());

                    MainDrone.candidates.add(candidate);
                }

                td.setPosition(new Coordinate(response.getX(), response.getY()));
            } catch (StatusRuntimeException sre) {
                System.out.println("Drone " + td.getId() + " not reachable");
            }

            channel.shutdownNow();
        } else if(type.equals("remove")) {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
                    .usePlaintext(true)
                    .build();

            NetworkProtoGrpc.NetworkProtoStub stub = NetworkProtoGrpc.newStub(channel);
            NetworkService.DroneId request = NetworkService.DroneId.newBuilder()
                    .setId(idDeletedDrone)
                    .build();
            try {
                stub.removeDrone(request, new StreamObserverCallback());
            } catch (StatusRuntimeException sre) {
                System.out.println("Drone " + td.getId() + " not reachable");
            }

            channel.shutdownNow();
        } else if(type.equals("recharge")) {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
                    .usePlaintext(true)
                    .build();

            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.RechargeRequest request = NetworkService.RechargeRequest.newBuilder()
                    .setId(d.getId())
                    .setTimestamp(timestamp)
                    .build();
            try {
                stub.recharging(request);
            } catch (StatusRuntimeException sre) {
                System.out.println("Drone " + td.getId() + " not reachable");
            }

            channel.shutdownNow();
        } else if(type.equals("rechargeOk")) {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
                    .usePlaintext(true)
                    .build();

            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.HelloRequest request = NetworkService.HelloRequest.newBuilder()
                    .setId(d.getId())
                    .build();
            try {
                stub.rechargeOK(request);
            } catch (StatusRuntimeException sre) {
                System.out.println("Drone " + td.getId() + " not reachable");
            }

            channel.shutdownNow();
        }
    }

}
