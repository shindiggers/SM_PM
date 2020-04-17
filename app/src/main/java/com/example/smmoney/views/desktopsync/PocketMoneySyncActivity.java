package com.example.smmoney.views.desktopsync;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.CheckBoxTint;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.transactions.TransactionEditActivity;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Objects;

import static com.example.smmoney.misc.Prefs.getIntPref;

public class PocketMoneySyncActivity extends PocketMoneyActivity {
    private final int DIALOG_FIRST_UDID_CLIENT = 2;
    private final int DIALOG_FIRST_UDID_SERVER = 3;
    private final int DIALOG_NO_HOST = 1;
    private final int DIALOG_OPEN_CONNECTION = 4;
    private final int DIALOG_PURCHASE = 6;
    private final int DIALOG_REPEATINGWARNING = 7;
    private final int DIALOG_RESTORE = 5;
    private final int DIALOG_UPGRADE = 8;
    private final int DIALOG_WIFI = 9;
    private Hashtable addresses;
    private RadioButton clientRadioButton;
    private TextView descriptionTextView;
    private int firstSyncToUDIDAction;
    private EditText ipaddressEditText;
    private TextView ipaddressTextView;
    private String myIP = "";
    private CharSequence[] myIPs = null;
    int photoCount = 0;
    private PocketMoneySyncClass pocketmoneySync;
    private EditText portEditText;
    private boolean programmaticUpdate;
    private ProgressBar progressBar;
    private CheckBox restoreCheckBox;
    private TextView restoreTextView;
    private RadioButton serverRadioButton;
    private ProgressBar spinningWheel;
    private TextView statusTextView;
    private Button syncButton;
    private String syncUdid = "";
    private TextView titleTextView;
    int udidFirstActionBlock = 0;
    private WakeLock wakelock;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync);
        setupViews();
        this.wakelock = ((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).newWakeLock(26, "PocketMoneySyncActivity:DoNotDimScreen");
        Objects.requireNonNull(getSupportActionBar()).setTitle(Locales.kLOC_DESKTOPSYNC_TITLE);
    }

    protected void onResume() {
        super.onResume();
        try {
            displayInfoUpdate(getLocalIpAddresses());
        } catch (SocketException e) {
            showDialog(DIALOG_WIFI/*9*/);
        }
        this.wakelock.acquire(10*60*1000L /*10 minutes*/);
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    protected void onPause() {
        super.onPause();
        this.wakelock.release();
    }

    private void setupViews() {
        View aView = findViewById(R.id.outter);
        this.clientRadioButton = aView.findViewById(R.id.clientbutton);
        this.serverRadioButton = aView.findViewById(R.id.serverbutton);
        aView = aView.findViewById(R.id.addressview);
        this.ipaddressEditText = aView.findViewById(R.id.ipaddress);
        this.ipaddressTextView = aView.findViewById(R.id.ipaddresstextview);
        this.portEditText = aView.findViewById(R.id.port);
        this.descriptionTextView = aView.findViewById(R.id.instructions);
        this.syncButton = aView.findViewById(R.id.syncbutton);
        this.statusTextView = aView.findViewById(R.id.status);
        this.progressBar = aView.findViewById(R.id.progressbar);
        this.spinningWheel = aView.findViewById(R.id.spinningwheel);
        this.restoreTextView = aView.findViewById(R.id.restorelabel);
        this.restoreCheckBox = aView.findViewById(R.id.restorecheckbox);
        CheckBoxTint.colorCheckBox(this.restoreCheckBox);
        this.ipaddressEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.ipaddressTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.ipaddressTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PocketMoneySyncActivity.this.myIPs != null && PocketMoneySyncActivity.this.myIPs.length > 0) {
                    Builder b = new Builder(PocketMoneySyncActivity.this);
                    b.setItems(PocketMoneySyncActivity.this.myIPs, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which < PocketMoneySyncActivity.this.myIPs.length) {
                                PocketMoneySyncActivity.this.myIP = PocketMoneySyncActivity.this.myIPs[which].toString();
                                PocketMoneySyncActivity.this.reloadData();
                            }
                        }
                    });
                    b.create().show();
                }
            }
        });
        ((TextView) aView.findViewById(R.id.portlabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.portEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.descriptionTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.statusTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        ((TextView) aView.findViewById(R.id.iplabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        ((TextView) aView.findViewById(R.id.restorelabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) aView.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.clientRadioButton.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.spinningWheel.setVisibility(View.INVISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
        this.statusTextView.setText("");
        ((RadioGroup) this.clientRadioButton.getParent()).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!PocketMoneySyncActivity.this.programmaticUpdate) {
                    Prefs.setPref(Prefs.PMSYNC_CLIENTSERVER, PocketMoneySyncActivity.this.clientRadioButton.isChecked());
                    if (PocketMoneySyncActivity.this.clientRadioButton.isChecked()) {
                        PocketMoneySyncActivity.this.stopSyncing();
                    } else {
                        PocketMoneySyncActivity.this.startSyncing();
                    }
                    PocketMoneySyncActivity.this.reloadData();
                }
            }
        });
        this.syncButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    if (PocketMoneySyncActivity.this.pocketmoneySync != null) {
                        PocketMoneySyncActivity.this.stopSyncing();
                    } else if (!PocketMoneySyncActivity.this.clientRadioButton.isChecked()) {
                        PocketMoneySyncActivity.this.startSyncing();
                    } else if (PocketMoneySyncActivity.this.restoreCheckBox.isChecked()) {
                        PocketMoneySyncActivity.this.showDialog(DIALOG_RESTORE/*5*/);
                    } else {
                        PocketMoneySyncActivity.this.startSyncing();
                    }
                    PocketMoneySyncActivity.this.reloadData();
                } catch (Exception e) {
                    PocketMoneySyncActivity.this.stopSyncing();
                    Log.e(SMMoney.TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        });
        this.ipaddressEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Prefs.setPref(Prefs.PMSYNC_IP, PocketMoneySyncActivity.this.ipaddressEditText.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        this.portEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try {
                    Prefs.setPref(Prefs.PMSYNC_PORT, Integer.parseInt(PocketMoneySyncActivity.this.portEditText.getText().toString()));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        this.clientRadioButton.setChecked(true);
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        FrameLayout theView = findViewById(R.id.the_tool_bar);
        theView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        theView.setVisibility(View.GONE);
    }

    public void stopSyncing() {
        Database.sqlite3_rollback();
        this.spinningWheel.setVisibility(View.INVISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
        if (this.pocketmoneySync != null) {
            this.pocketmoneySync.disconnect();
        }
        this.pocketmoneySync = null;
        desktopSyncWithState(null, Enums.kDesktopSyncStateDisconnected /*67*/);
        reloadData();
    }

    private void startSyncing() {
        reloadData();
        if (this.clientRadioButton.isChecked()) {
            this.pocketmoneySync = new PocketMoneySyncClientClass();
            this.pocketmoneySync.server = false;
            this.pocketmoneySync.delegate = this;
            this.pocketmoneySync.host = Prefs.getStringPref(Prefs.PMSYNC_IP);
            this.pocketmoneySync.port = getIntPref(Prefs.PMSYNC_PORT);
            this.pocketmoneySync.restoreFromServer = this.restoreCheckBox.isChecked();
            if (this.pocketmoneySync.host == null || this.pocketmoneySync.host.equals("")) {
                showDialog(DIALOG_NO_HOST/*1*/);
                return;
            } else {
                new Thread() {
                    public void run() {
                        ((PocketMoneySyncClientClass) PocketMoneySyncActivity.this.pocketmoneySync).connectToServer();
                    }
                }.start();
                return;
            }
        }
        this.spinningWheel.setVisibility(View.VISIBLE);
        if (this.pocketmoneySync == null) {
            this.pocketmoneySync = new PocketMoneySyncServerClass();
            this.pocketmoneySync.server = true;
            this.pocketmoneySync.delegate = this;
            this.pocketmoneySync.host = this.myIP;
            this.pocketmoneySync.port = getIntPref(Prefs.PMSYNC_PORT);
            this.pocketmoneySync.restoreFromServer = false;
            ((PocketMoneySyncServerClass) this.pocketmoneySync).startServer();
        }
    }

    private void displayInfoUpdate(String ip) {
        if (ip == null) {
            showDialog(DIALOG_WIFI/*9*/);
        } else {
            this.myIP = ip;
        }
        reloadData();
    }

    private void displayInfoUpdate(ArrayList<String> ips) {
        if (ips == null || ips.size() == 0) {
            showDialog(DIALOG_WIFI /*9*/);
        } else {
            this.myIPs = new String[ips.size()];
            for (int i = 0; i < ips.size(); i++) {
                this.myIPs[i] = ips.get(i);
            }
            this.myIP = ips.get(0);
        }
        reloadData();
    }

    private void reloadData() {
        this.portEditText.setText(String.valueOf(getIntPref(Prefs.PMSYNC_PORT)));
        if (this.clientRadioButton.isChecked()) {
            this.syncButton.setText(this.pocketmoneySync == null ? Locales.kLOC_DESKTOPSYNC_SYNC : Locales.kLOC_DESKTOPSYNC_STOP);
            this.ipaddressEditText.setText(Prefs.getStringPref(Prefs.PMSYNC_IP));
            this.ipaddressEditText.setVisibility(View.VISIBLE);
            this.ipaddressTextView.setVisibility(View.INVISIBLE);
            this.descriptionTextView.setText(Locales.kLOC_DESKTOPSYNC_DIRECTIONS);
            this.restoreTextView.setVisibility(View.VISIBLE);
            this.restoreCheckBox.setVisibility(View.VISIBLE);
            return;
        }
        this.syncButton.setText(this.pocketmoneySync == null ? Locales.kLOC_DESKTOPSYNC_START : Locales.kLOC_DESKTOPSYNC_STOP);
        this.ipaddressTextView.setText(this.myIP);
        this.ipaddressEditText.setVisibility(View.INVISIBLE);
        this.ipaddressTextView.setVisibility(View.VISIBLE);
        this.descriptionTextView.setText(Locales.kLOC_SYNCSERVER_DIRECTIONS);
        this.restoreTextView.setVisibility(View.INVISIBLE);
        this.restoreCheckBox.setVisibility(View.INVISIBLE);
    }

    public void desktopSyncComplete(PocketMoneySyncClass pocketMoneySyncClass) {
        this.spinningWheel.setVisibility(View.INVISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
        this.pocketmoneySync = null;
        reloadData();
        if (this.clientRadioButton.isChecked() && Prefs.getBooleanPref(Prefs.RECURPOSTINGENABLED) && !Prefs.getBooleanPref(Prefs.RECURPOSTINGDISABLEDWARNING)) {
            showDialog(DIALOG_REPEATINGWARNING/*7*/);
            Prefs.setPref(Prefs.RECURPOSTINGDISABLEDWARNING, true);
            Prefs.setPref(Prefs.RECURPOSTINGENABLED, false);
        }
    }

    public boolean pocketMoneySyncRequestActionForFirstSyncUDID(PocketMoneySyncClass pmSync, String udid) {
        this.firstSyncToUDIDAction = 0;
        this.syncUdid = udid;
        if (this.clientRadioButton.isChecked()) {
            runOnUiThread(new Runnable() {
                public void run() {
                    PocketMoneySyncActivity.this.showDialog(DIALOG_FIRST_UDID_CLIENT /*2*/);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                public void run() {
                    PocketMoneySyncActivity.this.showDialog(DIALOG_FIRST_UDID_SERVER /*3*/);
                }
            });
        }
        this.udidFirstActionBlock = 1;
        while (this.udidFirstActionBlock == 1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.udidFirstActionBlock == 0;
    }

    public void desktopSyncWithState(final Object object, final int state) {
        runOnUiThread(new Runnable() {
            public void run() {
                PocketMoneySyncActivity.this.desktopSyncWithStateMain(object, state);
            }
        });
    }

    private void desktopSyncWithStateMain(Object object, int state) {
        int i = 18;
        int i2 = 15;
        String status = this.statusTextView.getText().toString();
        boolean isClient = this.clientRadioButton.isChecked();
        switch (state) {
            case Enums.kDesktopSyncStateNone /*0*/:
                status = "";
                break;
            case Enums.kDesktopSyncStateInitialized /*2*/:
                status = Locales.kLOC_DESKTOPSYNC_CLIENTINIT;
                this.progressBar.setProgress(5);
                break;
            case Enums.kDesktopSyncStateConnecting /*3*/:
                status = Locales.kLOC_DESKTOPSYNC_CONNECTINGSERVER;
                this.spinningWheel.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.VISIBLE);
                this.progressBar.setProgress(5);
                break;
            case Enums.kDesktopSyncStateClientConnected /*4*/:
                status = Locales.kLOC_DESKTOPSYNC_WAITINGSERVERID;
                this.progressBar.setVisibility(View.VISIBLE);
                this.progressBar.setProgress(10);
                break;
            case Enums.kDesktopSyncStateServerAuthenticated /*5*/:
                status = Locales.kLOC_DESKTOPSYNC_CONNECTEDSENDCHANGES;
                this.progressBar.setProgress(10);
                break;
            case Enums.kDesktopSyncStateServerInitialized /*6*/:
                status = Locales.kLOC_DESKTOPSYNC_SERVERINIT;
                break;
            case Enums.kDesktopSyncStateServerListening /*7*/:
                status = Locales.kLOC_DESKTOPSYNC_WAITINGCLIENTCONNECT;
                this.spinningWheel.setVisibility(View.VISIBLE);
                this.progressBar.setVisibility(View.INVISIBLE);
                this.progressBar.setProgress(0);
                break;
            case Enums.kDesktopSyncStateServerConnected /*8*/:
                status = Locales.kLOC_DESKTOPSYNC_CONNECTEDSENDSID;
                this.progressBar.setVisibility(View.VISIBLE);
                this.progressBar.setProgress(10);
                break;
            case Enums.kDesktopSyncStateSendingSyncVersion /*9*/:
                status = Locales.kLOC_DESKTOPSYNC_SENDSYNCVERSION;
                this.progressBar.setProgress(15);
                break;
            case Enums.kDesktopSyncStateSentSyncVersion /*10*/:
                status = Locales.kLOC_DESKTOPSYNC_SENTSYNCVERSION;
                ProgressBar progressBar = this.progressBar;
                if (isClient) {
                    i2 = 20;
                }
                progressBar.setProgress(i2);
                break;
            case Enums.kDesktopSyncStateReceivingSyncVersion /*13*/:
                status = Locales.kLOC_DESKTOPSYNC_RECEIVINGSYNCVERSION;
                break;
            case Enums.kDesktopSyncStateSyncVersionReceived /*14*/:
                status = Locales.kLOC_DESKTOPSYNC_RECEIVEDSYNCVERSION;
                ProgressBar progressBar2 = this.progressBar;
                if (!isClient) {
                    i2 = 18;
                }
                progressBar2.setProgress(i2);
                break;
            case Enums.kDesktopSyncStateSyncVersionProcessed /*16*/:
                status = Locales.kLOC_DESKTOPSYNC_SYNCVERSIONPROCESSED;
                ProgressBar progressBar3 = this.progressBar;
                if (!isClient) {
                    i = 20;
                }
                progressBar3.setProgress(i);
                break;
            case Enums.kDesktopSyncStateSendingUDID /*17*/:
                status = Locales.kLOC_DESKTOPSYNC_SENDINGUDID;
                break;
            case Enums.kDesktopSyncStateSentUDID /*18*/:

                status = getString(R.string.kLOC_DESKTOPSYNC_SENTUDID, SMMoney.getID());
                this.progressBar.setProgress(isClient ? 60 : 25);
                break;
            case Enums.kDesktopSyncStateReceivingUDID /*21*/:
                status = Locales.kLOC_DESKTOPSYNC_RECEIVINGUDID;
                break;
            case Enums.kDesktopSyncStateUDIDReceived /*22*/:
                status = Locales.kLOC_DESKTOPSYNC_RECEIVEDUDUD;
                this.progressBar.setProgress(isClient ? 25 : 60);
                break;
            case Enums.kDesktopSyncStateUDIDProcessed /*24*/:
                status = Locales.kLOC_DESKTOPSYNC_SENTRECENTCHANGES;
                this.progressBar.setProgress(isClient ? 27 : 62);
                break;
            case Enums.kDesktopSyncStateSendPhotos /*30*/:
            case Enums.kDesktopSyncStateSendingPhoto /*31*/:
            case Enums.kDesktopSyncStateSentPhoto /*32*/:
            case Enums.kDesktopSyncStateReceivingPhotoHeader /*33*/:
            case Enums.kDesktopSyncStatePhotoHeaderReceived /*34*/:
            case Enums.kDesktopSyncStateReceivingPhoto /*35*/:
            case Enums.kDesktopSyncStatePhotoReceived /*36*/:
            case TransactionEditActivity.REQUEST_PHOTO_OPTION /*37*/:
                status = Locales.kLOC_INPPURCHASES_PHOTOTITLE + " : " + this.photoCount;
                break;
            case Enums.kDesktopSyncStateSendingRecentChanges /*46*/:
                status = Locales.kLOC_DESKTOPSYNC_SENDINGRECENTCHANGES;
                this.progressBar.setProgress(isClient ? 65 : 75);
                break;
            case Enums.kDesktopSyncStateSentRecentChanges /*47*/:
                status = Locales.kLOC_DESKTOPSYNC_RECENTCHANGEDSENT;
                this.progressBar.setProgress(isClient ? 45 : 80);
                break;
            case Enums.kDesktopSyncStateReceivingRecentChanges /*50*/:
                status = Locales.kLOC_DESKTOPSYNC_RECEIVINGRECENTCHANGES;
                this.progressBar.setProgress(isClient ? 30 : 65);
                break;
            case Enums.kDesktopSyncStateRecentChangesReceived /*51*/:
                status = Locales.kLOC_DESKTOPSYNC_RECEIVEDRECENTCHANGES;
                this.progressBar.setProgress(isClient ? 70 : 40);
                break;
            case Enums.kDesktopSyncStateRecentChangesProcessed /*53*/:
                status = Locales.kLOC_DESKTOPSYNC_CHANGESPROCESSED;
                this.progressBar.setProgress(isClient ? 80 : 50);
                break;
            case Enums.kDesktopSyncStateSendingACK /*54*/:
                status = Locales.kLOC_DESKTOPSYNC_SENDINGACK;
                break;
            case Enums.kDesktopSyncStateSentACK /*55*/:
                status = Locales.kLOC_DESKTOPSYNC_SENTACK;
                this.progressBar.setProgress(isClient ? 90 : 55);
                break;
            case Enums.kDesktopSyncStateReceivingACK /*58*/:
                status = Locales.kLOC_DESKTOPSYNC_RECEIVINGACK;
                break;
            case Enums.kDesktopSyncStateACKReceived /*59*/:
                status = Locales.kLOC_DESKTOPSYNC_RECEIVEDACK;
                this.progressBar.setProgress(isClient ? 50 : 85);
                break;
            case Enums.kDesktopSyncStateACKProcessed /*61*/:
                status = Locales.kLOC_DESKTOPSYNC_ACKPROCESSED;
                this.progressBar.setProgress(isClient ? 55 : 90);
                break;
            case Enums.kDesktopSyncStateDisconnected /*67*/:
                status = Locales.kLOC_DESKTOPSYNC_SYNCCOMPLETE;
                this.progressBar.setProgress(100);
                break;
        }
        this.statusTextView.setText(status);
    }

    protected Dialog onCreateDialog(int id) {
        Builder builder;
        switch (id) {
            case DIALOG_NO_HOST /*1*/:
                return new Builder(this).setTitle("Hostname Error").setMessage("Could Not Find Server").setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PocketMoneySyncActivity.this.stopSyncing();
                        dialog.dismiss();
                    }
                }).create();
            case DIALOG_FIRST_UDID_CLIENT /*2*/:
                CharSequence[] items = new CharSequence[]{Locales.kLOC_DESKTOPSYNC_RESTOREFROMSERVER, Locales.kLOC_DESKTOPSYNC_SYNC, Locales.kLOC_GENERAL_CANCEL};
                builder = new Builder(this);
                builder.setTitle(getString(R.string.kLOC_DESKTOPSYNC_UDIDFIRSTSEEN, this.syncUdid));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0 /*0*/:
                                Builder ab = new Builder(PocketMoneySyncActivity.this);
                                ab.setTitle("Are you sure?");
                                ab.setMessage("This will destroy all your SMMoney data on your device. Are you sure you want to overwrite the data on your device with the data from the server?");
                                ab.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        PocketMoneySyncActivity.this.firstSyncToUDIDAction = Enums.kDesktopSyncFirstSyncActionRestoreFromServer/*2*/;
                                        dialog.dismiss();
                                        PocketMoneySyncActivity.this.pocketmoneySync.firstUDIDSyncAction(PocketMoneySyncActivity.this.firstSyncToUDIDAction);
                                    }
                                });
                                ab.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        PocketMoneySyncActivity.this.stopSyncing();
                                        PocketMoneySyncActivity.this.udidFirstActionBlock = -1;
                                    }
                                });
                                ab.create().show();
                                return;
                            case 1 /*1*/:
                                PocketMoneySyncActivity.this.firstSyncToUDIDAction = Enums.kDesktopSyncFirstSyncActionSync/*3*/;
                                break;
                            case 2 /*2*/:
                                PocketMoneySyncActivity.this.firstSyncToUDIDAction = Enums.kDesktopSyncFirstSyncActionNone/*0*/;
                                PocketMoneySyncActivity.this.stopSyncing();
                                PocketMoneySyncActivity.this.udidFirstActionBlock = -1;
                                return;
                        }
                        dialog.dismiss();
                        PocketMoneySyncActivity.this.pocketmoneySync.firstUDIDSyncAction(PocketMoneySyncActivity.this.firstSyncToUDIDAction);
                    }
                });
                return builder.create();
            case DIALOG_FIRST_UDID_SERVER /*3*/:
                CharSequence[] items2 = new CharSequence[]{Locales.kLOC_DESKTOPSYNC_SENDDATA, Locales.kLOC_GENERAL_CANCEL};
                builder = new Builder(this);
                builder.setTitle(getString(R.string.kLOC_DESKTOPSYNC_UDIDFIRSTSEEN, this.syncUdid));
                builder.setItems(items2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0 /*0*/:
                                PocketMoneySyncActivity.this.firstSyncToUDIDAction = Enums.kDesktopSyncFirstSyncActionSync/*3*/;
                                break;
                            case 1 /*1*/:
                                PocketMoneySyncActivity.this.firstSyncToUDIDAction = Enums.kDesktopSyncFirstSyncActionNone/*0*/;
                                PocketMoneySyncActivity.this.pocketmoneySync.reset();
                                PocketMoneySyncActivity.this.udidFirstActionBlock = -1;
                                return;
                        }
                        dialog.dismiss();
                        PocketMoneySyncActivity.this.pocketmoneySync.firstUDIDSyncAction(PocketMoneySyncActivity.this.firstSyncToUDIDAction);
                    }
                });
                return builder.create();
            case DIALOG_OPEN_CONNECTION /*4*/:
                return new Builder(this).setTitle(Locales.kLOC_DESKTOPSYNC_TITLE).setMessage(Locales.kLOC_DESKTOPSYNC_CONNECTIONOPEN).setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
            case DIALOG_RESTORE /*5*/:
                return new Builder(this).setMessage(Locales.KLOC_TOOLS_DELETERESTORE_BODY).setPositiveButton(Locales.KLOC_TOOLS_DELETERESTORE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Builder ab = new Builder(PocketMoneySyncActivity.this);
                        ab.setTitle("Are you sure?");
                        ab.setMessage("This will destroy all your SMMoney data on your device. Are you sure you want to overwrite the data on your device with the data from the server?");
                        ab.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                PocketMoneySyncActivity.this.startSyncing();
                                PocketMoneySyncActivity.this.reloadData();
                            }
                        });
                        ab.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ab.create().show();
                    }
                }).setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
            case DIALOG_PURCHASE /*6*/:
                if (SMMoney.isLiteVersion()) {
                    return new Builder(this).setTitle("SMMoney Sync Server").setMessage("SMMoney Sync Server is not available in the Lite version").setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            PocketMoneySyncActivity.this.clientRadioButton.setChecked(true);
                            dialog.dismiss();
                        }
                    }).create();
                }
                return new Builder(this).setTitle("SMMoney Sync Server").setMessage("SMMoney Sync Server allows you to easily sync to other mobile devices.\nWould you like to buy it?").setPositiveButton(Locales.kLOC_GENERAL_YES, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNegativeButton(Locales.kLOC_GENERAL_NO, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PocketMoneySyncActivity.this.clientRadioButton.setChecked(true);
                        dialog.dismiss();
                    }
                }).create();
            case DIALOG_REPEATINGWARNING /*7*/:
                return new Builder(this).setTitle(Locales.kLOC_REPEATING_TRANSACTIONS).setMessage(Locales.kLOC_REPEATING_TRANSACTIONS_DISABLED).setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
            case DIALOG_UPGRADE /*8*/:
                return new Builder(this).setMessage(Locales.kLOC_DESKTOPSYNC_SYNVVERSIONINCOMPATIBLE).setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
            case DIALOG_WIFI /*9*/:
                return new Builder(this).setTitle(Locales.kLOC_ERROR_NO_NETWORK_CONNECTION_TITLE).setMessage(Locales.kLOC_ERROR_NO_NETWORK_CONNECTION_MSG).setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
            default:
                return null;
        }
    }

    private static ArrayList<String> getLocalIpAddresses() throws SocketException {
        ArrayList<String> ips = new ArrayList<>();
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            Enumeration<InetAddress> enumIpAddr = en.nextElement().getInetAddresses();
            while (enumIpAddr.hasMoreElements()) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress.getClass().equals(Inet4Address.class)) {
                    ips.add(inetAddress.getHostAddress());
                }
            }
        }
        return ips;
    }

    public static String getLocalIpAddress() throws SocketException {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            Enumeration<InetAddress> enumIpAddr = en.nextElement().getInetAddresses();
            while (enumIpAddr.hasMoreElements()) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress.getClass().equals(Inet4Address.class)) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        return null;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.pocketmoneySync != null) {
            stopSyncing();
        }
        return super.onKeyDown(keyCode, event);
    }
}
