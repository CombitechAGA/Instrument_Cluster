package combitech.com.againstrumentcluster.iot;

import android.util.Log;

/**
 * Created by Fredrik on 2015-06-25.
 */

public class Monitor {

    private final static int SPEED = 1;
    private final static int FUEL = 2;
    private final static int DISTANCE_TRAVELED = 3;


    private final static int NETWORK_CONNECTION_ON = 1;
    private final static int NETWORK_CONNECTION_OFF = 2;
    private final static int CLOUD_CONNECTION_LOST = 3;
    private final static int MANUAL_CONNECTION_ATTEMPT = 4;
    private static final String LOG_TAG = Monitor.class.getSimpleName();


    private float speed;
    private float fuel;
    private long distanceTraveled;
    private double longitude;
    private double latitude;

    private boolean speedUpdated = false;
    private boolean fuelUpdated = false;
    private boolean distanceTraveledUpdated = false;


    private boolean connectedToCloud=false;


    private boolean manualDisconnectRequest=false;


    private boolean allowedToConnectToCloud = false;
    private boolean connectionAttemptFinished = false;
    private boolean networkedChanged = false;


    private int lastNetworkChange;

    public Monitor() {
    }

    public synchronized void updateSpeed(float speed) {
        this.speed = speed;
        speedUpdated = true;
        notifyAll();
    }

    public synchronized void updateFuel(float fuel) {
        this.fuel = fuel;
        fuelUpdated = true;
        notifyAll();
    }

    public synchronized void updatedistanceTraveled(long distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
        distanceTraveledUpdated = true;
        notifyAll();
    }

    public synchronized float getFuel() {
        return fuel;
    }

    public synchronized float getSpeed() {
        return speed;
    }

    public synchronized long getDistanceTraveled() {
        return distanceTraveled;
    }


    public synchronized int dataUpdated() {
        while (true) {
            System.out.println("data updated maybe baybe");
            if (speedUpdated) {
                speedUpdated = false;
                return SPEED;
            } else if (fuelUpdated) {
                fuelUpdated = false;
                return FUEL;
            } else if (distanceTraveledUpdated) {
                distanceTraveledUpdated = false;
                return DISTANCE_TRAVELED;
            }
              else if (manualDisconnectRequest){
                manualDisconnectRequest=false;
                return -1;

            }
            else if(lastNetworkChange==NETWORK_CONNECTION_OFF){
                return -1;
            }
            else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    public synchronized void networkOnline() {
        networkedChanged = true;
        lastNetworkChange = NETWORK_CONNECTION_ON;
        notifyAll();
    }

    public synchronized void networkOffline() {
        networkedChanged = true;
        lastNetworkChange = NETWORK_CONNECTION_OFF;
        notifyAll();

    }

    public synchronized void waitForNetworkChange() {
        while(!networkedChanged){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        networkedChanged=false;
    }

    public synchronized int getNetworkChange() {
        return lastNetworkChange;
    }

    public synchronized void notifyCloudConnection() {
        allowedToConnectToCloud = true;
        notifyAll();

    }
    public synchronized void waitForCloudConnectionAllowance(){
        System.out.println("väntar");
        while(!allowedToConnectToCloud){
            System.out.println("väntar2");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("färdigväntad");
    }

    public synchronized boolean waitForCloudConnectionResult() {
        while(!connectionAttemptFinished){
            System.out.println("Jag väntar på att connection result ska vara klart");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        connectionAttemptFinished=false;
        return connectedToCloud;
    }

    public synchronized void notifyCloudConnectionResult(boolean result){
        allowedToConnectToCloud=false;
        connectionAttemptFinished=true;
        connectedToCloud=result;
        notifyAll();
    }

    public synchronized boolean isConnectedToCloud(){
        return connectedToCloud;
    }

    public synchronized void disconnectedFromCloud() {
        networkedChanged=true;
        connectedToCloud=false;
        lastNetworkChange = CLOUD_CONNECTION_LOST;
        notifyAll();
    }


    public synchronized void doManualDisconnect(){
        manualDisconnectRequest=true;
        notifyAll();
    }

    public synchronized void doManualConnect() {
        networkedChanged=true;
        lastNetworkChange = MANUAL_CONNECTION_ATTEMPT;
        notifyAll();
        System.out.println("Do manual connect");
    }
    public void testPrint(){
        Log.d(LOG_TAG, "In the monitor");
    }

    public synchronized void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public synchronized void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public synchronized double getLongitude() {
        return longitude;
    }

    public synchronized double getLatitude() {
        return latitude;
    }

//    public synchronized float getDistancePrediction() {
//        return distancePrediction;
//    }
}
