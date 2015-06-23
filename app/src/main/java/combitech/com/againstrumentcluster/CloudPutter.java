package combitech.com.againstrumentcluster;

/**
 * Created by Fredrik on 2015-06-23.
 */
public interface CloudPutter {

    public void readConfig();

    public void createConnection();

    public void updateSpeed(float speed);

    public void updateBattery(float percent);

    public void updateRange(long range);
}
