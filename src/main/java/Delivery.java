import Libraries.Drone;
import Libraries.Order;


public class Delivery extends Thread {

    private Drone d;
    private Order order;

    public Delivery() {}

    public Delivery(Drone d, Order order) {
        this.d = d;
        this.order = order;
    }

    public void run() {
        d.setDelivering(true);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        d.setPosition(order.getFinishPoint());
        d.setBattery(d.getBattery()-10);
        d.setDelivering(false);
    }

    public Drone getD() {
        return d;
    }

    public void setD(Drone d) {
        this.d = d;
    }

}
