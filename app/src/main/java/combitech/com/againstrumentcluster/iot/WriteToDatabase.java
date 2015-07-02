package combitech.com.againstrumentcluster.iot;

import android.os.AsyncTask;

import combitech.com.againstrumentcluster.iot.CarSnapShot;
import combitech.com.againstrumentcluster.iot.Database;

/**
 * Created by Fredrik on 2015-07-02.
 */
public class WriteToDatabase extends AsyncTask<CarSnapShot,Void,Void> {
    Database database;
    public WriteToDatabase(Database database){
        this.database = database;
    }


    @Override
    protected Void doInBackground(CarSnapShot... carSnapShots) {
        for (CarSnapShot snapShot : carSnapShots){
            database.putSnapShot(snapShot);
        }
        return null;
    }
}
