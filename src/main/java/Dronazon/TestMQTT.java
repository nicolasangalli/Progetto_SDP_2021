package Dronazon;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import java.sql.Timestamp;
import java.util.Scanner;


public class TestMQTT {

    private static Gson gson;
    private static MqttClient test;
    private static String broker = "tcp://localhost:1883";
    private static String topic = "dronazon/smartcity/orders";
    private static int qos = 2;

    public static void main(String[] args) {
        gson = new Gson();
        String clientId = MqttClient.generateClientId();

        try {
            test = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            //Connect the heater to broker
            System.out.println("Test " + clientId + " try to connect to broker " + broker + " ...");
            test.connect(connOpts);
            System.out.println("Test " + clientId + " connected");

            //Callback
            test.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    String receivedMessage = new String(message.getPayload());
                    Order order = gson.fromJson(receivedMessage, Order.class);
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    System.out.println("Test " + clientId +" received a message! (Thread PID: " + Thread.currentThread().getId() + ")" +
                                "\n\tTime:    " + time +
                                "\n\tOrder id: " + order.getId() + "\n");
                }

                public void connectionLost(Throwable cause) {
                    System.out.println("Test " + clientId + " lost connection (" + cause.getMessage()+ ")");
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            //Subscribe to temp topic
            System.out.println("Test " + clientId + " try to subscribe...");
            test.subscribe(topic,qos);
            System.out.println(clientId + " subscribed to topics : " + topic);

            //Disconnect
            System.out.println("\n ***  Press a random key to exit *** \n");
            Scanner command = new Scanner(System.in);
            command.nextLine();
            test.disconnect();
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

}
