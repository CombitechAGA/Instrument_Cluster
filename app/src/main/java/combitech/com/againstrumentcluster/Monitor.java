package combitech.com.againstrumentcluster;

/**
 * Created by Fredrik on 2015-06-25.
 */

public class Monitor {

    private final static int SPEED = 1;
    private final static int FUEL = 2;
    private final static int DISTANCE_TRAVELED = 3;


    private float speed;
    private float fuel;
    private long distanceTraveled;

    private boolean speedUpdated = false;
    private boolean fuelUpdated = false;
    private boolean distanceTraveledUpdated = false;


    private boolean connectedToCloud=false;
    private boolean allowedToConnectToCloud = false;
    private boolean connectionAttemptFinished = false;
    private boolean networkedChanged = false;
    private boolean networkOnline=false;


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
            if (speedUpdated) {
                speedUpdated = false;
                return SPEED;
            } else if (fuelUpdated) {
                fuelUpdated = false;
                return FUEL;
            } else if (distanceTraveledUpdated) {
                distanceTraveledUpdated = false;
                return DISTANCE_TRAVELED;
            } else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


//    public synchronized void connected() {
//        online=true;
//        notifyAll();
//    }
//
//    public synchronized void waitUntilOnline() {
//        while(!online){
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public synchronized void networkOnline() {
        networkedChanged = true;
        networkOnline = true;
        notifyAll();
    }

    public synchronized void networkOffline() {
        networkedChanged = true;
        networkOnline = false;
        notifyAll();

    }

    public synchronized boolean waitForNetworkChange() {
        while(!networkedChanged){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        networkedChanged=false;
        return networkOnline;
    }

    public synchronized void notifyCloudConnection() {
        allowedToConnectToCloud = true;
        notifyAll();

    }
    public synchronized void waitForCloudConnectionAllowance(){
        System.out.println("väntar");
        while(!allowedToConnectToCloud){
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
        connectedToCloud=false;
    }
}
