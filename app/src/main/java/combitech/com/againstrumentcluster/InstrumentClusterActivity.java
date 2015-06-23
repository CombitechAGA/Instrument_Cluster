package combitech.com.againstrumentcluster;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.swedspot.automotive.AutomotiveManager;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSFloat;
import android.swedspot.scs.data.SCSInteger;
import android.swedspot.scs.data.SCSLong;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.combitech.safe.SAFEClient;
import com.swedspot.automotiveapi.AutomotiveListener;

public class InstrumentClusterActivity extends Activity {

    private static AutomotiveManager manager;
    private final static VehicleDataModel vehicleDataModel = new VehicleDataModel();
    private final static SAFEDataModel safeDataModel = new SAFEDataModel();
    private final static SAFEClient safeClient = new SAFEClient("admin", "safe", 1, "Resurs");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SAFEClientRunnable updateRunnable = new SAFEClientRunnable(safeClient, safeDataModel, vehicleDataModel);
        new Thread(updateRunnable).start();
        final CloudPutter mqtt = new MQTT(this);
        mqtt.createConnection();

        // Ändra layout här
        //final ActivityLayoutManager layoutManager = new ActivityLayoutManager(this, vehicleDataModel, safeDataModel, safeClient);
        final ActivityLayoutManager_v2 layoutManager = new ActivityLayoutManager_v2(this, vehicleDataModel, safeDataModel, safeClient);
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
                    mqtt.updateSpeed(data.getFloatValue());

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
                } else if (automotiveSignal.getSignalId() == AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE) {
                    final SCSLong data = (SCSLong) automotiveSignal.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vehicleDataModel.setOdometer(data.getLongValue());
                            layoutManager.updateOdometer();
                        }
                    });
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
    }

    @Override
    protected void onDestroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                manager.unregister(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);
                manager.unregister(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
                manager.unregister(AutomotiveSignalId.FMS_FUEL_RATE);
                manager.unregister(AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);
            }
        }).start();
        super.onDestroy();
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


}
