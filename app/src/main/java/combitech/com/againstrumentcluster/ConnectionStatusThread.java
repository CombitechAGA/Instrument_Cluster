package combitech.com.againstrumentcluster;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * Created by Fredrik on 2015-06-25.
 */
public class ConnectionStatusThread extends Thread {
    ActivityLayoutManager_v2 layoutManager;
    Monitor monitor;
    private boolean networkOnline;
    private Activity activity;

    public ConnectionStatusThread(Monitor monitor, ActivityLayoutManager_v2 layoutManager, Activity activity) {
        this.layoutManager = layoutManager;
        this.monitor = monitor;
        this.activity = activity;
    }

    //för när vi roterar etc?
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void run() {
        System.out.println("Running online checker thread");
        while (true) {
            networkOnline = monitor.waitForNetworkChange();
            // vi har ngt nätverk
            if (networkOnline) {
                System.out.println("Vi har ngt nätverk");
                monitor.notifyCloudConnection();
                System.out.println("Vi har notifiat cloudconnectorn");
                boolean connected = monitor.waitForCloudConnectionResult();
                System.out.println("Vi har fått ett resultat: " + connected);
                if (connected) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.setConnectionStatus(true);
                        }
                    });

                }
            }
            //vi har inget nätverk
            else {
                System.out.println("nu ska jag bli röd");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("så röd och fin jag ska bli");
                        layoutManager.setConnectionStatus(false);
                    }
                });
            }
        }

    }

    //   monitor.waitUntilOnline();
    //   layoutManager.setConnectionStatus(true);


}

