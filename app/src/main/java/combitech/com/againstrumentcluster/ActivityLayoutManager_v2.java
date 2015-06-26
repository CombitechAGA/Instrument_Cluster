package combitech.com.againstrumentcluster;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.combitech.safe.SAFEClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ActivityLayoutManager_v2 extends RelativeLayout {

    private BatteryRangeView batteryRangeView;

    private Timer leftBlinkersTimer;
    private Timer rightBlinkersTimer;
    private Timer hazardBlinkersTimer;
    private Timer defrosterTimer;
    private Timer updateGUITimer;

    private ImageView leftBlinkerGlow;
    private ImageView rightBlinkerGlow;
    private ImageView backIcon;
    private ImageView leftBlinker;
    private ImageView rightBlinker;
    private ImageView hazardIcon;
    private ImageView defrosterIcon;
    private ImageView energyGlow;
    private ImageView messageButton;
    private ImageView telltaleHazard;
    private ImageView telltaleDefroster;
    private ImageView telltaleBlinkers;
    private ImageView connectionButton;

    private Button acceptButton;
    private Button cancelButton;
    private Button doneButton;
    private Button issueButton;

    private TextView batteryPercentage;
    private TextView speedNumber;
    private TextView odometer;
    private TextView message;
    private TextView clock;

    private boolean isBlinkingLeft;
    private boolean isBlinkingLeftOn;
    private boolean isBlinkingRight;
    private boolean isBlinkingRightOn;
    private boolean isHazardOn;
    private boolean isMessageView;

    private boolean isDefrosting;
    private boolean isHazard;

    private boolean connected = false;

    private InstrumentClusterActivity activity;

    private VehicleDataModel vehicleDataModel;
    private SAFEDataModel safeDataModel;
    private SAFEClient safeClient;
    private Monitor monitor;

    public ActivityLayoutManager_v2(final InstrumentClusterActivity activity, final VehicleDataModel vehicleDataModel, final SAFEDataModel safeDataModel, final SAFEClient safeClient,Monitor monitor) {
        super(activity);
        this.activity = activity;
        this.vehicleDataModel = vehicleDataModel;
        this.safeDataModel = safeDataModel;
        this.safeClient = safeClient;
        this.monitor=monitor;

        leftBlinkersTimer = new Timer(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBlinkingLeftOn(!isBlinkingLeftOn());
                    }
                });
            }
        }, 750);
        leftBlinkersTimer.setRepeat(true);

        rightBlinkersTimer = new Timer(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBlinkingRightOn(!isBlinkingRightOn());
                    }
                });
            }
        }, 750);
        rightBlinkersTimer.setRepeat(true);

        hazardBlinkersTimer = new Timer(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBlinkingRightOn(!isBlinkingRightOn());
                        setBlinkingLeftOn(!isBlinkingLeftOn());
                        setHazardOn(!isHazardOn());
                    }
                });
            }
        }, 750);

        hazardBlinkersTimer.setRepeat(true);

        defrosterTimer = new Timer(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setDefrosting(false);
                        defrosterIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_defrost_off);
                        telltaleDefroster.setAlpha(0.0f);
                        sendTelematicsData();
                    }
                });
            }
        }, 12000);//Should be 2 minutes (120 000)but set to 12s for testing purposes

        setupViewNormal();

        updateGUITimer = new Timer(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clock.setText(new SimpleDateFormat("HH:mm").format(new Date()).toString());

                        if (safeDataModel.getCurrentMission() == null) {
                            messageButton.setImageResource(R.drawable.aga_zbee_v2_button_left_message_off);
                        } else {
                            if (safeDataModel.haveReadMessage()) {
                                messageButton.setImageResource(R.drawable.aga_zbee_v2_button_left_message_off);
                            } else {
                                messageButton.setImageResource(R.drawable.aga_zbee_v2_button_left_message_new);
                            }
                        }
                    }

                });
            }
        }, 1000);
        updateGUITimer.setRepeat(true);
        updateGUITimer.start();

        setLayoutParams(new ActionBar.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        );
    }

    public void setupViewNormal() {
        System.out.println("Nu visar jag normala vyn");
        activity.setContentView(R.layout.aga_zbee_main_v2);
        isMessageView = false;
        batteryPercentage = (TextView) activity.findViewById(R.id.percentageBattery);
        speedNumber = (TextView) activity.findViewById(R.id.speedNumber);
        odometer = (TextView) activity.findViewById(R.id.tripMeter);
        batteryRangeView = (BatteryRangeView) activity.findViewById(R.id.batteryAndRange);
        energyGlow = (ImageView) activity.findViewById(R.id.energyGlow);
        clock = (TextView) activity.findViewById(R.id.clockCluster);
        clock.setText(new SimpleDateFormat("HH:mm").format(new Date()).toString());

        setupBlinkers();
        setupHazardBlinkers();
        setupDefroster();

        messageButton = (ImageView) activity.findViewById(R.id.messageButton);
        if (safeDataModel.haveReadMessage()) {
            messageButton.setImageResource(R.drawable.aga_zbee_v2_button_left_message_off);
        } else {
            messageButton.setImageResource(R.drawable.aga_zbee_v2_button_left_message_new);
        }
        messageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                setupViewMessage();
            }
        });

        updateSpeed();
        updateBattery();
        updateRange();
        updateOdometer();

        if (connected){
            ImageView connectionButton = (ImageView) activity.findViewById(R.id.connectionButton);
            connectionButton.setImageResource(R.drawable.aga_zbee_v2_button_connection_on);
        }
        connectionButton = (ImageView) activity.findViewById(R.id.connectionButton);
        connectionButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                System.out.println("nu klickade du");
                if(connected){
                    System.out.println("nu vill jag att du disconnectar");
                    connectionButton.setImageResource(R.drawable.aga_zbee_v2_button_connection_off);
                    monitor.doManualDisconnect();
                }
                else{
                    System.out.println("nu vill jag att du connectar");
                    monitor.doManualConnect();
                }

            }
        });
    }

    public void setupViewMessage() {
        isMessageView = true;
        activity.setContentView(R.layout.aga_zbee_message_v2);
        batteryPercentage = (TextView) activity.findViewById(R.id.percentageBattery);
        speedNumber = (TextView) activity.findViewById(R.id.speedNumber);
        batteryRangeView = (BatteryRangeView) activity.findViewById(R.id.batteryAndRange);
        odometer = (TextView) activity.findViewById(R.id.tripMeter);
        energyGlow = (ImageView) activity.findViewById(R.id.energyGlow);

        leftBlinkerGlow = (ImageView) activity.findViewById(R.id.blinkerGlowLeft);
        rightBlinkerGlow = (ImageView) activity.findViewById(R.id.blinkerGlowRight);
        telltaleBlinkers = (ImageView) activity.findViewById(R.id.telltaleBlinker);
        clock = (TextView) activity.findViewById(R.id.clockCluster);
        clock.setText(new SimpleDateFormat("HH:mm").format(new Date()).toString());

        if (isBlinkingRightOn) {
            rightBlinkerGlow.setAlpha(1.0f);
            telltaleBlinkers.setAlpha(1.0f);
        } else {
            rightBlinkerGlow.setAlpha(0.0f);
            telltaleBlinkers.setAlpha(0.0f);
        }

        if (isBlinkingLeftOn) {
            leftBlinkerGlow.setAlpha(1.0f);
            telltaleBlinkers.setAlpha(1.0f);
        } else {
            leftBlinkerGlow.setAlpha(0.0f);
            telltaleBlinkers.setAlpha(0.0f);
        }

        telltaleHazard = (ImageView) activity.findViewById(R.id.telltaleWarning);

        if (isHazardOn()) {
            telltaleHazard.setAlpha(1.0f);
        } else {
            telltaleHazard.setAlpha(0.0f);
        }

        telltaleDefroster = (ImageView) activity.findViewById(R.id.telltaleDefrost);

        if (isDefrosting()) {
            telltaleDefroster.setAlpha(1.0f);
        } else {
            telltaleDefroster.setAlpha(0.0f);
        }


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

    private void setupBlinkers() {

        leftBlinker = (ImageView) activity.findViewById(R.id.blinkerLeft);
        leftBlinkerGlow = (ImageView) activity.findViewById(R.id.blinkerGlowLeft);
        rightBlinker = (ImageView) activity.findViewById(R.id.blinkerRight);
        rightBlinkerGlow = (ImageView) activity.findViewById(R.id.blinkerGlowRight);
        telltaleBlinkers = (ImageView) activity.findViewById(R.id.telltaleBlinker);

        if (isBlinkingRightOn) {
            rightBlinker.setImageResource(R.drawable.aga_zbee_v2_button_right_blinker_on);
            rightBlinkerGlow.setAlpha(1.0f);
            telltaleBlinkers.setAlpha(1.0f);
        } else {
            rightBlinker.setImageResource(R.drawable.aga_zbee_v2_button_right_blinker_off);
            rightBlinkerGlow.setAlpha(0.0f);
            telltaleBlinkers.setAlpha(0.0f);
        }

        if (isBlinkingLeftOn) {
            leftBlinker.setImageResource(R.drawable.aga_zbee_v2_button_left_blinker_on);
            leftBlinkerGlow.setAlpha(1.0f);
            telltaleBlinkers.setAlpha(1.0f);
        } else {
            leftBlinker.setImageResource(R.drawable.aga_zbee_v2_button_left_blinker_off);
            leftBlinkerGlow.setAlpha(0.0f);
            telltaleBlinkers.setAlpha(0.0f);
        }

        leftBlinker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setBlinkingLeft(!isBlinkingLeft());
                if (isBlinkingLeft()) {
                    setBlinkingRightOn(false);
                    setBlinkingRight(false);
                    setHazard(false);
                    setHazardOn(false);
                    hazardBlinkersTimer.stop();
                    rightBlinkersTimer.stop();
                    setBlinkingLeftOn(true);
                    leftBlinkersTimer.start();
                } else {
                    leftBlinkersTimer.stop();
                    setBlinkingLeftOn(false);
                }
                sendTelematicsData();
            }
        });

        rightBlinker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setBlinkingRight(!isBlinkingRight());
                if (isBlinkingRight) {
                    leftBlinkersTimer.stop();
                    setBlinkingLeft(false);
                    setHazard(false);
                    setHazardOn(false);
                    hazardBlinkersTimer.stop();
                    setBlinkingLeftOn(false);
                    setBlinkingRightOn(true);
                    rightBlinkersTimer.start();
                } else {
                    setBlinkingRightOn(false);
                    rightBlinkersTimer.stop();
                }
                sendTelematicsData();
            }
        });

    }

    public void setupHazardBlinkers() {

        hazardIcon = (ImageView) activity.findViewById(R.id.warningButton);
        telltaleHazard = (ImageView) activity.findViewById(R.id.telltaleWarning);

        if (isHazardOn()) {
            hazardIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_warning_on);
            telltaleHazard.setAlpha(1.0f);
        } else {
            hazardIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_warning_off);
            telltaleHazard.setAlpha(0.0f);
        }

        hazardIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setHazard(!isHazard());
                setBlinkingLeft(false);
                setBlinkingRight(false);
                leftBlinkersTimer.stop();
                rightBlinkersTimer.stop();
                System.out.println("Is hazard: "+isHazard);
                if (isHazard()) {
                    setBlinkingRightOn(true);
                    setBlinkingLeftOn(true);
                    setHazardOn(true);
                    hazardBlinkersTimer.start();
                } else {
                    hazardBlinkersTimer.stop();
                    setBlinkingLeftOn(false);
                    setBlinkingRightOn(false);
                    setHazard(false);
                    setHazardOn(false);
                }
                sendTelematicsData();
            }
        });

    }

    public void setupDefroster() {
        defrosterIcon = (ImageView) activity.findViewById(R.id.defrostButton);
        telltaleDefroster = (ImageView) activity.findViewById(R.id.telltaleDefrost);

        if (isDefrosting()) {
            defrosterIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_defrost_on);
            telltaleDefroster.setAlpha(1.0f);
        } else {
            defrosterIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_defrost_off);
            telltaleDefroster.setAlpha(0.0f);
        }

        defrosterIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefrosting(!isDefrosting());
                if (isDefrosting()) {
                    defrosterIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_defrost_on);
                    telltaleDefroster.setAlpha(1.0f);
                    defrosterTimer.start();
                } else {
                    defrosterIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_defrost_off);
                    telltaleDefroster.setAlpha(0.0f);
                    defrosterTimer.stop();
                }
                sendTelematicsData();
            }
        });
    }

    public void sendTelematicsData() {

        Intent intent = new Intent("sendTelematicsData");
        intent.putExtra("isBlinkingRight", isBlinkingRightOn());
        intent.putExtra("isBlinkingLeft", isBlinkingLeftOn());
        intent.putExtra("isDefrosting", isDefrosting());
        intent.putExtra("isHazard", isHazard());
        Log.e("SEND_TELEMATIC", intent.toString());
        activity.sendBroadcast(intent);
    }

    public void updateSpeed() {
        speedNumber.setText(String.valueOf((int) (vehicleDataModel.getVehicleSpeed())));
    }

    public void updateBattery() {
        int batteryLevel = (int) (vehicleDataModel.getBatteryLevel() * 100);
        batteryPercentage.setText(String.valueOf(batteryLevel) + "%");

        if (batteryLevel < 20) {
            batteryPercentage.setBackgroundResource(R.drawable.aga_zbee_v2_battery_empty_red);
            batteryPercentage.setTextColor(Color.parseColor("#FF0000"));

        } else {
            batteryPercentage.setBackgroundResource(R.drawable.aga_zbee_v2_battery_full);
            batteryPercentage.setTextColor(Color.parseColor("#FFFFFF"));
        }

        batteryRangeView.setBatteryPercentageFilled(vehicleDataModel.getBatteryLevel());

        int id = getResources().getIdentifier("combitech.com.againstrumentcluster:drawable/aga_zbee_v2_battery_glow_" + batteryLevel, null, null);
        energyGlow.setImageResource(id);
    }

    public void updateRange() {
        batteryRangeView.setRangePercentageFilled(vehicleDataModel.getBatteryLevel());
    }

    public void updateOdometer() {
        long odo = vehicleDataModel.getOdometer() / 256;
        odometer.setText(String.valueOf(odo) + " km");
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

    public boolean isBlinkingRight() {
        return isBlinkingRight;
    }

    public boolean isBlinkingLeft() {
        return isBlinkingLeft;
    }

    public void setBlinkingLeft(boolean isBlinkingLeft) {
        this.isBlinkingLeft = isBlinkingLeft;
    }

    public void setBlinkingRight(boolean isBlinkingRight) {
        this.isBlinkingRight = isBlinkingRight;
    }

    public boolean isBlinkingLeftOn() {
        return isBlinkingLeftOn;
    }

    public void setBlinkingLeftOn(boolean isBlinkingLeftOn) {
        this.isBlinkingLeftOn = isBlinkingLeftOn;
        if (isBlinkingLeftOn) {
            leftBlinker.setImageResource(R.drawable.aga_zbee_v2_button_left_blinker_on);
            leftBlinkerGlow.setAlpha(1.0f);
            telltaleBlinkers.setAlpha(1.0f);
        } else {
            leftBlinker.setImageResource(R.drawable.aga_zbee_v2_button_left_blinker_off);
            leftBlinkerGlow.setAlpha(0.0f);
            telltaleBlinkers.setAlpha(0.0f);
        }
    }

    public boolean isBlinkingRightOn() {
        return isBlinkingRightOn;
    }

    public void setBlinkingRightOn(boolean isBlinkingRightOn) {
        this.isBlinkingRightOn = isBlinkingRightOn;
        if (isBlinkingRightOn) {
            rightBlinker.setImageResource(R.drawable.aga_zbee_v2_button_right_blinker_on);
            rightBlinkerGlow.setAlpha(1.0f);
            telltaleBlinkers.setAlpha(1.0f);
        } else {
            rightBlinker.setImageResource(R.drawable.aga_zbee_v2_button_right_blinker_off);
            rightBlinkerGlow.setAlpha(0.0f);
            telltaleBlinkers.setAlpha(0.0f);
        }
    }

    public boolean isDefrosting() {
        return isDefrosting;
    }

    public void setDefrosting(boolean isDefrosting) {
        this.isDefrosting = isDefrosting;
    }

    public boolean isHazard() {
        return isHazard;
    }

    public void setHazard(boolean isHazard) {
        this.isHazard = isHazard;
    }

    public boolean isHazardOn() {
        return isHazardOn;
    }

    public void setHazardOn(boolean isHazardOn) {
        this.isHazardOn = isHazardOn;
        if (isHazardOn) {
            hazardIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_warning_on);
            telltaleHazard.setAlpha(1.0f);
        } else {
            hazardIcon.setImageResource(R.drawable.aga_zbee_v2_button_right_warning_off);
            telltaleHazard.setAlpha(0.0f);
        }

    }

    public void setConnectionStatus(boolean status) {
        connected = status;
       // ImageView connectionButton = (ImageView) activity.findViewById(R.id.connectionButton);
        //borde kanske göras på något annat sätt
        if(connectionButton!=null) {
            if (connected) {
                connectionButton.setImageResource(R.drawable.aga_zbee_v2_button_connection_on);
            } else {
                connectionButton.setImageResource(R.drawable.aga_zbee_v2_button_connection_off);
            }
        }
        else{
            System.out.println("Jag var visst null null");
        }
    }
}
