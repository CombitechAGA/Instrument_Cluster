package combitech.com.againstrumentcluster.iot;

/**
 * Created by Fredrik on 2015-07-02.
 */
public class CarSnapShot {
    long timestamp;
    float fuel;
    float speed;
    long distanceTraveled;
    String carID;
    double longitude;
    double latitude;
    String zbeeName;
    public CarSnapShot(String carID,long timestamp, float fuel, float speed, long distanceTraveled, double longitude, double latitude, String zbeeName){
        this.timestamp=timestamp;
        this.fuel=fuel;
        this.speed=speed;
        this.distanceTraveled=distanceTraveled;
        this.carID = carID;
        this.longitude=longitude;
        this.latitude=latitude;
        this.zbeeName = zbeeName;
    }
    @Override
    public String toString(){
        String toString = "carID:"+carID+";timestamp:"+timestamp+";fuel:"+fuel+";speed:"+speed+";distanceTraveled:"+distanceTraveled+";longitude:"+longitude+";latitude:"+latitude+";zbeename:"+zbeeName;
        return toString;
    }
}
