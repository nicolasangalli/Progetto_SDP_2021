import Libraries.DroneSmartCity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class NetworkTopology {

    private ArrayList<DroneSmartCity> dronesList;

    public NetworkTopology() {
        dronesList = new ArrayList<>();
    }

    public ArrayList<DroneSmartCity> getDronesList() {
        return dronesList;
    }

    public void setDronesList(ArrayList<DroneSmartCity> dronesList) {
        this.dronesList = dronesList;
        orderDroneList();
    }

    public void addNewDrone(DroneSmartCity droneSmartCity) {
        dronesList.add(droneSmartCity);
        orderDroneList();
    }

    private void orderDroneList() {
        if(dronesList.size() > 1) {
            Collections.sort(dronesList, new Comparator<DroneSmartCity>() {
                @Override
                public int compare(DroneSmartCity dsc1, DroneSmartCity dsc2) {
                    if (dsc1.getId() > dsc2.getId()) {
                        return 1;
                    }
                    if (dsc1.getId() < dsc2.getId()) {
                        return -1;
                    }
                    return 0;
                }
            });
            this.dronesList = dronesList;
        }
    }

    public String getNetworkDrones() {
        String ret = "Drones in the network:\n";
        for (DroneSmartCity dsc : dronesList) {
            ret += "(" + dsc.getId() + ", ip: " + dsc.getIp() + ", port: " + dsc.getPort() + ", position: (" + dsc.getPosition().getX() + "," + dsc.getPosition().getY() + "))\n";
        }
        return ret;
    }

}
