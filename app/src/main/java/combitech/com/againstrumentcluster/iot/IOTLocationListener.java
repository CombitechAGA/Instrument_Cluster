package combitech.com.againstrumentcluster.iot;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Fredrik on 2015-07-02.
 */
public class IOTLocationListener implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG_TAG = IOTLocationListener.class.getSimpleName();
    private Context mContext;
    private Monitor mMonitor;
    LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public IOTLocationListener(Context context, Monitor monitor) {
        mContext = context;
        mMonitor = monitor;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public void start() {
        Log.d(LOG_TAG, "start");
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();

//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    public void stop() {
        Log.d(LOG_TAG, "stop");
//        mLocationManager.removeUpdates(this);
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            Log.d(LOG_TAG, location.toString());
            Toast.makeText(mContext, location.toString(), Toast.LENGTH_LONG).show();
            mMonitor.setLongitude(location.getLongitude());
            mMonitor.setLatitude(location.getLatitude());
        } else {
            Log.d(LOG_TAG, "Location �r null");
            Toast.makeText(mContext, "Location null", Toast.LENGTH_LONG).show();
        }
    }

//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//        Log.d(LOG_TAG, "On status changed");
//    }
//
//    @Override
//    public void onProviderEnabled(String s) {
//        Log.d(LOG_TAG, "onProviderEnabled");
//    }
//
//    @Override
//    public void onProviderDisabled(String s) {
//        Log.d(LOG_TAG, "onProviderDisabled");
//    }

    @Override
    public void onConnected(Bundle bundle) {
        //        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
//        Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastKnownLocation != null) {
            System.out.println("Använder GPS");
            Log.d(LOG_TAG, lastKnownLocation.toString());
            Toast.makeText(mContext, lastKnownLocation.toString(), Toast.LENGTH_LONG).show();
            mMonitor.setLongitude(lastKnownLocation.getLongitude());
            mMonitor.setLatitude(lastKnownLocation.getLatitude());
        }
        else{
            Log.d(LOG_TAG,"Last known locatin is null");
            Toast.makeText(mContext, "Last known locatin is null", Toast.LENGTH_LONG).show();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
