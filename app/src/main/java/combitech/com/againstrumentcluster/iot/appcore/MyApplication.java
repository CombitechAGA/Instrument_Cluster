package combitech.com.againstrumentcluster.iot.appcore;

import android.app.Application;

import combitech.com.againstrumentcluster.iot.MQTT;
import combitech.com.againstrumentcluster.iot.Monitor;

/**
 * Created by Fredrik on 2015-07-01.
 */
public class MyApplication extends Application {

    private MQTT mCloudPutter;
    private Monitor mMonitor;

    @Override
    public void onCreate() {
        super.onCreate();

        mMonitor = new Monitor();
        mCloudPutter = new MQTT(mMonitor, this);
        mCloudPutter.start();
    }

    public Monitor getMonitor() {
        return mMonitor;
    }
    public MQTT getMQTT(){
        return mCloudPutter;
    }
}
