import Libraries.Drone;
import Libraries.Order;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;


public class MQTTSubscription extends Thread {

    private Drone d;

    private static Gson gson;
    public MqttClient mqttClient;
    private static String broker = "tcp://localhost:1883";
    private static String topic = "dronazon/smartcity/orders";
    private static int qos = 2;

    public MQTTSubscription(Drone d) {
        this.d = d;
    }

    public void run() {
        gson = new Gson();
        String clientId = MqttClient.generateClientId();

        try {
            mqttClient = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            mqttClient.connect(connOpts);

            //Callback
            mqttClient.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    String receivedMessage = new String(message.getPayload());
                    Order order = gson.fromJson(receivedMessage, Order.class);
                    d.addOrder(order);

                    MainDrone.assignOrder(d, true);
                }

                public void connectionLost(Throwable cause) {
                    System.out.println("Test " + clientId + " lost connection (" + cause.getMessage()+ ")");
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            //Subscribe to topic
            mqttClient.subscribe(topic,qos);
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

}
