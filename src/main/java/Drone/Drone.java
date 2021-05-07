package Drone;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Drone {

    private int id;
    private boolean master;
    private int battery;

    public Drone() {}

    public Drone(int id) {
        this.id = id;
        master = false;
        battery = 100;

        System.out.println("Created new drone with id: " + id);
        System.out.println("    - battery level: " + battery);
        System.out.println("    - master: " + master + "\n");
    }

    public boolean getMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

}
