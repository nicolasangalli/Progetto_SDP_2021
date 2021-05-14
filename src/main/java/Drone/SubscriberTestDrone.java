package Drone;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import java.sql.Timestamp;
import Dronazon.Order;


public class SubscriberTestDrone {

    private static MqttClient droneClient;
    private static Gson gson;
    private static String subTopic = "dronazon/smartcity/orders";
    private static int qos = 2;

    public static void main(String[] args) {
        gson = new Gson();
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();

        try {
            droneClient = new MqttClient(broker, clientId);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            //Connect the master drone to broker
            System.out.println("Master drone " + clientId + " try to connect to broker " + broker + " ...");
            droneClient.connect(connectOptions);
            System.out.println("Master drone connected");

            //Callback
            droneClient.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    String receivedMessage = new String(message.getPayload());
                    Order order = gson.fromJson(receivedMessage, Order.class);
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    System.out.println("Received a message!" +
                            "\n\tTime:    " + time +
                            "\n\t" + order.getId() + "\n");
                }

                public void connectionLost(Throwable cause) {
                    System.out.println("Master drone " + clientId + " lost connection (" + cause.getMessage()+ ")");
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            //Subscribe to orders topic
            System.out.println("Master drone " + clientId + " try to subscribe...");
            droneClient.subscribe(subTopic, qos);
            System.out.println("Master drone subscribed to topics : " + subTopic+ "\n");
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

}
