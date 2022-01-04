import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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

        //set the drone position to the starting point
        int distance = (int) Math.sqrt(Math.pow(order.getStartPoint().getX() - d.getPosition().getX(), 2) + Math.pow(order.getStartPoint().getY() - d.getPosition().getY(),2));
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
        distance = (int) Math.sqrt(Math.pow(order.getFinishPoint().getX() - order.getStartPoint().getX(), 2) + Math.pow(order.getFinishPoint().getY() - order.getStartPoint().getY(),2));
        d.setPosition(order.getFinishPoint());
        td.setPosition(d.getPosition());
        d.setBattery(d.getBattery()-10);
        d.setnOrders(d.getnOrders()+1);
        d.setCoveredDistance(d.getCoveredDistance() + distance);

        d.setDelivering(false);

        System.out.println("Order " + order.getId());
        System.out.println(d.getStatus());

        //info to master
        if(d.getMaster() == false) {
            TopologyDrone master = d.getNetworkTopology().getDroneWithId(d.getMasterId());
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(master.getIp() + ":" + master.getPort())
                    .usePlaintext(true)
                    .build();

            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.DroneStats request = NetworkService.DroneStats.newBuilder()
                    .setTimestamp(new Timestamp(System.currentTimeMillis()).toString())
                    .setId(d.getId())
                    .setX(d.getPosition().getX())
                    .setY(d.getPosition().getY())
                    .setDistance(d.getCoveredDistance())
                    .setBattery(d.getBattery())
                    .build();
            stub.deliverStats(request);
            channel.shutdownNow();
        }

    }

    public Drone getD() {
        return d;
    }

    public void setD(Drone d) {
        this.d = d;
    }

}
