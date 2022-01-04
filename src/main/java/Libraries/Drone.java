package Libraries;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;


@XmlRootElement
public class Drone {

    private int id;
    private String ip;
    private int port;
    private String serverAmmAddress;

    private Coordinate position;
    private NetworkTopology networkTopology;
    private boolean master;
    private int masterId;
    private int battery;

    //Chang and Roberts
    private boolean participant;

    //Delivery
    private ArrayList<Order> ordersList;
    private boolean delivering;
    private int nOrders;
    private int coveredDistance;

    public Drone() {}

    public Drone(int id, String ip, int port, String serverAmmAddress) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.serverAmmAddress = serverAmmAddress;

        position = new Coordinate(-1, -1);
        networkTopology = new NetworkTopology();
        master = false;
        masterId = -1;
        battery = 100;

        participant = false;

        ordersList = new ArrayList<>();
        delivering = false;
        nOrders = 0;
        coveredDistance = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public NetworkTopology getNetworkTopology() {
        return networkTopology;
    }

    public void setNetworkTopology(NetworkTopology networkTopology) {
        this.networkTopology = networkTopology;
    }

    public int getMasterId() {
        return masterId;
    }

    public void setMasterId(int masterId) {
        this.masterId = masterId;
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

    public boolean getParticipant() {
        return participant;
    }

    public void setParticipant(boolean participant) {
        this.participant = participant;
    }

    public ArrayList<Order> getOrdersList() {
        return ordersList;
    }

    public void addOrder(Order o) {
        ordersList.add(o);
    }

    public boolean getDelivering() {
        return delivering;
    }

    public void setDelivering(boolean delivering) {
        this.delivering = delivering;
    }

    public int getnOrders() {
        return nOrders;
    }

    public void setnOrders(int nOrders) {
        this.nOrders = nOrders;
    }

    public int getCoveredDistance() {
        return coveredDistance;
    }

    public void setCoveredDistance(int coveredDistance) {
        this.coveredDistance = coveredDistance;
    }

    public String getStatus() {
        String status = "id: " + this.id + "\nport: " + this.port + "\nmaster: " + this.master + "\nmaster id: " + this.masterId + "\nbattery level: " + this.battery + "%\nposition: (" + this.position.getX() + "," + this.position.getY() + ")\nnOrders: " + this.nOrders + "\ncovered distance: " + this.coveredDistance + "\n";
        return status;
    }

}
