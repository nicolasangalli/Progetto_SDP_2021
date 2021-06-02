package Drone.Sensor;

import java.util.ArrayList;
import java.util.List;
import static Drone.Sensor.MainSensor.avg;


public class MeasurementBuffer implements Buffer {

    private List<Measurement> list;

    public MeasurementBuffer() {
        list = new ArrayList<Measurement>();
    }

    @Override
    public void addMeasurement(Measurement m) {
        list.add(m);
        System.out.println(m.getValue() + " - " + m.getTimestamp());
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
        avg = sum/8;
        System.out.println(avg);
        for(int i=0; i<4; i++) {
            list.remove(i);
        }
        return list;
    }

    public List<Measurement> getList() {
        return list;
    }

}
