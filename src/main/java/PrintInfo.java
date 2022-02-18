import Libraries.Drone;

public class PrintInfo extends Thread {

    private Drone d;
    private boolean running;

    public PrintInfo(Drone d) {
        this.d = d;
        running = true;
    }

    public void run() {
        while(running) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\nNumber of orders delivered: " + d.getnOrders());
            System.out.println("Covered distance: " + d.getCoveredDistance() + " km");
            System.out.println("Battery level: " + d.getBattery() + "%");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
