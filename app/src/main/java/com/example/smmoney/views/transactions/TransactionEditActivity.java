package com.example.smmoney.views.transactions;

import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.misc.TransactionTransferRetVals;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class TransactionEditActivity extends PocketMoneyActivity {
    public static final int REQUEST_PHOTO_OPTION = 37;
    public static final String TAG = "TRANS_EDIT_ACTIVITY";
    private final int EDITTEXT_AMOUNT = 3;
    private final int EDITTEXT_CATEGORY = 2;
    private final int EDITTEXT_CLASS = 5;
    private final int EDITTEXT_ID = 4;
    private final int EDITTEXT_PAYEE = 1;
    private final int MENU_SAVE = 6;
    private final int MSG_CLEARDROPDOWNS = 1;

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
    private int dateChanged;
    private TextView dateTextView;
    private final ArrayList<String> deletedImages = new ArrayList<>();
    private MaterialButton depositButton;
    private AutoCompleteTextView idEditText;
    private boolean isIReceipt = false;
    private boolean isLocalNotification = false;
    private final ArrayList<String> newlyAddedImages = new ArrayList<>();
    private EditText memoEditText;
    private TextView keepTheChangeButton;
    private AutoCompleteTextView payeeEditText;
    private TextView payeeLabelTextView;
    private boolean programaticUpdate;
    private int repeatingChanged;
    private ImageView repeatingImageView;
    private RepeatingTransactionClass repeatingTransaction;
    private FrameLayout keyboardToolBar;
    private File tempPhotoPath;
    private TextView timeTextView;
    private ScrollView scrollView;
    private TransactionClass transaction;
    private MaterialButton transferButton;
    private MaterialButton withdrawalButton;
    private WakeLock wakeLock;

    private final OnTimeSetListener mTimeSetListener = (view, hourOfDay, minute) -> {
        GregorianCalendar date = transaction.getDate();
        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
        date.set(Calendar.MINUTE, minute);
        transaction.setDate(date);
        timeTextView.setText(CalExt.descriptionWithShortTime(date));
        transaction.dirty = true;
    };

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_CLEARDROPDOWNS) {
                clearDropDowns();
            }
        }
    };

    public final ActivityResultLauncher<Intent> photoOptionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null) {
            String fileName = result.getData().getStringExtra("imageName");
            if (result.getResultCode() == PhotoReceiptOptionsActivity.RESULT_DELETED) {
                if (fileName != null) {
                    ArrayList<String> images = this.transaction.imageFileNames();
                    images.remove(fileName);
                    this.transaction.setImageLocation(this.transaction.imageLocationFromNames(images));
                    this.transaction.dirty = true;
                    reloadData();
                }
            } else if (result.getResultCode() == PhotoReceiptOptionsActivity.RESULT_REPLACE) {
                // DON'T delete yet. Just open the picker.
                // We will handle the deletion inside the camera/gallery success callbacks
                // to ensure we only remove the old photo if a new one is actually provided.
                showCameraDialog(fileName); // Pass the old filename so we know what to replace
            }
        }
    });
    
    private final ActivityResultLauncher<Intent> accountLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.transaction.setAccount(selection);
            this.transaction.dirty = true;
            reloadData();
        }
    });

    private final ActivityResultLauncher<Intent> payeeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            if (this.transaction.isTransfer()) {
                this.transaction.setTransferToAccount(selection);
            } else {
                this.transaction.setPayee(selection);
            }
            this.transaction.dirty = true;
            reloadData();
        }
    });

    private final ActivityResultLauncher<Intent> categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.transaction.setCategory(selection);
            this.transaction.dirty = true;
            reloadData();
        }
    });

    private final ActivityResultLauncher<Intent> classLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.transaction.setClassName(selection);
            this.transaction.dirty = true;
            reloadData();
        }
    });

    private final ActivityResultLauncher<Intent> idLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.transaction.setCheckNumber(selection);
            this.transaction.dirty = true;
            reloadData();
        }
    });

    private final ActivityResultLauncher<Intent> splitsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1 && result.getData() != null) {
            this.transaction = (TransactionClass) result.getData().getExtras().get("Transaction");
            this.transaction.dirty = true;
            reloadData();
            reloadBalanceBar();
        }
    });

    private final ActivityResultLauncher<Intent> repeatingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1 && result.getData() != null) {
            this.transaction = (TransactionClass) result.getData().getExtras().get("Transaction");
            this.repeatingTransaction = (RepeatingTransactionClass) result.getData().getExtras().get("RepeatingTransaction");
            this.transaction.dirty = true;
            this.transaction.hydrated = true;
            this.repeatingTransaction.dirty = true;
            if (this.repeatingTransaction.getTransaction() != null) {
                this.repeatingTransaction.getTransaction().hydrated = false;
            }
            reloadData();
        }
    });

    private final ActivityResultLauncher<Intent> transferLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.transaction.setTransferToAccount(selection);
            this.transaction.dirty = true;
            reloadData();
        }
    });

    private final ActivityResultLauncher<Intent> currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1 && result.getData() != null) {
            Bundle b = result.getData().getExtras();
            if (b != null) {
                double xrate = b.getDouble("xrate", 1.0);
                double amount = b.getDouble("amount", 0.0);
                String currency = b.getString("currency");
                
                this.transaction.setXrate(xrate);
                this.transaction.setCurrencyCode(currency);
                
                double multiplier = (this.transaction.getType() == 0 || this.transaction.getType() == 2) ? -1.0d : 1.0d;
                this.transaction.setSubTotal(Math.abs(amount) * multiplier);
                if (this.transaction.getNumberOfSplits() <= 1) {
                    this.transaction.setAmount(this.transaction.getSubTotal());
                }
                
                this.transaction.dirty = true;
                reloadData();
            }
        }
    });

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            showCameraDialog();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    });

    private String imageToReplace = null;

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
        if (result) {
            try {
                // If replacing, remove the old one first
                if (imageToReplace != null) {
                    ArrayList<String> images = this.transaction.imageFileNames();
                    images.remove(imageToReplace);
                    this.transaction.setImageLocation(this.transaction.imageLocationFromNames(images));
                    imageToReplace = null;
                }

                File photoDir = new File(getFilesDir(), "photos");
                if (!photoDir.exists()) photoDir.mkdirs();
                String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(photoDir, fileName);
                
                try (FileInputStream in = new FileInputStream(tempPhotoPath);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
                
                ArrayList<String> images = this.transaction.imageFileNames();
                images.add(fileName);
                this.transaction.setImageLocation(this.transaction.imageLocationFromNames(images));
                this.newlyAddedImages.add(fileName);
                this.transaction.dirty = true;
                reloadData();
            } catch (Exception e) {
                Log.e(TAG, "Error saving camera photo", e);
            }
        }
    });

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (result != null) {
            try {
                // If replacing, remove the old one first
                if (imageToReplace != null) {
                    ArrayList<String> images = this.transaction.imageFileNames();
                    images.remove(imageToReplace);
                    this.transaction.setImageLocation(this.transaction.imageLocationFromNames(images));
                    imageToReplace = null;
                }

                File photoDir = new File(getFilesDir(), "photos");
                if (!photoDir.exists()) photoDir.mkdirs();
                String fileName = "gallery_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(photoDir, fileName);

                try (InputStream in = getContentResolver().openInputStream(result)) {
                    try (FileOutputStream out = new FileOutputStream(destFile)) {
                        byte[] buffer = new byte[1024];
                        int read;
                        if (in != null) {
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                        }
                    }
                }
                
                ArrayList<String> images = this.transaction.imageFileNames();
                images.add(fileName);
                this.transaction.setImageLocation(this.transaction.imageLocationFromNames(images));
                this.newlyAddedImages.add(fileName);
                this.transaction.dirty = true;
                reloadData();
            } catch (Exception e) {
                Log.e(TAG, "Error saving gallery photo", e);
            }
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "TransactionEditActivity:WakeLock");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.transaction = (TransactionClass) extras.getSerializable("Transaction");
            this.isIReceipt = extras.getBoolean("isIReceipt", false);
            this.isLocalNotification = extras.getBoolean("isLocalNotification", false);
        }
        
        if (this.transaction == null) {
            this.transaction = new TransactionClass();
        }
        this.transaction.hydrate();
        this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
        this.repeatingTransaction.hydrate();

        setContentView(R.layout.transaction_edit);
        setupButtons();
        reloadData();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_EDIT_TRANSACTION_TITLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadData();
        reloadBalanceBar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void setupButtons() {
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.scrollView = findViewById(R.id.scroll_view);
        this.scrollView.setVerticalScrollBarEnabled(false);
        
        this.withdrawalButton = findViewById(R.id.withdrawalbutton);
        this.depositButton = findViewById(R.id.depositbutton);
        this.transferButton = findViewById(R.id.transferbutton);
        this.balanceBar = findViewById(R.id.balancebar);
        this.payeeLabelTextView = findViewById(R.id.payeelabeltextview);
        
        MaterialButtonToggleGroup group = findViewById(R.id.radiogroup);
        group.addOnButtonCheckedListener(getRadioChangedListener());
        
        android.content.res.ColorStateList bgTint = PocketMoneyThemes.segmentedButtonBackgroundTint();
        android.content.res.ColorStateList textTint = PocketMoneyThemes.segmentedButtonTextTint();
        android.content.res.ColorStateList strokeTint = android.content.res.ColorStateList.valueOf(PocketMoneyThemes.currentTintColor());
        
        this.withdrawalButton.setBackgroundTintList(bgTint);
        this.withdrawalButton.setTextColor(textTint);
        this.withdrawalButton.setStrokeColor(strokeTint);
        
        this.depositButton.setBackgroundTintList(bgTint);
        this.depositButton.setTextColor(textTint);
        this.depositButton.setStrokeColor(strokeTint);
        
        this.transferButton.setBackgroundTintList(bgTint);
        this.transferButton.setTextColor(textTint);
        this.transferButton.setStrokeColor(strokeTint);
        
        this.dateTextView = findViewById(R.id.datetextview);
        this.dateTextView.setOnClickListener(v -> {
            GregorianCalendar date = transaction.getDate();
            new DatePickerDialog(this, PocketMoneyThemes.datePickerTheme(), (view, year, monthOfYear, dayOfMonth) -> {
                GregorianCalendar date1 = transaction.getDate();
                date1.set(Calendar.YEAR, year);
                date1.set(Calendar.MONTH, monthOfYear);
                date1.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                transaction.setDate(date1);
                dateTextView.setText(CalExt.descriptionWithMediumDate(date1));
                transaction.dirty = true;
            }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show();
        });

        this.timeTextView = findViewById(R.id.timetextview);
        this.timeTextView.setOnClickListener(v -> showTimePickerDialog());
        
        this.repeatingImageView = findViewById(R.id.repeatingimageview);
        this.repeatingImageView.setOnClickListener(v -> repeatingAction());
        
        this.accountTextView = findViewById(R.id.accounttextview);
        this.accountTextView.setOnClickListener(v -> accountAction());
        findViewById(R.id.account_drop_down).setOnClickListener(v -> accountAction());
        
        this.payeeEditText = findViewById(R.id.payeetextview);
        this.payeeEditText.setOnClickListener(v -> payeeAction());
        findViewById(R.id.payee_drop_down).setOnClickListener(v -> payeeAction());
        
        this.categoryEditText = findViewById(R.id.categoryedittext);
        this.categoryTextView = findViewById(R.id.categorytextview);
        this.categoryEditText.setOnClickListener(v -> categoryAction());
        findViewById(R.id.category_drop_down).setOnClickListener(v -> categoryAction());
        
        this.classEditText = findViewById(R.id.classedittext);
        this.classTextView = findViewById(R.id.classtextview);
        this.classEditText.setOnClickListener(v -> classAction());
        findViewById(R.id.class_drop_down).setOnClickListener(v -> classAction());
        
        this.amountEditText = findViewById(R.id.amountedittext);
        this.amountEditText.setOnClickListener(v -> {
            amountAction();
            scrollView.smoothScrollTo(0, amountEditText.getTop());
        });
        findViewById(R.id.amount_currency_button).setOnClickListener(v -> currencyAction());
        
        this.idEditText = findViewById(R.id.idedittext);
        this.idEditText.setOnClickListener(v -> idAction());
        findViewById(R.id.id_drop_down).setOnClickListener(v -> idAction());
        
        this.clearedCheckBox = findViewById(R.id.clearedcheckbox);
        CheckBoxTint.colorCheckBox(this.clearedCheckBox);
        this.clearedCheckBox.setOnClickListener(v -> {
            transaction.setCleared(clearedCheckBox.isChecked());
            transaction.dirty = true;
        });

        this.memoEditText = findViewById(R.id.memoedittext);

        findViewById(R.id.split_button).setOnClickListener(v -> splitsAction());
        findViewById(R.id.camera_button).setOnClickListener(v -> {
            getCells();
            ArrayList<String> images = transaction.imageFileNames();
            if (images != null && !images.isEmpty()) {
                Intent intent = new Intent(this, PhotoReceiptOptionsActivity.class);
                intent.putExtra("imageName", images.get(0));
                photoOptionLauncher.launch(intent);
            } else {
                showCameraDialog();
            }
        });
        findViewById(R.id.duplicate_button).setOnClickListener(v -> showDuplicateDialog());
        findViewById(R.id.fee_button).setOnClickListener(v -> includeFeeAction());
        findViewById(R.id.delete_button).setOnClickListener(v -> showDeleteConfirmDialog());

        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();
        int primaryTextColor = PocketMoneyThemes.primaryCellTextColor();
        int tintColor = PocketMoneyThemes.currentTintColor();
        PorterDuff.Mode srcIn = PorterDuff.Mode.SRC_IN;

        ((TextView) findViewById(R.id.date_label)).setTextColor(fieldLabelColor);
        ((TextView) findViewById(R.id.account_label)).setTextColor(fieldLabelColor);
        ((TextView) findViewById(R.id.payeelabeltextview)).setTextColor(fieldLabelColor);
        ((TextView) findViewById(R.id.category_label)).setTextColor(fieldLabelColor);
        ((TextView) findViewById(R.id.amount_label)).setTextColor(fieldLabelColor);
        ((TextView) findViewById(R.id.id_label)).setTextColor(fieldLabelColor);
        ((TextView) findViewById(R.id.cleared_label)).setTextColor(fieldLabelColor);
        ((TextView) findViewById(R.id.class_label)).setTextColor(fieldLabelColor);
        ((TextView) findViewById(R.id.memo_label)).setTextColor(fieldLabelColor);

        // Theme the dividers for visibility in all themes
        int[] dividerIds = {R.id.divider1, R.id.divider2, R.id.divider3, R.id.divider4, R.id.divider5, R.id.divider6, R.id.divider7, R.id.divider8, R.id.divider9};
        for (int id : dividerIds) {
            View divider = findViewById(id);
            if (divider != null) {
                divider.setBackgroundColor(fieldLabelColor);
                divider.setAlpha(0.3f); // Subtle but visible
            }
        }

        this.dateTextView.setTextColor(primaryTextColor);
        this.timeTextView.setTextColor(primaryTextColor);
        this.accountTextView.setTextColor(primaryTextColor);
        this.payeeEditText.setTextColor(primaryTextColor);
        this.categoryEditText.setTextColor(primaryTextColor);
        this.categoryTextView.setTextColor(primaryTextColor);
        this.amountEditText.setTextColor(primaryTextColor);
        this.idEditText.setTextColor(primaryTextColor);
        this.classEditText.setTextColor(primaryTextColor);
        this.classTextView.setTextColor(primaryTextColor);
        this.memoEditText.setTextColor(primaryTextColor);
        this.foreignAmountTextView = findViewById(R.id.foreign_amount_text_view);
        this.foreignAmountTextView.setTextColor(primaryTextColor);
        this.xRateTextView = findViewById(R.id.amount_xrate_text_view);
        this.xRateTextView.setTextColor(primaryTextColor);

        this.repeatingImageView.setColorFilter(fieldLabelColor, srcIn);
        ((ImageView) findViewById(R.id.account_drop_down)).setColorFilter(fieldLabelColor, srcIn);
        ((ImageView) findViewById(R.id.payee_drop_down)).setColorFilter(fieldLabelColor, srcIn);
        ((ImageView) findViewById(R.id.category_drop_down)).setColorFilter(fieldLabelColor, srcIn);
        ((ImageView) findViewById(R.id.id_drop_down)).setColorFilter(fieldLabelColor, srcIn);
        ((ImageView) findViewById(R.id.class_drop_down)).setColorFilter(fieldLabelColor, srcIn);
        ((ImageView) findViewById(R.id.amount_currency_button)).setColorFilter(fieldLabelColor, srcIn);
        ((ImageView) findViewById(R.id.split_button)).setColorFilter(fieldLabelColor, srcIn);
        ((ImageView) findViewById(R.id.camera_button)).setColorFilter(fieldLabelColor, srcIn);

        ((MaterialButton) findViewById(R.id.duplicate_button)).setTextColor(tintColor);
        ((MaterialButton) findViewById(R.id.fee_button)).setTextColor(tintColor);

        TextWatcher dirtyWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) { if (!programaticUpdate) transaction.dirty = true; }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        this.payeeEditText.addTextChangedListener(dirtyWatcher);
        this.memoEditText.addTextChangedListener(dirtyWatcher);
        this.amountEditText.addTextChangedListener(dirtyWatcher);
        this.idEditText.addTextChangedListener(dirtyWatcher);
        this.categoryEditText.addTextChangedListener(dirtyWatcher);
        this.classEditText.addTextChangedListener(dirtyWatcher);

        this.currencyKeyboard = findViewById(R.id.keyboardView);
        this.currencyKeyboard.setEditText(this.amountEditText, () -> {
            if (amountEditText.hasFocus()) {
                clearKeepTheChange();
                AccountClass act = AccountDB.recordFor(transaction.getAccount());
                boolean keepTheChangeEnabled = act != null && act.getKeepTheChangeAccount() != null && !act.getKeepTheChangeAccount().isEmpty() && (transaction.getType() == 0 || transaction.getType() == 2);
                currencyKeyboard.setToolbarEnabled(keepTheChangeEnabled);
                return;
            }
            saveAmountXrates();
            loadAmountXrateValues();
        });

        this.keyboardToolBar = findViewById(R.id.keyboard_toolbar);
        this.keyboardToolBar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.currencyKeyboard.setToolbarView(this.keyboardToolBar);
        this.keepTheChangeButton = findViewById(R.id.keep_the_change_toolbar_button);
        this.keepTheChangeButton.setOnClickListener(v -> keepTheChange());
        
        findViewById(R.id.subcategory_toolbar_button).setOnClickListener(v -> {
            Intent i = new Intent(this, CategoryLookupListActivity.class);
            i.putExtra("type", 5);
            i.putExtra("isSubCategory", true);
            categoryLauncher.launch(i);
        });

        if (Prefs.getBooleanPref(Prefs.AUTO_FILL)) {
            this.categoryEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CategoryClass.allCategoryNamesInDatabase()));
            this.payeeEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, PayeeClass.allPayeesInDatabase()));
            this.idEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, IDClass.allCategoriesInDatabase()));
            this.classEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ClassNameClass.allClassNamesInDatabase()));
        }
        
        this.categoryEditText.setThreshold(2);
        this.payeeEditText.setThreshold(2);
        this.idEditText.setThreshold(2);
        this.classEditText.setThreshold(2);

        this.payeeEditText.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) editTextDidFinishChanging(EDITTEXT_PAYEE); });
        this.categoryEditText.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) editTextDidFinishChanging(EDITTEXT_CATEGORY); });
        this.idEditText.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) editTextDidFinishChanging(EDITTEXT_ID); });
        this.classEditText.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) editTextDidFinishChanging(EDITTEXT_CLASS); });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (transaction.dirty) {
                    new Builder(TransactionEditActivity.this, PocketMoneyThemes.dialogTheme())
                            .setTitle(R.string.dialog_discard_changes_title)
                            .setMessage(R.string.dialog_discard_changes_message)
                            .setPositiveButton(R.string.dialog_discard_changes_positive, (dialog, which) -> finish())
                            .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null)
                            .show();
                } else {
                    finish();
                }
            }
        });
    }

    private void accountAction() {
        getCells();
        Intent i = new Intent(this, LookupsListActivity.class);
        i.putExtra("type", 3);
        accountLauncher.launch(i);
    }

    private void payeeAction() {
        getCells();
        if (transaction.isTransfer()) {
            Intent i = new Intent(this, LookupsListActivity.class);
            i.putExtra("type", 3);
            transferLauncher.launch(i);
            return;
        }
        Intent i = new Intent(this, CategoryLookupListActivity.class);
        i.putExtra("category", categoryEditText.getText().toString());
        payeeLauncher.launch(i);
    }

    private void categoryAction() {
        if (transaction.getNumberOfSplits() > 1) {
            splitsAction();
            return;
        }
        getCells();
        Intent i = new Intent(this, CategoryLookupListActivity.class);
        i.putExtra("payee", payeeEditText.getText().toString());
        categoryLauncher.launch(i);
    }

    private void classAction() {
        if (transaction.getNumberOfSplits() > 1) {
            splitsAction();
            return;
        }
        getCells();
        Intent i = new Intent(this, LookupsListActivity.class);
        i.putExtra("type", 6);
        classLauncher.launch(i);
    }

    private void amountAction() {
        this.currencyKeyboard.show();
    }

    private void currencyAction() {
        getCells();
        Intent i = new Intent(this, ExchangeRateActivity.class);
        i.putExtra("transaction", this.transaction);
        try {
            i.putExtra("split", this.transaction.getSplits().get(0));
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException in amount parent listener", e);
        }
        currencyLauncher.launch(i);
    }

    private void idAction() {
        getCells();
        Intent i = new Intent(this, LookupsListActivity.class);
        i.putExtra("type", 7);
        idLauncher.launch(i);
    }

    private void repeatingAction() {
        if (!AccountsActivity.isLite(this) || TransactionDB.queryAllRepeatingTransactions().size() < 2) {
            Intent i = new Intent(this, RepeatingEditActivity.class);
            getCells();
            if (repeatingTransaction.repeatingID == 0) {
                repeatingTransaction.setTransaction(transaction.copy());
            }
            i.putExtra("Transaction", transaction);
            i.putExtra("RepeatingTransaction", repeatingTransaction);
            repeatingLauncher.launch(i);
            return;
        }
        AccountsActivity.displayLiteDialog(this);
    }

    private void saveButtonAction() {
        getCells();
        if (transaction.getAccount() == null || transaction.getAccount().isEmpty()) {
            showNeedAccountDialog();
        } else if (!transaction.isRepeatingTransaction || repeatingTransaction.isRepeating()) {
            saveAction();
            finish();
        } else {
            showNeedRepeatingDialog();
        }
    }

    private void saveAction() {
        if (this.transaction.isRepeatingTransaction) {
            saveRepeatingTransaction();
        } else {
            this.transaction.saveToDatabase();
            if (this.repeatingTransaction.repeatingID != 0 && (this.repeatingChanged == 2 || this.dateChanged == 2)) {
                saveRepeatingTransaction();
            }
        }
        deleteDeletedImages();
        keepTheChangeUpdate();
    }

    private void loadCells() {
        this.programaticUpdate = true;
        setType();
        this.programaticUpdate = false;
        
        this.dateTextView.setText(CalExt.descriptionWithMediumDate(this.transaction.getDate()));
        if (this.repeatingTransaction == null || !this.repeatingTransaction.isRepeating()) {
            this.dateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        } else {
            this.dateTextView.setTextColor(0xFF00FF00); // Green for repeating
        }

        if (Prefs.getBooleanPref(Prefs.SHOWTIME)) {
            this.timeTextView.setVisibility(View.VISIBLE);
            this.timeTextView.setText(CalExt.descriptionWithShortTime(this.transaction.getDate()));
        } else {
            this.timeTextView.setVisibility(View.GONE);
        }

        this.accountTextView.setText(this.transaction.getAccount());
        
        if (this.transaction.isTransfer()) {
            this.payeeEditText.setText(this.transaction.getTransferToAccount());
        } else {
            this.payeeEditText.setText(this.transaction.getPayee());
        }

        loadAmountXrateValues();
        updateAmountFieldTextColor();
        
        this.idEditText.setText(this.transaction.getCheckNumber());
        this.clearedCheckBox.setChecked(this.transaction.getCleared());
        this.memoEditText.setText(this.transaction.getMemo());

        if (this.transaction.getNumberOfSplits() > 1) {
            this.categoryEditText.setVisibility(View.GONE);
            this.categoryTextView.setVisibility(View.VISIBLE);
            this.categoryTextView.setText(Locales.kLOC_GENERAL_SPLITS);
        } else {
            this.categoryEditText.setText(this.transaction.getCategory());
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

        // Update Camera Button Thumbnail
        ImageButton cameraBtn = findViewById(R.id.camera_button);
        ArrayList<String> imageNames = transaction.imageFileNames();
        if (imageNames != null && !imageNames.isEmpty()) {
            String firstName = imageNames.get(0);
            File photoDir = new File(getFilesDir(), "photos");
            File f = new File(photoDir, firstName);
            if (f.exists()) {
                android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                options.inSampleSize = 8; // Scale down for thumbnail
                android.graphics.Bitmap thumb = android.graphics.BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                if (thumb != null) {
                    // Fix rotation for thumbnail
                    thumb = rotateImageIfRequired(thumb, f.getAbsolutePath());
                    
                    cameraBtn.setImageBitmap(thumb);
                    cameraBtn.setPadding(4, 4, 4, 4); // Minimal padding for thumbnail
                    cameraBtn.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    cameraBtn.setColorFilter(null); // Clear tint for thumbnail
                } else {
                    cameraBtn.setImageResource(R.drawable.ic_photo_camera_white_24dp);
                    cameraBtn.setPadding(12, 12, 12, 12);
                    cameraBtn.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
                }
            } else {
                cameraBtn.setImageResource(R.drawable.ic_photo_camera_white_24dp);
                cameraBtn.setPadding(12, 12, 12, 12);
                cameraBtn.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
            }
        } else {
            cameraBtn.setImageResource(R.drawable.ic_photo_camera_white_24dp);
            cameraBtn.setPadding(12, 12, 12, 12);
            cameraBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            cameraBtn.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
        }
    }

    private android.graphics.Bitmap rotateImageIfRequired(android.graphics.Bitmap img, String path) {
        try {
            androidx.exifinterface.media.ExifInterface ei = new androidx.exifinterface.media.ExifInterface(path);
            int orientation = ei.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL);

            return switch (orientation) {
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90);
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180);
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270);
                default -> img;
            };
        } catch (Exception e) {
            return img;
        }
    }

    private android.graphics.Bitmap rotateImage(android.graphics.Bitmap img, int degree) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(degree);
        android.graphics.Bitmap rotatedImg = android.graphics.Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void getCells() {
        saveAmountXrates();
        this.transaction.setDateFromString(this.dateTextView.getText().toString());
        if (Prefs.getBooleanPref(Prefs.SHOWTIME)) {
            this.transaction.updateDateWithTimeString(this.timeTextView.getText().toString());
        }
        this.transaction.setAccount(this.accountTextView.getText().toString());
        if (this.transaction.isTransfer()) {
            this.transaction.setPayee("");
            this.transaction.setTransferToAccount(this.payeeEditText.getText().toString());
        } else {
            this.transaction.setTransferToAccount("");
            this.transaction.setPayee(this.payeeEditText.getText().toString());
        }
        if (this.transaction.getNumberOfSplits() <= 1) {
            this.transaction.setCategory(this.categoryEditText.getText().toString());
        }
        this.transaction.setCheckNumber(this.idEditText.getText().toString());
        this.transaction.setCleared(this.clearedCheckBox.isChecked());
        if (!this.transaction.multipleClassNames()) {
            this.transaction.setClassName(this.classEditText.getText().toString());
        }
        this.transaction.setMemo(this.memoEditText.getText().toString());
    }

    private void loadAmountXrateValues() {
        if (this.transaction.getSubTotal() == 0.0d) {
            this.amountEditText.setText("");
        } else if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            double xRate = this.transaction.getXrate();
            String currencyCode = this.transaction.getCurrencyCode();
            String tempFxString = String.format("1 %s = %.3f %s", Prefs.getStringPref(Prefs.HOMECURRENCYCODE), xRate, currencyCode);

            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal()), Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
            this.foreignAmountTextView.setVisibility(View.VISIBLE);
            this.xRateTextView.setVisibility(View.VISIBLE);

            this.foreignAmountTextView.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal() / this.transaction.getXrate()), this.transaction.getCurrencyCode()));
            this.xRateTextView.setText(tempFxString);
        } else {
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.transaction.getSubTotal())));
        }
    }

    private void saveAmountXrates() {
        double amount = CurrencyExt.amountFromString(this.amountEditText.getText().toString());
        double multiplier = (this.transaction.getType() == 0 || this.transaction.getType() == 2) ? -1.0d : 1.0d;
        double newSubTotal = Math.abs(amount) * multiplier;
        this.transaction.setSubTotal(newSubTotal);
        if (this.transaction.getNumberOfSplits() <= 1) {
            this.transaction.setAmount(newSubTotal);
        }
    }

    private void showTimePickerDialog() {
        GregorianCalendar theTime = this.transaction.getDate();
        new TimePickerDialog(this, PocketMoneyThemes.timePickerTheme(), this.mTimeSetListener, 
            theTime.get(Calendar.HOUR_OF_DAY), theTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(this)).show();
    }

    private void showDeleteConfirmDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_GENERAL_DELETE)
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton(Locales.kLOC_GENERAL_DELETE, (dialog, which) -> {
                    editTransactionDelete();
                    finish();
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null)
                .show();
    }

    private void showDuplicateDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_DUPLICATE_TRANSACTION_TITLE)
                .setNegativeButton(Locales.kLOC_DUPLICATE_TRANSACTION_EXISTING_TIME, (dialog, item) -> duplicateTransaction(false))
                .setPositiveButton(Locales.kLOC_DUPLICATE_TRANSACTION_PRESENT_TIME, (dialog, item) -> duplicateTransaction(true))
                .show();
    }

    private void duplicateTransaction(boolean presentTime) {
        getCells(); // Sync current UI state to the object
        
        TransactionClass dupe = this.transaction.copy();
        dupe.transactionID = 0; // Mark as a new record
        dupe.dirty = true;
        
        if (presentTime) {
            dupe.setDate(new GregorianCalendar());
        }
        
        // Switch the active transaction to the duplicate
        this.transaction = dupe;
        this.repeatingTransaction = new RepeatingTransactionClass(this.transaction);
        this.repeatingTransaction.hydrate();
        
        reloadData(); // Refresh UI with the new (unsaved) data
        Toast.makeText(this, "Duplicated: Ready to edit and save", Toast.LENGTH_SHORT).show();
    }

    private void includeFeeAction() {
        Log.d(TAG, "includeFeeAction() called");
        AccountClass account = AccountDB.recordFor(this.transaction.getAccount());
        if (account == null || account.getFee() <= 0.0d) {
            showFeeDialog();
            return;
        }
        if (this.transaction.transactionID == 0) {
            saveAction();
        }
        TransactionClass feeTrans = new TransactionClass();
        feeTrans.setAccount(this.transaction.getAccount());
        feeTrans.setSubTotal(Math.abs(this.transaction.getSubTotal()) * account.getFee() * -1.0d);
        feeTrans.setAmount(feeTrans.getSubTotal());
        feeTrans.setCurrencyCode(account.getCurrencyCode());
        feeTrans.setCategory(Locales.kLOC_ACCOUNT_FEE_LABEL);
        feeTrans.initType();
        feeTrans.saveToDatabase();
        Toast.makeText(this, "Fee transaction added", Toast.LENGTH_SHORT).show();
    }

    private void showFeeDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setMessage(Locales.kLOC_FEE_MISSING_ALERT)
                .setPositiveButton(Locales.kLOC_GENERAL_OK, null).show();
    }

    private void showCameraDialog() {
        showCameraDialog(null);
    }

    private void showCameraDialog(String replaceTarget) {
        this.imageToReplace = replaceTarget;
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle("Add Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        try {
                            tempPhotoPath = new File(getExternalCacheDir(), "temp.jpg");
                            Uri photoUri = FileProvider.getUriForFile(this, "com.example.fileprovider", tempPhotoPath);
                            cameraLauncher.launch(photoUri);
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting camera", e);
                        }
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, which) -> imageToReplace = null)
                .setOnCancelListener(dialog -> imageToReplace = null)
                .show();
    }

    private void showNeedAccountDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setMessage(Locales.kLOC_EDIT_TRANSACTION_MISSINGACCOUNT)
                .setPositiveButton(Locales.kLOC_GENERAL_OK, null).show();
    }

    private void showNeedRepeatingDialog() {
        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setMessage("How often this transaction repeats must be entered before you can save a repeating transaction.")
                .setPositiveButton(Locales.kLOC_GENERAL_OK, null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SAVE, 0, Locales.kLOC_GENERAL_SAVE)
                .setIcon(R.drawable.ic_save_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_SAVE) {
            saveButtonAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editTransactionDelete() {
        if (this.transaction.isRepeatingTransaction) {
            if (this.repeatingTransaction.getTransaction() != null) {
                this.repeatingTransaction.getTransaction().deleteFromDatabase();
            }
            this.repeatingTransaction.deleteFromDatabase();
        }
        this.transaction.transactionDelete();
    }

    private void reloadBalanceBar() {
        if (this.transaction.isRepeatingTransaction) {
            this.balanceBar.setVisibility(View.GONE);
            return;
        }
        AccountClass account = AccountDB.recordFor(this.transaction.getAccount());
        if (account != null) {
            int balanceType = Prefs.getIntPref(Prefs.BALANCETYPE);
            this.balanceBar.balanceAmountTextView.setText(account.formatAmountAsCurrency(account.balanceOfType(balanceType)));
            this.balanceBar.balanceTypeTextView.setText(AccountDB.totalWorthLabel(balanceType));
        }
    }

    private void updateAmountFieldTextColor() {
        if (this.transaction.getType() == 0 || this.transaction.getType() == 2) {
            this.amountEditText.setTextColor(PocketMoneyThemes.redLabelColor());
        } else {
            this.amountEditText.setTextColor(PocketMoneyThemes.greenDepositColor());
        }
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
        } else if (this.repeatingChanged != Enums.RepeatingChangeTypeSeparateTransactionFromRepeating && this.dateChanged != Enums.DateChangeTypeSeparateTransactionFromRepeating) {
            GregorianCalendar originalDate = (GregorianCalendar) this.repeatingTransaction.getTransaction().getDate().clone();
            this.repeatingTransaction.getTransaction().deleteFromDatabase();
            this.repeatingTransaction.setTransaction(null);
            this.repeatingTransaction.setTransaction(this.transaction.copy());
            this.repeatingTransaction.dirty = true;
            if (Enums.DateChangeTypeSeparateTransactionFromRepeating == this.dateChanged || (!this.transaction.isRepeatingTransaction && this.dateChanged == Enums.DateChangeTypeNone)) {
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

    private void keepTheChange() {
        AccountClass act = AccountDB.recordFor(this.transaction.getAccount());
        if (act != null) {
            double roundTo = act.getKeepChangeRoundTo();
            double current = CurrencyExt.amountFromString(this.amountEditText.getText().toString());
            double next = Math.ceil(current / roundTo) * roundTo;
            if (next == current) next += roundTo;
            this.changeKept = next - current;
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(next, act.getCurrencyCode()));
        }
    }

    private void clearKeepTheChange() {
        this.changeKept = 0.0d;
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
            keepTheChangeRecord.setType(Enums.kTransactionTypeTransferFrom);
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

    private void saveUpdateTransferFromOriginalRecord(TransactionClass oldRecP, TransactionClass modRecP) {
        double d;
        double newRate;
        double newAmount;
        int i = 0;
        while (i < oldRecP.getNumberOfSplits()) {
            if (oldRecP.getTransferToAccountAtIndex(i) != null && !oldRecP.getTransferToAccountAtIndex(i).isEmpty()) {
                String str;
                boolean regularTransfer = oldRecP.getCurrencyCodeAtIndex(i).equals(AccountDB.recordFor(oldRecP.getTransferToAccountAtIndex(i)).getCurrencyCode());
                TransactionTransferRetVals ret = new TransactionTransferRetVals();
                double amountAtIndex = regularTransfer ? -1.0d * (oldRecP.getAmountAtIndex(i) / oldRecP.getXrateAtIndex(i)) : -1.0d * oldRecP.getAmountAtIndex(i);
                str = regularTransfer ? null : oldRecP.getCurrencyCodeAtIndex(i);
                TransactionDB.transactionGetTransfer(oldRecP.getTransferToAccountAtIndex(i), oldRecP.getAccount(), oldRecP.getDate(), amountAtIndex, str, ret);
                if (ret.transferRecID > 0) {
                    TransactionClass transactionClass = new TransactionClass(ret.transferRecID);
                    transactionClass.hydrate();
                    if (i >= modRecP.getNumberOfSplits() || modRecP.getTransferToAccountAtIndex(i) == null || modRecP.getTransferToAccountAtIndex(i).isEmpty()) {
                        transactionClass.setDeleted(true);
                        transactionClass.saveToDatabase();
                    } else {
                        if (!Prefs.getBooleanPref(Prefs.TRANSACTIONS_UNLINK_ID_FIELD)) {
                            transactionClass.setCheckNumber(modRecP.getCheckNumber());
                        }
                        transactionClass.setAccount(modRecP.getTransferToAccountAtIndex(i));
                        transactionClass.setTransferToAccountAtIndex(modRecP.getAccount(), ret.transferSplitItem);
                        transactionClass.setCategoryAtIndex(modRecP.getCategoryAtIndex(i), ret.transferSplitItem);
                        transactionClass.setMemoAtIndex(modRecP.getMemoAtIndex(i), ret.transferSplitItem);
                        transactionClass.setClassNameAtIndex(modRecP.getClassNameAtIndex(i), ret.transferSplitItem);
                        transactionClass.setCurrencyCodeAtIndex(modRecP.getCurrencyCode(), ret.transferSplitItem);
                        if (modRecP.getNumberOfSplits() == 1) {
                            double xrate = xrateFromAccountToAccount(modRecP.getTransferToAccountAtIndex(i), modRecP.getAccount());
                            if (modRecP.getCurrencyCodeAtIndex(i).equals(AccountDB.recordFor(modRecP.getTransferToAccountAtIndex(i)).getCurrencyCode())) {
                                d = xrate;
                            } else {
                                d = 1.0d;
                            }
                            newRate = Math.abs(modRecP.getAmountAtIndex(i) / d);
                            newAmount = -1.0d * modRecP.getAmountAtIndex(i);
                            transactionClass.setSubTotal(newAmount);
                            transactionClass.setAmount(newAmount);
                            transactionClass.setXrate(newRate);
                        }
                        transactionClass.setDate(modRecP.getDate());
                        transactionClass.setCleared(modRecP.getCleared());
                        transactionClass.initType();
                        transactionClass.saveToDatabase();
                    }
                }
            }
            i++;
        }
    }

    private double xrateFromAccountToAccount(String account1, String account2) {
        return AccountDB.recordFor(account1).getExchangeRate() / AccountDB.recordFor(account2).getExchangeRate();
    }

    private void splitsAction() {
        getCells();
        Intent i = new Intent(this, SplitsActivity.class);
        i.putExtra("Transaction", this.transaction);
        splitsLauncher.launch(i);
    }

    private void deleteDeletedImages() {
        File photoDir = new File(getFilesDir(), "photos");
        for (String img : deletedImages) {
            new File(photoDir, img).delete();
        }
    }

    private void editTextDidFinishChanging(int code) {
        if (code == EDITTEXT_AMOUNT) {
            saveAmountXrates();
            loadAmountXrateValues();
        }
    }

    private MaterialButtonToggleGroup.OnButtonCheckedListener getRadioChangedListener() {
        return (group, checkedId, isChecked) -> {
            if (isChecked && !programaticUpdate) {
                if (checkedId == R.id.withdrawalbutton) transaction.setType(0);
                else if (checkedId == R.id.depositbutton) transaction.setType(1);
                else if (checkedId == R.id.transferbutton) {
                    transaction.setType(3);
                    accountAction();
                }
                reloadData();
            }
        };
    }

    private void setType() {
        if (transaction.isWithdrawal()) withdrawalButton.setChecked(true);
        else if (transaction.isDeposit()) depositButton.setChecked(true);
        else if (transaction.isTransfer()) transferButton.setChecked(true);
    }

    private void reloadData() {
        loadCells();
    }

    private void clearDropDowns() {
        this.categoryEditText.dismissDropDown();
        this.payeeEditText.dismissDropDown();
        this.idEditText.dismissDropDown();
        this.classEditText.dismissDropDown();
    }
}
