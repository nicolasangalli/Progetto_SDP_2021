package Other;

import Libraries.Drone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class NetworkChecker extends Thread {

    private Drone d;
    private DroneSmartCity dsc;

    public NetworkChecker(Drone d, DroneSmartCity dsc) {
        this.d = d;
        this.dsc = dsc;
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DroneSmartCity nextDrone = d.getNetworkTopology().getNextDrone(dsc);
            if(nextDrone != null) {
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                        .usePlaintext(true)
                        .build();
                NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                NetworkService.HelloRequest request = NetworkService.HelloRequest.newBuilder()
                        .setId(d.getId())
                        .build();
                try {
                    NetworkService.HelloResponse response = stub.greeting(request);
                } catch (Exception e) {
                    d.getNetworkTopology().removeDrone(nextDrone);

                    //comunico a tutti i nodi della rete di rimuovere nextDrone;

                    if(nextDrone.getId() == d.getMasterId()) {
                        //elezione nuovo master, metto in pausa il thread
                    }
                }

                channel.shutdownNow();
            }
        }
    }

}
