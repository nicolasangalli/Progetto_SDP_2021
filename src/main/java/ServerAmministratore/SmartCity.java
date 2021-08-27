package ServerAmministratore;

import Libraries.Coordinate;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Random;


@XmlRootElement
public class SmartCity {

    private static SmartCity instance;
    private ArrayList<DroneSmartCity> drones;

    private SmartCity() {
        drones = new ArrayList<>();
    }

    public synchronized static SmartCity getInstance() {
        if(instance == null) {
            instance = new SmartCity();
        }
        return instance;
    }

    public synchronized boolean checkAvailable(int id) {
        for(DroneSmartCity drone : drones) {
            if(drone.getId() == id) {
                return false;
            }
        }
        return true;
    }

    //Add drone with id to SmartCity and return the position that it should be placed
    public synchronized Coordinate addNewDrone(DroneSmartCity drone) {
        drones.add(drone);

        //generate random position
        Random random = new Random();
        Coordinate position = new Coordinate(random.nextInt(10), random.nextInt(10));

        return position;
    }

    //return a list with the position of drone and all the other drones
    public ArrayList<Object> getDrones(int id, int x, int y) {
        ArrayList<DroneSmartCity> list = drones;
        ArrayList<Object> ret = new ArrayList<>();

        ret.add(x);
        ret.add(y);
        for(DroneSmartCity drone : list) {
            if(drone.getId() != id) {
                ret.add(drone);
            }
        }

        return ret;
    }

    public synchronized void removeDrone(int id) {
        for(DroneSmartCity drone : drones) {
            if(drone.getId() == id) {
                drones.remove(drone);
                break;
            }
        }
    }

    //return the drones list
    public ArrayList<DroneSmartCity> getStats() {
        ArrayList<DroneSmartCity> list = drones;
        return list;
    }

}
