package Drone;

import java.io.InputStreamReader;
import java.util.Scanner;

public class Console extends Thread{

    public void run() {
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        while(true) {
            System.out.print("Insert a command: ");
            String input = scanner.nextLine();
            if(input.trim().equalsIgnoreCase("stats")) {
            } else if(input.trim().equalsIgnoreCase("quit") || input.trim().equalsIgnoreCase("q") || input.trim().equalsIgnoreCase("exit")) {
                break;
            }
        }
    }

}
