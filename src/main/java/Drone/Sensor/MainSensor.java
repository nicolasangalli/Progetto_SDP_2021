package Drone.Sensor;


public class MainSensor {

    public static void main(String[] args) {
        MeasurementBuffer myBuffer = new MeasurementBuffer();
        PM10Simulator pm10Simulator = new PM10Simulator(myBuffer);
        pm10Simulator.run();
    }

}
