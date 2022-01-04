package ServerAmministratore;

import Libraries.Coordinate;
import Libraries.TopologyDrone;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Random;


@XmlRootElement
public class SmartCity {

    private static SmartCity instance;
    private ArrayList<TopologyDrone> drones;

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
        for(TopologyDrone d : drones) {
            if(d.getId() == id) {
                return false;
            }
        }
        return true;
    }

    public synchronized Coordinate addNewDrone(TopologyDrone d) {
        //generate random position
        Random random = new Random();
        Coordinate position = new Coordinate(random.nextInt(10), random.nextInt(10));

        d.setPosition(position);
        drones.add(d);

        return position;
    }

    //return a list with the position of drone and all the other drones
    public ArrayList<Object> getDrones(int id, int x, int y) {
        ArrayList<TopologyDrone> list = drones;
        ArrayList<Object> ret = new ArrayList<>();

        ret.add(x);
        ret.add(y);
        for(TopologyDrone d : list) {
            if(d.getId() != id) {
                ret.add(d);
            }
        }

        return ret;
    }

    public synchronized void removeDrone(int id) {
        for(TopologyDrone d : drones) {
            if(d.getId() == id) {
                drones.remove(d);
                break;
            }
        }
    }

    //return the drones list
    public ArrayList<TopologyDrone> getStats() {
        ArrayList<TopologyDrone> list = drones;
        return list;
    }

}
