package combitech.com.againstrumentcluster;

/**
 * Created by Fredrik on 2015-06-23.
 */
public interface CloudPutter {

    public void readConfig();

    public boolean createConnection();

    public boolean publishSpeed(float speed);

    public boolean publishBatteryLevel(float percent);

    public boolean publishDistanceTraveled(long distance);

    public void start();
}
