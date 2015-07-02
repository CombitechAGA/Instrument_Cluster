package combitech.com.againstrumentcluster.iot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import combitech.com.againstrumentcluster.R;

/**
 * Created by Fredrik on 2015-06-23.
 */
public class MQTT extends Thread implements CloudPutter {

    private Context context;
    private final static int SPEED = 1;
    private final static int FUEL = 2;
    private final static int DISTANCE_TRAVELED = 3;


    private String IP; // = "tcp://mqtt.phelicks.net:1883";
    //private static String IP = "tcp://81.236.122.249:1883";

    //Fr�n bilen, hur?
    private String clientID;// = "carID";
    private String user;
    private String pass;
    private int interval;

    int qos = 0;// = 2;

    private MqttClient client;
    private boolean connected = false;

    private Monitor monitor;
    private Database database;
    private AlarmManager mAlarmMgr;
    private PendingIntent mAlarmIntent;

    public MQTT(Monitor monitor, Context context) {
        this.monitor = monitor;
        this.context = context;
        database = new ArrayListDatabase();
        readConfig();
        startIntervalTimer();

    }

    private void startIntervalTimer() {
        mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, IOTAlarmBroadcastReceiver.class);
        mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        mAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000,
                interval * 1000, mAlarmIntent);
    }
    public void stopInterevalTimer(){
        if (mAlarmMgr != null) {
            mAlarmMgr.cancel(mAlarmIntent);
        }
    }



    public void start() {
        super.start();
    }

    @Override
    public void run() {
         while (true) {
            System.out.println("mqttn k�r--------------------------------------");
            monitor.waitForCloudConnectionAllowance();
            boolean connected = createConnection();
            System.out.println("Connection: " + connected);
            monitor.notifyCloudConnectionResult(connected);
            while (connected) {


                int type = monitor.dataUpdated();
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
                    case -1:
                        //-1 means that the user wants to disconnect.
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
        //f�r att l�sa in ip, qos, topics osv.
        InputStream is = context.getResources().openRawResource(R.raw.config);
        BufferedReader br;
        ArrayList<String> list = new ArrayList<String>();
        try {
            //väldigt fult!! hitta inte filen i projektet.
            br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            int n = 0;
            while (line != null) {
                String[] parts = line.split("#");
                list.add(parts[1]);
                line = br.readLine();
            }
            IP = list.get(0);
            clientID = list.get(1);
            qos = Integer.parseInt(list.get(2));
            user = list.get(3);
            pass = list.get(4);
            interval = Integer.parseInt(list.get(5));

        }catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public boolean createConnection() {
        try {

            client = new MqttClient(IP, clientID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(user);
            options.setPassword(pass.toCharArray());
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

    public Database getDatabase() {
        return database;
    }

    public void sendIntervalData() throws MqttException {
        if (connected) {
            while (!database.isEmpty()) {
                CarSnapShot snapShot = database.getLatestSnapshot();
                client.publish("telemetry/snapshot", snapShot.toString().getBytes(), 0, false);
            }
        }
    }
}
