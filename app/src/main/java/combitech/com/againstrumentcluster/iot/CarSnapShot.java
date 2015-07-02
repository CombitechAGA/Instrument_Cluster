package combitech.com.againstrumentcluster.iot;

/**
 * Created by Fredrik on 2015-07-02.
 */
public class CarSnapShot {
    long timestamp;
    float fuel;
    float speed;
    long distanceTraveled;
    public CarSnapShot(long timestamp, float fuel, float speed, long distanceTraveled){
        this.timestamp=timestamp;
        this.fuel=fuel;
        this.speed=speed;
        this.distanceTraveled=distanceTraveled;
    }
    @Override
    public String toString(){
        String toString = "timestamp:"+timestamp+";fuel:"+fuel+";speed:"+speed+";distanceTraveled:"+distanceTraveled;
        return toString;
    }
}
