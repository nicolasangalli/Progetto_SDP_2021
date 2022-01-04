package Sensor;

import Libraries.Drone;
import java.util.ArrayList;
import java.util.List;


public class MeasurementBuffer implements Buffer {

    private Drone d;
    private List<Measurement> list;

    public MeasurementBuffer(Drone d) {
        this.d = d;
        list = new ArrayList<Measurement>();
    }

    @Override
    public void addMeasurement(Measurement m) {
        list.add(m);
        if(list.size() == 8) {
            list = readAllAndClean();
        }
    }

    @Override
    public List<Measurement> readAllAndClean() {
        double sum = 0;
        for(Measurement m: list) {
            sum += m.getValue();
        }
        d.setPollutionLevel(Math.round(sum/8*100.0)/100.0);
        for(int i=0; i<4; i++) {
            list.remove(i);
        }
        return list;
    }

    public List<Measurement> getList() {
        return list;
    }

}
