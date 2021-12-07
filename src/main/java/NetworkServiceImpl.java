import Libraries.DroneSmartCity;
import io.grpc.stub.StreamObserver;

public class NetworkServiceImpl extends NetworkProtoGrpc.NetworkProtoImplBase {

    private Drone d;

    @Override
    public void newDrone(NetworkService.DroneSmartCity request, StreamObserver<NetworkService.Master> responseObserver) {
        DroneSmartCity droneSmartCity = new DroneSmartCity(request.getId(), request.getIp(), request.getPort());
        d.getNetworkTopology().addNewDrone(droneSmartCity);

        NetworkService.Master response = NetworkService.Master.newBuilder()
                .setMasterId(d.getMasterId())
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
