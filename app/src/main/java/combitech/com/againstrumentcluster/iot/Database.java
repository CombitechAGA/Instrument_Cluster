package combitech.com.againstrumentcluster.iot;

import java.util.ArrayList;

/**
 * Created by Fredrik on 2015-07-02.
 */
public interface Database {
    public void putSnapShot(CarSnapShot carSnapShot);

    public ArrayList<CarSnapShot> getAllUnsentSnapShots();

    public boolean isEmpty();

    public CarSnapShot getLatestSnapshot();

}
