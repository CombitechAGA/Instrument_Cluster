package combitech.com.againstrumentcluster;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Fredrik on 2015-06-23.
 */
public class MQTT implements CloudPutter {

    private static String IP = "tcp://mqtt.phelicks.net:1883";
    //private static String IP = "tcp://81.236.122.249:1883";
    //Från bilen, hur?
    private static String clientID = "carID";

    int qos = 2;

    private MqttClient client;
    private boolean connected = false;

    public MQTT() {
    }

    @Override
    public void readConfig() {

        //för att läsa in ip, qos, topics osv.

    }

    @Override
    public void createConnection() {
        try {
            client = new MqttClient(IP, clientID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("cab");
            options.setPassword("sjuttongubbar".toCharArray());
            client.connect(options);

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateSpeed(float speed) {
        String topic = "telemetry/speed";
        String message = "" + speed;
        try {
            client.publish(topic,message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateBatteryLevel(float percent) {
        String topic = "telemetry/fuel";
        String message = "" + percent;
        try {
            client.publish(topic,message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateDistanceTraveled(long distance) {
        String topic = "telemetry/distanceTraveled";
        String message = "" + distance;
        try {
            client.publish(topic,message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }
}
