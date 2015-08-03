package combitech.com.againstrumentcluster.iot;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Fredrik on 2015-07-02.
 */
public class IOTLocationListener implements android.location.LocationListener {
    private static final String LOG_TAG = IOTLocationListener.class.getSimpleName();
    private Context mContext;
    private Monitor mMonitor;
    LocationManager mLocationManager;


    public IOTLocationListener(Context context, Monitor monitor) {
        mContext = context;
        mMonitor = monitor;
    }

    public void start() {
        Log.d(LOG_TAG, "start");
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location netLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location passLoc = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastKnownLocation != null) {
            System.out.println("Använder GPS");
            Log.d(LOG_TAG, lastKnownLocation.toString());
            Toast.makeText(mContext, lastKnownLocation.toString(), Toast.LENGTH_LONG).show();
            mMonitor.setLongitude(lastKnownLocation.getLongitude());
            mMonitor.setLatitude(lastKnownLocation.getLatitude());
        }
        else if (netLoc != null) {
            System.out.println("Använder network");
            Log.d(LOG_TAG, netLoc.toString());
            Toast.makeText(mContext, netLoc.toString(), Toast.LENGTH_LONG).show();
            mMonitor.setLongitude(netLoc.getLongitude());
            mMonitor.setLatitude(netLoc.getLatitude());

        }
        else if (passLoc != null) {
            System.out.println("Använder network");
            Log.d(LOG_TAG, passLoc.toString());
            Toast.makeText(mContext, netLoc.toString(), Toast.LENGTH_LONG).show();
            mMonitor.setLongitude(netLoc.getLongitude());
            mMonitor.setLatitude(netLoc.getLatitude());

        }
        else{
            Log.d(LOG_TAG,"Last known locatin is null");
            Toast.makeText(mContext, "Last known locatin is null", Toast.LENGTH_LONG).show();
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    public void stop() {
        Log.d(LOG_TAG, "stop");
        mLocationManager.removeUpdates(this);
    }


    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            Log.d(LOG_TAG, location.toString());
            Toast.makeText(mContext, location.toString(), Toast.LENGTH_LONG).show();
        } else {
            Log.d(LOG_TAG, "Location �r null");
            Toast.makeText(mContext, "Location null", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(LOG_TAG, "On status changed");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(LOG_TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(LOG_TAG, "onProviderDisabled");
    }
}
