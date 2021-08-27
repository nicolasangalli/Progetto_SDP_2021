package Dronazon;

import Libraries.Coordinate;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.Random;


public class MainDronazon {

    private static int orderIdCount = 0;
    private static Gson gson;
    private static MqttClient dronazon;
    private static String broker = "tcp://localhost:1883";
    private static String pubTopic = "dronazon/smartcity/orders";
    private static int qos = 2;

    public static void main(String[] args) {
        gson = new Gson();
        String clientId = MqttClient.generateClientId();

        try {
            dronazon = new MqttClient(broker, clientId);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            //Connect the dronazon to broker
            System.out.println("Try to connect Dronazon to broker " + broker + " ...");
            dronazon.connect(connectOptions);
            System.out.println("Dronazon connected to broker MQTT");
        } catch (MqttException me) {
            me.printStackTrace();
        }

        while(true) {
            try {
                Thread.sleep(5000);
                generateOrder();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public static void generateOrder() {
        int id = orderIdCount + 1;
        Random random = new Random();
        Coordinate startPoint = new Coordinate(random.nextInt(10), random.nextInt(10));
        Coordinate finishPoint = new Coordinate(random.nextInt(10), random.nextInt(10));
        while(startPoint.getX() == finishPoint.getX() && startPoint.getY() == finishPoint.getY()) { //start point == finish point
            finishPoint.setX(random.nextInt(10));
            finishPoint.setY(random.nextInt(10));
        }
        Order o = new Order(id, startPoint, finishPoint);
        orderIdCount++;

        System.out.println("\nGenerated new order:");
        System.out.println("id: " + o.getId());
        System.out.println("(" + o.getStartPoint().getX() + "," + o.getStartPoint().getY() + ") -> (" + o.getFinishPoint().getX() + "," + o.getFinishPoint().getY() + ")");

        MqttMessage message = new MqttMessage(gson.toJson(o).getBytes());
        message.setQos(qos);
        System.out.println("Publishing order " + o.getId() + "...");
        try {
            dronazon.publish(pubTopic, message);
            System.out.println("Order published");
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

}
