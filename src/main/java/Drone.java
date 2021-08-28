import Libraries.Coordinate;
import Libraries.DroneSmartCity;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Iterator;


@XmlRootElement
public class Drone {

    private int id;
    private int port;
    private String serverAmmAddress;

    private Coordinate position;
    private ArrayList<DroneSmartCity> dronesList;
    private boolean master;
    private int battery;

    public Drone() {}

    public Drone(int id, int port, String serverAmmAddress) {
        this.id = id;
        this.port = port;
        this.serverAmmAddress = serverAmmAddress;

        position = new Coordinate(-1, -1);
        dronesList = new ArrayList<>();
        master = false;
        battery = 100;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerAmmAddress() {
        return serverAmmAddress;
    }

    public void setServerAmmAddress(String serverAmmAddress) {
        this.serverAmmAddress = serverAmmAddress;
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public ArrayList<DroneSmartCity> getDronesList() {
        return dronesList;
    }

    public void setDronesList(ArrayList<DroneSmartCity> dronesList) {
        this.dronesList = dronesList;
    }

    public boolean getMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getStatus() {
        String status = "id: " + this.id + "\nport: " + this.port + "\nmaster: " + this.master + "\nbattery level: " + this.battery + "%\nposition: (" + this.position.getX() + "," + this.position.getY() + ")\n";
        return status;
    }

    public String printDronesList() {
        String toPrint = "";
        Iterator<DroneSmartCity> it = this.dronesList.iterator();
        while(it.hasNext()) {
            DroneSmartCity dsc = it.next();
            toPrint += "(" + dsc.getId() + "," + dsc.getIp() + "," + dsc.getPort() + ")\n";
        }
        return toPrint;
    }

}
