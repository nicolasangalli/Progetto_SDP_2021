package Libraries;


public class TopologyDrone {

    private int id;
    private String ip;
    private int port;
    private String serverAmmAddress;

    public TopologyDrone() {}

    public TopologyDrone(int id, String ip, int port, String serverAmmAddress) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.serverAmmAddress = serverAmmAddress;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getServerAmmAddress() {
        return serverAmmAddress;
    }

}
