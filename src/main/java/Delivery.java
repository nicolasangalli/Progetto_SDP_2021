import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.sql.Timestamp;


public class Delivery extends Thread {

    private Drone d;
    private Order order;

    public Delivery() {}

    public Delivery(Drone d, Order order) {
        this.d = d;
        this.order = order;
    }

    public void run() {
        d.setDelivering(true);

        if(d.getBattery() >= 15) {
            //set the drone position to the starting point
            int distance = (int) Math.sqrt(Math.pow(order.getStartPoint().getX() - d.getPosition().getX(), 2) + Math.pow(order.getStartPoint().getY() - d.getPosition().getY(), 2));
            d.setPosition(order.getStartPoint());
            TopologyDrone td = d.getNetworkTopology().getDroneWithId(d.getId());
            td.setPosition(d.getPosition());
            d.setCoveredDistance(d.getCoveredDistance() + distance);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //set the drone position to the finish point
            distance = (int) Math.sqrt(Math.pow(order.getFinishPoint().getX() - order.getStartPoint().getX(), 2) + Math.pow(order.getFinishPoint().getY() - order.getStartPoint().getY(), 2));
            d.setPosition(order.getFinishPoint());
            td.setPosition(d.getPosition());
            d.setBattery(d.getBattery() - 10);
            d.setnOrders(d.getnOrders() + 1);
            d.setCoveredDistance(d.getCoveredDistance() + distance);

            System.out.println("Order " + order.getId());
            System.out.println("Battery level: " + d.getBattery());

            //info to master
            if (d.getMaster() == false) {
                TopologyDrone master = d.getNetworkTopology().getDroneWithId(d.getMasterId());
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(master.getIp() + ":" + master.getPort())
                        .usePlaintext(true)
                        .build();

                NetworkProtoGrpc.NetworkProtoStub stub = NetworkProtoGrpc.newStub(channel);
                NetworkService.DroneStats request = NetworkService.DroneStats.newBuilder()
                        .setTimestamp(new Timestamp(System.currentTimeMillis()).toString())
                        .setId(d.getId())
                        .setX(d.getPosition().getX())
                        .setY(d.getPosition().getY())
                        .setNOrders(d.getnOrders())
                        .setDistance(d.getCoveredDistance())
                        .setPollutionLevel(d.getPollutionLevel())
                        .setBattery(d.getBattery())
                        .build();
                try {
                    stub.deliverStats(request, new StreamObserverCallback());
                } catch (StatusRuntimeException sre) {
                    System.out.println("Can't send info to master because is not reachable");
                }
                channel.shutdownNow();
            } else {
                d.getAvgOrder().add(d.getnOrders());
                d.getAvgKm().add(d.getCoveredDistance());
                d.getAvgPollution().add(d.getPollutionLevel());
                d.getAvgBattery().add(d.getBattery());
            }
        }

        if(d.getBattery() < 15) {
            MainDrone.rechargingRequest(d);
        } else {
            d.setDelivering(false);
        }

    }

    public Drone getD() {
        return d;
    }

    public void setD(Drone d) {
        this.d = d;
    }

}
