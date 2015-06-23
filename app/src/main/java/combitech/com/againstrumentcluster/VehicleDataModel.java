package combitech.com.againstrumentcluster;

/**
 * Created by Erik on 2014-10-07.
 */
public class VehicleDataModel {
    private float vehicleSpeed = 45f;
    private float batteryLevel = 0.80f;
    private float latitude = 59.406150f; //Latitude of Kista mässan
    private float longitude = 17.957383f; //Longitude of Kista mässan
    private long odometer = 0;

    public long getOdometer() {
        return odometer;
    }

    public void setOdometer(long odometer) {
        this.odometer = odometer;
    }

    public float getVehicleSpeed() {
        return vehicleSpeed;
    }

    public void setVehicleSpeed(float vehicleSpeed) {
        this.vehicleSpeed = vehicleSpeed;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(float batteryLevel) {
        this.batteryLevel = batteryLevel / 100f;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
