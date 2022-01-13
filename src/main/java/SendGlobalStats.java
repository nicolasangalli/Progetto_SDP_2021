import Libraries.Drone;
import java.util.ArrayList;


public class SendGlobalStats extends Thread {

    private Drone d;

    public SendGlobalStats(Drone d) {
        this.d = d;
    }

    public void run() {
        while(d.isLastSend() == false) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(d.getAvgOrder().size() >= 1) {
                ArrayList<Integer> avgOrderList = (ArrayList<Integer>) d.getAvgOrder().clone();
                ArrayList<Integer> avgKmList = (ArrayList<Integer>) d.getAvgKm().clone();
                ArrayList<Double> avgPollutionList = (ArrayList<Double>) d.getAvgPollution().clone();
                ArrayList<Integer> avgBatteryList = (ArrayList<Integer>) d.getAvgBattery().clone();

                d.getAvgOrder().clear();
                d.getAvgKm().clear();
                d.getAvgPollution().clear();
                d.getAvgBattery().clear();

                double avgOrder = 0;
                for (int i : avgOrderList) {
                    avgOrder += i;
                }
                avgOrder /= avgOrderList.size();
                double avgKm = 0;
                for (int i : avgKmList) {
                    avgKm += i;
                }
                avgKm /= avgKmList.size();

                double avgPollution = 0;
                for (double i : avgPollutionList) {
                    avgPollution += i;
                }
                avgPollution /= avgPollutionList.size();

                double avgBattery = 0;
                for (int i : avgBatteryList) {
                    avgBattery += i;
                }
                avgBattery /= avgBatteryList.size();

                System.out.println("Send global stats:\n" + avgOrder + " - " + avgKm + " - " + avgPollution + " - " + avgBattery + "\n");
            }
        }
    }

}
