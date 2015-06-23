package combitech.com.againstrumentcluster;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.combitech.safe.SAFEClient;

import org.w3c.dom.Text;

import java.io.IOException;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ActivityLayoutManager extends RelativeLayout {

    private ImageView messageIcon;
    private ImageView backIcon;
    private ImageView outerCircle;

    private ProgressBar batteryProgressBar;

    private Button acceptButton;
    private Button cancelButton;
    private Button doneButton;
    private Button issueButton;

    private TextView batteryPercentage;
    private TextView speedNumber;
    private TextView message;
    private TextView odometer;

    private InstrumentClusterActivity activity;
    private Timer updateGUITimer;

    private VehicleDataModel vehicleDataModel;
    private SAFEDataModel safeDataModel;
    private SAFEClient safeClient;

    public ActivityLayoutManager(final InstrumentClusterActivity activity, final VehicleDataModel vehicleDataModel, final SAFEDataModel safeDataModel, final SAFEClient safeClient) {
        super(activity);
        this.activity = activity;
        this.vehicleDataModel = vehicleDataModel;
        this.safeDataModel = safeDataModel;
        this.safeClient = safeClient;

        setupViewNormal();
        updateGUITimer = new Timer(new Runnable() {
            @Override
            public void run() {
                if (safeDataModel.getCurrentMissionMessage() != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (safeDataModel.getCurrentMission() == null) {
                                messageIcon.setImageResource(R.drawable.aga_zbee_message_icon);
                            } else {
                                if (safeDataModel.haveReadMessage()) {
                                    messageIcon.setImageResource(R.drawable.aga_zbee_message_icon);
                                } else {
                                    messageIcon.setImageResource(R.drawable.aga_zbee_message_icon_unread);
                                }
                            }
                        }
                    });
                }
            }
        }, 1000);
        updateGUITimer.setRepeat(true);
        updateGUITimer.start();

        setBackgroundColor(Color.parseColor("#FFFFFF"));
        setLayoutParams(new ActionBar.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    public void setupViewNormal() {
        activity.setContentView(R.layout.aga_zbee);
        batteryPercentage = (TextView) activity.findViewById(R.id.percentageBattery);
        speedNumber = (TextView) activity.findViewById(R.id.speedNumber);
        odometer = (TextView) activity.findViewById(R.id.tripMeter);

        batteryProgressBar = (ProgressBar) activity.findViewById(R.id.batteryProgressBar);
        outerCircle = (ImageView) activity.findViewById(R.id.outerCircle);

        messageIcon = (ImageView) activity.findViewById(R.id.messageIcon);
        if (safeDataModel.haveReadMessage()) {
            messageIcon.setImageResource(R.drawable.aga_zbee_message_icon);
        } else {
            messageIcon.setImageResource(R.drawable.aga_zbee_message_icon_unread);
        }
        messageIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                setupViewMessage();
            }
        });

        updateSpeed();
        updateBattery();
        updateRange();
        updateOdometer();

    }

    public void setupViewMessage() {
        activity.setContentView(R.layout.aga_zbee_message);
        batteryPercentage = (TextView) activity.findViewById(R.id.percentageBattery);
        speedNumber = (TextView) activity.findViewById(R.id.speedNumber);

        batteryProgressBar = (ProgressBar) activity.findViewById(R.id.batteryProgressBar);
        outerCircle = (ImageView) activity.findViewById(R.id.outerCircle);

        cancelButton = (Button) activity.findViewById(R.id.cancelButton);
        acceptButton = (Button) activity.findViewById(R.id.acceptButton);
        doneButton = (Button) activity.findViewById(R.id.doneButton);
        issueButton = (Button) activity.findViewById(R.id.issueButton);

        issueButton.setText("Issue");
        issueButton.setTextSize(25f);
        doneButton.setText("Done");
        doneButton.setTextSize(25f);

        backIcon = (ImageView) activity.findViewById(R.id.backIcon);
        backIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                setupViewNormal();
            }
        });

        message = (TextView) activity.findViewById(R.id.messageText);

        if (safeDataModel.getCurrentMissionMessage() == null) { //We have no messages
            acceptButton.setX(5000);
            cancelButton.setX(5000);
            doneButton.setX(5000);
            issueButton.setX(5000);

            message.setText("No messages");
            message.setTextColor(Color.parseColor("#CCCCCC"));

        } else if (safeDataModel.haveReadMessage()) { //We have a message but it is already read
            acceptButton.setX(5000);
            cancelButton.setX(5000);

            doneButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    sendStatusUpdate(SAFEClient.DONE);
                    safeDataModel.setHaveReadMessage(true);
                    setupViewNormal();
                }
            });

            issueButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    sendStatusUpdate(SAFEClient.PROBLEM);
                    safeDataModel.setHaveReadMessage(true);
                    setupViewNormal();
                }
            });

            message.setText(safeDataModel.getCurrentMissionMessage());

        } else { //We have a new message
            issueButton.setX(5000);
            doneButton.setX(5000);

            acceptButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    sendStatusUpdate(SAFEClient.STARTED);
                    setupViewNormal();
                    safeDataModel.setHaveReadMessage(true);
                }
            });

            cancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    sendStatusUpdate(SAFEClient.PROBLEM);
                    setupViewNormal();
                    safeDataModel.setHaveReadMessage(true);
                }
            });


            message.setText(safeDataModel.getCurrentMissionMessage());
        }

        updateSpeed();
        updateBattery();
        updateRange();
        updateOdometer();
    }

    public void updateSpeed() {
        speedNumber.setText(String.valueOf((int) (vehicleDataModel.getVehicleSpeed())));
    }

    public void updateBattery() {
        int batteryLevel = (int) Math.floor(vehicleDataModel.getBatteryLevel());
        batteryPercentage.setText(String.valueOf(batteryLevel) + " %");

        if (batteryLevel <= 20) {
            batteryProgressBar.setProgressDrawable(activity.getResources().getDrawable(R.drawable.battery_progress_bar_red));
            batteryPercentage.setTextColor(Color.parseColor("#FF0000"));
        } else {
            batteryProgressBar.setProgressDrawable(activity.getResources().getDrawable(R.drawable.battery_progress_bar_blue));
            batteryPercentage.setTextColor(Color.parseColor("#000000"));
        }
        batteryProgressBar.setProgress(batteryLevel);
    }

    public void updateRange() {
        int range = (int) Math.floor(vehicleDataModel.getBatteryLevel()) / 2;
        int id = getResources().getIdentifier("combitech.com.againstrumentcluster:drawable/aga_zbee_outer_circle_range_" + range, null, null);
        outerCircle.setImageResource(id);
    }

    public void updateOdometer() {
        long odo = vehicleDataModel.getOdometer();
        odometer.setText(odo + " km");
    }

    public void sendStatusUpdate(final int update) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    safeClient.updateMission(SAFEDataModel.SERVER_ADRESS + "/safe/api/issues/", safeDataModel.getCurrentMissionId(), "/state", update);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}