package combitech.com.againstrumentcluster;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by Fredrik on 2015-06-23.
 */
public class MQTT implements CloudPutter {

    private Context context;
    private static String IP ="81.236.122.249";

    //Från bilen, hur?
    private static String clientID ="1";

    int qos = 2;

    private MqttAndroidClient client;
    public MQTT(Context context){
        this.context=context;
    }

    @Override
    public void readConfig() {

    }

    @Override
    public void createConnection() {
        client = new MqttAndroidClient(context,IP,clientID);
        try {
            client.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSpeed(float speed) {
        String topic = "Telemitry/speed";
        String message = ""+speed;
        try {
            client.publish(topic,message.getBytes(),qos,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateBattery(float percent) {

    }

    @Override
    public void updateRange(long range) {

    }
}
