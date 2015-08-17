package combitech.com.againstrumentcluster.iot;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import combitech.com.againstrumentcluster.InstrumentClusterActivity;
import combitech.com.againstrumentcluster.R;
import combitech.com.againstrumentcluster.iot.LocationInfo;
import combitech.com.againstrumentcluster.iot.appcore.MyApplication;

/**
 * Created by Fredrik on 2015-06-23.
 */
public class MQTT extends Thread implements CloudPutter {
    private static final String LOG_TAG = MQTT.class.getSimpleName();

    private Context context;
    private final static int SPEED = 1;
    private final static int FUEL = 2;
    private final static int DISTANCE_TRAVELED = 3;
    private final static int READ_MESSAGE = 4;

    private String IP; // = "tcp://mqtt.phelicks.net:1883";
    //private static String IP = "tcp://81.236.122.249:1883";
    private String port;
    //Fr�n bilen, hur?
    private String clientID;// = "carID";
    private String user;
    private String pass;
    private int interval;

    private ArrayList<String> subscribeTopics;

    int qos = 0;// = 2;

    private MqttClient client;
    private boolean connected = false;

    private Monitor monitor;
    private Database database;
    private AlarmManager mAlarmMgr;
    private PendingIntent mAlarmIntent;
    private Activity currentActivity = null;
    MqttListener listener = null;
    private double lat;
    private double lng;

    public MQTT(Monitor monitor, Context context) {
        subscribeTopics = new ArrayList<String>();
        this.monitor = monitor;
        this.context = context;
        database = new ArrayListDatabase();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        clientID = wInfo.getMacAddress();
        //ta från configen
        subscribeTopics.add(clientID+"/"+"distancePrediction");
        subscribeTopics.add(clientID+"/"+"message");
        subscribeTopics.add(clientID+"/"+"config");

        //listener = new MqttListener();
        readConfig();
        startIntervalTimer();

    }
    public String getClientID(){
        return clientID;
    }

    private void startIntervalTimer() {
        Log.d(LOG_TAG,"Starting intervalTimer");
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
            connected = createConnection();
            System.out.println("Connection: " + connected);
            monitor.notifyCloudConnectionResult(connected);
            if (connected){
                listener = new MqttListener(monitor);
                for (String topic : subscribeTopics) {
                    Log.d(LOG_TAG,"topic: "+topic);
                    try {
                        client.subscribe(topic);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                listener.registerActivity(currentActivity);
                client.setCallback(listener);
                requestNewConfig();

            }

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
                    case READ_MESSAGE:
                        Log.d(LOG_TAG,"ska köra publishAckkOfMessage");
                        publishResult = publishAckOfMessage(monitor.getReadMessage());
                        break;
                    case -1:
                        publishResult=false;
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

    private void requestNewConfig() {
        System.out.println("NU SKA JAG REQUESTA EN CONFIG");
        try {
            client.publish("request/config", clientID.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void readConfig() {
        //f�r att l�sa in ip, qos, topics osv.

        File file = new File(MyApplication.getAppContext().getFilesDir(), "config.txt");
        InputStream is;
        if (file.exists()){
            System.out.println("Det finns en fil på disk yay");
            is = null;
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Det finns ingen  fil på disk :(");
            is = context.getResources().openRawResource(R.raw.config);

        }
        BufferedReader br;
        ArrayList<String> list = new ArrayList<String>();
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            int n = 0;
            while (line != null) {
                Log.d(LOG_TAG, line);
                String[] parts = line.split("#");
                list.add(parts[1]);
                line = br.readLine();
            }
            IP = "tcp://" + list.get(0);
            port = list.get(1);
            interval = Integer.parseInt(list.get(2));
            user = list.get(3);
            pass = list.get(4);
            qos = Integer.parseInt(list.get(5));
            lat = Double.parseDouble(list.get(6).split(",")[0]);
            lng = Double.parseDouble(list.get(6).split(",")[1]);
            monitor.setHomelat(lat);
            monitor.setLatitude(lat);
            monitor.setLongitude(lng);
            monitor.setHomelng(lng);
            monitor.setZoomLevel(Integer.parseInt(list.get(7)));
            System.out.println(list.get(8));
            monitor.setGeofenceDistance(Integer.parseInt(list.get(9)));
            System.out.println("GeofenceDistance: " + list.get(9));

            System.out.println("geo dist efter: " +monitor.getGeofenceDistance());
            monitor.setSimulator(Boolean.parseBoolean(list.get(10)));

            monitor.setZbeeName(list.get(11));

            //läs in filen och lägg in i hashmap
            if(Boolean.parseBoolean(list.get(10))){
                monitor.setGeofenceDistance(0);
                //x,y som en sträng för finns ingen par typ i java?
                HashMap<Integer,LocationInfo> distanceToGPSMap = new HashMap<>();
                is = context.getResources().openRawResource(R.raw.corkscrew);
                br = new BufferedReader(new InputStreamReader(is));
                line = br.readLine();
                while(line !=null){
                    System.out.println("line:'"+line+"'");
                    String []lineParts = line.split("    ");
                    System.out.println(lineParts[0]);
                    System.out.println(lineParts[1]);
                    String[] latLong = lineParts[1].trim().split(", ");
                    LocationInfo temp = new LocationInfo(Double.parseDouble(latLong[0]),Double.parseDouble(latLong[1]));
                    distanceToGPSMap.put(Integer.parseInt(lineParts[0])/100,temp);
                    line = br.readLine();
                }
                monitor.setFakeGps(distanceToGPSMap);
            //lägg in hashmapen i monitorn
            }



            System.out.println("lat: " + lat + " lng: " + lng );
            Log.d(LOG_TAG, "Interval: " + interval);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean createConnection() {
        try {

            client = new MqttClient(IP+":"+port, clientID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(user);
            options.setPassword(pass.toCharArray());
            client.connect(options);
//            client.connect();

            return true;


        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public boolean publishSpeed(float speed) {
        String topic = "telemetry/speed";
        String message = clientID+";" + speed;
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
        String message = clientID+";" + percent;
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean publishAckOfMessage(String message){
        String topic = clientID+"/messageAck";
        System.out.println(topic);
        try {
            System.out.println(message);
            client.publish(topic, message.getBytes(), 0, false);
            monitor.removeReadMessage();
            System.out.println("message sent");
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean publishDistanceTraveled(long distance) {
        String topic = "telemetry/distanceTraveled";
        String message = clientID+";" + distance;
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
                System.out.println(snapShot.toString());
                client.publish("telemetry/snapshot", snapShot.toString().getBytes(), 0, false);
            }
        }
    }

    public void deRegisterActivity(){
        currentActivity = null;
        if(listener!=null){
            listener.unregisterActivity();
        }
    }

    public void registerActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
        if(listener!=null){
            listener.registerActivity(currentActivity);
        }
    }
}
