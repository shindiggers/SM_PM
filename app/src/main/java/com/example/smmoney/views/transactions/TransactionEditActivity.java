package com.example.smmoney.views.transactions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.smmoney.SMMoney;
import com.example.smmoney.R;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.NoteEditor;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.misc.TransactionTransferRetVals;
import com.example.smmoney.misc.iReceiptClass;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.ClassNameClass;
import com.example.smmoney.records.IDClass;
import com.example.smmoney.records.PayeeClass;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.BalanceBar;
import com.example.smmoney.views.CurrencyKeyboard;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.exchangerates.ExchangeRateActivity;
import com.example.smmoney.views.lookups.CategoryLookupListActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.repeating.RepeatingEditActivity;
import com.example.smmoney.views.splits.SplitsActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class TransactionEditActivity extends PocketMoneyActivity {
    public static final int REQUEST_PHOTO_OPTION = 37;
    public final int DATE_DIALOG_ID = 1;
    private final int DIALOG_CAMERA = 8;
    private final int DIALOG_DELETECONFIRM = 2;
    private final int DIALOG_DUPLICATE = 3;
    private final int DIALOG_FEE = 4;
    private final int DIALOG_NEED_ACCOUNT = 6;
    private final int DIALOG_NEED_REPEATING = 7;
    private final int EDITTEXT_AMOUNT = 3;
    private final int EDITTEXT_CATEGORY = 2;
    private final int EDITTEXT_CLASS = 5;
    private final int EDITTEXT_ID = 4;
    private final int EDITTEXT_PAYEE = 1;
    private final int MENU_CAMERA = 4;
    private final int MENU_DELETE = 5;
    private final int MENU_DUPE = 2;
    private final int MENU_FEE = 3;
    private final int MENU_SAVE = 6;
    private final int MENU_SPLIT = 1;
    private final int MSG_CLEARDROPDOWNS = 1;
    public final int NOTE_EDIT_BUTTON = 30;
    public final int REQUEST_CAMERA_NEW = 35;
    public final int REQUEST_CAMERA_PICK = 36;
    public final int REQUEST_CURRENCY = 34;
    public final int REQUEST_REPEATING = 32;
    public final int REQUEST_SPLITS = 31;
    public final int REQUEST_TRANSFER = 33;
    private final int TIME_DIALOG_ID = 5;
    private TextView accountTextView;
    private EditText amountEditText;
    private TextView amountXrateTextView;
    private BalanceBar balanceBar;
    private AutoCompleteTextView categoryEditText;
    private TextView categoryTextView;
    private double changeKept;
    private AutoCompleteTextView classEditText;
    private TextView classTextView;
    private final Timer clearTimer = new Timer();
    private CheckBox clearedCheckBox;
    private CurrencyKeyboard currencyKeyboard;
    private Activity currentActivity;
    private int dateChanged;
    private TextView dateTextView;
    private ArrayList<String> deletedImages = new ArrayList();
    private RadioButton depositButton;
    private AutoCompleteTextView idEditText;
    private boolean isIReceipt = false;
    private boolean isLocalNotification = false;
    private TextView keepTheChangeButton;
    private FrameLayout keyboardToolBar;
    private OnDateSetListener mDateSetListener = new OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            TransactionEditActivity.this.dateTextView.setText(CalExt.descriptionWithMediumDate(new GregorianCalendar(year, monthOfYear, dayOfMonth)));
            if ((TransactionEditActivity.this.repeatingDateChangedAlert != null && TransactionEditActivity.this.repeatingDateChangedAlert.isShowing()) || TransactionEditActivity.this.transaction.isRepeatingTransaction || TransactionEditActivity.this.repeatingTransaction == null || !TransactionEditActivity.this.repeatingTransaction.isRepeating()) {
                return;
            }
            if (TransactionEditActivity.this.transaction.transactionID == 0) {
                TransactionEditActivity.this.dateChanged = 2;
                return;
            }
            Builder b = new Builder(TransactionEditActivity.this);
            b.setTitle("Date");
            b.setMessage("Change the date of the transaction and repeating event, or change the date of this transaction only?");
            b.setPositiveButton("Both", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    TransactionEditActivity.this.getCells();
                    if (!(TransactionEditActivity.this.repeatingTransaction.getTransaction() == null || (TransactionEditActivity.this.repeatingTransaction.repeatsOnDate(TransactionEditActivity.this.repeatingTransaction.getTransaction().getDate()) && TransactionEditActivity.this.repeatingTransaction.repeatsOnDate(TransactionEditActivity.this.transaction.getDate())))) {
                        Builder b = new Builder(TransactionEditActivity.this);
                        b.setTitle("");
                        b.setMessage("Please ensure that the start date selected follows the rules of the repeating transaction. If you are changing an existing repeating transaction you may have to update the repeating settings in the repeating edit screen.");
                        b.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        b.create().show();
                    }
                    TransactionEditActivity.this.dateChanged = 2;
                    dialog.dismiss();
                }
            });
            b.setNegativeButton("This item only", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    TransactionEditActivity.this.dateChanged = 1;
                    dialog.dismiss();
                }
            });
            TransactionEditActivity.this.repeatingDateChangedAlert = b.create();
            TransactionEditActivity.this.repeatingDateChangedAlert.show();
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    TransactionEditActivity.this.clearDropDowns();
                    return;
                default:
                    return;
            }
        }
    };
    private OnTimeSetListener mTimeSetListener = new OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            TransactionEditActivity.this.timeTextView.setText(CalExt.descriptionWithShortTime(new GregorianCalendar(0, 0, 0, hourOfDay, minute)));
        }
    };
    private TextView memoTextView;
    private ArrayList<String> newlyAddedImages = new ArrayList();
    private AutoCompleteTextView payeeEditText;
    private TextView payeeLabelTextView;
    private PhotoReceiptsCell photoCell;
    private boolean posting = false;
    private boolean programaticUpdate;
    private int repeatingChanged;
    private AlertDialog repeatingDateChangedAlert = null;
    private ImageView repeatingImageView;
    private RepeatingTransactionClass repeatingTransaction;
    private ScrollView scrollView;
    protected File tempPhotoPath;
    private TextView timeTextView;
    private TextView titleTextView;
    private TransactionClass transaction;
    private RadioButton transferButton;
    private RadioButton withdrawalButton;

    class ClearTask extends TimerTask {
        ClearTask() {
        }

        public void run() {
            TransactionEditActivity.this.mHandler.sendMessage(Message.obtain(TransactionEditActivity.this.mHandler, 1, 0, 0));
        }
    }

    private class MyKeyListener implements KeyListener {
        private int editTextCode;
        KeyListener original;
        private int suggest;

        private MyKeyListener(KeyListener orig, int code) {
            this.original = orig;
            this.editTextCode = code;
            this.suggest = Prefs.getBooleanPref(Prefs.AUTO_SUGGEST) ? this.original.getInputType() : 524288;
        }

        public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
            TransactionEditActivity.this.editTextDidChange(this.editTextCode);
            return this.original.onKeyDown(view, text, keyCode, event);
        }

        public void clearMetaKeyState(View arg0, Editable arg1, int arg2) {
            this.original.clearMetaKeyState(arg0, arg1, arg2);
        }

        public int getInputType() {
            return this.suggest;
        }

        public boolean onKeyOther(View arg0, Editable arg1, KeyEvent arg2) {
            return this.original.onKeyOther(arg0, arg1, arg2);
        }

        public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
            return this.original.onKeyUp(view, text, keyCode, event);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        Uri data = i.getData();
        if (extras != null) {
            boolean z = extras.getBoolean("Posting");
            this.posting = z;
            if (z) {
                this.isLocalNotification = extras.getBoolean("localNotification");
                this.repeatingTransaction = (RepeatingTransactionClass) extras.get("repeatingTransaction");
                this.repeatingTransaction.hydrated = false;
                this.repeatingTransaction.hydratedTransaction = false;
                this.transaction = this.repeatingTransaction.getTransaction();
                if (this.transaction != null) {
                    this.transaction = this.transaction.copy();
                    this.transaction.initType();
                }
                if (this.isLocalNotification) {
                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(this.repeatingTransaction.repeatingID);
                }
                this.currentActivity = this;
                this.dateChanged = 0;
                this.repeatingChanged = 0;
                setContentView(R.layout.transaction_edit);
                setupButtons();
                setTitle(Locales.kLOC_EDIT_TRANSACTION_TITLE);
                this.balanceBar.setVisibility(View.GONE);
                getActionBar().setTitle(Locales.kLOC_EDIT_TRANSACTION_TITLE);
                getActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
            }
        }
        if (data == null) {
            this.transaction = (TransactionClass) getIntent().getExtras().get("Transaction");
            this.transaction.hydrated = true;
            this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
            this.transaction = (TransactionClass) getIntent().getExtras().get("Transaction");
            this.transaction.hydrated = true;
        } else {
            this.isIReceipt = true;
            handleIReceipt(data);
            this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
        }
        this.currentActivity = this;
        this.dateChanged = 0;
        this.repeatingChanged = 0;
        setContentView(R.layout.transaction_edit);
        setupButtons();
        setTitle(Locales.kLOC_EDIT_TRANSACTION_TITLE);
        this.balanceBar.setVisibility(View.GONE);
        getActionBar().setTitle(Locales.kLOC_EDIT_TRANSACTION_TITLE);
        getActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
    }

    public void onResume() {
        super.onResume();
        if (!Prefs.getBooleanPref(Prefs.HINT_EDITTRANSACTION)) {
            Builder alert = new Builder(this);
            alert.setMessage(Locales.kLOC_TIP_EDITTRANSACTION);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Prefs.setPref(Prefs.HINT_EDITTRANSACTION, true);
                    dialog.dismiss();
                }
            });
            alert.show();
        }
        reloadData();
        if (this.balanceBar != null) {
            reloadBalanceBar();
        }
        selectStartingCell();
    }

    public void onDestroy() {
        super.onDestroy();
        this.clearTimer.cancel();
    }

    private void handleIReceipt(Uri data) {
        this.transaction = new iReceiptClass(data).transaction;
        this.transaction.hydrated = true;
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    public void setupButtons() {
        ArrayList<View> theViews = new ArrayList();
        ArrayList<View> selectableViews = new ArrayList();
        this.balanceBar = findViewById(R.id.balancebar);
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.scrollView = findViewById(R.id.scroll_view);
        this.scrollView.setVerticalScrollBarEnabled(false);
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        this.titleTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TransactionEditActivity.this.openOptionsMenu();
            }
        });
        FrameLayout theView = findViewById(R.id.the_tool_bar);
        theView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        theView.setVisibility(View.GONE);
        View outterView = findViewById(R.id.outter_layout);
        View aView = outterView.findViewById(R.id.radiogroup);
        this.withdrawalButton = aView.findViewById(R.id.withdrawalbutton);
        this.depositButton = aView.findViewById(R.id.depositbutton);
        this.transferButton = aView.findViewById(R.id.transferbutton);
        ((RadioGroup) aView).setOnCheckedChangeListener(getRadioChangedListener());
        aView = outterView.findViewById(R.id.datebutton);
        theViews.add(aView);
        aView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TransactionEditActivity.this.showDialog(1);
            }
        });
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        this.dateTextView = aView.findViewById(R.id.datetextview);
        this.dateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.timeTextView = aView.findViewById(R.id.timetextview);
        this.timeTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.timeTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TransactionEditActivity.this.showDialog(5);
            }
        });
        this.repeatingImageView = aView.findViewById(R.id.repeatingimageview);
        this.repeatingImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!AccountsActivity.isLite(TransactionEditActivity.this) || TransactionDB.queryAllRepeatingTransactions().size() < 2) {
                    Intent i = new Intent(TransactionEditActivity.this.currentActivity, RepeatingEditActivity.class);
                    TransactionEditActivity.this.getCells();
                    if (TransactionEditActivity.this.repeatingTransaction.repeatingID == 0) {
                        TransactionEditActivity.this.repeatingTransaction.setTransaction(TransactionEditActivity.this.transaction.copy());
                    }
                    i.putExtra("Transaction", TransactionEditActivity.this.transaction);
                    i.putExtra("RepeatingTransaction", TransactionEditActivity.this.repeatingTransaction);
                    TransactionEditActivity.this.currentActivity.startActivityForResult(i, 32);
                    return;
                }
                AccountsActivity.displayLiteDialog(TransactionEditActivity.this);
            }
        });
        ((TextView) aView.findViewById(R.id.date_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = outterView.findViewById(R.id.accountbutton);
        theViews.add(aView);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        aView.setOnClickListener(getLookupListClickListener());
        aView.setTag(3);
        this.accountTextView = aView.findViewById(R.id.accounttextview);
        ((TextView) outterView.findViewById(R.id.account_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.accountTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        View cView = outterView.findViewById(R.id.categorybutton);
        cView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        cView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TransactionEditActivity.this.transaction.getNumberOfSplits() > 1) {
                    TransactionEditActivity.this.splitsAction();
                    return;
                }
                TransactionEditActivity.this.getCells();
                Intent i = new Intent(TransactionEditActivity.this.currentActivity, CategoryLookupListActivity.class);
                i.putExtra("payee", TransactionEditActivity.this.payeeEditText.getText().toString());
                TransactionEditActivity.this.currentActivity.startActivityForResult(i, 5);
            }
        });
        this.categoryEditText = cView.findViewById(R.id.categoryedittext);
        this.categoryEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.categoryEditText.setThreshold(2);
        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
            this.categoryEditText.setAdapter(new ArrayAdapter(this, R.layout.lookups_category, CategoryClass.allCategoryNamesInDatabase()));
        }
        this.categoryTextView = cView.findViewById(R.id.categorytextview);
        this.categoryTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CATEGORY_FIELD)) {
            cView.setVisibility(View.VISIBLE);
        } else {
            cView.setVisibility(View.GONE);
        }
        this.categoryEditText.setEnabled(Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CATEGORY_FIELD));
        ((TextView) outterView.findViewById(R.id.category_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        View pView = outterView.findViewById(R.id.payeebutton);
        pView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        pView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TransactionEditActivity.this.getCells();
                if (TransactionEditActivity.this.transaction.isTransfer()) {
                    Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
                    i.putExtra("type", 3);
                    TransactionEditActivity.this.currentActivity.startActivityForResult(i, 33);
                    return;
                }
                Intent i = new Intent(TransactionEditActivity.this.currentActivity, CategoryLookupListActivity.class);
                i.putExtra("category", TransactionEditActivity.this.categoryEditText.getText().toString());
                TransactionEditActivity.this.currentActivity.startActivityForResult(i, (Integer) view.getTag());
            }
        });
        pView.setTag(4);
        this.payeeEditText = pView.findViewById(R.id.payeetextview);
        this.payeeEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.payeeEditText.setThreshold(2);
        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
            this.payeeEditText.setAdapter(new ArrayAdapter(this, R.layout.lookups_category, PayeeClass.allPayeesInDatabase()));
        }
        this.payeeLabelTextView = pView.findViewById(R.id.payeelabeltextview);
        this.payeeLabelTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_CATBYPAYEE_FIELD)) {
            ViewGroup parent = (ViewGroup) cView.getParent();
            int index = parent.indexOfChild(pView);
            parent.removeView(cView);
            parent.addView(cView, index);
            if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CATEGORY_FIELD)) {
                selectableViews.add(this.categoryEditText);
                theViews.add(cView);
            }
            selectableViews.add(this.payeeEditText);
            theViews.add(pView);
        } else {
            theViews.add(pView);
            selectableViews.add(this.payeeEditText);
            if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CATEGORY_FIELD)) {
                selectableViews.add(this.categoryEditText);
                theViews.add(cView);
            }
        }
        this.amountEditText = outterView.findViewById(R.id.amountedittext);
        this.amountEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        selectableViews.add(this.amountEditText);
        this.currencyKeyboard = findViewById(R.id.keyboardView);
        this.currencyKeyboard.setEditText(this.amountEditText, new Runnable() {
            public void run() {
                if (TransactionEditActivity.this.amountEditText.hasFocus()) {
                    TransactionEditActivity.this.clearKeepTheChange();
                    AccountClass act = AccountDB.recordFor(TransactionEditActivity.this.transaction.getAccount());
                    boolean keepTheChangeEnabled = act != null && act.getKeepTheChangeAccount() != null && act.getKeepTheChangeAccount().length() > 0 && (TransactionEditActivity.this.transaction.getType() == 0 || TransactionEditActivity.this.transaction.getType() == 2);
                    TransactionEditActivity.this.currencyKeyboard.setToolbarEnabled(keepTheChangeEnabled);
                    return;
                }
                TransactionEditActivity.this.editTextDidFinishChanging(3);
            }
        });
        this.amountXrateTextView = outterView.findViewById(R.id.amount_xrate_text_view);
        this.amountXrateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        aView = (View) this.amountEditText.getParent();
        theViews.add(aView);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            aView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TransactionEditActivity.this.getCells();
                    Intent i = new Intent(TransactionEditActivity.this.currentActivity, ExchangeRateActivity.class);
                    i.putExtra("transaction", TransactionEditActivity.this.transaction);
                    try {
                        i.putExtra("split", TransactionEditActivity.this.transaction.getSplits().get(0));
                    } catch (NullPointerException e) {
                    }
                    TransactionEditActivity.this.currentActivity.startActivityForResult(i, 34);
                }
            });
        } else {
            outterView.findViewById(R.id.amount_currency_button).setVisibility(View.GONE);
            this.amountXrateTextView.setVisibility(View.GONE);
        }
        ((TextView) outterView.findViewById(R.id.amount_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = outterView.findViewById(R.id.idbutton);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        aView.setOnClickListener(getLookupListClickListener());
        aView.setTag(7);
        this.idEditText = aView.findViewById(R.id.idedittext);
        selectableViews.add(this.idEditText);
        this.idEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.idEditText.setThreshold(2);
        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
            this.idEditText.setAdapter(new ArrayAdapter(this, R.layout.lookups_category, IDClass.allCategoriesInDatabase()));
        }
        if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_ID_FIELD)) {
            aView.setVisibility(View.VISIBLE);
            theViews.add(aView);
        } else {
            aView.setVisibility(View.GONE);
        }
        this.idEditText.setEnabled(Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_ID_FIELD));
        ((TextView) outterView.findViewById(R.id.id_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.clearedCheckBox = outterView.findViewById(R.id.clearedcheckbox);
        this.clearedCheckBox.setButtonDrawable(Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android"));
        aView = (View) this.clearedCheckBox.getParent();
        if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CLEARED_FIELD)) {
            aView.setVisibility(View.VISIBLE);
            theViews.add(aView);
        } else {
            aView.setVisibility(View.GONE);
        }
        this.clearedCheckBox.setEnabled(Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CLEARED_FIELD));
        ((TextView) outterView.findViewById(R.id.cleared_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = outterView.findViewById(R.id.classbutton);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        aView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TransactionEditActivity.this.transaction.getNumberOfSplits() > 1) {
                    TransactionEditActivity.this.splitsAction();
                    return;
                }
                TransactionEditActivity.this.getCells();
                Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
                i.putExtra("type", 6);
                TransactionEditActivity.this.currentActivity.startActivityForResult(i, 6);
            }
        });
        this.classEditText = aView.findViewById(R.id.classedittext);
        selectableViews.add(this.classEditText);
        this.classEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.classEditText.setThreshold(2);
        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
            this.classEditText.setAdapter(new ArrayAdapter(this, R.layout.lookups_category, ClassNameClass.allClassNamesInDatabase()));
        }
        this.classTextView = aView.findViewById(R.id.classtextview);
        this.classTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CLASS_FIELD)) {
            aView.setVisibility(View.VISIBLE);
            theViews.add(aView);
        } else {
            aView.setVisibility(View.GONE);
        }
        this.classEditText.setEnabled(Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CLASS_FIELD));
        ((TextView) outterView.findViewById(R.id.class_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = outterView.findViewById(R.id.memobutton);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        aView.setOnClickListener(getBtnClickListener());
        aView.setTag(30);
        this.memoTextView = aView.findViewById(R.id.memotextview);
        this.memoTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_MEMO_FIELD)) {
            aView.setVisibility(View.VISIBLE);
            theViews.add(aView);
        } else {
            aView.setVisibility(View.GONE);
        }
        ((TextView) outterView.findViewById(R.id.memo_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        int i = 0;
        Iterator it = theViews.iterator();
        while (it.hasNext()) {
            ((View) it.next()).setBackgroundResource(i % 2 == 0 ? PocketMoneyThemes.primaryRowSelector() : PocketMoneyThemes.alternatingRowSelector());
            i++;
        }
        for (i = 1; i < selectableViews.size(); i++) {
            View previousView = selectableViews.get(i - 1);
            View currentView = selectableViews.get(i);
            previousView.setNextFocusDownId(currentView.getId());
            currentView.setNextFocusUpId(previousView.getId());
        }
        this.photoCell = findViewById(R.id.photocell);
        this.photoCell.setBackgroundColor(PocketMoneyThemes.alternatingRowColor());
        TextView button = findViewById(R.id.save_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TransactionEditActivity.this.getCells();
                TransactionEditActivity.this.deleteDeletedImages();
                if (TransactionEditActivity.this.accountTextView.getText() == null || TransactionEditActivity.this.accountTextView.getText().toString().length() == 0) {
                    TransactionEditActivity.this.showDialog(6);
                } else if (TransactionEditActivity.this.transaction.isRepeatingTransaction && (TransactionEditActivity.this.repeatingTransaction == null || !TransactionEditActivity.this.repeatingTransaction.isRepeating())) {
                    TransactionEditActivity.this.showDialog(7);
                } else if (TransactionEditActivity.this.transaction.transactionID == 0 || TransactionEditActivity.this.transaction.isRepeatingTransaction || TransactionEditActivity.this.repeatingTransaction.repeatingID <= 0 || !TransactionEditActivity.this.repeatingTransaction.isRepeating() || TransactionEditActivity.this.dateChanged != 0) {
                    TransactionEditActivity.this.saveAction();
                } else {
                    Builder b = new Builder(TransactionEditActivity.this);
                    b.setTitle("Edit");
                    b.setMessage("Change the information of the transaction and repeating event, or change the information of only this transaction?");
                    b.setPositiveButton("Both", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            TransactionEditActivity.this.repeatingChanged = 2;
                            TransactionEditActivity.this.saveAction();
                        }
                    });
                    b.setNegativeButton("This item only", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            TransactionEditActivity.this.repeatingChanged = 1;
                            TransactionEditActivity.this.saveAction();
                        }
                    });
                    b.create().show();
                }
            }
        });
        button = findViewById(R.id.cancel_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TransactionEditActivity.this.deleteNewlyAddedImages();
                TransactionEditActivity.this.finish();
            }
        });
        this.keyboardToolBar = findViewById(R.id.keyboard_toolbar);
        this.keyboardToolBar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.currencyKeyboard.setToolbarView(this.keyboardToolBar);
        this.keepTheChangeButton = findViewById(R.id.keep_the_change_toolbar_button);
        this.keepTheChangeButton.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        this.keepTheChangeButton.setTextColor(-1);
        this.keepTheChangeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TransactionEditActivity.this.keepTheChange();
            }
        });
        this.payeeEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(1));
        this.categoryEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(2));
        this.idEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(4));
        this.classEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(5));
        this.payeeEditText.setKeyListener(new MyKeyListener(this.payeeEditText.getKeyListener(), 1));
        this.categoryEditText.setKeyListener(new MyKeyListener(this.categoryEditText.getKeyListener(), 2));
        this.idEditText.setKeyListener(new MyKeyListener(this.idEditText.getKeyListener(), 4));
        this.classEditText.setKeyListener(new MyKeyListener(this.classEditText.getKeyListener(), 5));
    }

    private void saveAction() {
        editTextDidFinishChanging(3);
        if (save()) {
            if (!this.isIReceipt && Prefs.getBooleanPref(Prefs.EMAILPARTNER_ENABLED)) {
                sendEmail();
            }
            finish();
        }
    }

    private void sendEmail() {
        iReceiptClass receipt = new iReceiptClass(this.transaction);
        StringBuilder sb = new StringBuilder();
        sb.append(this.transaction.getAccount());
        sb.append("&rarr;");
        String transferToAccount = this.transaction.isTransfer() ? this.transaction.getTransferToAccount() == null ? "" : this.transaction.getTransferToAccount() : this.transaction.getPayee() == null ? "" : this.transaction.getPayee();
        sb.append(transferToAccount);
        sb.append("&rarr;");
        sb.append(CurrencyExt.amountAsString(this.transaction.getSubTotal()));
        sb.append("<p><p><a href=\"");
        sb.append(receipt.postString());
        sb.append("\" style=\"color: #000001;\">");
        sb.append(receipt.postString());
        sb.append("</a><p>");
        sb.append(Locales.kLOC_EMAILPARTNEROPTIONS_EMAILBODY);
        String[] emails = new String[]{Prefs.getStringPref(Prefs.EMAILPARTNER_EMAIL)};
        Intent emailIntent = new Intent("android.intent.action.SEND");
        emailIntent.setType("text/html");
        emailIntent.putExtra("android.intent.extra.EMAIL", emails);
        emailIntent.putExtra("android.intent.extra.SUBJECT", Locales.kLOC_EMAILPARTNEROPTIONS_EMAILSUBJECT);
        emailIntent.putExtra("android.intent.extra.TEXT", Html.fromHtml(sb.toString()));
        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            Log.i(getPackageName(), "phone doesnt support email intents");
        }
    }

    private void selectStartingCell() {
        clearFocus();
        if (this.transaction.getTransactionID() == 0) {
            String thePref = Prefs.getStringPref(Prefs.EDITTRANSACTION_STARTING_FIELD);
            InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (thePref.equals(Locales.kLOC_GENERAL_PAYEE)) {
                this.payeeEditText.requestFocus();
                mgr.showSoftInput(this.payeeEditText, 1);
            } else if (thePref.equals(Locales.kLOC_GENERAL_CATEGORY)) {
                this.categoryEditText.requestFocus();
                mgr.showSoftInput(this.categoryEditText, 1);
            } else if (thePref.equals(Locales.kLOC_GENERAL_AMOUNT)) {
                this.amountEditText.requestFocus();
                mgr.showSoftInput(this.amountEditText, 1);
            } else {
                getWindow().setSoftInputMode(3);
            }
        } else {
            getWindow().setSoftInputMode(3);
        }
        clearDropDownsTimerStart();
    }

    private void clearFocus() {
        this.withdrawalButton.clearFocus();
        this.depositButton.clearFocus();
        this.transferButton.clearFocus();
        this.dateTextView.clearFocus();
        this.repeatingImageView.clearFocus();
        this.accountTextView.clearFocus();
        this.payeeEditText.clearFocus();
        this.payeeLabelTextView.clearFocus();
        this.categoryEditText.clearFocus();
        this.categoryTextView.clearFocus();
        this.amountEditText.clearFocus();
        this.idEditText.clearFocus();
        this.clearedCheckBox.clearFocus();
        this.classEditText.clearFocus();
        this.classTextView.clearFocus();
        this.memoTextView.clearFocus();
    }

    public void loadCells() {
        this.programaticUpdate = true;
        setType();
        this.programaticUpdate = false;
        if (this.repeatingTransaction == null || !this.repeatingTransaction.isRepeating()) {
            this.dateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        } else {
            this.dateTextView.setTextColor(PocketMoneyThemes.orangeLabelColor());
        }
        this.dateTextView.setText(CalExt.descriptionWithMediumDate(this.transaction.getDate()));
        if (Prefs.getBooleanPref(Prefs.SHOWTIME)) {
            this.timeTextView.setVisibility(View.VISIBLE);
            this.timeTextView.setText(CalExt.descriptionWithShortTime(this.transaction.getDate()));
        } else {
            this.timeTextView.setVisibility(View.GONE);
        }
        this.accountTextView.setText(this.transaction.getAccount());
        if (this.transaction.getType() == 3 || this.transaction.getType() == 2) {
            this.payeeEditText.setText(this.transaction.getTransferToAccount());
        } else {
            this.payeeEditText.setText(this.transaction.getPayee());
        }
        loadAmountXrateValues();
        updateAmountFieldTextColor();
        this.idEditText.setText(this.transaction.getCheckNumber());
        this.clearedCheckBox.setChecked(this.transaction.getCleared());
        setNotesText(this.transaction.getMemo());
        if (this.transaction.getNumberOfSplits() > 1) {
            this.categoryEditText.setVisibility(View.GONE);
            this.categoryEditText.setEnabled(false);
            this.categoryTextView.setVisibility(View.VISIBLE);
            this.categoryTextView.setText(Locales.kLOC_GENERAL_SPLITS);
        } else {
            this.categoryEditText.setText(this.transaction.getCategory());
            this.categoryEditText.setEnabled(true);
            this.categoryEditText.setVisibility(View.VISIBLE);
            this.categoryTextView.setVisibility(View.GONE);
        }
        if (this.transaction.multipleClassNames()) {
            this.classEditText.setVisibility(View.GONE);
            this.classTextView.setVisibility(View.VISIBLE);
            this.classTextView.setText(Locales.kLOC_GENERAL_SPLITS);
        } else {
            this.classEditText.setText(this.transaction.getClassName());
            this.classEditText.setVisibility(View.VISIBLE);
            this.classTextView.setVisibility(View.GONE);
        }
        this.photoCell.setImageLocationString(this.transaction.getImageLocation());
        this.photoCell.requestLayout();
    }

    public void getCells() {
        this.transaction.hydrate();
        saveAmountXrates();
        this.transaction.setDateFromString(this.dateTextView.getText().toString());
        if (Prefs.getBooleanPref(Prefs.SHOWTIME)) {
            this.transaction.updateDateWithTimeString(this.timeTextView.getText().toString());
        }
        this.transaction.setAccount(this.accountTextView.getText().toString());
        if (this.transaction.getType() == 3 || this.transaction.getType() == 2) {
            this.transaction.setPayee("");
            this.transaction.setTransferToAccount(this.payeeEditText.getText().toString());
        } else {
            this.transaction.setTransferToAccount("");
            this.transaction.setPayee(this.payeeEditText.getText().toString());
        }
        if (this.transaction.getNumberOfSplits() == 1) {
            this.transaction.setCategory(this.categoryEditText.getText().toString());
        }
        this.transaction.setCheckNumber(this.idEditText.getText().toString());
        this.transaction.setCleared(this.clearedCheckBox.isChecked());
        if (!this.transaction.multipleClassNames()) {
            this.transaction.setClassName(this.classEditText.getText().toString());
        }
    }

    private void deleteDeletedImages() {
        Iterator it = this.deletedImages.iterator();
        while (it.hasNext()) {
            if (!new File(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/", (String) it.next()).delete()) {
                int hmm = 0 + 1;
            }
        }
    }

    private void deleteNewlyAddedImages() {
        Iterator it = this.newlyAddedImages.iterator();
        while (it.hasNext()) {
            if (!new File(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/", new StringBuilder(String.valueOf(it.next())).append(".jpg").toString()).delete()) {
                int hmm = 0 + 1;
            }
        }
    }

    public boolean save() {
        if (this.isIReceipt) {
            this.transaction.checkAccountAddIfMissing();
        }
        getCells();
        if (!this.transaction.isRepeatingTransaction && this.transaction.getDirty()) {
            TransactionClass originalRecord = new TransactionClass(this.transaction.getTransactionID());
            int modTransfer = useCanModifyChildTransferOfBasedOn(this.transaction, originalRecord);
            if (modTransfer == 0) {
                AlertDialog alert = new Builder(this).create();
                alert.setMessage(Locales.kLOC_EDIT_TRANSACTION_EDITOTHEREND);
                alert.setCancelable(false);
                alert.setButton(-1, Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                return false;
            }
            keepTheChangeUpdate();
            this.transaction.saveToDatabase();
            if (modTransfer == 2) {
                saveUpdateTransferFromOriginalRecord(originalRecord, this.transaction);
            }
            if (Prefs.getBooleanPref(Prefs.AUTOADD_LOOKUPS)) {
                Database.autoAddLookupItemsFromTransaction(this.transaction);
            }
        }
        if (!this.posting) {
            saveRepeatingTransaction();
        } else if (this.isLocalNotification) {
            TransactionDB.skipTransactionToDate(this.repeatingTransaction, this.repeatingTransaction.getTransaction().getDate());
        } else {
            this.repeatingTransaction.deleteNotification(getApplicationContext());
            this.repeatingTransaction.postAndAdvanceTransaction(this.transaction.getSubTotal(), this.transaction.getDate());
            this.repeatingTransaction.saveToDatabase();
        }
        return true;
    }

    private void saveRepeatingTransaction() {
        boolean processRepeatingEvents = false;
        if (this.repeatingTransaction.repeatingID == 0) {
            if (this.transaction.isRepeatingTransaction) {
                this.repeatingTransaction.setLastProcessedDate(CalExt.subtractDay(this.transaction.getDate()));
            } else {
                this.repeatingTransaction.setLastProcessedDate(this.transaction.getDate());
            }
            processRepeatingEvents = true;
        } else {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(this.repeatingTransaction.repeatingID);
        }
        if (!this.repeatingTransaction.isRepeating()) {
            if (this.repeatingTransaction.getTransaction() != null) {
                this.repeatingTransaction.getTransaction().deleteFromDatabase();
            }
            this.repeatingTransaction.deleteFromDatabase();
        } else if (this.repeatingChanged != 1 && this.dateChanged != 1) {
            GregorianCalendar originalDate = (GregorianCalendar) this.repeatingTransaction.getTransaction().getDate().clone();
            this.repeatingTransaction.getTransaction().deleteFromDatabase();
            this.repeatingTransaction.setTransaction(null);
            this.repeatingTransaction.setTransaction(this.transaction.copy());
            this.repeatingTransaction.dirty = true;
            if (1 == this.dateChanged || (!this.transaction.isRepeatingTransaction && this.dateChanged == 0)) {
                this.repeatingTransaction.getTransaction().setDate(originalDate);
            } else if (this.transaction.isRepeatingTransaction) {
                this.repeatingTransaction.setLastProcessedDate(CalExt.subtractDay(this.repeatingTransaction.getTransaction().getDate()));
            }
            this.repeatingTransaction.saveToDatabase();
            if (processRepeatingEvents) {
                TransactionDB.addRepeatingTransactions();
            }
        }
    }

    private void reloadData() {
        loadCells();
    }

    private View.OnClickListener getBalanceBarClickListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                int i = Prefs.getIntPref(Prefs.BALANCETYPE);
                if (v.equals(TransactionEditActivity.this.balanceBar.nextButton)) {
                    i = TransactionEditActivity.this.balanceBar.nextBalanceTypeAfter(i);
                } else {
                    i = TransactionEditActivity.this.balanceBar.nextBalanceTypeBefore(i);
                }
                Prefs.setPref(Prefs.BALANCETYPE, i);
                TransactionEditActivity.this.reloadBalanceBar();
                TransactionEditActivity.this.reloadData();
            }
        };
    }

    public void reloadBalanceBar() {
        if (this.transaction.isRepeatingTransaction) {
            this.balanceBar.setVisibility(View.GONE);
            return;
        }
        AccountClass account;
        if (this.accountTextView.getText().toString() == null || this.accountTextView.getText().toString().length() <= 0) {
            account = AccountDB.recordFor(this.transaction.getAccount());
        } else {
            account = AccountDB.recordFor(this.accountTextView.getText().toString());
        }
        int balanceType = Prefs.getIntPref(Prefs.BALANCETYPE);
        if (account != null) {
            this.balanceBar.balanceAmountTextView.setText(account.formatAmountAsCurrency(account.balanceOfType(balanceType)));
        }
        if (account == null || !account.balanceExceedsLimit()) {
            this.balanceBar.balanceAmountTextView.setTextColor(-1);
        } else {
            this.balanceBar.balanceAmountTextView.setTextColor(-65536);
        }
        this.balanceBar.balanceTypeTextView.setText(AccountDB.totalWorthLabel(balanceType));
        this.balanceBar.balanceTypeTextView.setTextColor(-1);
    }

    private void editTransactionDelete() {
        if (this.transaction.isRepeatingTransaction) {
            if (this.repeatingTransaction.getTransaction() != null) {
                this.repeatingTransaction.getTransaction().deleteFromDatabase();
            }
            this.repeatingTransaction.deleteFromDatabase();
            this.transaction.deleteFromDatabase();
            return;
        }
        this.transaction.transactionDelete();
    }

    private void clearKeepTheChange() {
        if (this.changeKept != 0.0d) {
            AccountClass act = AccountDB.recordFor(this.transaction.getAccount());
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(CurrencyExt.amountFromStringWithCurrency(this.amountEditText.getText().toString(), act.getCurrencyCode()) - this.changeKept, act.getCurrencyCode()));
            this.changeKept = 0.0d;
        }
    }

    private void keepTheChange() {
        AccountClass act = AccountDB.recordFor(this.transaction.getAccount());
        String amountString = this.amountEditText.getText().toString();
        if (amountString.length() > 0) {
            double keepTheChange = act.getKeepChangeRoundTo();
            double initialAmount = CurrencyExt.amountFromStringWithCurrency(this.currencyKeyboard.processMath(amountString), act.getCurrencyCode());
            double newBal = keepTheChange - (initialAmount % keepTheChange);
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(newBal + initialAmount, act.getCurrencyCode()));
            this.changeKept = newBal;
            this.currencyKeyboard.hide();
        }
    }

    private void keepTheChangeUpdate() {
        if (this.changeKept != 0.0d) {
            AccountClass account = AccountDB.recordFor(this.transaction.getAccount());
            AccountClass ktcAccount = AccountDB.recordFor(account.getKeepTheChangeAccount());
            TransactionClass keepTheChangeRecord = new TransactionClass();
            keepTheChangeRecord.setAccount(this.transaction.getAccount());
            if (ktcAccount != null) {
                keepTheChangeRecord.setTransferToAccount(account.getKeepTheChangeAccount());
            } else {
                keepTheChangeRecord.setPayee(account.getKeepTheChangeAccount());
            }
            GregorianCalendar greg = new GregorianCalendar();
            greg.setTimeInMillis(this.transaction.getDate().getTimeInMillis() + 1000);
            keepTheChangeRecord.setDate(greg);
            keepTheChangeRecord.setSubTotal(this.changeKept * -1.0d);
            keepTheChangeRecord.setAmount(this.changeKept * -1.0d);
            keepTheChangeRecord.setCurrencyCode(account.getCurrencyCode());
            keepTheChangeRecord.setCategory(Locales.kLOC_GENERAL_KEEP_CHANGE);
            keepTheChangeRecord.setType(3);
            keepTheChangeRecord.setClassName(this.transaction.getClassName());
            keepTheChangeRecord.initType();
            keepTheChangeRecord.saveToDatabase();
            if (ktcAccount != null) {
                saveUpdateTransferFromOriginalRecord(new TransactionClass(0), keepTheChangeRecord);
            }
            this.transaction.setSubTotal(this.transaction.getSubTotal() + this.changeKept);
            if (this.transaction.getSplits().size() == 1) {
                this.transaction.setAmount(this.transaction.getAmount() + this.changeKept);
            }
            this.changeKept = 0.0d;
        }
    }

    private void splitsAction() {
        getCells();
        Intent i = new Intent(this, SplitsActivity.class);
        i.putExtra("Transaction", this.transaction);
        startActivityForResult(i, 31);
    }

    private int getType() {
        if (this.withdrawalButton.isChecked()) {
            return 0;
        }
        if (this.depositButton.isChecked()) {
            return 1;
        }
        if (this.transferButton.isChecked()) {
            return 3;
        }
        return 0;
    }

    private void setNotesText(String note) {
        int i = 25;
        if (note == null || note.length() <= 0) {
            this.memoTextView.setText("");
            return;
        }
        TextView textView = this.memoTextView;
        if (25 > note.length()) {
            i = note.length();
        }
        textView.setText(note.substring(0, i));
    }

    private void setType() {
        if (this.transaction.isWithdrawal()) {
            this.payeeEditText.setEnabled(true);
            this.withdrawalButton.setChecked(true);
            this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TO);
        } else if (this.transaction.isDeposit()) {
            this.payeeEditText.setEnabled(true);
            this.depositButton.setChecked(true);
            this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_FROM);
        } else if (this.transaction.isTransfer()) {
            this.payeeEditText.setEnabled(false);
            this.transferButton.setChecked(true);
            if (this.transaction.getType() == 3) {
                this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_FROM);
            } else {
                this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_TO);
            }
        }
    }

    private void includeFeeAction() {
        AccountClass account = AccountDB.recordFor(this.accountTextView.getText().toString());
        if (account.getFee() <= 0.0d) {
            showDialog(4);
            return;
        }
        save();
        TransactionClass fee = new TransactionClass();
        fee.setAccount(account.getAccount());
        fee.setDate(CalExt.addSecond(this.transaction.getDate()));
        fee.initType();
        fee.setSubTotal(account.getFee() * -1.0d);
        fee.setAmount(account.getFee() * -1.0d);
        fee.setCurrencyCode(account.getCurrencyCode() == null ? Prefs.getStringPref(Prefs.HOMECURRENCYCODE) : account.getCurrencyCode());
        if (account.getInstitution() == null || account.getInstitution().length() <= 0) {
            fee.setPayee(this.transaction.getAccount());
        } else {
            fee.setPayee(account.getInstitution());
        }
        fee.setCleared(this.transaction.getCleared());
        fee.setClassName(this.transaction.getClassName());
        fee.setCategory(Locales.kLOC_FEE_CATEGORY);
        Intent i = new Intent(this, TransactionEditActivity.class);
        i.putExtra("Transaction", i);
    }

    private void deleteConfirmed() {
        editTransactionDelete();
        finish();
    }

    private void duplicateTransaction(boolean todaysDate) {
        TransactionClass dup = this.transaction.copy();
        dup.setCleared(false);
        dup.setOfxID("");
        if (todaysDate) {
            dup.setDate(new GregorianCalendar());
        }
        Intent i = new Intent(this, TransactionEditActivity.class);
        i.putExtra("Transaction", dup);
        startActivity(i);
        finish();
    }

    public void updateAmountFieldTextColor() {
        if (this.transaction.getType() == 0 || this.transaction.getType() == 2) {
            this.amountEditText.setTextColor(-65536);
        } else {
            this.amountEditText.setTextColor(-16711936);
        }
    }

    public void updateXrates() {
        getCells();
        if (this.transaction.isTransfer() && Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            AccountClass a1 = AccountDB.recordFor(this.transaction.getTransferToAccount());
            this.transaction.setXrate(xrateFromAccountToAccount(this.transaction.getAccount(), a1.getAccount()));
            this.transaction.setCurrencyCode(a1.getCurrencyCode());
            this.amountXrateTextView.setText("x" + this.transaction.getXrate());
            if (this.transaction.getSubTotal() == 0.0d) {
                this.amountEditText.setText("");
            } else {
                this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal() / this.transaction.getXrate()), this.transaction.getCurrencyCode()));
            }
            this.amountEditText.invalidate();
            this.amountXrateTextView.setVisibility(View.VISIBLE);
            this.amountXrateTextView.invalidate();
        }
    }

    public void loadAmountXrateValues() {
        try {
            if (AccountDB.recordFor(this.transaction.getAccount()).getCurrencyCode().equals(this.transaction.getCurrencyCode())) {
                this.amountXrateTextView.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            this.amountXrateTextView.setVisibility(View.GONE);
        }
        if (this.transaction.getSubTotal() == 0.0d) {
            this.amountEditText.setText("");
        } else if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal() / this.transaction.getXrate()), this.transaction.getCurrencyCode()));
            this.amountXrateTextView.setVisibility(View.VISIBLE);
            this.amountXrateTextView.setText("x" + CurrencyExt.exchangeRateAsString(this.transaction.getXrate()));
        } else {
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal())));
        }
    }

    public void saveAmountXrates() {
        double amount = CurrencyExt.amountFromStringWithCurrency(this.amountEditText.getText().toString(), this.transaction.getCurrencyCode());
        double multiplier = 1.0d;
        if (this.transaction.getType() == 0 || this.transaction.getType() == 2) {
            multiplier = -1.0d;
        }
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            multiplier *= this.transaction.getXrate();
        }
        this.transaction.setSubTotal(Math.abs(amount) * multiplier);
        if (this.transaction.getNumberOfSplits() <= 1) {
            this.transaction.setAmount(this.transaction.getSubTotal());
        }
    }

    private int useCanModifyChildTransferOfBasedOn(TransactionClass newRecord, TransactionClass oldRecord) {
        Integer transferRecID = 0;
        Integer transferSplitItem = 0;
        boolean regularTransfer = newRecord.getCurrencyCode().equals(AccountDB.recordFor(newRecord.getAccount()).getCurrencyCode());
        if (oldRecord.getNumberOfSplits() > 1 || oldRecord.getTransferToAccount() == null || oldRecord.getTransferToAccount().length() <= 0) {
            return 2;
        }
        if (oldRecord.getDate().equals(newRecord.getDate()) && oldRecord.getAmount() == newRecord.getAmount() && oldRecord.getXrate() == newRecord.getXrate() && !oldRecord.getTransferToAccount().equals(newRecord.getTransferToAccount()) && !oldRecord.getCategory().equals(newRecord.getCategory()) && !oldRecord.getMemo().equals(newRecord.getMemo()) && !oldRecord.getClassName().equals(newRecord.getClassName())) {
            return 1;
        }
        TransactionTransferRetVals ret = new TransactionTransferRetVals();
        TransactionDB.transactionGetTransfer(oldRecord.getTransferToAccount(), oldRecord.getAccount(), oldRecord.getDate(), regularTransfer ? (-1.0d * oldRecord.getAmount()) / oldRecord.getXrate() : -1.0d * oldRecord.getAmount(), regularTransfer ? null : oldRecord.getCurrencyCode(), ret);
        transferRecID = ret.transferRecID;
        transferSplitItem = ret.transferSplitItem;
        if (transferRecID == 0 || new TransactionClass(transferRecID).getNumberOfSplits() <= 1) {
            return 2;
        }
        return 0;
    }

    public void saveUpdateTransferFromOriginalRecord(TransactionClass oldRecP, TransactionClass modRecP) {
        double d;
        double newRate;
        double newAmount;
        TransactionClass transferRecord;
        Integer transferRecID = 0;
        Integer transferSplitItem = 0;
        String currencyCode = AccountDB.recordFor(modRecP.getAccount()).getCurrencyCode();
        String tToCurrencyCode = "";
        int i = 0;
        while (i < oldRecP.getNumberOfSplits()) {
            double amountAtIndex;
            double xrate;
            if (oldRecP.getTransferToAccountAtIndex(i) != null && oldRecP.getTransferToAccountAtIndex(i).length() > 0) {
                String str;
                boolean regularTransfer = oldRecP.getCurrencyCodeAtIndex(i).equals(AccountDB.recordFor(oldRecP.getTransferToAccountAtIndex(i)).getCurrencyCode());
                TransactionTransferRetVals ret = new TransactionTransferRetVals();
                String transferToAccountAtIndex = oldRecP.getTransferToAccountAtIndex(i);
                String account = oldRecP.getAccount();
                GregorianCalendar date = oldRecP.getDate();
                amountAtIndex = regularTransfer ? -1.0d * (oldRecP.getAmountAtIndex(i) / oldRecP.getXrateAtIndex(i)) : -1.0d * oldRecP.getAmountAtIndex(i);
                if (regularTransfer) {
                    str = null;
                } else {
                    str = oldRecP.getCurrencyCodeAtIndex(i);
                }
                TransactionDB.transactionGetTransfer(transferToAccountAtIndex, account, date, amountAtIndex, str, ret);
                transferRecID = ret.transferRecID;
                transferSplitItem = ret.transferSplitItem;
                if (transferRecID > 0) {
                    TransactionClass transactionClass = new TransactionClass(transferRecID);
                    transactionClass.hydrate();
                    if (i >= modRecP.getNumberOfSplits()) {
                        transactionClass.setDeleted(true);
                        transactionClass.saveToDatabase();
                    } else if (modRecP.getTransferToAccountAtIndex(i) == null || modRecP.getTransferToAccountAtIndex(i).length() <= 0) {
                        transactionClass.setDeleted(true);
                        transactionClass.saveToDatabase();
                    } else {
                        if (!Prefs.getBooleanPref(Prefs.TRANSACTIONS_UNLINK_ID_FIELD)) {
                            transactionClass.setCheckNumber(modRecP.getCheckNumber());
                        }
                        transactionClass.setAccount(modRecP.getTransferToAccountAtIndex(i));
                        transactionClass.setTransferToAccountAtIndex(modRecP.getAccount(), transferSplitItem);
                        transactionClass.setCategoryAtIndex(modRecP.getCategoryAtIndex(i), transferSplitItem);
                        transactionClass.setClassNameAtIndex(modRecP.getClassNameAtIndex(i), transferSplitItem);
                        transactionClass.setMemoAtIndex(modRecP.getMemoAtIndex(i), transferSplitItem);
                        if (modRecP.getCurrencyCodeAtIndex(i).equals(AccountDB.recordFor(modRecP.getTransferToAccountAtIndex(i)).getCurrencyCode())) {
                            transactionClass.setCurrencyCodeAtIndex(currencyCode, transferSplitItem);
                            transactionClass.setSubTotal((-modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i));
                            transactionClass.setAmountAtIndex((-modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i), transferSplitItem);
                            transactionClass.setXrateAtIndex(1.0d / modRecP.getXrateAtIndex(i), transferSplitItem);
                        } else {
                            xrate = xrateFromAccountToAccount(this.transaction.getAccount(), this.transaction.getTransferToAccount());
                            amountAtIndex = modRecP.getXrateAtIndex(i);
                            if (xrate == 0.0d) {
                                d = 1.0d;
                            } else {
                                d = xrate;
                            }
                            newRate = amountAtIndex / d;
                            d = ((-1.0d * modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i)) * modRecP.getXrateAtIndex(i);
                            if (xrate == 0.0d) {
                                xrate = 1.0d;
                            }
                            newAmount = d / xrate;
                            transactionClass.setCurrencyCodeAtIndex(modRecP.getCurrencyCodeAtIndex(i), transferSplitItem);
                            transactionClass.setSubTotal(newAmount);
                            transactionClass.setAmountAtIndex(newAmount, transferSplitItem);
                            transactionClass.setXrateAtIndex(newRate, transferSplitItem);
                        }
                        transactionClass.setDate(modRecP.getDate());
                        transactionClass.initType();
                        transactionClass.saveToDatabase();
                        if (AccountDB.recordFor(transactionClass.getAccount()) == null && transactionClass.getAccount() != null && transactionClass.getAccount().length() > 0) {
                            AccountClass.insertIntoDatabase(transactionClass.getAccount());
                        }
                    }
                } else {
                    transferRecord = new TransactionClass();
                    transferRecord.setAccount(modRecP.getTransferToAccountAtIndex(i));
                    transferRecord.setPayee(modRecP.getPayee());
                    if (!Prefs.getBooleanPref(Prefs.TRANSACTIONS_UNLINK_ID_FIELD)) {
                        transferRecord.setCheckNumber(modRecP.getCheckNumber());
                    }
                    transferRecord.setTransferToAccount(modRecP.getAccount());
                    transferRecord.setCategory(modRecP.getCategoryAtIndex(i));
                    transferRecord.setClassName(modRecP.getClassNameAtIndex(i));
                    transferRecord.setMemo(modRecP.getMemoAtIndex(i));
                    if (modRecP.getCurrencyCodeAtIndex(i).equals(AccountDB.recordFor(modRecP.getTransferToAccountAtIndex(i)).getCurrencyCode())) {
                        transferRecord.setCurrencyCode(currencyCode);
                        transferRecord.setSubTotal((-modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i));
                        transferRecord.setAmount((-modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i));
                        transferRecord.setXrate(1.0d / modRecP.getXrateAtIndex(i));
                    } else {
                        xrate = xrateFromAccountToAccount(modRecP.getAccount(), modRecP.getTransferToAccount());
                        amountAtIndex = modRecP.getXrateAtIndex(i);
                        if (xrate == 0.0d) {
                            d = 1.0d;
                        } else {
                            d = xrate;
                        }
                        newRate = amountAtIndex / d;
                        d = ((-1.0d * modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i)) * modRecP.getXrateAtIndex(i);
                        if (xrate == 0.0d) {
                            xrate = 1.0d;
                        }
                        newAmount = d / xrate;
                        transferRecord.setCurrencyCode(modRecP.getCurrencyCodeAtIndex(i));
                        transferRecord.setSubTotal(newAmount);
                        transferRecord.setAmount(newAmount);
                        transferRecord.setXrate(newRate);
                    }
                    transferRecord.setDate(modRecP.getDate());
                    transferRecord.initType();
                    transferRecord.saveToDatabase();
                }
            }
            i++;
        }
        i = 0;
        while (i < modRecP.getNumberOfSplits()) {
            if (modRecP.getTransferToAccountAtIndex(i) != null && modRecP.getTransferToAccountAtIndex(i).length() > 0 && ((i < oldRecP.getNumberOfSplits() && (oldRecP.getTransferToAccountAtIndex(i) == null || oldRecP.getTransferToAccountAtIndex(i).length() == 0)) || i >= oldRecP.getNumberOfSplits())) {
                transferRecord = new TransactionClass();
                transferRecord.setAccount(modRecP.getTransferToAccountAtIndex(i));
                transferRecord.setPayee(modRecP.getPayee());
                if (!Prefs.getBooleanPref(Prefs.TRANSACTIONS_UNLINK_ID_FIELD)) {
                    transferRecord.setCheckNumber(modRecP.getCheckNumber());
                }
                transferRecord.setTransferToAccount(modRecP.getAccount());
                transferRecord.setCategory(modRecP.getCategoryAtIndex(i));
                transferRecord.setClassName(modRecP.getClassNameAtIndex(i));
                transferRecord.setMemo(modRecP.getMemoAtIndex(i));
                if (modRecP.getCurrencyCodeAtIndex(i).equals(AccountDB.recordFor(modRecP.getTransferToAccountAtIndex(i)).getCurrencyCode())) {
                    transferRecord.setCurrencyCode(currencyCode);
                    transferRecord.setSubTotal((-modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i));
                    transferRecord.setAmount((-modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i));
                    transferRecord.setXrate(1.0d / modRecP.getXrateAtIndex(i));
                } else {
                    double xrate = xrateFromAccountToAccount(modRecP.getAccount(), modRecP.getTransferToAccount());
                    double amountAtIndex = modRecP.getXrateAtIndex(i);
                    if (xrate == 0.0d) {
                        d = 1.0d;
                    } else {
                        d = xrate;
                    }
                    newRate = amountAtIndex / d;
                    d = ((-1.0d * modRecP.getAmountAtIndex(i)) / modRecP.getXrateAtIndex(i)) * modRecP.getXrateAtIndex(i);
                    if (xrate == 0.0d) {
                        xrate = 1.0d;
                    }
                    newAmount = d / xrate;
                    transferRecord.setCurrencyCode(modRecP.getCurrencyCodeAtIndex(i));
                    transferRecord.setSubTotal(newAmount);
                    transferRecord.setAmount(newAmount);
                    transferRecord.setXrate(newRate);
                }
                transferRecord.setDate(modRecP.getDate());
                transferRecord.setCleared(modRecP.getCleared());
                transferRecord.initType();
                transferRecord.saveToDatabase();
            }
            i++;
        }
    }

    public double xrateFromAccountToAccount(String account1, String account2) {
        return AccountDB.recordFor(account1).getExchangeRate() / AccountDB.recordFor(account2).getExchangeRate();
    }

    private void editTextDidFinishChanging(int editTextCode) {
        if (editTextCode == 3) {
            saveAmountXrates();
            loadAmountXrateValues();
        }
    }

    private void editTextDidChange(int editTextCode) {
    }

    private void clearDropDowns() {
        this.categoryEditText.dismissDropDown();
        this.payeeEditText.dismissDropDown();
        this.idEditText.dismissDropDown();
        this.classEditText.dismissDropDown();
    }

    private void clearDropDownsTimerStart() {
        this.clearTimer.schedule(new ClearTask(), 750);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            String selection = "";
            if (!(data == null || data.getExtras() == null)) {
                selection = data.getExtras().getString("selection");
            }
            File file;
            switch (requestCode) {
                case SplitsActivity.REQUEST_EDIT /*3*/:
                    this.accountTextView.setText(selection);
                    clearDropDownsTimerStart();
                    break;
                case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                case LookupsListActivity.ACCOUNT_LOOKUP_TRANS /*17*/:
                    this.payeeEditText.setText(selection);
                    clearDropDownsTimerStart();
                    break;
                case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                    this.categoryEditText.setText(selection);
                    clearDropDownsTimerStart();
                    break;
                case LookupsListActivity.CLASS_LOOKUP /*6*/:
                    this.classEditText.setText(selection);
                    clearDropDownsTimerStart();
                    break;
                case LookupsListActivity.ID_LOOKUP /*7*/:
                    this.idEditText.setText(selection);
                    clearDropDownsTimerStart();
                    break;
                case Enums.kDesktopSyncStateSendPhotos /*30*/:
                    break;
                case Enums.kDesktopSyncStateSendingPhoto /*31*/:
                    this.transaction = (TransactionClass) data.getExtras().get("Transaction");
                    this.transaction.dirty = true;
                    reloadData();
                    break;
                case Enums.kDesktopSyncStateSentPhoto /*32*/:
                    this.transaction = (TransactionClass) data.getExtras().get("Transaction");
                    this.transaction.hydrated = true;
                    this.transaction.dirty = true;
                    this.repeatingTransaction = null;
                    this.repeatingTransaction = (RepeatingTransactionClass) data.getExtras().get("RepeatingTransaction");
                    this.repeatingTransaction.getTransaction().hydrated = false;
                    break;
                case Enums.kDesktopSyncStateReceivingPhotoHeader /*33*/:
                    if (selection == null || selection.length() == 0) {
                        this.withdrawalButton.setChecked(true);
                    }
                    this.payeeEditText.setText(selection);
                    updateXrates();
                    break;
                case Enums.kDesktopSyncStatePhotoHeaderReceived /*34*/:
                    try {
                        Bundle b = data.getExtras();
                        this.transaction.setCurrencyCode(b.getString("currency"));
                        this.transaction.setXrate(b.getDouble("xrate"));
                        this.transaction.setSubTotal(b.getDouble("amount"));
                        loadAmountXrateValues();
                        break;
                    } catch (NullPointerException e) {
                        break;
                    }
                case Enums.kDesktopSyncStateReceivingPhoto /*35*/:
                    FileChannel inChannel = null;
                    FileChannel outChannel = null;
                    String fileName = "";
                    try {
                        Object obj;
                        fileName = new StringBuilder(String.valueOf(this.payeeEditText.getText().toString())).append("-").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).toString();
                        this.newlyAddedImages.add(fileName);
                        file = new File(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/");
                        file = new File(file, new StringBuilder(String.valueOf(fileName)).append(".jpg").toString());
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        boolean wtff = file.createNewFile();
                        inChannel = new FileInputStream(this.tempPhotoPath).getChannel();
                        outChannel = new FileOutputStream(file).getChannel();
                        inChannel.transferTo(0, inChannel.size(), outChannel);
                        if (inChannel != null) {
                            try {
                                inChannel.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        if (outChannel != null) {
                            try {
                                outChannel.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        TransactionClass transactionClass = this.transaction;
                        if (this.transaction.getImageLocation() == null) {
                            obj = "";
                        } else {
                            obj = this.transaction.getImageLocation();
                        }
                        transactionClass.setImageLocation(new StringBuilder(String.valueOf(obj)).append(fileName).append(".jpg;").toString());
                        break;
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        if (inChannel != null) {
                            try {
                                inChannel.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        if (outChannel != null) {
                            try {
                                outChannel.close();
                                return;
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th) {
                        if (inChannel != null) {
                            try {
                                inChannel.close();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        if (outChannel != null) {
                            try {
                                outChannel.close();
                            } catch (IOException e222222) {
                                e222222.printStackTrace();
                            }
                        }
                    }
                case Enums.kDesktopSyncStatePhotoReceived /*36*/:
                    String fileName2 = new StringBuilder(String.valueOf(this.payeeEditText.getText().toString())).append("-").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).toString();
                    String[] filePathColumn = new String[]{"_data"};
                    Cursor cursor = getContentResolver().query(data.getData(), filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    String filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                    cursor.close();
                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                    try {
                        file = new File(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/");
                        file = new File(file, new StringBuilder(String.valueOf(fileName2)).append(".jpg").toString());
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        boolean fail = file.createNewFile();
                        yourSelectedImage.compress(CompressFormat.JPEG, 90, new FileOutputStream(file));
                        this.transaction.setImageLocation(new StringBuilder(String.valueOf(this.transaction.getImageLocation() == null ? "" : this.transaction.getImageLocation())).append(fileName2).append(".jpg;").toString());
                        this.newlyAddedImages.add(fileName2);
                        break;
                    } catch (Exception e32) {
                        e32.printStackTrace();
                        break;
                    }
                case REQUEST_PHOTO_OPTION /*37*/:
                    String fileNamee = data.getExtras().getString("imageName");
                    this.deletedImages.add(fileNamee);
                    this.transaction.setImageLocation(this.transaction.getImageLocation().replace(new StringBuilder(String.valueOf(fileNamee)).append(";").toString(), ""));
                    reloadData();
                    break;
            }
            if (resultCode == -1) {
                this.transaction.setMemo(selection);
                setNotesText(selection);
            }
            getCells();
        } else if (this.transferButton.isChecked() && 33 == requestCode) {
            this.withdrawalButton.setChecked(true);
        }
    }

    protected Dialog onCreateDialog(int id) {
        Builder builder;
        switch (id) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                GregorianCalendar theDate = this.transaction.getDate();
                return new DatePickerDialog(this, this.mDateSetListener, theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH));
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                builder = new Builder(this);
                builder.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(Locales.kLOC_GENERAL_DELETE, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        TransactionEditActivity.this.deleteConfirmed();
                    }
                });
                return builder.create();
            case SplitsActivity.REQUEST_EDIT /*3*/:
                builder = new Builder(this);
                builder.setTitle(Locales.kLOC_DUPLICATE_TRANSACTION_TITLE);
                builder.setNegativeButton(Locales.kLOC_DUPLICATE_TRANSACTION_EXISTING_TIME, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        TransactionEditActivity.this.duplicateTransaction(false);
                    }
                }).setPositiveButton(Locales.kLOC_DUPLICATE_TRANSACTION_PRESENT_TIME, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        TransactionEditActivity.this.duplicateTransaction(true);
                    }
                });
                return builder.create();
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                builder = new Builder(this);
                builder.setMessage(Locales.kLOC_FEE_MISSING_ALERT);
                builder.setNegativeButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                    }
                });
                return builder.create();
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                GregorianCalendar theTime = this.transaction.getDate();
                return new TimePickerDialog(this, this.mTimeSetListener, theTime.get(Calendar.HOUR_OF_DAY), theTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(this));
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                builder = new Builder(this);
                builder.setMessage(Locales.kLOC_EDIT_TRANSACTION_MISSINGACCOUNT);
                builder.setNegativeButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                    }
                });
                return builder.create();
            case LookupsListActivity.ID_LOOKUP /*7*/:
                builder = new Builder(this);
                builder.setMessage("How often this transaction repeats must be entered before you can save a repeating transaction.\n\nTap the calendar icon to the right of the Date to configure the repeating info for this transaction.");
                builder.setNegativeButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                    }
                });
                return builder.create();
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                builder = new Builder(this);
                builder.setMessage("Choose existing or take new");
                builder.setPositiveButton("New", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String tempPhotoDir = SMMoney.getExternalPocketMoneyDirectory();
                        TransactionEditActivity.this.tempPhotoPath = new File(tempPhotoDir, "temp.jpeg");
                        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                        takePictureIntent.putExtra("output", Uri.fromFile(TransactionEditActivity.this.tempPhotoPath));
                        TransactionEditActivity.this.startActivityForResult(takePictureIntent, 35);
                    }
                });
                builder.setNegativeButton("Choose", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        Intent photoPickerIntent = new Intent("android.intent.action.PICK");
                        photoPickerIntent.setType("image/*");
                        TransactionEditActivity.this.startActivityForResult(photoPickerIntent, 36);
                    }
                });
                return builder.create();
            default:
                return null;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, Locales.kLOC_GENERAL_SPLIT).setIcon(R.drawable.ic_arrow_drop_down_circle);
        menu.add(0, 2, 0, Locales.kLOC_GENERAL_DUPLICATE).setIcon(R.drawable.ic_arrow_drop_down_circle);
        if (this.transaction.getAccount() != null) {
            menu.add(0, 3, 0, Locales.kLOC_ACCOUNT_FEE_LABEL).setIcon(R.drawable.ic_arrow_drop_down_circle);
        }
        if (SMMoney.hasCamera()) {
            menu.add(0, 4, 0, "Camera").setIcon(R.drawable.ic_arrow_drop_down_circle);
        }
        menu.add(0, 5, 0, Locales.kLOC_GENERAL_DELETE).setIcon(R.drawable.ic_arrow_drop_down_circle);
        MenuItem item = menu.add(0, 6, 0, Locales.kLOC_TRANSACTION_NEW);
        item.setIcon(R.drawable.circleplus);
        item.setShowAsAction(2);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                splitsAction();
                break;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                showDialog(3);
                break;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                includeFeeAction();
                break;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                showDialog(8);
                break;
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                showDialog(2);
                break;
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                getCells();
                deleteDeletedImages();
                if (this.accountTextView.getText() != null && this.accountTextView.getText().toString().length() != 0) {
                    if (!this.transaction.isRepeatingTransaction || (this.repeatingTransaction != null && this.repeatingTransaction.isRepeating())) {
                        if (this.transaction.transactionID != 0 && !this.transaction.isRepeatingTransaction && this.repeatingTransaction.repeatingID > 0 && this.repeatingTransaction.isRepeating() && this.dateChanged == 0) {
                            Builder b = new Builder(this);
                            b.setTitle("Edit");
                            b.setMessage("Change the information of the transaction and repeating event, or change the information of only this transaction?");
                            b.setPositiveButton("Both", new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    TransactionEditActivity.this.repeatingChanged = 2;
                                    TransactionEditActivity.this.saveAction();
                                }
                            });
                            b.setNegativeButton("This item only", new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    TransactionEditActivity.this.repeatingChanged = 1;
                                    TransactionEditActivity.this.saveAction();
                                }
                            });
                            b.create().show();
                            break;
                        }
                        saveAction();
                        break;
                    }
                    showDialog(7);
                    break;
                }
                showDialog(6);
                break;
        }
        return false;
    }

    private View.OnClickListener getBtnClickListener() {
        return new View.OnClickListener() {
            public void onClick(View view) {
                switch ((Integer) view.getTag()) {
                    case Enums.kDesktopSyncStateSendPhotos /*30*/:
                        Intent i = new Intent(TransactionEditActivity.this.currentActivity, NoteEditor.class);
                        i.putExtra("note", TransactionEditActivity.this.transaction.getMemo());
                        TransactionEditActivity.this.currentActivity.startActivityForResult(i, (Integer) view.getTag());
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private View.OnClickListener getLookupListClickListener() {
        return new View.OnClickListener() {
            public void onClick(View view) {
                TransactionEditActivity.this.getCells();
                Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
                i.putExtra("type", ((Integer) view.getTag()).intValue());
                TransactionEditActivity.this.currentActivity.startActivityForResult(i, (Integer) view.getTag());
            }
        };
    }

    private OnCheckedChangeListener getRadioChangedListener() {
        return new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!TransactionEditActivity.this.programaticUpdate) {
                    TransactionEditActivity.this.clearKeepTheChange();
                    if (checkedId == R.id.withdrawalbutton) {
                        TransactionEditActivity.this.payeeEditText.setEnabled(true);
                        TransactionEditActivity.this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TO);
                        TransactionEditActivity.this.transaction.setType(0);
                        TransactionEditActivity.this.reloadData();
                    } else if (checkedId == R.id.depositbutton) {
                        TransactionEditActivity.this.payeeEditText.setEnabled(true);
                        TransactionEditActivity.this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_FROM);
                        TransactionEditActivity.this.transaction.setType(1);
                    } else if (checkedId == R.id.transferbutton) {
                        TransactionEditActivity.this.payeeEditText.setEnabled(false);
                        if (TransactionEditActivity.this.transaction.getSubTotal() > 0.0d) {
                            TransactionEditActivity.this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_FROM);
                            TransactionEditActivity.this.transaction.setType(3);
                        } else {
                            TransactionEditActivity.this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_TO);
                            TransactionEditActivity.this.transaction.setType(2);
                        }
                        TransactionEditActivity.this.getCells();
                        Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
                        i.putExtra("type", 3);
                        TransactionEditActivity.this.currentActivity.startActivityForResult(i, 33);
                    }
                    TransactionEditActivity.this.getCells();
                    TransactionEditActivity.this.reloadData();
                }
            }
        };
    }

    public OnFocusChangeListener getFocusChangedListenerWithID(int id) {
        final int theID = id;
        return new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    TransactionEditActivity.this.editTextDidFinishChanging(theID);
                }
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (this.currencyKeyboard.hide()) {
                return false;
            }
            deleteNewlyAddedImages();
        }
        return super.onKeyDown(keyCode, event);
    }
}
