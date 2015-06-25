package combitech.com.againstrumentcluster;

/**
 * Created by Fredrik on 2015-06-23.
 */
public interface CloudPutter {

    public void readConfig();

    public boolean createConnection();

    public void publishSpeed(float speed);

    public void publishBatteryLevel(float percent);

    public void publishDistanceTraveled(long distance);

    public void start();
}
