package ServerAmministratore;

import Dronazon.Coordinate;
import Drone.Drone;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Random;


@XmlRootElement
public class SmartCity {

    private ArrayList<String[]> drones;
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

    public synchronized boolean checkAvailable(int id, int port) {
        for(String[] drone : drones) {
            if(drone[0].equals(String.valueOf(id)) || drone[1].equals(String.valueOf(port))) {
                return false;
            }
        }
        return true;
    }

    public synchronized void addNewDrone(Drone d) {
        String[] drone = new String[2];
        drone[0] = String.valueOf(d.getId());
        drone[1] = String.valueOf(d.getPort());
        drones.add(drone);
    }

    public ArrayList<String[]> getDrones(int id) {
        ArrayList<String[]> list = drones;
        ArrayList<String[]> ret = new ArrayList<>();

        //generate random position
        Random random = new Random();
        Coordinate position = new Coordinate(random.nextInt(10), random.nextInt(10));
        String[] pos = new String[2];
        pos[0] = String.valueOf(position.getX());
        pos[1] = String.valueOf(position.getY());
        ret.add(pos);

        for(String[] drone : list) {
            if(!drone[0].equals(String.valueOf(id))) {
                ret.add(drone);
            }
        }
        return ret;
    }

}
