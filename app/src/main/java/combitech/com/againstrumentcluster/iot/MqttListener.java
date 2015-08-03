package combitech.com.againstrumentcluster.iot;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import combitech.com.againstrumentcluster.ActivityLayoutManager_v2;
import combitech.com.againstrumentcluster.InstrumentClusterActivity;
import combitech.com.againstrumentcluster.iot.appcore.MyApplication;

/**
 * Created by Fredrik on 2015-07-03.
 */
public class MqttListener implements MqttCallback {
    private Monitor monitor;
    private String LOG_TAG = MqttListener.class.getSimpleName();
    private boolean connectionLost=false;
    private Activity registeredActivity = null;

    public MqttListener(Monitor monitor){
        this.monitor = monitor;
    }


    public void registerActivity(Activity registeredActivity){
        this.registeredActivity=registeredActivity;
    }
    public void unregisterActivity(){
        registeredActivity=null;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        connectionLost=true;
        Log.d(LOG_TAG,"Tappade min connection fyfan");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
    //    System.out.println("is duplicate:"+mqttMessage.isDuplicate());
        System.out.println("QOS: " + mqttMessage.getQos());
        if (connectionLost){
            Log.d(LOG_TAG, "Getting messages after losing the connection");
        }

        Log.d(LOG_TAG,"topic:"+topic+" message: "+mqttMessage.toString());

        if (topic.contains("message")) {
            monitor.addMessage(mqttMessage.toString());
        }
        else if(topic.contains("config")){

            File file = new File(MyApplication.getAppContext().getFilesDir(), "config.txt");
            BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
            bWriter.write(mqttMessage.toString());
            bWriter.flush();
            bWriter.close();

            Log.d(LOG_TAG, mqttMessage.toString());
        }
        else {

            if(registeredActivity!=null) {
                ActivityLayoutManager_v2 layout = ((InstrumentClusterActivity) registeredActivity).getLayoutManager();
                float distancePrediction = Float.parseFloat(mqttMessage.toString()) / 100.0f;
                layout.lastRange = distancePrediction;
                layout.updateRangeOnline(distancePrediction);
            }
        }
        //if(registerActivity;)
       // registeredActivity
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
