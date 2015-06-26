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

    //f�r n�r vi roterar etc?
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void run() {
        System.out.println("Running online checker thread");
        while (true) {
            networkOnline = monitor.waitForNetworkChange();
            // vi har ngt n�tverk
            if (networkOnline) {
                System.out.println("Vi har ngt n�tverk");
                monitor.notifyCloudConnection();
                System.out.println("Vi har notifiat cloudconnectorn");
                boolean connected = monitor.waitForCloudConnectionResult();
                System.out.println("Vi har f�tt ett resultat: " + connected);
                if (connected) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.setConnectionStatus(true);
                        }
                    });

                }
            }
            //vi har inget n�tverk
            else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layoutManager.setConnectionStatus(false);
                    }
                });
            }
        }

    }

    //   monitor.waitUntilOnline();
    //   layoutManager.setConnectionStatus(true);


}

