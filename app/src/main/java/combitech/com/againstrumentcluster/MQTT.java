package combitech.com.againstrumentcluster;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Fredrik on 2015-06-23.
 */
public class MQTT extends Thread implements CloudPutter {


    private final static int SPEED = 1;
    private final static int FUEL = 2;
    private final static int DISTANCE_TRAVELED = 3;


    // private static String IP = "tcp://mqtt.phelicks.net:1883";
    private static String IP = "tcp://81.236.122.249:1883";

    //Från bilen, hur?
    private static String clientID = "carID";

    int qos = 2;

    private MqttClient client;
    private boolean connected = false;

    private Monitor monitor;

    public MQTT(Monitor monitor) {
        this.monitor = monitor;
    }

    public void start() {
        super.start();
    }

    @Override
    public void run() {
        readConfig();


        while (true) {
            System.out.println("mqttn kör--------------------------------------");
            monitor.waitForCloudConnectionAllowance();
            boolean connected = createConnection();
            System.out.println("Connection: " + connected);
            monitor.notifyCloudConnectionResult(connected);
            while (connected) {

                int type = monitor.dataUpdated();
                if (monitor.manualDisconnect()) {
                    connected = false;
                    monitor.disconnectedFromCloud();
                    break;
                }


                boolean publishResult = false;
                switch (type) {
                    case SPEED:
                        float speed = monitor.getSpeed();
                        //float speed = 5.0f;
                        publishResult = publishSpeed(speed);
                        break;
                    case FUEL:
                        float fuel = monitor.getFuel();
                        publishResult = publishBatteryLevel(fuel);
                        break;
                    case DISTANCE_TRAVELED:
                        long distanceTraveled = monitor.getDistanceTraveled();
                        publishResult = publishDistanceTraveled(distanceTraveled);
                        break;
                    default:
                        System.err.println("unknown datatype");
                        System.exit(1);

                }
                if (!publishResult) {
                    connected = false;
                    monitor.disconnectedFromCloud();
                }

            }


        }

    }


    @Override
    public void readConfig() {

        //för att läsa in ip, qos, topics osv.

    }

    @Override
    public boolean createConnection() {
        try {
            client = new MqttClient(IP, clientID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("cab");
            options.setPassword("sjuttongubbar".toCharArray());
            client.connect(options);

            return true;


        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public boolean publishSpeed(float speed) {
        String topic = "telemetry/speed";
        String message = "" + speed;
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (Exception e) {
            //  e.printStackTrace();
            return false;
        }
        return true;

    }

    @Override
    public boolean publishBatteryLevel(float percent) {
        String topic = "telemetry/fuel";
        String message = "" + percent;
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean publishDistanceTraveled(long distance) {
        String topic = "telemetry/distanceTraveled";
        String message = "" + distance;
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;


    }
}
