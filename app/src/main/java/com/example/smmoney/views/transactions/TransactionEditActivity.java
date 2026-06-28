package com.example.smmoney.views.transactions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
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
import com.example.smmoney.views.CheckBoxTint;
import com.example.smmoney.views.CurrencyKeyboard;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.budgets.BudgetsDatePickerDialog;
import com.example.smmoney.views.exchangerates.ExchangeRateActivity;
import com.example.smmoney.views.lookups.CategoryLookupListActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.repeating.RepeatingEditActivity;
import com.example.smmoney.views.splits.SplitsActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

public class TransactionEditActivity extends PocketMoneyActivity implements DatePickerDialog.OnDateSetListener {
    public static final int REQUEST_PHOTO_OPTION = 37;
    private static final String TAG = "TRANS_EDIT_ACTIVITY";
    private final int DIALOG_CAMERA = 8;
    private final int DIALOG_DELETECONFIRM = 2;
    private final int DIALOG_DUPLICATE = 3;
    private final int DIALOG_FEE = 4;
    private final int DIALOG_NEED_ACCOUNT = 6;
    private final int DIALOG_NEED_REPEATING = 7;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EDITTEXT_AMOUNT = 3;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EDITTEXT_CATEGORY = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EDITTEXT_CLASS = 5;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EDITTEXT_ID = 4;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EDITTEXT_PAYEE = 1;
    private final int MENU_CAMERA = 4;
    private final int MENU_DELETE = 5;
    private final int MENU_DUPE = 2;
    private final int MENU_FEE = 3;
    private final int MENU_SAVE = 6;
    private final int MENU_SPLIT = 1;
    private final int MSG_CLEARDROPDOWNS = 1;
    private final int NOTE_EDIT_BUTTON = 30;
    private final int REQUEST_CAMERA_NEW = 35;
    private final int REQUEST_CAMERA_PICK = 36;
    private final int REQUEST_CURRENCY = 34;
    private final int REQUEST_REPEATING = 32;
    private final int REQUEST_SPLITS = 31;
    private final int REQUEST_TRANSFER = 33;
    private final int TIME_DIALOG_ID = 5;
    private final Timer clearTimer = new Timer();

    final ActivityResultLauncher<Intent> photoOptionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String fileNamee = result.getData().getExtras().getString("imageName");
            this.deletedImages.add(fileNamee);
            this.transaction.setImageLocation(this.transaction.getImageLocation().replace(fileNamee + ";", ""));
            reloadData();
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> accountLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            this.accountTextView.setText(result.getData().getStringExtra("selection"));
            clearDropDownsTimerStart();
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> payeeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            this.payeeEditText.setText(result.getData().getStringExtra("selection"));
            clearDropDownsTimerStart();
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            this.categoryEditText.setText(result.getData().getStringExtra("selection"));
            clearDropDownsTimerStart();
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> classLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            this.classEditText.setText(result.getData().getStringExtra("selection"));
            clearDropDownsTimerStart();
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> idLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            this.idEditText.setText(result.getData().getStringExtra("selection"));
            clearDropDownsTimerStart();
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> noteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.transaction.setMemo(selection);
            setNotesText(selection);
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> splitsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                this.transaction = result.getData().getSerializableExtra("Transaction", TransactionClass.class);
            } else {
                //noinspection deprecation
                this.transaction = (TransactionClass) result.getData().getExtras().get("Transaction");
            }
            if (this.transaction != null) {
                this.transaction.hydrated = true;
                this.transaction.dirty = true;
            }
            reloadData();
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> repeatingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                this.transaction = result.getData().getSerializableExtra("Transaction", TransactionClass.class);
            } else {
                //noinspection deprecation
                this.transaction = (TransactionClass) result.getData().getExtras().get("Transaction");
            }
            this.transaction.hydrated = true;
            this.transaction.dirty = true;
            this.repeatingTransaction = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                this.repeatingTransaction = result.getData().getSerializableExtra("RepeatingTransaction", RepeatingTransactionClass.class);
            } else {
                //noinspection deprecation
                this.repeatingTransaction = (RepeatingTransactionClass) result.getData().getExtras().get("RepeatingTransaction");
            }
            this.repeatingTransaction.getTransaction().hydrated = false;
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> transferLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.payeeEditText.setText(selection);
            updateXrates();
            getCells();
        } else if (this.transferButton.isChecked()) {
            this.withdrawalButton.setChecked(true);
            getCells();
        }
    });
    private final ActivityResultLauncher<Intent> currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            try {
                Bundle b = result.getData().getExtras();
                this.transaction.setCurrencyCode(b.getString("currency"));
                this.transaction.setXrate(b.getDouble("xrate"));
                this.transaction.setSubTotal(b.getDouble("amount"));
                loadAmountXrateValues();
                getCells();
            } catch (NullPointerException e) {
                Log.e(TAG, "NullPointerException in currencyLauncher", e);
            }
        }
    });
    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            showCameraDialog();
        } else {
            android.widget.Toast.makeText(this, "Camera permission is required to take photos", android.widget.Toast.LENGTH_SHORT).show();
        }
    });

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
        if (result) {
            try {
                String fileName = this.payeeEditText.getText().toString() + "-" + CalExt.descriptionWithTimestamp(new GregorianCalendar());
                File photoDir = new File(SMMoney.getAppContext().getFilesDir(), "photos");
                if (!photoDir.exists()) photoDir.mkdirs();
                File photoFile = new File(photoDir, fileName + ".jpg");

                // Copy from tempPhotoPath to photoFile
                try (InputStream in = new FileInputStream(this.tempPhotoPath);
                     FileOutputStream out = new FileOutputStream(photoFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }

                String current = this.transaction.getImageLocation();
                this.transaction.setImageLocation((current == null ? "" : current) + fileName + ".jpg;");
                this.newlyAddedImages.add(fileName);
                this.photoCell.setImageLocationString(this.transaction.getImageLocation());
                getCells();
                reloadData();
                this.photoCell.invalidate();
            } catch (Exception e) {
                Log.e(TAG, "Exception in cameraLauncher", e);
            }
        }
    });
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            try {
                String fileName = this.payeeEditText.getText().toString() + "-" + CalExt.descriptionWithTimestamp(new GregorianCalendar());
                File photoDir = new File(SMMoney.getAppContext().getFilesDir(), "photos");
                if (!photoDir.exists()) photoDir.mkdirs();
                File photoFile = new File(photoDir, fileName + ".jpg");

                try (InputStream inputStream = getContentResolver().openInputStream(result);
                     FileOutputStream outputStream = new FileOutputStream(photoFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                String current = this.transaction.getImageLocation();
                this.transaction.setImageLocation((current == null ? "" : current) + fileName + ".jpg;");
                this.newlyAddedImages.add(fileName);
                getCells();
                reloadData();
            } catch (Exception e) {
                Log.e(TAG, "Exception in cameraLauncher", e);
            }
        }
    });

    private TextView accountTextView;
    private EditText amountEditText;
    private TextView foreignAmountTextView;
    private TextView xRateTextView;
    private BalanceBar balanceBar;
    private AutoCompleteTextView categoryEditText;
    private TextView categoryTextView;
    private double changeKept;
    private AutoCompleteTextView classEditText;
    private TextView classTextView;
    private CheckBox clearedCheckBox;
    private CurrencyKeyboard currencyKeyboard;
    private Activity currentActivity;
    private int dateChanged;
    private TextView dateTextView;
    private final ArrayList<String> deletedImages = new ArrayList<>();
    private RadioButton depositButton;
    private AutoCompleteTextView idEditText;
    private boolean isIReceipt = false;
    private boolean isLocalNotification = false;
    private final ArrayList<String> newlyAddedImages = new ArrayList<>();
    private final OnTimeSetListener mTimeSetListener = (view, hourOfDay, minute) -> TransactionEditActivity.this.timeTextView.setText(CalExt.descriptionWithShortTime(new GregorianCalendar(0, 0, 0, hourOfDay, minute)));
    private TextView memoTextView;
    @SuppressWarnings("FieldCanBeLocal")
    private TextView keepTheChangeButton;
    private AutoCompleteTextView payeeEditText;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CLEARDROPDOWNS) { /*1*/
                TransactionEditActivity.this.clearDropDowns();
            }
        }
    };
    private TextView payeeLabelTextView;
    private PhotoReceiptsCell photoCell;
    private boolean posting = false;
    private boolean programaticUpdate;
    private int repeatingChanged;
    private AlertDialog repeatingDateChangedAlert = null;
    private ImageView repeatingImageView;
    private RepeatingTransactionClass repeatingTransaction;
    @SuppressWarnings("FieldCanBeLocal")
    private FrameLayout keyboardToolBar;
    private File tempPhotoPath;
    private TextView timeTextView;
    @SuppressWarnings("FieldCanBeLocal")
    private ScrollView scrollView;
    private TransactionClass transaction;
    private RadioButton transferButton;
    private RadioButton withdrawalButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        Uri data = i.getData();

        if (extras != null && extras.getBoolean("Posting")) {
            Log.d(TAG, "onCreate: this.posting = true");
            this.posting = true;
            this.isLocalNotification = extras.getBoolean("localNotification");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                this.repeatingTransaction = extras.getSerializable("repeatingTransaction", RepeatingTransactionClass.class);
            } else {
                //noinspection deprecation
                this.repeatingTransaction = (RepeatingTransactionClass) extras.get("repeatingTransaction");
            }
            if (this.repeatingTransaction != null) {
                this.repeatingTransaction.hydrated = false;
                this.repeatingTransaction.hydratedTransaction = false;
                this.transaction = this.repeatingTransaction.getTransaction();
                if (this.transaction != null) {
                    this.transaction = this.transaction.copy();
                    GregorianCalendar nextDate = this.repeatingTransaction.getNextTransactionDateAfter(this.repeatingTransaction.getLastProcessedDate());
                    if (nextDate != null) {
                        this.transaction.setDate(nextDate);
                    }
                    this.transaction.initType();
                }
                if (this.isLocalNotification) {
                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(this.repeatingTransaction.repeatingID);
                }
            }
        } else if (data != null) {
            this.isIReceipt = true;
            handleIReceipt(data);
            this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
        } else if (extras != null && extras.containsKey("Transaction")) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                this.transaction = extras.getSerializable("Transaction", TransactionClass.class);
            } else {
                //noinspection deprecation
                this.transaction = (TransactionClass) extras.get("Transaction");
            }
            if (this.transaction != null) {
                this.transaction.hydrated = true;
                this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
            }
        }

        if (this.transaction == null) {
            Log.e(TAG, "Failed to initialize transaction");
            finish();
            return;
        }

        this.transaction.hydrate();
        this.transaction.hydrate();
        this.currentActivity = this;
        this.dateChanged = Enums.DateChangeTypeNone /*0*/;
        this.repeatingChanged = Enums.RepeatingChangeTypeNone /*0*/;
        setContentView(R.layout.transaction_edit);
        setupButtons();
        setTitle(Locales.kLOC_EDIT_TRANSACTION_TITLE);
        this.balanceBar.setVisibility(View.GONE);
        getSupportActionBar().setTitle(Locales.kLOC_EDIT_TRANSACTION_TITLE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onResume() {
        Log.d(TAG, "onResume() called");
        super.onResume();
        if (!Prefs.getBooleanPref(Prefs.HINT_EDITTRANSACTION)) {
            Builder alert = new Builder(this, PocketMoneyThemes.dialogTheme());
            alert.setMessage(Locales.kLOC_TIP_EDITTRANSACTION);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
                Prefs.setPref(Prefs.HINT_EDITTRANSACTION, true);
                dialog.dismiss();
            });
            alert.show();
        }
        reloadData();
        Log.d(TAG, "onResume: <--- after reloadData()");
        if (this.balanceBar != null) {
            reloadBalanceBar();
            Log.d(TAG, "onResume: <--- after reloadBalanceBar()");
        }
        selectStartingCell();
        Log.d(TAG, "onResume: <--- after selectStartingCell()");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        this.clearTimer.cancel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp() called");
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void handleIReceipt(Uri data) {
        this.transaction = new iReceiptClass(data).transaction;
        this.transaction.hydrated = true;
    }

    private void setTitle(String title) {
    }

    private void setupButtons() {
        Log.d(TAG, "setupButtons() called");
        ArrayList<View> theViews = new ArrayList<>();
        ArrayList<View> selectableViews = new ArrayList<>();
        this.balanceBar = findViewById(R.id.balancebar);
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.scrollView = findViewById(R.id.scroll_view);
        this.scrollView.setVerticalScrollBarEnabled(false);
        View outterView = findViewById(R.id.outter_layout);
        View aView = outterView.findViewById(R.id.radiogroup);
        this.withdrawalButton = aView.findViewById(R.id.withdrawalbutton);
        this.depositButton = aView.findViewById(R.id.depositbutton);
        this.transferButton = aView.findViewById(R.id.transferbutton);
        ((RadioGroup) aView).setOnCheckedChangeListener(getRadioChangedListener());
        aView = outterView.findViewById(R.id.datebutton);
        theViews.add(aView);
        aView.setOnClickListener(v -> {
            GregorianCalendar theDate = CalExt.dateFromDescriptionWithMediumDate(dateTextView.getText().toString());
            long datelong = theDate.getTimeInMillis();
            Bundle args = new Bundle();
            args.putLong("dateInt", datelong);
            DialogFragment datePicker = new BudgetsDatePickerDialog();
            datePicker.setArguments(args);
            datePicker.show(getSupportFragmentManager(), "date picker");
        });
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        this.dateTextView = aView.findViewById(R.id.datetextview);
        this.dateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.timeTextView = aView.findViewById(R.id.timetextview);
        this.timeTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.timeTextView.setOnClickListener(v -> TransactionEditActivity.this.showTimePickerDialog());
        this.repeatingImageView = aView.findViewById(R.id.repeatingimageview);
        repeatingImageView.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
        this.repeatingImageView.setOnClickListener(v -> {
            if (!AccountsActivity.isLite(TransactionEditActivity.this) || TransactionDB.queryAllRepeatingTransactions().size() < 2) {
                Intent i = new Intent(TransactionEditActivity.this.currentActivity, RepeatingEditActivity.class);
                TransactionEditActivity.this.getCells();
                if (TransactionEditActivity.this.repeatingTransaction.repeatingID == 0) {
                    TransactionEditActivity.this.repeatingTransaction.setTransaction(TransactionEditActivity.this.transaction.copy());
                }
                i.putExtra("Transaction", TransactionEditActivity.this.transaction);
                i.putExtra("RepeatingTransaction", TransactionEditActivity.this.repeatingTransaction);
                repeatingLauncher.launch(i);
                return;
            }
            AccountsActivity.displayLiteDialog(TransactionEditActivity.this);
        });
        ((TextView) aView.findViewById(R.id.date_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = outterView.findViewById(R.id.accountbutton);
        theViews.add(aView);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        aView.setOnClickListener(v -> {
            TransactionEditActivity.this.getCells();
            Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
            i.putExtra("type", 3);
            accountLauncher.launch(i);
        });
        this.accountTextView = aView.findViewById(R.id.accounttextview);
        ((TextView) outterView.findViewById(R.id.account_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.accountTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());

        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();
        ImageView iconView;
        if ((iconView = outterView.findViewById(R.id.account_drop_down)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = outterView.findViewById(R.id.payee_drop_down)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = outterView.findViewById(R.id.category_drop_down)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = outterView.findViewById(R.id.id_drop_down)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = outterView.findViewById(R.id.class_drop_down)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = outterView.findViewById(R.id.amount_currency_button)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);

        View cView = outterView.findViewById(R.id.categorybutton);

        cView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        cView.setOnClickListener(view -> {
            if (TransactionEditActivity.this.transaction.getNumberOfSplits() > 1) {
                TransactionEditActivity.this.splitsAction();
                return;
            }
            TransactionEditActivity.this.getCells();
            Intent i = new Intent(TransactionEditActivity.this.currentActivity, CategoryLookupListActivity.class);
            i.putExtra("payee", TransactionEditActivity.this.payeeEditText.getText().toString());
            categoryLauncher.launch(i);
        });
        this.categoryEditText = cView.findViewById(R.id.categoryedittext); // categoryEditText is an AutoCompleteTextView
        this.categoryEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.categoryEditText.setThreshold(2);
        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
            Log.d("TransactionEditAct", "before setAdapter()");
            //this.categoryEditText.setAdapter(new ArrayAdapter(this, R.layout.lookups_category, CategoryClass.allCategoryNamesInDatabase()));
            // TODO Customise the simple_list_item_1 so that it looks how it should. Just used here to make code work as original code above does not point to a TextView and therefore crashes!!
            this.categoryEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CategoryClass.allCategoryNamesInDatabase()));
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
        pView.setOnClickListener(view -> {
            TransactionEditActivity.this.getCells();
            if (TransactionEditActivity.this.transaction.isTransfer()) {
                Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
                i.putExtra("type", Enums.kTransactionTypeTransferFrom /*3*/);
                transferLauncher.launch(i);
                return;
            }
            Intent i = new Intent(TransactionEditActivity.this.currentActivity, CategoryLookupListActivity.class);
            i.putExtra("category", TransactionEditActivity.this.categoryEditText.getText().toString());
            payeeLauncher.launch(i);
        });
        this.payeeEditText = pView.findViewById(R.id.payeetextview);
        this.payeeEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.payeeEditText.setThreshold(2);
        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
//            this.payeeEditText.setAdapter(new ArrayAdapter(this, R.layout.lookups_category, PayeeClass.allPayeesInDatabase()));
            // TODO Customise as for category class above
            this.payeeEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, PayeeClass.allPayeesInDatabase()));
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
        this.currencyKeyboard.setEditText(this.amountEditText, () -> {
            if (TransactionEditActivity.this.amountEditText.hasFocus()) {
                TransactionEditActivity.this.clearKeepTheChange();
                AccountClass act = AccountDB.recordFor(TransactionEditActivity.this.transaction.getAccount());
                boolean keepTheChangeEnabled = act != null && act.getKeepTheChangeAccount() != null && !act.getKeepTheChangeAccount().isEmpty() && (TransactionEditActivity.this.transaction.getType() == Enums.kTransactionTypeWithdrawal /*0*/ || TransactionEditActivity.this.transaction.getType() == Enums.kTransactionTypeTransferTo /*2*/);
                TransactionEditActivity.this.currencyKeyboard.setToolbarEnabled(keepTheChangeEnabled);
                return;
            }
            TransactionEditActivity.this.editTextDidFinishChanging(3);
        });
        this.foreignAmountTextView = outterView.findViewById(R.id.foreign_amount_text_view);
        this.foreignAmountTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.xRateTextView = outterView.findViewById(R.id.amount_xrate_text_view);
        this.xRateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        aView = (View) this.amountEditText.getParent();
        theViews.add(aView);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            aView.setOnClickListener(v -> {
                TransactionEditActivity.this.getCells();
                Intent i = new Intent(TransactionEditActivity.this.currentActivity, ExchangeRateActivity.class);
                i.putExtra("transaction", TransactionEditActivity.this.transaction);
                try {
                    i.putExtra("split", TransactionEditActivity.this.transaction.getSplits().get(0));
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullPointerException in amount parent listener", e);
                }
                currencyLauncher.launch(i);
            });
        } else {
            outterView.findViewById(R.id.amount_currency_button).setVisibility(View.GONE);
            this.foreignAmountTextView.setVisibility(View.GONE);
            this.xRateTextView.setVisibility(View.GONE);
        }
        ((TextView) outterView.findViewById(R.id.amount_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = outterView.findViewById(R.id.idbutton);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        aView.setOnClickListener(v -> {
            TransactionEditActivity.this.getCells();
            Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
            i.putExtra("type", 7);
            idLauncher.launch(i);
        });
        this.idEditText = aView.findViewById(R.id.idedittext);
        selectableViews.add(this.idEditText);
        this.idEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.idEditText.setThreshold(2);
        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
//            this.idEditText.setAdapter(new ArrayAdapter(this, R.layout.lookups_category, IDClass.allCategoriesInDatabase()));
            // TODO Customise as for above category class adapter
            this.idEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, IDClass.allCategoriesInDatabase()));
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
        aView = (View) this.clearedCheckBox.getParent();
        if (Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CLEARED_FIELD)) {
            aView.setVisibility(View.VISIBLE);
            theViews.add(aView);
        } else {
            aView.setVisibility(View.GONE);
        }
        this.clearedCheckBox.setEnabled(Prefs.getBooleanPref(Prefs.EDITTRANSACTION_SHOW_CLEARED_FIELD));
        CheckBoxTint.colorCheckBox(this.clearedCheckBox);
        ((TextView) outterView.findViewById(R.id.cleared_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = outterView.findViewById(R.id.classbutton);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        aView.setOnClickListener(view -> {
            if (TransactionEditActivity.this.transaction.getNumberOfSplits() > 1) {
                TransactionEditActivity.this.splitsAction();
                return;
            }
            TransactionEditActivity.this.getCells();
            Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
            i.putExtra("type", 6 /* 6 = 'ClassName' type in LookupsListActivity.java switch statement */);
            classLauncher.launch(i);
        });
        this.classEditText = aView.findViewById(R.id.classedittext);
        selectableViews.add(this.classEditText);
        this.classEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.classEditText.setThreshold(2);
        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
//            this.classEditText.setAdapter(new ArrayAdapter<>(this, R.layout.lookups_category, ClassNameClass.allClassNamesInDatabase()));
            // TODO Customise as for above category class adapter
            this.classEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ClassNameClass.allClassNamesInDatabase()));
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
        aView.setTag(NOTE_EDIT_BUTTON /*30*/);
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
        for (View view : theViews) {
            view.setBackgroundResource(PocketMoneyThemes.editRowSelector(i));
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
        this.keyboardToolBar = findViewById(R.id.keyboard_toolbar);
        this.keyboardToolBar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.currencyKeyboard.setToolbarView(this.keyboardToolBar);
        this.keepTheChangeButton = findViewById(R.id.keep_the_change_toolbar_button);
        this.keepTheChangeButton.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        this.keepTheChangeButton.setTextColor(-1);
        this.keepTheChangeButton.setOnClickListener(v -> TransactionEditActivity.this.keepTheChange());
        this.payeeEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(EDITTEXT_PAYEE /*1*/));
        this.categoryEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(EDITTEXT_CATEGORY /*2*/));
        this.idEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(EDITTEXT_ID /*4*/));
        this.classEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(EDITTEXT_CLASS /*5*/));
        this.payeeEditText.setKeyListener(new MyKeyListener(this.payeeEditText.getKeyListener(), EDITTEXT_PAYEE /*1*/));
        this.categoryEditText.setKeyListener(new MyKeyListener(this.categoryEditText.getKeyListener(), EDITTEXT_CATEGORY /*2*/));
        this.idEditText.setKeyListener(new MyKeyListener(this.idEditText.getKeyListener(), EDITTEXT_ID /*4*/));
        this.classEditText.setKeyListener(new MyKeyListener(this.classEditText.getKeyListener(), EDITTEXT_CLASS /*5*/));
    }

    private void saveButtonAction() {
        getCells();
        deleteDeletedImages();
        if (this.accountTextView.getText() == null || this.accountTextView.getText().toString().isEmpty()) {
            showNeedAccountDialog();
        } else if (this.transaction.isRepeatingTransaction && (this.repeatingTransaction == null || !this.repeatingTransaction.isRepeating())) {
            showNeedRepeatingDialog();
        } else if (this.transaction.transactionID == 0 || this.transaction.isRepeatingTransaction || this.repeatingTransaction.repeatingID <= 0 || !this.repeatingTransaction.isRepeating() || this.dateChanged != 0) {
            saveAction();
        } else {
            Builder b = new Builder(this, PocketMoneyThemes.dialogTheme());
            b.setTitle(Locales.kLOC_GENERAL_EDIT);
            b.setMessage(Locales.kLOC_EDIT_TRANSACTION_CHANGE_INFO);
            b.setPositiveButton(Locales.kLOC_EDIT_TRANSACTION_CHANGE_BOTH, (dialog, which) -> {
                TransactionEditActivity.this.repeatingChanged = Enums.RepeatingChangeTypeUpdateRepeating /*2*/;
                TransactionEditActivity.this.saveAction();
            });
            b.setNegativeButton(Locales.kLOC_EDIT_TRANSACTION_CHANGE_THIS, (dialog, which) -> {
                TransactionEditActivity.this.repeatingChanged = Enums.RepeatingChangeTypeSeparateTransactionFromRepeating /*1*/;
                TransactionEditActivity.this.saveAction();
            });
            b.create().show();
        }
    }

    private void saveAction() {
        Log.d(TAG, "saveAction() called");
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
        android.text.Spanned body;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            body = android.text.Html.fromHtml(sb.toString(), android.text.Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            body = android.text.Html.fromHtml(sb.toString());
        }
        emailIntent.putExtra("android.intent.extra.TEXT", body);
        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            Log.i(getPackageName(), "phone doesnt support email intents");
        }
    }

    private void selectStartingCell() {
        Log.d(TAG, "selectStartingCell() called");
        clearFocus();
        if (this.transaction.getTransactionID() == 0) {
            String thePref = Prefs.getStringPref(Prefs.EDITTRANSACTION_STARTING_FIELD);
            InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (thePref.equals(Locales.kLOC_GENERAL_PAYEE)) {
                this.payeeEditText.requestFocus();
                if (mgr != null) {
                    mgr.showSoftInput(this.payeeEditText, 1);
                }
            } else if (thePref.equals(Locales.kLOC_GENERAL_CATEGORY)) {
                this.categoryEditText.requestFocus();
                if (mgr != null) {
                    mgr.showSoftInput(this.categoryEditText, 1);
                }
            } else if (thePref.equals(Locales.kLOC_GENERAL_AMOUNT)) {
                this.amountEditText.requestFocus();
                if (mgr != null) {
                    mgr.showSoftInput(this.amountEditText, 1);
                }
            } else {
                getWindow().setSoftInputMode(3);
            }
        } else {
            getWindow().setSoftInputMode(3);
        }
        clearDropDownsTimerStart();
    }

    private void clearFocus() {
        Log.d(TAG, "clearFocus() called");
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

    private void loadCells() {
        Log.d(TAG, "loadCells() called");
        this.programaticUpdate = true;
        setType();
        this.programaticUpdate = false;
        if (this.repeatingTransaction == null || !this.repeatingTransaction.isRepeating()) {
            this.dateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        } else {
            this.dateTextView.setTextColor(0xFF00FF00);
            //this.dateTextView.setTextColor(PocketMoneyThemes.orangeLabelColor());
        }
        this.dateTextView.setText(CalExt.descriptionWithMediumDate(this.transaction.getDate()));
        if (Prefs.getBooleanPref(Prefs.SHOWTIME)) {
            this.timeTextView.setVisibility(View.VISIBLE);
            this.timeTextView.setText(CalExt.descriptionWithShortTime(this.transaction.getDate()));
        } else {
            this.timeTextView.setVisibility(View.GONE);
        }
        this.accountTextView.setText(this.transaction.getAccount());
        if (this.transaction.getType() == Enums.kTransactionTypeTransferFrom/*3*/ || this.transaction.getType() == Enums.kTransactionTypeTransferTo /*2*/) {
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

    private void getCells() {
        Log.d(TAG, "getCells() called");
        saveAmountXrates();
        this.transaction.setDateFromString(this.dateTextView.getText().toString());
        if (Prefs.getBooleanPref(Prefs.SHOWTIME)) {
            this.transaction.updateDateWithTimeString(this.timeTextView.getText().toString());
        }
        this.transaction.setAccount(this.accountTextView.getText().toString());
        if (this.transaction.getType() == Enums.kTransactionTypeTransferFrom /*3*/ || this.transaction.getType() == Enums.kTransactionTypeTransferTo /*2*/) {
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
        Log.d(TAG, "deleteDeletedImages() called");
        File photoDir = new File(SMMoney.getAppContext().getFilesDir(), "photos");
        for (String deletedImage : this.deletedImages) {
            if (!new File(photoDir, deletedImage).delete()) {
                int hmm = 1;
            }
        }
    }

    private void deleteNewlyAddedImages() {
        Log.d(TAG, "deleteNewlyAddedImages() called");
        File photoDir = new File(SMMoney.getAppContext().getFilesDir(), "photos");
        for (String newlyAddedImage : this.newlyAddedImages) {
            if (!new File(photoDir, newlyAddedImage + ".jpg").delete()) {
                int hmm = 1;
            }
        }
    }

    private boolean save() {
        Log.d(TAG, "save() called");
        if (this.isIReceipt) {
            this.transaction.checkAccountAddIfMissing();
        }
        getCells();
        if (!this.transaction.isRepeatingTransaction && this.transaction.getDirty()) {
            TransactionClass originalRecord = new TransactionClass(this.transaction.getTransactionID());
            int modTransfer = useCanModifyChildTransferOfBasedOn(this.transaction, originalRecord);
            if (modTransfer == Enums.kModifyOtherEndOnly /*0*/) {
                AlertDialog alert = new Builder(this, PocketMoneyThemes.dialogTheme()).create();
                alert.setMessage(Locales.kLOC_EDIT_TRANSACTION_EDITOTHEREND);
                alert.setCancelable(false);
                alert.setButton(-1, Locales.kLOC_GENERAL_OK, (dialog, id) -> dialog.dismiss());
                alert.show();
                return false;
            }
            keepTheChangeUpdate();
            this.transaction.saveToDatabase();
            if (modTransfer == Enums.kModifyEitherEnd /*2*/) {
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
        Log.d(TAG, "saveRepeatingTransaction() called");
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
        } else if (this.repeatingChanged != Enums.RepeatingChangeTypeSeparateTransactionFromRepeating /*1*/ && this.dateChanged != Enums.DateChangeTypeSeparateTransactionFromRepeating /*1*/) {
            GregorianCalendar originalDate = (GregorianCalendar) this.repeatingTransaction.getTransaction().getDate().clone();
            this.repeatingTransaction.getTransaction().deleteFromDatabase();
            this.repeatingTransaction.setTransaction(null);
            this.repeatingTransaction.setTransaction(this.transaction.copy());
            this.repeatingTransaction.dirty = true;
            if (Enums.DateChangeTypeSeparateTransactionFromRepeating /*1*/ == this.dateChanged || (!this.transaction.isRepeatingTransaction && this.dateChanged == Enums.DateChangeTypeNone /*0*/)) {
                this.repeatingTransaction.getTransaction().setDate(originalDate);
            } else if (this.transaction.isRepeatingTransaction) {
                this.repeatingTransaction.setLastProcessedDate(CalExt.subtractDay(this.repeatingTransaction.getTransaction().getDate()));
            }
            this.repeatingTransaction.saveToDatabase();
            this.repeatingTransaction.setupNotification(getApplicationContext());
            if (processRepeatingEvents) {
                TransactionDB.addRepeatingTransactions();
            }
        }
    }

    private void reloadData() {
        Log.d(TAG, "reloadData() called");
        loadCells();
    }

    @SuppressWarnings("unused")
    private View.OnClickListener getBalanceBarClickListener() {
        return v -> {
            int i = Prefs.getIntPref(Prefs.BALANCETYPE);
            if (v.equals(TransactionEditActivity.this.balanceBar.nextButton)) {
                i = TransactionEditActivity.this.balanceBar.nextBalanceTypeAfter(i);
            } else {
                i = TransactionEditActivity.this.balanceBar.nextBalanceTypeBefore(i);
            }
            Prefs.setPref(Prefs.BALANCETYPE, i);
            TransactionEditActivity.this.reloadBalanceBar();
            TransactionEditActivity.this.reloadData();
        };
    }

    private void reloadBalanceBar() {
        Log.d(TAG, "reloadBalanceBar() called");
        if (this.transaction.isRepeatingTransaction) {
            this.balanceBar.setVisibility(View.GONE);
            return;
        }
        AccountClass account;
        if (this.accountTextView.getText().toString().length() <= 0) {
            account = AccountDB.recordFor(this.transaction.getAccount());
        } else {
            account = AccountDB.recordFor(this.accountTextView.getText().toString());
        }
        int balanceType = Prefs.getIntPref(Prefs.BALANCETYPE);
        if (account != null) {
            this.balanceBar.balanceAmountTextView.setText(account.formatAmountAsCurrency(account.balanceOfType(balanceType)));
        }
        if (account == null || !account.balanceExceedsLimit()) {
            this.balanceBar.balanceAmountTextView.setTextColor(ContextCompat.getColor(this, R.color.black_theme_text)/*Original value -1 = white*/);
        } else {
            this.balanceBar.balanceAmountTextView.setTextColor(ContextCompat.getColor(this, R.color.theme_red_label_color_on_black)/*Original color -65536*/);
        }
        this.balanceBar.balanceTypeTextView.setText(AccountDB.totalWorthLabel(balanceType));
        this.balanceBar.balanceTypeTextView.setTextColor(ContextCompat.getColor(this, R.color.black_theme_text)/* Original value -1 = white*/);
    }

    private void editTransactionDelete() {
        Log.d(TAG, "editTransactionDelete() called");
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
        Log.d(TAG, "clearKeepTheChange() called");
        if (this.changeKept != 0.0d) {
            AccountClass act = AccountDB.recordFor(this.transaction.getAccount());
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(CurrencyExt.amountFromStringWithCurrency(this.amountEditText.getText().toString(), act.getCurrencyCode()) - this.changeKept, act.getCurrencyCode()));
            this.changeKept = 0.0d;
        }
    }

    private void keepTheChange() {
        Log.d(TAG, "keepTheChange() called");
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
        Log.d(TAG, "keepTheChangeUpdate() called");
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
            keepTheChangeRecord.setType(Enums.kTransactionTypeTransferFrom /*3*/);
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
        Log.d(TAG, "splitsAction() called");
        getCells();
        Intent i = new Intent(this, SplitsActivity.class);
        i.putExtra("Transaction", this.transaction);
        i.putExtra("dontShowPass", "");
        splitsLauncher.launch(i);
    }

    @SuppressWarnings("unused")
    private int getType() {
        Log.d(TAG, "getType() called");
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
        Log.d(TAG, "setNotesText() called with: note = [" + note + "]");
        if (note == null || note.length() <= 0) {
            this.memoTextView.setText("");
            return;
        }
        TextView textView = this.memoTextView;
        textView.setText(note);
    }

    private void setType() {
        Log.d(TAG, "setType() called");
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
            if (this.transaction.getType() == Enums.kTransactionTypeTransferFrom /*3*/) {
                this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_FROM);
            } else {
                this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_TO);
            }
        }
    }

    private void includeFeeAction() {
        Log.d(TAG, "includeFeeAction() called");
        AccountClass account = AccountDB.recordFor(this.accountTextView.getText().toString());
        if (account.getFee() <= 0.0d) {
            showFeeDialog();
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
        Log.d(TAG, "deleteConfirmed() called");
        editTransactionDelete();
        finish();
    }

    private void duplicateTransaction(boolean todaysDate) {
        Log.d(TAG, "duplicateTransaction() called with: todaysDate = [" + todaysDate + "]");
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

    private void updateAmountFieldTextColor() {
        Log.d(TAG, "updateAmountFieldTextColor() called");
        if (this.transaction.getType() == Enums.kTransactionTypeWithdrawal /*0*/ || this.transaction.getType() == Enums.kTransactionTypeTransferTo /*2*/) {
            this.amountEditText.setTextColor(-65536);
        } else {
            this.amountEditText.setTextColor(-16711936);
        }
    }

    private void updateXrates() {
        Log.d(TAG, "updateXrates() called");
        getCells();
        if (this.transaction.isTransfer() && Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            AccountClass a1 = AccountDB.recordFor(this.transaction.getTransferToAccount());
            this.transaction.setXrate(xrateFromAccountToAccount(this.transaction.getAccount(), a1.getAccount()));
            this.transaction.setCurrencyCode(a1.getCurrencyCode());
            //this.amountXrateTextView.setText("x" + this.transaction.getXrate());
            double xRate = this.transaction.getXrate();
            String currencyCode = this.transaction.getCurrencyCode();
            String tempFxString = "1" + Prefs.getStringPref(Prefs.HOMECURRENCYCODE) + " = " + currencyCode + xRate;
            this.foreignAmountTextView.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal() / this.transaction.getXrate()), this.transaction.getCurrencyCode()));
            if (this.transaction.getSubTotal() == 0.0d) {
                this.amountEditText.setText("");
            } else {
                //this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal() / this.transaction.getXrate()), this.transaction.getCurrencyCode()));
                this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal()), Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
            }
            this.amountEditText.invalidate();
            this.foreignAmountTextView.setVisibility(View.VISIBLE);
            this.foreignAmountTextView.invalidate();
        }
    }

    private void loadAmountXrateValues() {
        Log.d(TAG, "loadAmountXrateValues() called");
        try {
            if (AccountDB.recordFor(this.transaction.getAccount()).getCurrencyCode().equals(this.transaction.getCurrencyCode())) {
                this.foreignAmountTextView.setVisibility(View.GONE);
                this.xRateTextView.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            this.foreignAmountTextView.setVisibility(View.GONE);
            this.xRateTextView.setVisibility(View.GONE);
        }
        if (this.transaction.getSubTotal() == 0.0d) {
            this.amountEditText.setText("");
        } else if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            double xRate = this.transaction.getXrate();
            String currencyCode = this.transaction.getCurrencyCode();
            String tempFxString = "1 " + Prefs.getStringPref(Prefs.HOMECURRENCYCODE) + " = " + xRate + " " + currencyCode;

            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal()), Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
            this.foreignAmountTextView.setVisibility(View.VISIBLE);
            this.xRateTextView.setVisibility(View.VISIBLE);

            //this.amountXrateTextView.setText("x" + CurrencyExt.exchangeRateAsString(this.transaction.getXrate()));
            this.foreignAmountTextView.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal() / this.transaction.getXrate()), this.transaction.getCurrencyCode()));
            this.xRateTextView.setText(tempFxString);
        } else {
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal())));
        }
    }

    private void saveAmountXrates() {
        double amount = CurrencyExt.amountFromString(this.amountEditText.getText().toString());
        double multiplier = 1.0d;
        if (this.transaction.getType() == Enums.kTransactionTypeWithdrawal /*0*/ || this.transaction.getType() == Enums.kTransactionTypeTransferTo /*2*/) {
            multiplier = -1.0d;
        }

        double newSubTotal = Math.abs(amount) * multiplier;
        this.transaction.setSubTotal(newSubTotal);

        // Only auto-update the first split if we aren't currently using multiple splits
        if (this.transaction.getNumberOfSplits() <= 1) {
            this.transaction.setAmount(newSubTotal);
        }
    }

    private int useCanModifyChildTransferOfBasedOn(TransactionClass newRecord, TransactionClass oldRecord) {
        Log.d(TAG, "useCanModifyChildTransferOfBasedOn() called with: newRecord = [" + newRecord + "], oldRecord = [" + oldRecord + "]");
        int transferRecID;
        int transferSplitItem = 0;
        boolean regularTransfer = newRecord.getCurrencyCode().equals(AccountDB.recordFor(newRecord.getAccount()).getCurrencyCode());
        if (oldRecord.getNumberOfSplits() > 1 || oldRecord.getTransferToAccount() == null || oldRecord.getTransferToAccount().length() <= 0) {
            return Enums.kModifyEitherEnd /*2*/;
        }
        if (oldRecord.getDate().equals(newRecord.getDate()) && oldRecord.getAmount() == newRecord.getAmount() && oldRecord.getXrate() == newRecord.getXrate() && !oldRecord.getTransferToAccount().equals(newRecord.getTransferToAccount()) && !oldRecord.getCategory().equals(newRecord.getCategory()) && !oldRecord.getMemo().equals(newRecord.getMemo()) && !oldRecord.getClassName().equals(newRecord.getClassName())) {
            return Enums.kModifyThisEndOnly /*1*/;
        }
        TransactionTransferRetVals ret = new TransactionTransferRetVals();
        TransactionDB.transactionGetTransfer(oldRecord.getTransferToAccount(), oldRecord.getAccount(), oldRecord.getDate(), regularTransfer ? (-1.0d * oldRecord.getAmount()) / oldRecord.getXrate() : -1.0d * oldRecord.getAmount(), regularTransfer ? null : oldRecord.getCurrencyCode(), ret);
        transferRecID = ret.transferRecID;
        transferSplitItem = ret.transferSplitItem;
        if (transferRecID == 0 || new TransactionClass(transferRecID).getNumberOfSplits() <= 1) {
            return Enums.kModifyEitherEnd /*2*/;
        }
        return Enums.kModifyOtherEndOnly /*0*/;
    }

    private void saveUpdateTransferFromOriginalRecord(TransactionClass oldRecP, TransactionClass modRecP) {
        Log.d(TAG, "saveUpdateTransferFromOriginalRecord() called with: oldRecP = [" + oldRecP + "], modRecP = [" + modRecP + "]");
        double d;
        double newRate;
        double newAmount;
        TransactionClass transferRecord;
        int transferRecID;
        int transferSplitItem;
        String currencyCode = AccountDB.recordFor(modRecP.getAccount()).getCurrencyCode();
        String tToCurrencyCode = "";
        int i = 0;
        while (i < oldRecP.getNumberOfSplits()) {
            double amountAtIndex;
            double xrate;
            if (oldRecP.getTransferToAccountAtIndex(i) != null && !oldRecP.getTransferToAccountAtIndex(i).isEmpty()) {
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
                        if (AccountDB.recordFor(transactionClass.getAccount()) == null && transactionClass.getAccount() != null && !transactionClass.getAccount().isEmpty()) {
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
            if (modRecP.getTransferToAccountAtIndex(i) != null && !modRecP.getTransferToAccountAtIndex(i).isEmpty() && ((i < oldRecP.getNumberOfSplits() && (oldRecP.getTransferToAccountAtIndex(i) == null || oldRecP.getTransferToAccountAtIndex(i).isEmpty())) || i >= oldRecP.getNumberOfSplits())) {
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

    private double xrateFromAccountToAccount(String account1, String account2) {
        Log.d(TAG, "xrateFromAccountToAccount() called with: account1 = [" + account1 + "], account2 = [" + account2 + "]");
        return AccountDB.recordFor(account1).getExchangeRate() / AccountDB.recordFor(account2).getExchangeRate();
    }

    private void editTextDidFinishChanging(int editTextCode) {
        Log.d(TAG, "editTextDidFinishChanging() called with: editTextCode = [" + editTextCode + "]");
        if (editTextCode == EDITTEXT_AMOUNT) {
            saveAmountXrates();
            loadAmountXrateValues();
        }
    }

    @SuppressWarnings("EmptyMethod")
    private void editTextDidChange(int editTextCode) {
    }

    private void clearDropDowns() {
        Log.d(TAG, "clearDropDowns() called");
        this.categoryEditText.dismissDropDown();
        this.payeeEditText.dismissDropDown();
        this.idEditText.dismissDropDown();
        this.classEditText.dismissDropDown();
    }

    private void clearDropDownsTimerStart() {
        Log.d(TAG, "clearDropDownsTimerStart() called");
        this.clearTimer.schedule(new ClearTask(), 750);
    }

    private void showDeleteConfirmDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, item) -> dialog.dismiss()).setPositiveButton(Locales.kLOC_GENERAL_DELETE, (dialog, item) -> TransactionEditActivity.this.deleteConfirmed()).show();
    }

    private void showDuplicateDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_DUPLICATE_TRANSACTION_TITLE)
                .setNegativeButton(Locales.kLOC_DUPLICATE_TRANSACTION_EXISTING_TIME, (dialog, item) -> TransactionEditActivity.this.duplicateTransaction(false)).setPositiveButton(Locales.kLOC_DUPLICATE_TRANSACTION_PRESENT_TIME, (dialog, item) -> TransactionEditActivity.this.duplicateTransaction(true)).show();
    }

    private void showFeeDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setMessage(Locales.kLOC_FEE_MISSING_ALERT)
                .setNegativeButton(Locales.kLOC_GENERAL_OK, (dialog, item) -> dialog.dismiss()).show();
    }

    private void showTimePickerDialog() {
        GregorianCalendar theTime = this.transaction.getDate();
        new TimePickerDialog(this, PocketMoneyThemes.timePickerTheme(), this.mTimeSetListener, theTime.get(Calendar.HOUR_OF_DAY), theTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(this)).show();
    }

    private void showNeedAccountDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setMessage(Locales.kLOC_EDIT_TRANSACTION_MISSINGACCOUNT)
                .setNegativeButton(Locales.kLOC_GENERAL_OK, (dialog, item) -> dialog.dismiss()).show();
    }

    private void showNeedRepeatingDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setMessage("How often this transaction repeats must be entered before you can save a repeating transaction.\n\nTap the calendar icon to the right of the Date to configure the repeating info for this transaction.")
                .setNegativeButton(Locales.kLOC_GENERAL_OK, (dialog, item) -> dialog.dismiss()).show();
    }

    private void showCameraDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setMessage("Choose existing or take new")
                .setPositiveButton("New", (dialog, item) -> {
                    try {
                        File cacheDir = getExternalCacheDir();
                        TransactionEditActivity.this.tempPhotoPath = new File(cacheDir, "temp.jpg");
                        if (tempPhotoPath.exists()) tempPhotoPath.delete();
                        tempPhotoPath.createNewFile();

                        Uri photoUri = FileProvider.getUriForFile(TransactionEditActivity.this, "com.example.fileprovider", TransactionEditActivity.this.tempPhotoPath);
                        cameraLauncher.launch(photoUri);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception in showCameraDialog (New)", e);
                    }
                })
                .setNegativeButton("Choose", (dialog, item) -> {
                    dialog.dismiss();
                    galleryLauncher.launch("image/*");
                }).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SPLIT /*1*/, 0, Locales.kLOC_GENERAL_SPLIT).setIcon(R.drawable.ic_arrow_drop_down_circle);
        menu.add(0, MENU_DUPE /*2*/, 0, Locales.kLOC_GENERAL_DUPLICATE).setIcon(R.drawable.ic_arrow_drop_down_circle);
        if (this.transaction.getAccount() != null) {
            menu.add(0, MENU_FEE /*3*/, 0, Locales.kLOC_ACCOUNT_FEE_LABEL).setIcon(R.drawable.ic_arrow_drop_down_circle);
        }
        if (SMMoney.hasCamera()) {
            menu.add(0, MENU_CAMERA /*4*/, 0, "Camera").setIcon(R.drawable.ic_arrow_drop_down_circle);
        }
        menu.add(0, MENU_DELETE /*5*/, 0, Locales.kLOC_GENERAL_DELETE).setIcon(R.drawable.ic_arrow_drop_down_circle);
        MenuItem item = menu.add(0, MENU_SAVE /*6*/, 0, Locales.kLOC_GENERAL_SAVE);
        item.setIcon(R.drawable.ic_save_white_24dp);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS /*2*/);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        switch (item.getItemId()) {
            case MENU_SPLIT /*1*/ -> splitsAction();
            case MENU_DUPE /*2*/ -> showDuplicateDialog();
            case MENU_FEE /*3*/ -> includeFeeAction();
            case MENU_CAMERA /*4*/ -> {
                if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    showCameraDialog();
                } else {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
                }
            }
            case MENU_DELETE /*5*/ -> showDeleteConfirmDialog();
            case MENU_SAVE /*6*/ -> {
                Log.d(TAG, "onOptionsItemSelected() called with: item = [" + item + "] - i.e. MENU_SAVE");
                saveButtonAction();
            }
            default -> {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    private View.OnClickListener getBtnClickListener() {
        return view -> {
            if ((Integer) view.getTag() == NOTE_EDIT_BUTTON) { /*30*/
                Intent i = new Intent(TransactionEditActivity.this.currentActivity, NoteEditor.class);
                i.putExtra("note", TransactionEditActivity.this.transaction.getMemo());
                noteLauncher.launch(i);
            }
        };
    }

    private OnCheckedChangeListener getRadioChangedListener() {
        return (group, checkedId) -> {
            if (!TransactionEditActivity.this.programaticUpdate) {
                TransactionEditActivity.this.clearKeepTheChange();
                if (checkedId == R.id.withdrawalbutton) {
                    TransactionEditActivity.this.payeeEditText.setEnabled(true);
                    TransactionEditActivity.this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TO);
                    TransactionEditActivity.this.transaction.setType(Enums.kTransactionTypeWithdrawal /*0*/);
                    TransactionEditActivity.this.reloadData();
                } else if (checkedId == R.id.depositbutton) {
                    TransactionEditActivity.this.payeeEditText.setEnabled(true);
                    TransactionEditActivity.this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_FROM);
                    TransactionEditActivity.this.transaction.setType(Enums.kTransactionTypeDeposit /*1*/);
                } else if (checkedId == R.id.transferbutton) {
                    TransactionEditActivity.this.payeeEditText.setEnabled(false);
                    if (TransactionEditActivity.this.transaction.getSubTotal() > 0.0d) {
                        TransactionEditActivity.this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_FROM);
                        TransactionEditActivity.this.transaction.setType(Enums.kTransactionTypeTransferFrom /*3*/);
                    } else {
                        TransactionEditActivity.this.payeeLabelTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_TO);
                        TransactionEditActivity.this.transaction.setType(Enums.kTransactionTypeTransferTo /*2*/);
                    }
                    TransactionEditActivity.this.getCells();
                    Intent i = new Intent(TransactionEditActivity.this.currentActivity, LookupsListActivity.class);
                    i.putExtra("type", 3 /* LOOKUP TYPE - see LookupsListActivity.class switch statement */);
                    transferLauncher.launch(i);
                }
                TransactionEditActivity.this.getCells();
                TransactionEditActivity.this.reloadData();
            }
        };
    }

    private OnFocusChangeListener getFocusChangedListenerWithID(int id) {
        final int theID = id;
        return (v, hasFocus) -> {
            if (!hasFocus) {
                TransactionEditActivity.this.editTextDidFinishChanging(theID);
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown() called with: keyCode = [" + keyCode + "], event = [" + event + "]");
        if (keyCode == 4) {
            if (this.currencyKeyboard.hide()) {
                return false;
            }
            deleteNewlyAddedImages();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        TransactionEditActivity.this.dateTextView.setText(CalExt.descriptionWithMediumDate(new GregorianCalendar(year, monthOfYear, dayOfMonth)));
        if ((TransactionEditActivity.this.repeatingDateChangedAlert != null && TransactionEditActivity.this.repeatingDateChangedAlert.isShowing()) || TransactionEditActivity.this.transaction.isRepeatingTransaction || TransactionEditActivity.this.repeatingTransaction == null || !TransactionEditActivity.this.repeatingTransaction.isRepeating()) {
            return;
        }
        if (TransactionEditActivity.this.transaction.transactionID == 0) {
            TransactionEditActivity.this.dateChanged = Enums.DateChangeTypeUpdateRepeating /*2*/;
            return;
        }
        Builder b = new Builder(TransactionEditActivity.this, PocketMoneyThemes.dialogTheme());
        b.setTitle("Date");
        b.setMessage("Change the date of the transaction and repeating event, or change the date of this transaction only?");
        b.setPositiveButton("Both", (dialog, which) -> {
            TransactionEditActivity.this.getCells();
            if (!(TransactionEditActivity.this.repeatingTransaction.getTransaction() == null || (TransactionEditActivity.this.repeatingTransaction.repeatsOnDate(TransactionEditActivity.this.repeatingTransaction.getTransaction().getDate()) && TransactionEditActivity.this.repeatingTransaction.repeatsOnDate(TransactionEditActivity.this.transaction.getDate())))) {
                Builder b1 = new Builder(TransactionEditActivity.this, PocketMoneyThemes.dialogTheme());
                b1.setTitle("");
                b1.setMessage("Please ensure that the start date selected follows the rules of the repeating transaction. If you are changing an existing repeating transaction you may have to update the repeating settings in the repeating edit screen.");
                b1.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog1, which1) -> dialog1.dismiss());
                b1.create().show();
            }
            TransactionEditActivity.this.dateChanged = Enums.DateChangeTypeUpdateRepeating /*2*/;
            dialog.dismiss();
        });
        b.setNegativeButton("This item only", (dialog, which) -> {
            TransactionEditActivity.this.dateChanged = Enums.DateChangeTypeSeparateTransactionFromRepeating /*1*/;
            dialog.dismiss();
        });
        TransactionEditActivity.this.repeatingDateChangedAlert = b.create();
        TransactionEditActivity.this.repeatingDateChangedAlert.show();
    }

    class ClearTask extends TimerTask {
        ClearTask() {
        }

        public void run() {
            TransactionEditActivity.this.mHandler.sendMessage(Message.obtain(TransactionEditActivity.this.mHandler, MSG_CLEARDROPDOWNS /*1*/, 0, 0));
        }
    }

    private class MyKeyListener implements KeyListener {
        final KeyListener original;
        private final int editTextCode;
        private final int suggest;

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
}
