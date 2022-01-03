package Libraries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class NetworkTopology {

    private ArrayList<Drone> dronesList;

    public NetworkTopology() {
        dronesList = new ArrayList<>();
    }

    public ArrayList<Drone> getDronesList() {
        return dronesList;
    }

    public void setDronesList(ArrayList<Drone> dronesList) {
        this.dronesList = dronesList;
        orderDroneList();
    }

    public void addNewDrone(Drone d) {
        dronesList.add(d);
        orderDroneList();
    }

    private void orderDroneList() {
        if(dronesList.size() > 1) {
            Collections.sort(dronesList, new Comparator<Drone>() {
                @Override
                public int compare(Drone d1, Drone d2) {
                    if (d1.getId() > d2.getId()) {
                        return 1;
                    }
                    if (d1.getId() < d2.getId()) {
                        return -1;
                    }
                    return 0;
                }
            });
            this.dronesList = dronesList;
        }
    }

    public Drone getNextDrone(Drone d) {
        if(dronesList.size() <= 1) {
            return null;
        }
        int index = dronesList.indexOf(d);
        if(index == dronesList.size()-1) {
            index = 0;
        } else {
            index++;
        }
        return dronesList.get(index);
    }

    public String getNetworkDrones() {
        String ret = "Drones in the network:\n";
        for (Drone d : dronesList) {
            ret += "(" + d.getId() + ", ip: " + d.getIp() + ", port: " + d.getPort() + ", position: (" + d.getPosition().getX() + "," + d.getPosition().getY() + "))\n";
        }
        return ret;
    }

    public void removeDrone(Drone d) {
        dronesList.remove(d);
    }

}
