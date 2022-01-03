import Libraries.Drone;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;


public class NetworkServiceImpl extends NetworkProtoGrpc.NetworkProtoImplBase {

    private Drone d;

    @Override
    public void addNewDrone(NetworkService.NewDrone request, StreamObserver<NetworkService.Master> responseObserver) {
        TopologyDrone otherDrone = new TopologyDrone(request.getId(), request.getIp(), request.getPort());
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
    public void election(NetworkService.ElectionId request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        int id = request.getId();
        if(id > d.getId()) {
            d.setParticipant(true);

            TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                    .usePlaintext(true)
                    .build();
            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.ElectionId req = NetworkService.ElectionId.newBuilder()
                    .setId(id)
                    .build();
            stub.election(req);
            channel.shutdownNow();
        } else if(id < d.getId()) {
            if(d.getParticipant() == false) {
                d.setParticipant(true);

                TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                        .usePlaintext(true)
                        .build();
                NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                NetworkService.ElectionId req = NetworkService.ElectionId.newBuilder()
                        .setId(d.getId())
                        .build();
                stub.election(req);
                channel.shutdownNow();
            }
        } else if(id == d.getId()) {
            d.setParticipant(false);
            d.setMaster(true);
            d.setMasterId(d.getId());

            TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                    .usePlaintext(true)
                    .build();
            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.ElectionId req = NetworkService.ElectionId.newBuilder()
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
    public void elected(NetworkService.ElectionId request, StreamObserver<NetworkService.HelloResponse> responseObserver) {
        int id = request.getId();
        d.setParticipant(false);
        d.setMasterId(id);

        if(id != d.getId()) {
            TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                    .usePlaintext(true)
                    .build();
            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.ElectionId req = NetworkService.ElectionId.newBuilder()
                    .setId(id)
                    .build();
            stub.elected(req);
            channel.shutdownNow();
        }

        NetworkService.HelloResponse response = NetworkService.HelloResponse.newBuilder()
                .setResp("elected...")
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
