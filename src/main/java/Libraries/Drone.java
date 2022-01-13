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
    private ArrayList<Order> ordersList; //master-only
    private boolean delivering;
    private int nOrders;
    private int coveredDistance;
    private double pollutionLevel;

    //Global stats (master-only)
    private boolean lastSend;
    private ArrayList<Integer> avgOrder;
    private ArrayList<Integer> avgKm;
    private ArrayList<Double> avgPollution;
    private ArrayList<Integer> avgBattery;

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

        lastSend = false;
        avgOrder = new ArrayList<>();
        avgKm = new ArrayList<>();
        avgPollution = new ArrayList<>();
        avgBattery = new ArrayList<>();
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

    public double getPollutionLevel() {
        return pollutionLevel;
    }

    public void setPollutionLevel(double pollutionLevel) {
        this.pollutionLevel = pollutionLevel;
    }

    public ArrayList<Integer> getAvgOrder() {
        return avgOrder;
    }

    public void setAvgOrder(ArrayList<Integer> avgOrder) {
        this.avgOrder = avgOrder;
    }

    public ArrayList<Integer> getAvgKm() {
        return avgKm;
    }

    public void setAvgKm(ArrayList<Integer> avgKm) {
        this.avgKm = avgKm;
    }

    public ArrayList<Double> getAvgPollution() {
        return avgPollution;
    }

    public void setAvgPollution(ArrayList<Double> avgPollution) {
        this.avgPollution = avgPollution;
    }

    public ArrayList<Integer> getAvgBattery() {
        return avgBattery;
    }

    public void setAvgBattery(ArrayList<Integer> avgBattery) {
        this.avgBattery = avgBattery;
    }

    public String getStatus() {
        String status = "id: " + this.id + "\nport: " + this.port + "\nmaster: " + this.master + "\nmaster id: " + this.masterId + "\nbattery level: " + this.battery + "%\nposition: (" + this.position.getX() + "," + this.position.getY() + ")\nnOrders: " + this.nOrders + "\ncovered distance: " + this.coveredDistance + "\npollution level: " + this.pollutionLevel + "\n";
        return status;
    }

    public boolean isLastSend() {
        return lastSend;
    }

    public void setLastSend(boolean lastSend) {
        this.lastSend = lastSend;
    }
}
