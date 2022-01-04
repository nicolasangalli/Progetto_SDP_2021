import Libraries.Drone;
import Libraries.Order;
import Libraries.TopologyDrone;
import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.eclipse.paho.client.mqttv3.*;
import java.util.ArrayList;

public class MQTTSubscription extends Thread {

    private Drone d;

    private static Gson gson;
    private static MqttClient test;
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
            test = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            test.connect(connOpts);

            //Callback
            test.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    String receivedMessage = new String(message.getPayload());
                    Order order = gson.fromJson(receivedMessage, Order.class);
                    d.addOrder(order);

                    assignOrder(d);
                }

                public void connectionLost(Throwable cause) {
                    System.out.println("Test " + clientId + " lost connection (" + cause.getMessage()+ ")");
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            //Subscribe to topic
            test.subscribe(topic,qos);
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    private static void assignOrder(Drone d) {
        if(d.getOrdersList().size() > 0) {
            Order order = d.getOrdersList().get(0);
            d.getOrdersList().remove(0);
            int idCandidate = bestAvailableDrone(d, order);
            if(idCandidate == -1) { //no drones available, reinsert the order in the list
                d.getOrdersList().add(order);
            } else {
                TopologyDrone drone = d.getNetworkTopology().getDroneWithId(idCandidate);

                final ManagedChannel channel = ManagedChannelBuilder.forTarget(drone.getIp() + ":" + drone.getPort())
                        .usePlaintext(true)
                        .build();

                NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
                NetworkService.Order request = NetworkService.Order.newBuilder()
                        .setOrderId(order.getId())
                        .setX1(order.getStartPoint().getX())
                        .setY1(order.getStartPoint().getY())
                        .setX2(order.getFinishPoint().getX())
                        .setY2(order.getFinishPoint().getY())
                        .build();
                stub.deliverOrder(request);
                channel.shutdownNow();
            }
        }

    }

    private static int bestAvailableDrone(Drone d, Order order) {
        ArrayList<ArrayList<Integer>> candidates = new ArrayList<>();
        ArrayList<TopologyDrone> dronesList = d.getNetworkTopology().getDronesList();
        for(TopologyDrone td : dronesList) {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(td.getIp() + ":" + td.getPort())
                    .usePlaintext(true)
                    .build();

            NetworkProtoGrpc.NetworkProtoBlockingStub stub = NetworkProtoGrpc.newBlockingStub(channel);
            NetworkService.HelloRequest request = NetworkService.HelloRequest.newBuilder()
                    .setId(d.getId())
                    .build();
            NetworkService.DroneDeliveryInfo response = stub.freeDrone(request);

            if(response.getDelivery() == false) {
                ArrayList<Integer> candidate = new ArrayList<>();
                candidate.add(response.getId());
                int distance = (int) Math.sqrt(Math.pow(order.getStartPoint().getX() - response.getX(), 2) + Math.pow(order.getStartPoint().getY() - response.getY(),2));
                candidate.add(distance);
                candidate.add(response.getBattery());

                candidates.add(candidate);
            }

            channel.shutdownNow();
        }

        int minDistance = Integer.MAX_VALUE;
        int maxBattery = -1;
        int idCandidate = -1;
        ArrayList<Integer> candidate = new ArrayList<>();
        for(ArrayList<Integer> ali : candidates) {
            if(ali.get(1) < minDistance) {
                minDistance = ali.get(1);
            }
        }
        for(ArrayList<Integer> ali : candidates) {
            if(ali.get(2) > maxBattery && ali.get(1) == minDistance) {
                maxBattery = ali.get(2);
            }
        }
        for(ArrayList<Integer> ali : candidates) {
            if(ali.get(0) > idCandidate && ali.get(1) == minDistance && ali.get(2) == maxBattery) {
                idCandidate = ali.get(0);
            }
        }

        return idCandidate;
    }

}
