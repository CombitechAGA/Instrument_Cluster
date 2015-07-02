package combitech.com.againstrumentcluster.iot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import combitech.com.againstrumentcluster.iot.appcore.MyApplication;

/**
 * Created by Fredrik on 2015-07-01.
 */
public class IOTAlarmBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = IOTAlarmBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Alarm received");
        MyApplication app = (MyApplication) context.getApplicationContext();
        MQTT mqtt = app.getMQTT();
        Monitor monitor = app.getMonitor();
        Database db = mqtt.getDatabase();
        CarSnapShot snapShot = new CarSnapShot(System.currentTimeMillis(),monitor.getFuel(),monitor.getSpeed(),monitor.getDistanceTraveled());
        new WriteToDatabase(db).execute(snapShot);
        Intent mqttIntervalServiceIntent = new Intent(MQTTIntervalService.ACTION_SYNC,null,context,MQTTIntervalService.class);
        context.startService(mqttIntervalServiceIntent);



        //skriv ner datan med en asynctask
        //intentservice som får ut montior och mqtt och skickar ut datan.

//        Monitor monitor =app.getMonitor();
//        monitor.testPrint();
    }
}
