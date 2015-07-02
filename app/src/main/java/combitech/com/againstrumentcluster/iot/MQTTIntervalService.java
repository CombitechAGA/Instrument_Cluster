package combitech.com.againstrumentcluster.iot;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttException;

import combitech.com.againstrumentcluster.iot.appcore.MyApplication;

/**
 * Created by Fredrik on 2015-07-02.
 */
public class MQTTIntervalService extends IntentService {
    public static String ACTION_SYNC = "combitech.com.againstrumentcluster.iot.ACTION_SYNC";
    private static final String LOG_TAG = MQTTIntervalService.class.getSimpleName();

    public MQTTIntervalService() {
        super("MQTTIntervalService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "on start command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "intent started");
        if (intent.getAction().equals(ACTION_SYNC)) {
            Log.d(LOG_TAG, "Action sync received");
            MQTT mqtt = ((MyApplication) getApplicationContext()).getMQTT();
            try {
                mqtt.sendIntervalData();
            } catch (MqttException e) {

            }
        }


    }
}
