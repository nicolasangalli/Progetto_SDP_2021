import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.InputStreamReader;
import java.util.Scanner;


public class Console extends Thread {

    private Drone d;

    public Console(Drone d) {
        this.d = d;
    }

    public void run() {
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        while(true) {
            String input = scanner.nextLine();
            if(input.trim().equalsIgnoreCase("stats")) {

            } else if(input.trim().equalsIgnoreCase("print")) {
                System.out.println(d.getNetworkTopology().getNetworkDrones());
            } else if(input.trim().equalsIgnoreCase("status")) {
                System.out.println(d.getStatus());
            } else if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                System.out.println("quitting...");
            } else if(input.trim().equalsIgnoreCase("master")) {
                masterElection(d);
            } else if(input.trim().equalsIgnoreCase("order")) {
                assignOrder(d);
            }
        }
    }

    private static void masterElection(Drone d) {
        d.setParticipant(true);

        TopologyDrone nextDrone = d.getNetworkTopology().getNextDrone(d);

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextDrone.getIp() + ":" + nextDrone.getPort())
                .usePlaintext(true)
                .build();

        NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
        NetworkService.ElectionId request = NetworkService.ElectionId.newBuilder()
                .setId(d.getId())
                .build();
        stub.election(request);
        channel.shutdownNow();
    }

    private static void assignOrder(Drone d) {
        if(d.getOrdersList().size() > 0) {
            Order order = d.getOrdersList().get(0);
            d.getOrdersList().remove(0);


        }
    }

}
