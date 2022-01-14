import Libraries.Coordinate;
import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;


public class NetworkServiceImpl extends NetworkProtoGrpc.NetworkProtoImplBase {

    private Drone d;

    /*
    Add a new drone in the drone topology.
    Return the drone master id to the caller.
    Called when:
    - a new drone presents itself to the other drones
    */
    @Override
    public void addNewDrone(NetworkService.NewDrone request, StreamObserver<NetworkService.Master> responseObserver) {
        TopologyDrone otherDrone = new TopologyDrone(request.getId(), request.getIp(), request.getPort(), new Coordinate(request.getX(), request.getY()));
        d.getNetworkTopology().addNewDrone(otherDrone);

        NetworkService.Master response = NetworkService.Master.newBuilder()
                .setMasterId(d.getMasterId())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /*
        Remove the drone with a specific id in the network.
        Return a dummy string.
        Called when:
        - a drone know that the next drone is not reachable and wants to inform the others drones in the network
        */
    @Override
    public void removeDrone(NetworkService.DroneId request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        d.getNetworkTopology().removeDrone(request.getId());

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("remove")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /*
        Answer the caller saying that this drone is reachable
        Return the string "online" to the caller
        Called when:
        - the 'NetworkChecker' thread check if the next drone in the ring network is online
         */
    @Override
    public void greeting(NetworkService.HelloRequest request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("online")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /*
    First method of the 'Chang and Roberts' master election algorithm, used to find the new master id.
    First of all the function removed the old master drone from the network.
    The algorithm choose the drones with the greater battery level, in case of equality it chooses the drones with the greater id
    Return a dummy string to the previous drone in the network ring
    Called when:
    - console call (temporary)
     */
    @Override
    public void election(NetworkService.ElectionMsg request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        int id = request.getId();
        int battery = request.getBattery();
        int idOldMaster = request.getIdOldMaster();

        TopologyDrone oldMaster = d.getNetworkTopology().getDroneWithId(idOldMaster);
        d.getNetworkTopology().removeDrone(oldMaster);

        if(battery > d.getBattery() || (battery == d.getBattery() && id > d.getId())) {
            d.setParticipant(true);

            TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                    .usePlaintext(true)
                    .build();
            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.ElectionMsg req = NetworkService.ElectionMsg.newBuilder()
                    .setId(id)
                    .setBattery(battery)
                    .build();
            stub.election(req);
            channel.shutdownNow();
        } else if(battery < d.getBattery() || (battery == d.getBattery() && id < d.getId())) {
            if(d.getParticipant() == false) {
                d.setParticipant(true);

                TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                        .usePlaintext(true)
                        .build();
                NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                NetworkService.ElectionMsg req = NetworkService.ElectionMsg.newBuilder()
                        .setId(d.getId())
                        .setBattery(d.getBattery())
                        .build();
                stub.election(req);
                channel.shutdownNow();
            }
        } else if(id == d.getId()) { //master
            d.setParticipant(false);
            d.setMaster(true);
            d.setMasterId(d.getId());
            TopologyDrone master = d.getNetworkTopology().getDroneWithId(d.getId());
            master.setPosition(d.getPosition());

            TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                    .usePlaintext(true)
                    .build();
            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.ElectionMsg req = NetworkService.ElectionMsg.newBuilder()
                    .setId(d.getId())
                    .build();
            stub.elected(req);
            channel.shutdownNow();
        }

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("election...")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /*
    Second method of the 'Chang and Roberts' master election algorithm, used to tell the new master id to the drones in the network ring
    Return a dummy string to the previous drone in the network ring
    Called when:
    - the 'election' method has finished
     */
    @Override
    public void elected(NetworkService.ElectionMsg request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        int id = request.getId();
        d.setParticipant(false);
        d.setMasterId(id);

        if(id != d.getId()) {
            d.setMaster(false);

            TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                    .usePlaintext(true)
                    .build();
            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.ElectionMsg req = NetworkService.ElectionMsg.newBuilder()
                    .setId(id)
                    .build();
            stub.elected(req);
            channel.shutdownNow();

            //send the drone position to master
            TopologyDrone master = d.getNetworkTopology().getDroneWithId(id);
            final ManagedChannel channel2 = ManagedChannelBuilder.forTarget(master.getIp() + ":" + master.getPort())
                    .usePlaintext(true)
                    .build();
            NetworkProtoGrpc.NetworkProtoBlockingStub stub2 = NetworkProtoGrpc.newBlockingStub(channel2);
            NetworkService.DronePosition req2 = NetworkService.DronePosition.newBuilder()
                    .setId(d.getId())
                    .setX(d.getPosition().getX())
                    .setY(d.getPosition().getY())
                    .build();
            stub2.newDronePosition(req2);
            channel2.shutdownNow();
        } else { //master
            MainDrone.mqttSubscription = new MQTTSubscription(d);
            MainDrone.mqttSubscription.start();
            System.out.println("MQTTSubscription thread started");

            MainDrone.sendGlobalStats = new SendGlobalStats(d);
            MainDrone.sendGlobalStats.start();
            System.out.println("SendGlobalStats thread started");
        }

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("elected...")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /*
    Master function only
    Saves the new position of the caller drone in the network topology
    Return a dummy string to the caller
    Called when:
    - during the 'elected' phase, a no-master drone receive the master id
     */
    @Override
    public void newDronePosition(NetworkService.DronePosition request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        TopologyDrone td = d.getNetworkTopology().getDroneWithId(request.getId());
        td.setPosition(new Coordinate(request.getX(), request.getY()));

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("new pos...")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /*
    Return to the caller (master) the information about this drone availability for a delivery
    Called when:
    - the 'MQTTSubscription' master thread wants to assign a new order to a drone
     */
    @Override
    public void freeDrone(NetworkService.HelloRequest request, StreamObserver<NetworkService.DroneDeliveryInfo> responseObserver) {
        NetworkService.DroneDeliveryInfo response = NetworkService.DroneDeliveryInfo.newBuilder()
                .setId(d.getId())
                .setDelivery(d.getDelivering())
                .setX(d.getPosition().getX())
                .setY(d.getPosition().getY())
                .setBattery(d.getBattery())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /*
    Uses the received order info from the master and start the 'Delivery' thread
    Return a dummy string to the master
    Called when:
    - the master has assigned an order
     */
    @Override
    public void deliverOrder(NetworkService.Order request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        Order order = new Order(request.getOrderId(), new Coordinate(request.getX1(), request.getY1()), new Coordinate(request.getX2(), request.getY2()));
        MainDrone.delivery = new Delivery(d, order);
        MainDrone.delivery.start();

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("online")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /*
    Master function only
    Updates the stats values with the received info from the caller
    Return a dummy string to the caller
    Called when:
    - the 'Delivery' thread of the caller has finished the delivery
     */
    @Override
    public void deliverStats(NetworkService.DroneStats request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        d.getAvgOrder().add(request.getNOrders());
        d.getAvgKm().add(request.getDistance());
        d.getAvgPollution().add(request.getPollutionLevel());
        d.getAvgBattery().add(request.getBattery());

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("online")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public Drone getD() {
        return d;
    }

    public void setD(Drone d) {
        this.d = d;
    }

}
