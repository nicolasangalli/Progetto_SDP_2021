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
            System.out.print("Insert a command: ");
            String input = scanner.nextLine();
            if(input.trim().equalsIgnoreCase("stats")) {

            } else if(input.trim().equalsIgnoreCase("print")) {
                System.out.println(d.getNetworkTopology().getNetworkDrones());
            } else if(input.trim().equalsIgnoreCase("status")) {
                System.out.println(d.getStatus());
            } else if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                System.out.println("quitting...");
            }
        }
    }

}
