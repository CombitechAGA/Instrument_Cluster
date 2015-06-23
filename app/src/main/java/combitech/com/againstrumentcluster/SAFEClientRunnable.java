package combitech.com.againstrumentcluster;

import android.util.Log;

import com.combitech.safe.SAFEClient;

import java.io.IOException;

/**
 * Created by Erik on 2014-10-07.
 */
//Folke och Thomas

public class SAFEClientRunnable implements Runnable {
    private final SAFEDataModel sAFEDataModel;
    private final VehicleDataModel vehicleDataModel;
    private SAFEClient client;
    private boolean isRunning;
    private long oldTime;
    private long elapsedTime;
    private long currentTime;
    private long timeDelta;

    public SAFEClientRunnable(SAFEClient client, SAFEDataModel sAFEDataModel, VehicleDataModel vehicleDataModel) {
        this.sAFEDataModel = sAFEDataModel;
        this.vehicleDataModel = vehicleDataModel;
        this.client = client;
        isRunning = true;
    }

    @Override
    public void run() {
        return;
//        try {
//            currentTime = System.currentTimeMillis();
//            client.loginAndGetSessionId(SAFEDataModel.SERVER_ADRESS + "/safe/api/sessions");
//            while (isRunning) {
//                updateTime();
//                if ((elapsedTime % 2) == 0) {
//                    client.updateMissionList(SAFEDataModel.SERVER_ADRESS + "/safe/api/issues/10/children");
//                    sAFEDataModel.setCurrentMission(client.clientData.getMissionList());
//                }
//                if ((elapsedTime % 300) == 0) {
//                    client.loginAndGetSessionId(SAFEDataModel.SERVER_ADRESS + "/safe/api/sessions");
//                }
//                if ((elapsedTime % 2) == 0) {
//                    client.clientData.setLatitude(vehicleDataModel.getLatitude());
//                    client.clientData.setLongitude(vehicleDataModel.getLongitude());
//                    client.sendPosition(SAFEDataModel.SERVER_ADRESS + "/safe/api/issues/", 10, "/position?doNotUpdateIssueLog=true");
//                }
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                }
//            }
//        } catch (IOException e) {
//        }
    }

    private void updateTime() {
        oldTime = currentTime;
        currentTime = System.currentTimeMillis();
        timeDelta = currentTime - oldTime;
        elapsedTime += timeDelta;
    }

    public void stopSAFEClientRunnable() {
        isRunning = false;
    }
}
