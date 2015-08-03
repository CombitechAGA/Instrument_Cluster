package combitech.com.againstrumentcluster.iot.appcore;

import android.app.Application;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

import combitech.com.againstrumentcluster.iot.IOTLocationListener;
import combitech.com.againstrumentcluster.iot.MQTT;
import combitech.com.againstrumentcluster.iot.Monitor;

/**
 * Created by Fredrik on 2015-07-01.
 */
public class MyApplication extends Application {

    private MQTT mCloudPutter;
    private Monitor mMonitor;
    IOTLocationListener mIOTLocationListener;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();

        mMonitor = new Monitor();
        mCloudPutter = new MQTT(mMonitor, this);
        mCloudPutter.start();
        mIOTLocationListener = new IOTLocationListener(this,mMonitor);
        mIOTLocationListener.start();

    }

    @Override
    public void onTerminate() {
        mIOTLocationListener.stop();
        mCloudPutter.stopInterevalTimer();
        super.onTerminate();
    }




    public Monitor getMonitor() {
        return mMonitor;
    }
    public MQTT getMQTT(){
        return mCloudPutter;
    }

    public static Context getAppContext(){
        return MyApplication.context;
    }
}
