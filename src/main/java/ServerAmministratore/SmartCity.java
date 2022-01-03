package ServerAmministratore;

import Libraries.Coordinate;
import Libraries.Drone;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Random;


@XmlRootElement
public class SmartCity {

    private static SmartCity instance;
    private ArrayList<Drone> drones;

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
        for(Drone d : drones) {
            if(d.getId() == id) {
                return false;
            }
        }
        return true;
    }

    //Add drone with id to SmartCity and return the position that it should be placed
    public synchronized Coordinate addNewDrone(Drone d) {
        drones.add(d);

        //generate random position
        Random random = new Random();
        Coordinate position = new Coordinate(random.nextInt(10), random.nextInt(10));

        return position;
    }

    //return a list with the position of drone and all the other drones
    public ArrayList<Object> getDrones(int id, int x, int y) {
        ArrayList<Drone> list = drones;
        ArrayList<Object> ret = new ArrayList<>();

        ret.add(x);
        ret.add(y);
        for(Drone d : list) {
            if(d.getId() != id) {
                ret.add(d);
            }
        }

        return ret;
    }

    public synchronized void removeDrone(int id) {
        for(Drone d : drones) {
            if(d.getId() == id) {
                drones.remove(d);
                break;
            }
        }
    }

    //return the drones list
    public ArrayList<Drone> getStats() {
        ArrayList<Drone> list = drones;
        return list;
    }

}
