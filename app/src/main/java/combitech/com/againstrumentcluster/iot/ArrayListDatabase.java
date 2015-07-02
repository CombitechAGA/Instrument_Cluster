package combitech.com.againstrumentcluster.iot;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Fredrik on 2015-07-02.
 */
public class ArrayListDatabase implements Database{
    private ArrayList<CarSnapShot> dataStore;
    private static final String LOG_TAG = ArrayListDatabase.class.getSimpleName();
    public ArrayListDatabase(){
        dataStore = new ArrayList<CarSnapShot>();
    }
    @Override
    public synchronized void putSnapShot(CarSnapShot carSnapShot) {
        dataStore.add(carSnapShot);
        Log.d(LOG_TAG, "size: " + dataStore.size());
    }

    @Override
    public synchronized ArrayList<CarSnapShot> getAllUnsentSnapShots() {
        ArrayList<CarSnapShot> temp = (ArrayList<CarSnapShot>)dataStore.clone();
        dataStore.clear();
        return temp;
    }

    @Override
    public synchronized boolean isEmpty() {
        return dataStore.isEmpty();
    }

    @Override
    public synchronized CarSnapShot getLatestSnapshot() {
        return dataStore.remove(dataStore.size()-1);
    }
}
