package combitech.com.againstrumentcluster.iot;


/**
 * Created by thomasstrahl on 2015-08-12.
 */
public class LocationInfo {

    private double lat;
    private double lng;

    public LocationInfo(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLat(double newLat) {
        lat = newLat;
    }

    public void setLng(double newLng) {
        lng = newLng;
    }
}