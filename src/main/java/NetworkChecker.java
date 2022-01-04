import Libraries.Drone;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class NetworkChecker extends Thread {

    private Drone d;

    public NetworkChecker(Drone d) {
        this.d = d;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(d.getNetworkTopology().getDronesList().size() > 1) {
                TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
                System.out.println("Check if the next drone is online");
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                        .usePlaintext(true)
                        .build();

                NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                NetworkService.HelloRequest request = NetworkService.HelloRequest.newBuilder()
                        .setId(d.getId())
                        .build();
                NetworkService.HelloResponse response = stub.greeting(request);
                if (response.getResp().equalsIgnoreCase("online")) {
                    System.out.println("ok");
                } else {
                    System.out.println("not reachable");
                }
                channel.shutdownNow();
            }
        }
    }

}
