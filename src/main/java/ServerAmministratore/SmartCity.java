package ServerAmministratore;

import Drone.Drone;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;


@XmlRootElement
public class SmartCity {

    private ArrayList<Drone> drones;
    private static SmartCity instance;

    private SmartCity() {
        drones = new ArrayList<>();
    }

    public synchronized static SmartCity getInstance() {
        if(instance == null) {
            instance = new SmartCity();
        }
        return instance;
    }

    public synchronized void addNewDrone(Drone d) {
        drones.add(d);
    }

    public synchronized void removeDrone(Drone d) {
        int id = d.getId();
        for(Drone i : drones) {
            if(i.getId() == id) {
                drones.remove(i);
                break;
            }
        }
    }

    public synchronized boolean checkAvailable(Drone d) {
        int id = d.getId();
        int port = d.getPort();
        for(Drone i : drones) {
            if(i.getId() == id || i.getPort() == port) {
                return false;
            }
        }
        return true;
    }

    public String prettyPrinter(Drone d) {
        int id = d.getId();
        ArrayList<Drone> listToPrint = drones;
        String toPrint = "List of other drones in SmartCity:";
        for(Drone i : listToPrint) {
            if(i.getId() != id) {
                toPrint += "\nid: " + i.getId() + " (...)";
            }
        }
        return toPrint;
    }

    public String prettyPrinter() {
        ArrayList<Drone> listToPrint = drones;
        String toPrint = "List of drones in SmartCity:";
        for(Drone i : listToPrint) {
            toPrint += "\nid: " + i.getId() + " (...)";
        }
        return toPrint;
    }

    public String getStats() {
        String stats = "";
        ArrayList<Drone> listOfDrones = drones;
        int avgBatteryLevel = 0;
        for(Drone i : listOfDrones) {
            avgBatteryLevel += i.getBattery();
        }
        avgBatteryLevel /= listOfDrones.size();
        stats = "Average battery level: " + avgBatteryLevel;
        return stats;
    }

}
