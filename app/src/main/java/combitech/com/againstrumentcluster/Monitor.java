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


}
