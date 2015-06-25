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


    private static String IP = "tcp://mqtt.phelicks.net:1883";
    //private static String IP = "tcp://81.236.122.249:1883";

    //Från bilen, hur?
    private static String clientID = "carID";

    int qos = 2;

    private MqttClient client;
    private boolean connected = false;

    private Monitor monitor;

    public MQTT(Monitor monitor) {
        this.monitor=monitor;
    }

    public void start(){
        super.start();
    }

    @Override
    public void run(){
        readConfig();
        if(!createConnection()){ //borde vara en while loop
            //kör någon waitForConnectionMetod (som blockerar) den bör aktivera om vi får internet eller användaren klickar på connect
            System.out.println("Connection failed");
            return;
        }
        while(true){
            int type = monitor.dataUpdated();
            switch (type){
                case SPEED:
                    float speed = monitor.getSpeed();
                    publishSpeed(speed);
                    break;
                case FUEL:
                    float fuel = monitor.getFuel();
                    publishBatteryLevel(fuel);
                    break;
                case DISTANCE_TRAVELED:
                    long distanceTraveled = monitor.getDistanceTraveled();
                    publishDistanceTraveled(distanceTraveled);
                    break;
                default:
                    System.err.println("Unkown datatype");
                    System.exit(1);

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
    public void publishSpeed(float speed) {
        String topic = "telemetry/speed";
        String message = "" + speed;
        try {
            client.publish(topic,message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void publishBatteryLevel(float percent) {
        String topic = "telemetry/fuel";
        String message = "" + percent;
        try {
            client.publish(topic,message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publishDistanceTraveled(long distance) {
        String topic = "telemetry/distanceTraveled";
        String message = "" + distance;
        try {
            client.publish(topic,message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }
}
