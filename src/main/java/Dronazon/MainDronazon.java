package Dronazon;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.ArrayList;
import java.util.Random;


public class MainDronazon {

    private static int orderIdCount = 0;
    private static Gson gson;
    private static ArrayList<Order> orderList;
    private static MqttClient dronazon;
    private static String pubTopic = "dronazon/smartcity/orders";
    private static int qos = 2;

    public static void main(String[] args) {
        orderList = new ArrayList<>();
        gson = new Gson();
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();

        try {
            dronazon = new MqttClient(broker, clientId);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            //Connect the dronazon to broker
            System.out.println("Try to connect Dronazon (id: " + clientId + ") to broker " + broker + " ...");
            dronazon.connect(connectOptions);
            System.out.println("Dronazon connected to broker MQTT");
        } catch (MqttException me) {
            me.printStackTrace();
        }

        while(true) {
            generateOrder();
            try {
                Thread.sleep(5000);
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
        Order o = new Order(id, startPoint, finishPoint);
        orderList.add(o);
        orderIdCount++;

        System.out.println("\nGenerated new order: " + o.getId() + ": ");
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
