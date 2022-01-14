package Libraries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class NetworkTopology {

    private ArrayList<TopologyDrone> dronesList;

    public NetworkTopology() {
        dronesList = new ArrayList<>();
    }

    public ArrayList<TopologyDrone> getDronesList() {
        return dronesList;
    }

    public void setDronesList(ArrayList<TopologyDrone> dronesList) {
        this.dronesList = dronesList;
        orderDroneList();
    }

    public synchronized TopologyDrone getDroneWithId(int id) {
        for(TopologyDrone td : dronesList) {
            if(td.getId() == id) {
                return td;
            }
        }
        return null;
    }

    public synchronized void addNewDrone(TopologyDrone d) {
        dronesList.add(d);
        orderDroneList();
    }

    public synchronized void addNewDrone(Drone d) {
        TopologyDrone td = new TopologyDrone(d.getId(), d.getIp(), d.getPort(), d.getPosition());
        dronesList.add(td);
        orderDroneList();
    }

    private void orderDroneList() {
        if(dronesList.size() > 1) {
            Collections.sort(dronesList, new Comparator<TopologyDrone>() {
                @Override
                public int compare(TopologyDrone d1, TopologyDrone d2) {
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

    public synchronized TopologyDrone getNextDrone(TopologyDrone d) {
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

    public synchronized TopologyDrone getNextDrone(Drone d) {
        if(dronesList.size() <= 1) {
            return dronesList.get(0);
        }

        int index = 0;
        for(TopologyDrone td : dronesList) {
            if(dronesList.get(index).getId() != d.getId()) {
                index++;
            } else {
                break;
            }
        }

        if(index == dronesList.size()-1) {
            index = 0;
        } else {
            index++;
        }
        return dronesList.get(index);
    }

    public synchronized String getNetworkDrones() {
        String ret = "Drones in the network:\n";
        for (TopologyDrone d : dronesList) {
            ret += "(" + d.getId() + ", ip: " + d.getIp() + ", port: " + d.getPort() + ", pos:("+ d.getPosition().getX() + "," + d.getPosition().getY() + "))\n";
        }
        return ret;
    }

    public synchronized void removeDrone(TopologyDrone d) {
        dronesList.remove(d);
    }

    public synchronized void removeDrone(int id) {
        for(TopologyDrone td : dronesList) {
            if(td.getId() == id) {
                dronesList.remove(td);
                break;
            }
        }
    }

}
