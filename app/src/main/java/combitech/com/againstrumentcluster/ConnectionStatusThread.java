package combitech.com.againstrumentcluster;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * Created by Fredrik on 2015-06-25.
 */
public class ConnectionStatusThread extends Thread {

    private final static int NETWORK_CONNECTION_ON = 1;
    private final static int NETWORK_CONNECTION_OFF = 2;
    private final static int CLOUD_CONNECTION_LOST = 3;
    private final static int MANUAL_CONNECTION_ATTEMPT = 4;


    ActivityLayoutManager_v2 layoutManager;
    Monitor monitor;
    private boolean networkOnline;
    private Activity activity;

    public ConnectionStatusThread(Monitor monitor, ActivityLayoutManager_v2 layoutManager, Activity activity) {
        this.layoutManager = layoutManager;
        this.monitor = monitor;
        this.activity = activity;
    }


    public void run() {
        System.out.println("Running online checker thread");
        while (true) {

            //wifi-on
            //wifi-off
            //connectionLost
            //connectionOn
            monitor.waitForNetworkChange();
            int change = monitor.getNetworkChange();

            switch (change) {
                case NETWORK_CONNECTION_ON:
                    tryToConnect();
//                    System.out.println("Vi har ngt nätverk");
//                    monitor.notifyCloudConnection();
//                    System.out.println("Vi har notifiat cloudconnectorn");
//                    boolean connected = monitor.waitForCloudConnectionResult();
//                    System.out.println("Vi har fått ett resultat: " + connected);
//                    if (connected) {
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                layoutManager.setConnectionStatus(true);
//                            }
//                        });
//
//                    }
                    break;
                case NETWORK_CONNECTION_OFF:
                    updateConnectionButton(false);
                    break;
//                    System.out.println("nu ska jag bli röd");
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            System.out.println("network connection lost");
//                            layoutManager.setConnectionStatus(false);
//                        }
//                    });
//                    break;
                case MANUAL_CONNECTION_ATTEMPT:
//                    System.out.println("manual connection attempt");
//                    monitor.notifyCloudConnection();
//                    System.out.println("Vi har notifiat cloudconnectorn");
//                    connected = monitor.waitForCloudConnectionResult();
//                    System.out.println("Vi har fått ett resultat: " + connected);
//                    if (connected) {
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                layoutManager.setConnectionStatus(true);
//                            }
//                        });
//
//                    }
                    tryToConnect();
                    break;

                case CLOUD_CONNECTION_LOST:
                    updateConnectionButton(false);
                    break;
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            System.out.println("cloud connection lost");
//                            layoutManager.setConnectionStatus(false);
//                        }
//                    });
//                    break;
            }


        }

    }

    private void tryToConnect(){
        monitor.notifyCloudConnection();
        boolean connected = monitor.waitForCloudConnectionResult();
        updateConnectionButton(connected);
    }


    private void updateConnectionButton(boolean status){
        final boolean temp = status;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.setConnectionStatus(temp);
            }
        });
    }




}

