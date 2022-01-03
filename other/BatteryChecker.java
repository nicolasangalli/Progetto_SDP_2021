package Other;

import Libraries.Drone;

public class BatteryChecker extends Thread {

    private Drone d;

    public BatteryChecker(Drone d) {
        this.d = d;
    }

    @Override
    public synchronized void run() {
        while(d.getBattery() >= 15) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(d.getBattery() < 15) {
                d.setBattery(100);
            }
        }
    }

}
