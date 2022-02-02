import Libraries.Coordinate;
import Libraries.Drone;


public class Recharge extends Thread {

    private Drone d;

    public Recharge(Drone d) {
        this.d = d;
    }

    public void run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        d.setPosition(new Coordinate(0, 0));
        d.getNetworkTopology().getDroneWithId(d.getId()).setPosition(d.getPosition());
        d.setBattery(100);
    }

}
