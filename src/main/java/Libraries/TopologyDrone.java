package Libraries;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class TopologyDrone {

    private int id;
    private String ip;
    private int port;
    private Coordinate position;

    public TopologyDrone() {}

    public TopologyDrone(int id, String ip, int port, Coordinate position) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.position = position;
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

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

}
