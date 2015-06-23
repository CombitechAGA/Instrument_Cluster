package combitech.com.againstrumentcluster;

import com.combitech.Mission;
import com.combitech.Pair;
import com.combitech.safe.SAFEClient;
import com.combitech.safe.model.SAFEClientData;

import java.util.LinkedList;

/**
 * Created by Erik on 2014-10-07.
 */
public class SAFEDataModel {
    private Mission currentMission;
    private boolean haveReadMessage = true;
    public final static String SERVER_ADRESS = "http://192.168.1.187";
    public final static String LOCALHOST = "http://localhost";

    public String getCurrentMissionMessage() {
        if (currentMission != null) {
            return currentMission.getMissionText();
        }
        return null;
    }

    public int getCurrentMissionId() {
        if (currentMission != null) {
            return currentMission.getId();
        }
        return -1;
    }

    public void setCurrentMission(LinkedList<Mission> missionList) {
        Mission firstMission = null;
        for (Mission m : missionList) {
            if (m.getStatus() != SAFEClient.DONE && m.getStatus() != SAFEClient.FINISHED && m.getStatus() != SAFEClient.PROBLEM) {
                firstMission = m;
                break;
            }
        }

        if (firstMission == null) {
            currentMission = null;
            return;
        }

        if (currentMission != null) {
            if (currentMission.getId() != firstMission.getId()) {
                haveReadMessage = false;
            }
        } else {
            haveReadMessage = false;
        }
        currentMission = firstMission;
    }

    public boolean haveReadMessage() {
        return haveReadMessage;
    }

    public void setHaveReadMessage(boolean haveReadMessage) {
        this.haveReadMessage = haveReadMessage;
    }

    public void resetMessage() {
        currentMission = null;
    }

    public Mission getCurrentMission() {
        return currentMission;
    }
}
