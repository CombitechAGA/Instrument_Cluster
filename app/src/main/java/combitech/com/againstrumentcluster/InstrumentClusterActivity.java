package combitech.com.againstrumentcluster;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.swedspot.automotive.AutomotiveManager;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSFloat;
import android.swedspot.scs.data.SCSLong;
import android.util.Log;
import android.view.View;

import com.combitech.safe.SAFEClient;
import com.swedspot.automotiveapi.AutomotiveListener;

import combitech.com.againstrumentcluster.iot.ConnectionStatusThread;
import combitech.com.againstrumentcluster.iot.MQTT;
import combitech.com.againstrumentcluster.iot.Monitor;
import combitech.com.againstrumentcluster.iot.NetworkStateReceiver;
import combitech.com.againstrumentcluster.iot.appcore.MyApplication;
import combitech.com.againstrumentcluster.iot.IOTAlarmBroadcastReceiver;

public class InstrumentClusterActivity extends FragmentActivity {

    private static final String LOG_TAG = InstrumentClusterActivity.class.getSimpleName();
    private static AutomotiveManager manager;
    private final static VehicleDataModel vehicleDataModel = new VehicleDataModel();
    private final static SAFEDataModel safeDataModel = new SAFEDataModel();
    private final static SAFEClient safeClient = new SAFEClient("admin", "safe", 1, "Resurs");
    private IOTAlarmBroadcastReceiver mIOTAlarmBroadcastReceiver;
    private AlarmManager mAlarmMgr;
    private PendingIntent mAlarmIntent;
    private Monitor mMonitor;
    private MQTT mMqtt;
    private ActivityLayoutManager_v2 layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.aga_zbee_main_v2);
        SAFEClientRunnable updateRunnable = new SAFEClientRunnable(safeClient, safeDataModel, vehicleDataModel);
        new Thread(updateRunnable).start();

        mMonitor = ((MyApplication) getApplicationContext()).getMonitor();
        mMqtt = ((MyApplication) getApplicationContext()).getMQTT();
        mMqtt.registerActivity(this);
        //registerActivity

        // Setup Alarm:
//        mAlarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(this, IOTAlarmBroadcastReceiver.class);
//        mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
//        Log.d(LOG_TAG, "Activate alarm");
//        //mAlarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis() + 2000, mAlarmIntent);
//        mAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+1000,
//                1000, mAlarmIntent);

        //lyssnare till mqtt:n och mqttn kastar upp event när saker händer.
        //observer pattern


        // Ändra layout här
        //final ActivityLayoutManager layoutManager = new ActivityLayoutManager(this, vehicleDataModel, safeDataModel, safeClient);
        layoutManager = new ActivityLayoutManager_v2(this, vehicleDataModel, safeDataModel, safeClient, mMonitor);
        ConnectionStatusThread connectionStatusThread= new ConnectionStatusThread(mMonitor, layoutManager,this);
        connectionStatusThread.start();

        manager = (AutomotiveManager) getApplicationContext().getSystemService(Context.AUTOMOTIVE_SERVICE);

        manager.setListener(new AutomotiveListener() {
            @Override
            public void receive(AutomotiveSignal automotiveSignal) {
                //Log.e("REC", "Received :" + automotiveSignal.getSignalId() + " Value " + ((SCSFloat) automotiveSignal.getData()).getFloatValue());
                if (automotiveSignal.getSignalId() == AutomotiveSignalId.FMS_WHEEL_BASED_SPEED) {
                    final SCSFloat data = (SCSFloat) automotiveSignal.getData();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vehicleDataModel.setVehicleSpeed(data.getFloatValue());
                            layoutManager.updateSpeed();
                        }
                    });
                    //  cloudPutter.publishSpeed(data.getFloatValue());
                    mMonitor.updateSpeed(data.getFloatValue());

                } else if (automotiveSignal.getSignalId() == AutomotiveSignalId.FMS_FUEL_LEVEL_1) {
                    final SCSFloat data = (SCSFloat) automotiveSignal.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vehicleDataModel.setBatteryLevel(data.getFloatValue());
                            layoutManager.updateBattery();
                            layoutManager.updateRange();
                        }
                    });
                    //   cloudPutter.publishBatteryLevel(data.getFloatValue());
                    mMonitor.updateFuel(data.getFloatValue());
                } else if (automotiveSignal.getSignalId() == AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE) {
                    final SCSLong data = (SCSLong) automotiveSignal.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vehicleDataModel.setOdometer(data.getLongValue());
                            layoutManager.updateOdometer();
                        }
                    });
                    mMonitor.updatedistanceTraveled(data.getLongValue());
                    if(mMonitor.isSimulator()){
                        //hantera uppdatering av gps
                        //ha en hashmap med nyckel distans och värde gps? och hämta ut nyckeln som är närmst?
                        //Dvs loopa igenom alla nycklarna varje gång det kommer en uppdatering här och välj den nyckeln som är närmast (Och sen då välja den gps:en och uppdatera gpsen)
                        //borde finnas ett bättre sätt
                        //men gör det i metoden här
                        System.out.println("avstånd kört: "+data.getLongValue());
                        mMonitor.updateFakeGps(data.getLongValue());
                        layoutManager.setFakeGPSOnGui(mMonitor.getLatitude(),mMonitor.getLongitude());

                    }

                    //cloudPutter.publishDistanceTraveled(data.getLongValue());
                }

            }

            @Override
            public void timeout(int i) {
            }

            @Override
            public void notAllowed(int i) {
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                manager.register(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);
                manager.register(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
                manager.register(AutomotiveSignalId.FMS_FUEL_RATE);
                manager.register(AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);
            }
        }).start();

        NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver(mMonitor);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, intentFilter);

    }

    @Override
    public void onPause(){
        System.out.println("nu körs on pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        System.out.println("nu körs on destroy");
        new Thread(new Runnable() {
            @Override
            public void run() {
                manager.unregister(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);
                manager.unregister(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
                manager.unregister(AutomotiveSignalId.FMS_FUEL_RATE);
                manager.unregister(AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);
            }
        }).start();
//        if (mAlarmMgr != null) {
//            mAlarmMgr.cancel(mAlarmIntent);
//        }
        ((MyApplication) getApplicationContext()).getIOTLocationListener().stop();
        mMqtt.stopInterevalTimer();
        super.onDestroy();
    }

    @Override
    protected void onStop(){
        System.out.println("nu körs onstop");
        new Thread(new Runnable() {
            @Override
            public void run() {
                manager.unregister(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);
                manager.unregister(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
                manager.unregister(AutomotiveSignalId.FMS_FUEL_RATE);
                manager.unregister(AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);
            }
        }).start();
//        if (mAlarmMgr != null) {
//            mAlarmMgr.cancel(mAlarmIntent);
//        }
        ((MyApplication) getApplicationContext()).getIOTLocationListener().stop();
        mMqtt.stopInterevalTimer();
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        View decorView = getWindow().getDecorView();
        super.onWindowFocusChanged(hasFocus);
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public ActivityLayoutManager_v2 getLayoutManager(){
        return layoutManager;

    }

}
