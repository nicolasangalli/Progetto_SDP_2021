package Libraries;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class GlobalStat {

    private String timestamp;
    private double avgOrder;
    private double avgKm;
    private double avgPollution;
    private double avgBattery;

    public GlobalStat() {}

    public GlobalStat(String timestamp, double avgOrder, double avgKm, double avgPollution, double avgBattery) {
        this.timestamp = timestamp;
        this.avgOrder = avgOrder;
        this.avgKm = avgKm;
        this.avgPollution = avgPollution;
        this.avgBattery = avgBattery;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getAvgOrder() {
        return avgOrder;
    }

    public void setAvgOrder(double avgOrder) {
        this.avgOrder = avgOrder;
    }

    public double getAvgKm() {
        return avgKm;
    }

    public void setAvgKm(double avgKm) {
        this.avgKm = avgKm;
    }

    public double getAvgPollution() {
        return avgPollution;
    }

    public void setAvgPollution(double avgPollution) {
        this.avgPollution = avgPollution;
    }

    public double getAvgBattery() {
        return avgBattery;
    }

    public void setAvgBattery(double avgBattery) {
        this.avgBattery = avgBattery;
    }

}
