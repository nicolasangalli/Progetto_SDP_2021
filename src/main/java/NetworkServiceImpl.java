import Libraries.Coordinate;
import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;


public class NetworkServiceImpl extends NetworkProtoGrpc.NetworkProtoImplBase {

    private Drone d;

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

    @Override
    public void greeting(NetworkService.HelloRequest request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("online")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void election(NetworkService.ElectionMsg request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        int id = request.getId();
        int battery = request.getBattery();

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
        } else if(id == d.getId()) {
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
            MQTTSubscription mqttSubscription = new MQTTSubscription(d);
            mqttSubscription.start();
        }

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("elected...")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

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

    @Override
    public void deliverOrder(NetworkService.Order request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        Order order = new Order(request.getOrderId(), new Coordinate(request.getX1(), request.getY1()), new Coordinate(request.getX2(), request.getY2()));
        Delivery delivery = new Delivery(d, order);
        delivery.start();

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("online")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

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
