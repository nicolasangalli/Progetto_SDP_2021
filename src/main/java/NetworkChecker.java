import Libraries.Drone;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;


public class NetworkChecker extends Thread {

    private Drone d;
    public boolean running;

    public NetworkChecker(Drone d) {
        this.d = d;
        running = true;
    }

    public void run() {
        while(running) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(d.getNetworkTopology().getDronesList().size() > 1) {
                TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);
                int idDeletedDrone = nextDrone.getId();

                final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                        .usePlaintext(true)
                        .build();

                try {
                    NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                    NetworkService.HelloRequest request = NetworkService.HelloRequest.newBuilder()
                            .setId(d.getId())
                            .build();
                    stub.greeting(request);
                    channel.shutdownNow();
                    System.out.println("drone " + nextDrone.getId() + " reachable");
                } catch (StatusRuntimeException sre) {
                    System.out.println("drone " + nextDrone.getId() + " not reachable");
                    MainDrone.removeFromSmartCity(nextDrone.getId());
                    d.getNetworkTopology().removeDrone(nextDrone);

                    if(d.getNetworkTopology().getDronesList().size() > 1) {
                        if(d.getMasterId() == idDeletedDrone) {

                            d.setParticipant(true);
                            nextDrone = d.getNetworkTopology().getNextDrone(d);
                            System.out.println("next drone: " + nextDrone.getId());
                            final ManagedChannel channel2 = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                                    .usePlaintext(true)
                                    .build();

                            NetworkProtoGrpc.NetworkProtoBlockingStub stub2 = NetworkProtoGrpc.newBlockingStub(channel2);
                            NetworkService.ElectionMsg request2 = NetworkService.ElectionMsg.newBuilder()
                                    .setId(d.getId())
                                    .setBattery(d.getBattery())
                                    .setIdOldMaster(idDeletedDrone)
                                    .build();
                            stub2.election(request2);
                            channel.shutdownNow();
                        } else {
                            System.out.println("Droni rimasti: " + d.getNetworkTopology().getDronesList().size());

                            if(d.getNetworkTopology().getDronesList().size() > 0) {
                                ArrayList<ParallelCommunication> parallelThreads = new ArrayList<>();
                                for (TopologyDrone td : d.getNetworkTopology().getDronesList()) {
                                    ParallelCommunication parallelCommunication = new ParallelCommunication(d, td, "remove", idDeletedDrone);
                                    parallelThreads.add(parallelCommunication);

                                    /*
                                    final ManagedChannel channel2 = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
                                            .usePlaintext(true)
                                            .build();

                                    NetworkProtoGrpc.NetworkProtoBlockingStub stub2 = NetworkProtoGrpc.newBlockingStub(channel2);
                                    NetworkService.DroneId request2 = NetworkService.DroneId.newBuilder()
                                            .setId(idDeletedDrone)
                                            .build();
                                    stub2.removeDrone(request2);
                                    channel.shutdownNow();
                                    */
                                }
                                for (ParallelCommunication pc : parallelThreads) {
                                    pc.start();
                                }
                                for (ParallelCommunication pc : parallelThreads) {
                                    try {
                                        pc.join();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } else {
                        d.setMaster(true);
                        d.setMasterId(d.getId());

                        MainDrone.mqttSubscription = new MQTTSubscription(d);
                        MainDrone.mqttSubscription.start();
                        System.out.println("MQTTSubscription thread started");

                        MainDrone.sendGlobalStats = new SendGlobalStats(d);
                        MainDrone.sendGlobalStats.start();
                        System.out.println("SendGlobalStats thread started");
                    }
                }

            }
        }
    }

}
