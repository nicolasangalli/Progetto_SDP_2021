import Libraries.Drone;
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
            if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                MainDrone.explicitExit(d);
            } else if(input.trim().equalsIgnoreCase("recharge")) {
                MainDrone.rechargingRequest(d);
            }
            //debug command
            else if(input.trim().equalsIgnoreCase("print")) {
                System.out.println(d.getNetworkTopology().getNetworkDrones());
            } else if(input.trim().equalsIgnoreCase("status")) {
                System.out.println(d.getStatus());
            } else if(input.trim().equalsIgnoreCase("d")) {
                System.out.println(d.getDelivering());
            }
        }
    }

}
