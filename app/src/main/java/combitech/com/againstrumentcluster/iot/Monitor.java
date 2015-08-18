package combitech.com.againstrumentcluster.iot;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Fredrik on 2015-06-25.
 */

public class Monitor {

    private double homelat;
    private double homelng;

    private final static int SPEED = 1;
    private final static int FUEL = 2;
    private final static int DISTANCE_TRAVELED = 3;
    private final static int READ_MESSAGE = 4;


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
    private ArrayList<String> messages = new ArrayList<String>();
    private ArrayList<String> readMessages = new ArrayList<String>();
    private int zoomLevel;
    private int geofenceDistance;
    private boolean simulator = false;
    private HashMap<Integer, LocationInfo> fakeGps;
    private long totalTrackDistance = 3600;
    private String zbeeName;
    private boolean mission;
    private LocationInfo missionLoc;

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
            else if(readMessages.size()!=0){
                Log.d(LOG_TAG, "ska skicka medelande");
                return READ_MESSAGE;
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
        System.out.println("v�ntar");
        while(!allowedToConnectToCloud){
            System.out.println("v�ntar2");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("f�rdigv�ntad");
    }

    public synchronized boolean waitForCloudConnectionResult() {
        while(!connectionAttemptFinished){
            System.out.println("Jag v�ntar p� att connection result ska vara klart");
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

    public synchronized boolean newMessage() {
        if (messages.size() > 0) {
            return true;
        }
        return false;
    }
    public synchronized String getMessage() {
        return messages.get(0);
    }

    public synchronized void addMessage(String message) {
        messages.add(message);
    }

    public synchronized void messageRead() {
        readMessages.add(messages.remove(0));
        notifyAll();
    }
    public synchronized String getReadMessage(){
        return readMessages.get(0);
    }

    public synchronized void removeReadMessage() {
        readMessages.remove(0);
    }


    public synchronized double getHomelng() {
        return homelng;
    }

    public synchronized double getHomelat() {
        return homelat;
    }

    public synchronized void setHomelng(double homelng) {
        this.homelng = homelng;
    }

    public synchronized void setHomelat(double homelat) {
        this.homelat = homelat;
    }
    public synchronized void setZoomLevel(int zoomLevel){
        this.zoomLevel = zoomLevel;
    }

    public synchronized int getZoomLevel() {
        return zoomLevel;
    }
    public synchronized  void setGeofenceDistance(int geofenceDistance){
        this.geofenceDistance=geofenceDistance;
    }
    public synchronized int getGeofenceDistance(){
        return geofenceDistance;
    }
    public synchronized void setSimulator(boolean simulator){
        this.simulator=simulator;
    }
    public synchronized boolean isSimulator(){
        return simulator;
    }

    public synchronized void setFakeGps(HashMap<Integer,LocationInfo> fakeGps) {
        this.fakeGps = fakeGps;
    }

    public synchronized void updateFakeGps(long distanceTraveled) {
        int index = (int) (distanceTraveled % totalTrackDistance) / 100;
        System.out.println("Location index är: " + index);
        latitude = fakeGps.get(index).getLat();
        System.out.println(latitude);
        longitude = fakeGps.get(index).getLng();
        System.out.println(longitude);

        //uppdatera också på kartan om den inte är uppe



    }

    public synchronized void setZbeeName(String name) {
        this.zbeeName = name;
    }

    public synchronized String getZbeeName() {
        return zbeeName;
    }

    public synchronized void setMission(boolean mission) {
        this.mission = mission;
    }

    public void setMissionPosition(LocationInfo missionPosition) {
        this.missionLoc = missionPosition;
    }

    public synchronized LocationInfo getMissionPosition(){
        return missionLoc;
    }
    public synchronized boolean haveMission() {
        return mission;
    }

//    public synchronized float getDistancePrediction() {
//        return distancePrediction;
//    }
}
