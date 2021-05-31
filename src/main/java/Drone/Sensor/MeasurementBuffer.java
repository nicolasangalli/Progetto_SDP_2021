package Drone.Sensor;

import java.util.ArrayList;
import java.util.List;


public class MeasurementBuffer implements Buffer {

    List<Measurement> list;

    public MeasurementBuffer() {
        list = new ArrayList<Measurement>();
    }

    @Override
    public void addMeasurement(Measurement m) {
        list.add(m);
        System.out.println(m.getValue() + " - " + m.getTimestamp());
    }

    @Override
    public List<Measurement> readAllAndClean() {
        return null;
    }
}
