package com.example.smmoney.views.transactions;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.importexport.ImportExportCSV;
import com.example.smmoney.importexport.ImportExportQIF;
import com.example.smmoney.importexport.ImportExportTDF;
import com.example.smmoney.importexport.ofx.ImportExportOFX;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PMGlobal;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.BalanceBar;
import com.example.smmoney.views.HandlerActivity;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.PocketMoneyProgressDialog;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.filters.FiltersMainActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.reports.AccountsReportDataSource;
import com.example.smmoney.views.reports.CategoryReportDataSource;
import com.example.smmoney.views.reports.ClassReportDataSource;
import com.example.smmoney.views.reports.PayeeReportDataSource;
import com.example.smmoney.views.reports.ReportsActivity;
import com.example.smmoney.views.splits.SplitsActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class TransactionsActivity extends PocketMoneyActivity implements HandlerActivity {
    @SuppressWarnings("unused")
    public final int REQUEST_EDIT = 2;
    @SuppressWarnings("unused")
    public final int REQUEST_NEW = 1;
    public final int TRANSACTION_REQUEST_EMAIL = 2;
    public final int TRANSACTION_REQUEST_FILTER = 1;
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    private final int DATE_DIALOG_ID = 8;
    private final int EMAIL_CSV = 2;
    private final int EMAIL_QIF = 0;
    private final int EMAIL_TDF = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int IMPORT_PROGRESS_DIALOG = 9;
    private final int MENU_EMAILTRANSFERS = 3;
    private final int MENU_FILETRANSFERS = 1;
    private final int MENU_FILTER = 4;
    private final int MENU_NEW = 1;
    @SuppressWarnings("unused")
    private final int MENU_REPORTS = 5;
    private final int MENU_REPORTS_ACCOUNT = 6;
    private final int MENU_REPORTS_CATEGORY = 7;
    private final int MENU_REPORTS_CLASS = 8;
    private final int MENU_REPORTS_PAYEE = 9;
    @SuppressWarnings("unused")
    private final int MENU_SDCARDTRANSFER = 5;
    private final int MENU_SD_EXPORT = 7;
    @SuppressWarnings("unused")
    private final int MENU_SD_IMPORT = 6;
    private final int MENU_SEARCH = 24;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS = 3;
    private final int MENU_TOOLS_ADJUSTBALANCE = 13;
    private final int MENU_TOOLS_FILETRANSFERS = 10;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS_FILETRANSFERS_EMAIL = 17;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS_FILETRANSFERS_EMAIL_CSV = 23;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS_FILETRANSFERS_EMAIL_QIF = 21;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS_FILETRANSFERS_EMAIL_TDF = 22;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS_FILETRANSFERS_EXPORT = 16;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS_FILETRANSFERS_EXPORT_CSV = 20;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS_FILETRANSFERS_EXPORT_QIF = 18;
    @SuppressWarnings("unused")
    private final int MENU_TOOLS_FILETRANSFERS_EXPORT_TDF = 19;
    private final int MENU_TOOLS_GOTODATE = 11;
    private final int MENU_TOOLS_MARKASCLEAR = 14;
    private final int MENU_TOOLS_ROLLUP = 15;
    private final int MENU_VIEW = 2;
    @SuppressWarnings("unused")
    private final int MENU_WIFITRANSFERS = 2;
    @SuppressWarnings("unused")
    private final int MENU_WIFI_EXPORT = 4;
    private final int MENU_SORT = 25;
    private FilterClass _filter;
    private TransactionRecyclerViewAdapter adapter;
    private final OnDateSetListener mDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        GregorianCalendar targetDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        targetDate = CalExt.beginningOfDay(targetDate);
        
        boolean descending = Prefs.getStringPref(Prefs.NEWESTTRANSACTIONFIRST).equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING);
        int i = 0;
        for (TransactionClass transaction : this.adapter.getElements()) {
            GregorianCalendar transDate = CalExt.beginningOfDay(transaction.getDate());
            
            // Descending: Stop at the first transaction that is ON or BEFORE the target date
            // Ascending: Stop at the first transaction that is ON or AFTER the target date
            if (descending) {
                if (!transDate.after(targetDate)) break;
            } else {
                if (!transDate.before(targetDate)) break;
            }
            i++;
        }
        
        // Ensure index is within bounds
        int finalPosition = Math.min(i, this.adapter.getItemCount() - 1);
        if (finalPosition < 0) finalPosition = 0;

        LinearLayoutManager layoutManager = (LinearLayoutManager) this.recyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.scrollToPositionWithOffset(finalPosition, 0);
        }
    };
    private BalanceBar balanceBar;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private MaterialButton allButton;
    private Context context;
    private String emailFileLocation;
    private ArrayList<String> fileNames;
    private boolean firstOpenOfView;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private MaterialButton clearedButton;
    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView recyclerView;
    private Handler mHandler = null;
    private int msgEmail = -1;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private MaterialButton pendingButton;
    private PocketMoneyProgressDialog progressDialog = null;
    private EditText searchEditText;
    private LinearLayout searchView;
    private boolean shouldEmail = false;
    @SuppressWarnings("unused")
    private TextView titleTextView;
    private WakeLock wakeLock;

    final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                reloadData();
                reloadBalanceBar();
            }
    );
    private final ActivityResultLauncher<Intent> filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 1 && result.getData() != null) {
                    String currentAccount = this._filter.getAccount();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        this._filter = result.getData().getSerializableExtra("Filter", FilterClass.class);
                    } else {
                        //noinspection deprecation
                        this._filter = (FilterClass) Objects.requireNonNull(result.getData().getExtras()).get("Filter");
                    }
                    if (this._filter != null && this._filter.getAccount() != null && this._filter.getAccount().equals(Locales.kLOC_FILTERS_CURRENT_ACCOUNT)) {
                        this._filter.setAccount(currentAccount);
                    }
                    
                    String title = this._filter.customFilter() ?
                            (this._filter.getFilterName().isEmpty() ? Locales.kLOC_TOOLS_FILTER : this._filter.getFilterName()) :
                            (this._filter.getAccount().isEmpty() ? Locales.kLOC_ALL_TRANSACTIONS : this._filter.getAccount());
                    
                    Objects.requireNonNull(getSupportActionBar()).setTitle(title);
                    reloadData();
                }
            }
    );

    private final ActivityResultLauncher<Intent> emailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (this.fileNames != null) {
                    for (String fileName : this.fileNames) {
                        new File(fileName).delete();
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).newWakeLock(26, "TransactionsActivity:DoNotDimScreen");
        this.context = this;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            this._filter = Objects.requireNonNull(getIntent().getExtras()).getSerializable("Filter", FilterClass.class);
        } else {
            //noinspection deprecation
            this._filter = (FilterClass) Objects.requireNonNull(getIntent().getExtras()).get("Filter");
        }
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.transactions, null);
        setupView(layout);
        setContentView(layout);

        this.firstOpenOfView = true;

        String title = getIntent().getStringExtra("title");
        if (title == null) {
            title = this._filter.customFilter() ?
                    (this._filter.getFilterName().isEmpty() ? Locales.kLOC_TOOLS_FILTER : this._filter.getFilterName()) :
                    (this._filter.getAccount().isEmpty() ? Locales.kLOC_ALL_TRANSACTIONS : this._filter.getAccount());
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            String subtitle = getIntent().getStringExtra("subtitle");
            if (subtitle != null) {
                getSupportActionBar().setSubtitle(subtitle);
            }
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void onResume() {
        super.onResume();
        reloadData();
        reloadBalanceBar();
        if (this.firstOpenOfView) {
            RecyclerView recyclerView = findViewById(R.id.the_list);
            if (Prefs.getStringPref(Prefs.NEWESTTRANSACTIONFIRST).equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING)) {
                recyclerView.scrollToPosition(0 /*i.e. first transaction*/);
            } else {
                recyclerView.scrollToPosition(this.adapter.getItemCount() > 0 ? this.adapter.getItemCount() - 1 : 0/*i.e. last transaction*/);
            }
            this.firstOpenOfView = false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @SuppressWarnings("unused")
    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    private void setupView(LinearLayout layout) {
        createHandler();
        this.balanceBar = layout.findViewById(R.id.balancebar);
        this.balanceBar.nextButton.setOnClickListener(getBalanceBarClickListener());
        this.balanceBar.previousButton.setOnClickListener(getBalanceBarClickListener());
        this.recyclerView = layout.findViewById(R.id.the_list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new TransactionRecyclerViewAdapter(this);
        this.recyclerView.setAdapter(this.adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                TransactionClass transaction = adapter.getElements().get(position);
                if (direction == ItemTouchHelper.RIGHT) {
                    // Edit
                    Intent intent = new Intent(TransactionsActivity.this, TransactionEditActivity.class);
                    intent.putExtra("Transaction", transaction);
                    editLauncher.launch(intent);
                    adapter.notifyItemChanged(position);
                } else if (direction == ItemTouchHelper.LEFT) {
                    // Delete
                    deleteTransaction(transaction);
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    
                    if (dX > 0) { // Swipe Right (Edit)
                        paint.setColor(Color.parseColor("#4CAF50")); // Green
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), paint);
                        
                        Drawable icon = ContextCompat.getDrawable(TransactionsActivity.this, R.drawable.ic_edit_white_24dp);
                        if (icon != null) {
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }
                        
                        paint.setColor(Color.WHITE);
                        paint.setTextSize(40);
                        paint.setAntiAlias(true);
                        c.drawText("Edit", (float) itemView.getLeft() + 140, (float) itemView.getTop() + (itemView.getHeight() / 2f) + 15, paint);

                    } else if (dX < 0) { // Swipe Left (Delete)
                        paint.setColor(Color.parseColor("#F44336")); // Red
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                        
                        Drawable icon = ContextCompat.getDrawable(TransactionsActivity.this, R.drawable.ic_delete_white_24dp);
                        if (icon != null) {
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();
                            int iconRight = itemView.getRight() - iconMargin;
                            int iconLeft = iconRight - icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }

                        paint.setColor(Color.WHITE);
                        paint.setTextSize(40);
                        paint.setAntiAlias(true);
                        float textWidth = paint.measureText("Delete");
                        c.drawText("Delete", (float) itemView.getRight() - 140 - textWidth, (float) itemView.getTop() + (itemView.getHeight() / 2f) + 15, paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this.recyclerView);

        this.searchView = layout.findViewById(R.id.searchlayout);
        this.searchEditText = layout.findViewById(R.id.searcheditext);
        this.searchEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                TransactionsActivity.this._filter.setSpotlight(TransactionsActivity.this.searchEditText.getText().toString());
                TransactionsActivity.this.reloadData();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        View aView = layout.findViewById(R.id.radiogroup);
        this.pendingButton = aView.findViewById(R.id.pendingbutton);
        this.clearedButton = aView.findViewById(R.id.clearedbutton);
        this.allButton = aView.findViewById(R.id.allbutton);
        
        MaterialButtonToggleGroup group = (MaterialButtonToggleGroup) aView;
        group.addOnButtonCheckedListener(getRadioChangedListener());
        
        // Theme the buttons
        android.content.res.ColorStateList bgTint = PocketMoneyThemes.segmentedButtonBackgroundTint();
        android.content.res.ColorStateList textTint = PocketMoneyThemes.segmentedButtonTextTint();
        android.content.res.ColorStateList strokeTint = android.content.res.ColorStateList.valueOf(PocketMoneyThemes.currentTintColor());
        
        this.pendingButton.setBackgroundTintList(bgTint);
        this.pendingButton.setTextColor(textTint);
        this.pendingButton.setStrokeColor(strokeTint);
        
        this.clearedButton.setBackgroundTintList(bgTint);
        this.clearedButton.setTextColor(textTint);
        this.clearedButton.setStrokeColor(strokeTint);
        
        this.allButton.setBackgroundTintList(bgTint);
        this.allButton.setTextColor(textTint);
        this.allButton.setStrokeColor(strokeTint);
        
        layout.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    private void toggleSearch() {
        int i = View.GONE /*8*/;
        LinearLayout linearLayout = this.searchView;
        if (this.searchView.getVisibility() == View.GONE) {
            i = View.VISIBLE /*0*/;
        }
        linearLayout.setVisibility(i);
        this.searchView.invalidate();
    }

    public void reloadData() {
        this.adapter.setElements(TransactionDB.queryWithFilter(this._filter));
        this.adapter.notifyDataSetChanged();
    }

    private void deleteTransaction(final TransactionClass transaction) {
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_GENERAL_DELETE)
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton(Locales.kLOC_GENERAL_DELETE, (dialog, which) -> {
                    if (transaction != null) transaction.transactionDelete();
                    reloadData();
                    reloadBalanceBar();
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null)
                .show();
    }

    private void showSortDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_transaction_sort, null);
        RadioGroup propertyGroup = dialogView.findViewById(R.id.sort_property_group);
        RadioGroup directionGroup = dialogView.findViewById(R.id.sort_direction_group);

        // Theme the dialog
        int labelColor = PocketMoneyThemes.fieldLabelColor();
        int textColor = PocketMoneyThemes.primaryCellTextColor();
        android.content.res.ColorStateList tint = android.content.res.ColorStateList.valueOf(PocketMoneyThemes.currentTintColor());

        ((TextView) dialogView.findViewById(R.id.sort_by_label)).setTextColor(labelColor);
        ((TextView) dialogView.findViewById(R.id.order_label)).setTextColor(labelColor);

        for (int i = 0; i < propertyGroup.getChildCount(); i++) {
            View child = propertyGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                rb.setTextColor(textColor);
                rb.setButtonTintList(tint);
            }
        }
        for (int i = 0; i < directionGroup.getChildCount(); i++) {
            View child = directionGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                rb.setTextColor(textColor);
                rb.setButtonTintList(tint);
            }
        }

        // Set current state
        String currentSort = Prefs.getStringPref(Prefs.TRANSACTIONS_SORTON);
        int currentSortType = TransactionDB.transactionSortTypeFromString(currentSort);
        
        switch (currentSortType) {
            case Enums.kTransactionsSortTypeDate -> propertyGroup.check(R.id.sort_date);
            case Enums.kTransactionsSortTypeAmount -> propertyGroup.check(R.id.sort_amount);
            case Enums.kTransactionsSortTypePayee -> propertyGroup.check(R.id.sort_payee);
            case Enums.kTransactionsSortTypeClass -> propertyGroup.check(R.id.sort_class);
            case Enums.kTransactionsSortTypeCategory -> propertyGroup.check(R.id.sort_category);
            case Enums.kTransactionsSortTypeDateAmount -> propertyGroup.check(R.id.sort_date_amount);
        }

        boolean isAsc = Prefs.getStringPref(Prefs.NEWESTTRANSACTIONFIRST).equals(Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING);
        if (isAsc) directionGroup.check(R.id.sort_asc);
        else directionGroup.check(R.id.sort_desc);

        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_TRANSACTIONS_OPTIONS_SORTON)
                .setView(dialogView)
                .setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, which) -> {
                    int selectedPropertyId = propertyGroup.getCheckedRadioButtonId();
                    String newSort = Locales.kLOC_GENERAL_DATE;
                    if (selectedPropertyId == R.id.sort_amount) newSort = Locales.kLOC_GENERAL_AMOUNT;
                    else if (selectedPropertyId == R.id.sort_payee) newSort = Locales.kLOC_GENERAL_PAYEE;
                    else if (selectedPropertyId == R.id.sort_class) newSort = Locales.kLOC_GENERAL_CLASS;
                    else if (selectedPropertyId == R.id.sort_category) newSort = Locales.kLOC_GENERAL_CATEGORY;
                    else if (selectedPropertyId == R.id.sort_date_amount) newSort = Locales.kLOC_TRANSACTION_SORTDATEAMOUNT;

                    int selectedDirectionId = directionGroup.getCheckedRadioButtonId();
                    String newDir = (selectedDirectionId == R.id.sort_asc) ? Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING : Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING;

                    Prefs.setPref(Prefs.TRANSACTIONS_SORTON, newSort);
                    Prefs.setPref(Prefs.NEWESTTRANSACTIONFIRST, newDir);
                    reloadData();
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null)
                .show();
    }

    private void markAsClear() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
        alert.setMessage("Are you sure you want to mark all the transactions clear?");
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
            for (TransactionClass trans : TransactionsActivity.this.adapter.getElements()) {
                trans.hydrate();
                trans.setCleared(true);
                trans.saveToDatabase();
            }
            TransactionsActivity.this.reloadData();
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, whichButton) -> dialog.cancel());
        alert.show();
    }

    private void newTransaction() {
        boolean z = true; //boolean that tracks whether a transaction has been marked as cleared
        Intent i = new Intent(this, TransactionEditActivity.class);
        TransactionClass trans = new TransactionClass(); //creates new TransactionClass object using empty contructor i.e. no params
        trans.setAccount(this._filter.getAccount());
        if (this._filter.getCleared() != Enums.kClearedCleared/*1*/) {
            z = false;
        }
        trans.setCleared(z);
        if (this._filter.getCategory() != null) {
            trans.setCategory(this._filter.getCategory());
        }
        if (this._filter.getPayee() != null) {
            trans.setPayee(this._filter.getPayee());
        }
        AccountClass a1 = AccountDB.recordFor(this._filter.getAccount());
        if (a1 != null) {
            trans.setCurrencyCode(a1.getCurrencyCode());
        }
        trans.dirty = false;
        trans.getSplits().get(0).dirty = false; // trans.getSplits() returns an ArrayList. get(0) gets the 0th element of the ArrayList. dirty = false tells system this is not an exisiting Transaction so don't update Database
        i.putExtra("Transaction", trans);
        editLauncher.launch(i); // starts Transaction activity (I think?) and passes trans TransactionClass object with properties as set by code logic above
    }

    private void adjustBalance() {
        // check if there is a valid account for which we are going to adjust the balance. Return without doing anything if not
        if (this._filter.getAccount() == null || this._filter.getAccount().isEmpty() || AccountDB.recordFor(this._filter.getAccount()) == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
            alert.setMessage("You need to have an account selected that exists to adjust the balance");
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> dialog.dismiss());
            alert.show();
            return;
        }
        // Open AlertDialog, allow user to input a signed & decimal number and then adjust the balance to the number entered, or allow user to cancel
        AlertDialog.Builder alert = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_NUMBER_FLAG_SIGNED /*12290*/); // 12290 = InputType.TYPE_NUMBER_FLAG_DECIMAL (8192) + IT.TYPE_NUMBER_FLAG_SIGNED (4096) + IT.TYPE_CLASS_NUMBER (2)
        alert.setMessage(Locales.kLOC_TOOLS_RECONCILE_BODY);
        alert.setView(input);
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
            String adjustString = input.getText().toString().trim();
            if (!adjustString.isEmpty()) {
                TransactionsActivity.this.adjustBalanceAlert(Double.parseDouble(adjustString));
            }
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, whichButton) -> dialog.cancel());
        alert.show();
    }

    private void adjustBalanceAlert(double newBalance) {
        AccountClass act = AccountDB.recordFor(this._filter.getAccount());
        final double clearedAdjust;
        final double futureAdjust;
        if (act != null) {
            clearedAdjust = act.balanceOfType(Enums.kBalanceTypeCleared /*1*/);
            futureAdjust = act.balanceOfType(Enums.kBalanceTypeFuture /*0*/);

            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
            final double d = newBalance;
            final double d2 = newBalance;
            alt_bld.setCancelable(false).setMessage(Locales.kLOC_TOOLS_RECONCILE_ALLCLEARED).setPositiveButton(Locales.kLOC_GENERAL_CLEARED, (dialog, id) -> TransactionsActivity.this.adjustBalanceConfirm(true, d - clearedAdjust)).setNegativeButton(Locales.kLOC_TOOLS_RECONCILE_ALL, (dialog, id) -> TransactionsActivity.this.adjustBalanceConfirm(false, d2 - futureAdjust));

            alt_bld.create().show();
        }
    }

    private void adjustBalanceConfirm(final boolean onlyCleared, final double newBalance) {
        AccountClass act = AccountDB.recordFor(this._filter.getAccount());
        String titleString = null;
        if (act != null) {
            titleString = getString(R.string.kLOC_TOOLS_RECONCILE_CONFIRM, act.formatAmountAsCurrency(newBalance));
        }
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
        alt_bld.setCancelable(false).setMessage(titleString).setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, id) -> TransactionsActivity.this.adjustBalanceToAmount(onlyCleared, newBalance)).setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, id) -> dialog.cancel());
        alt_bld.create().show();
    }

    private void adjustBalanceToAmount(boolean onlyCleared, double newBalance) {
        AccountClass act = AccountDB.recordFor(this._filter.getAccount());
        TransactionClass trans = new TransactionClass();
        trans.setAccount(this._filter.getAccount());
        trans.setSubTotal(newBalance);
        trans.setAmount(newBalance);
        trans.initType();
        trans.setCleared(onlyCleared);
        trans.setCategory(Locales.kLOC_TOOLS_RECONCILE_CATEGORY);
        trans.setPayee(Locales.kLOC_TOOLS_RECONCILE_PAYEE);
        if (act != null) {
            trans.setCurrencyCode(act.getCurrencyCode());
        }
        trans.saveToDatabase();
        reloadData();
    }

    private OnClickListener getBalanceBarClickListener() {
        return v -> {
            int i = Prefs.getIntPref(Prefs.BALANCETYPE);
            if (v.equals(TransactionsActivity.this.balanceBar.nextButton)) {
                i = TransactionsActivity.this.balanceBar.nextBalanceTypeAfter(i);
            } else {
                i = TransactionsActivity.this.balanceBar.nextBalanceTypeBefore(i);
            }
            Prefs.setPref(Prefs.BALANCETYPE, i);
            TransactionsActivity.this.reloadBalanceBar();
            TransactionsActivity.this.reloadData();
        };
    }

    public void reloadBalanceBar() {
        if (this._filter != null && this._filter.customFilter()) {
            Prefs.setPref(Prefs.BALANCETYPE, Enums.kBalanceTypeFiltered /*5*/);
        } else if (Prefs.getIntPref(Prefs.BALANCETYPE) == Enums.kBalanceTypeFiltered /*5*/) {
            Prefs.setPref(Prefs.BALANCETYPE, Enums.kBalanceTypeCurrent /*2*/);
        }
        int balanceType = Prefs.getIntPref(Prefs.BALANCETYPE);
        double balance = 0.0d;
        AccountClass act = this._filter.getAccount() != null ? AccountDB.recordFor(this._filter.getAccount()) : null;
        this.balanceBar.setFilter(this._filter);
        if (this._filter.allAccounts()) {
            balanceType = Enums.kBalanceTypeFiltered /*5*/;
            balance = TransactionDB.balanceWithFilter(this._filter);
        } else if (balanceType == Enums.kBalanceTypeFiltered /*5*/) {
            balance = TransactionDB.balanceWithFilter(this._filter);
        } else if (act != null) {
            balance = act.balanceOfType(balanceType);
        }
        if (Enums.kBalanceTypeFiltered /*5*/ == this._filter.getType() || act == null || !act.balanceExceedsLimit()) {
            this.balanceBar.balanceAmountTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
        } else {
            this.balanceBar.balanceAmountTextView.setTextColor(PocketMoneyThemes.redLabelColor());
        }
        if (this._filter.getAccount() == null || this._filter.allAccounts()) {
            this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(balance));
        } else {
            this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(balance));
        }
        String label = AccountDB.totalWorthLabel(balanceType);
        if (balanceType == Enums.kBalanceTypeFiltered) {
            String subtitle = getIntent().getStringExtra("subtitle");
            if (subtitle != null) {
                label = subtitle;
            }
        }
        this.balanceBar.balanceTypeTextView.setText(label);
        this.balanceBar.balanceTypeTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
    }

    private void rollupAction() {
        if (!this.adapter.getElements().isEmpty()) {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
            alt_bld.setMessage(getString(R.string.kLOC_TOOLS_ROLLUP_ALERT, String.valueOf(this.adapter.getElements().size()))).setCancelable(false).setTitle(Locales.kLOC_TOOLS_ROLLUP).setPositiveButton(Locales.kLOC_GENERAL_YES, (dialog, id) -> {
                AlertDialog.Builder alt_bld1 = new AlertDialog.Builder(TransactionsActivity.this.context, PocketMoneyThemes.dialogTheme());
                alt_bld1.setMessage(Locales.kLOC_TOOLS_ROLLUP_CONFIRM).setCancelable(false).setTitle(Locales.kLOC_TOOLS_ROLLUP).setPositiveButton(Locales.kLOC_GENERAL_YES, (dialog2, id2) -> {
                    TransactionDB.rollupTransactionsInFilter(TransactionsActivity.this.adapter.getElements(), TransactionsActivity.this._filter);
                    TransactionsActivity.this.reloadData();
                }).setNegativeButton(Locales.kLOC_GENERAL_NO, (dialog1, id1) -> dialog1.cancel());
                alt_bld1.create().show();
            }).setNegativeButton(Locales.kLOC_GENERAL_NO, (dialog, id) -> dialog.cancel());
            alt_bld.create().show();
        }
    }

    private void exportOFXToSD() {
        final PocketMoneyProgressDialog pd = new PocketMoneyProgressDialog(this.context);
        pd.setMessage("Exporting...");
        pd.show();
        new Thread() {
            public void run() {
                String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
                ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(TransactionsActivity.this._filter);
                String fileName = (TransactionsActivity.this._filter.allAccounts() ? "SMMoney" : TransactionsActivity.this._filter.getAccount()) + "-" + CalExt.descriptionWithTimestamp(new GregorianCalendar()) + ".qfx";
                ImportExportOFX exportofx = new ImportExportOFX(TransactionsActivity.this.context, pmExternalPath + fileName);
                exportofx.filter = TransactionsActivity.this._filter;
                if (!TransactionsActivity.this._filter.allAccounts()) {
                    exportofx.accountNameBeingImported = TransactionsActivity.this._filter.getAccount();
                }
                exportofx.exportRecords(query);
                pd.dismiss();
            }
        }.start();
    }

    private void generateEmailForOFX(ArrayList<String> fileNames) {
        Intent emailIntent = new Intent("android.intent.action.SEND");
        emailIntent.setType("text/ofx");
        Prefs.exportDB(this);
        for (String fileName : fileNames) {
            Uri contentUri = FileProvider.getUriForFile(this, "com.example.fileprovider", new File(fileName));
            emailIntent.putExtra("android.intent.extra.STREAM", contentUri);
        }
        emailIntent.putExtra("android.intent.extra.SUBJECT", "SMMoney OFX/QFX File");
        emailIntent.putExtra("android.intent.extra.TEXT", getString(R.string.kLOC_FILETRANSFERS_EMAIL_BODY, "OFX/QFX", CalExt.descriptionWithMediumDate(new GregorianCalendar())));
        this.fileNames = fileNames;
        emailLauncher.launch(emailIntent);
    }

    private void generateOFXForEmail() {
        final PocketMoneyProgressDialog pd = new PocketMoneyProgressDialog(this.context);
        pd.setMessage("Exporting...");
        pd.show();
        new Thread() {
            public void run() {
                String pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                FilterClass filter = new FilterClass();
                final ArrayList<String> fileNames = new ArrayList<>();
                ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(TransactionsActivity.this._filter);
                String fileName = pmExternalPath + "/PocketMoneyBackup/" + (TransactionsActivity.this._filter.allAccounts() ? "SMMoney" : TransactionsActivity.this._filter.getAccount()) + "-" + CalExt.descriptionWithTimestamp(new GregorianCalendar()) + ".qfx";
                fileNames.add(fileName);
                ImportExportOFX exportofx = new ImportExportOFX(TransactionsActivity.this.context, fileName);
                exportofx.filter = TransactionsActivity.this._filter;
                if (!TransactionsActivity.this._filter.allAccounts()) {
                    exportofx.accountNameBeingImported = TransactionsActivity.this._filter.getAccount();
                }
                exportofx.exportRecords(query);
                pd.dismiss();
                TransactionsActivity.this.runOnUiThread(() -> TransactionsActivity.this.generateEmailForOFX(fileNames));
            }
        }.start();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, MENU_NEW, 0, Locales.kLOC_TRANSACTION_NEW);
        item.setIcon(R.drawable.ic_add_circle_outline_white_24dp_svg);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS/*2*/);
        
        menu.add(0, MENU_SORT, 0, Locales.kLOC_TRANSACTIONS_OPTIONS_SORTON).setIcon(R.drawable.ic_sort).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        SubMenu toolsMenu = menu.addSubMenu(Locales.kLOC_GENERAL_TOOLS);
        toolsMenu.setIcon(R.drawable.icon);
        menu.add(0, MENU_SEARCH, 0, Locales.kLOC_TOOLS_SEARCH).setIcon(R.drawable.places_ic_search);
        toolsMenu.add(0, MENU_TOOLS_FILETRANSFERS, 0, Locales.kLOC_TOOLS_FILETRANSFERS);
        toolsMenu.add(0, MENU_TOOLS_GOTODATE, 0, Locales.kLOC_TRANSACTIONS_OPTIONS_GOTO);
        toolsMenu.add(0, MENU_TOOLS_ADJUSTBALANCE, 0, Locales.kLOC_TOOLS_RECONCILE);
        toolsMenu.add(0, MENU_TOOLS_MARKASCLEAR, 0, Locales.kLOC_TOOLS_MARKCLEARED);
        toolsMenu.add(0, MENU_TOOLS_ROLLUP, 0, Locales.kLOC_TOOLS_ROLLUP);
        menu.add(0, MENU_FILTER, 0, Locales.kLOC_TOOLS_FILTERS).setIcon(R.drawable.ic_arrow_drop_down_circle);
        SubMenu reportsMenu = menu.addSubMenu(Locales.kLOC_GENERAL_REPORTS);
        reportsMenu.setIcon(R.drawable.icon);
        reportsMenu.add(0, MENU_REPORTS_ACCOUNT, 0, Locales.kLOC_TOOLS_ACCOUNTREPORT);
        reportsMenu.add(0, MENU_REPORTS_CATEGORY, 0, Locales.kLOC_TOOLS_CATEGORYREPORT);
        reportsMenu.add(0, MENU_REPORTS_CLASS, 0, Locales.kLOC_TOOLS_CLASSREPORT);
        reportsMenu.add(0, MENU_REPORTS_PAYEE, 0, Locales.kLOC_TOOLS_PAYEEREPORT);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case MENU_NEW /*1*/:
                newTransaction();
                return true;
            case MENU_SORT /*25*/:
                showSortDialog();
                return true;
            case MENU_FILTER /*4*/:
                if (AccountsActivity.isLite(this)) {
                    AccountsActivity.displayLiteDialog(this);
                    return true;
                }
                i = new Intent(this, FiltersMainActivity.class);
                i.putExtra("Filter", this._filter);
                filterLauncher.launch(i);
                return true;
            case MENU_REPORTS_ACCOUNT /*6*/:
                AccountsReportDataSource ds = new AccountsReportDataSource(this._filter);
                i = new Intent(this, ReportsActivity.class);
                PMGlobal.datasource = ds;
                startActivity(i);
                break;
            case MENU_REPORTS_CATEGORY /*7*/:
                CategoryReportDataSource ds2 = new CategoryReportDataSource(this._filter);
                i = new Intent(this, ReportsActivity.class);
                PMGlobal.datasource = ds2;
                startActivity(i);
                break;
            case MENU_REPORTS_CLASS /*8*/:
                ClassReportDataSource ds3 = new ClassReportDataSource(this._filter);
                i = new Intent(this, ReportsActivity.class);
                PMGlobal.datasource = ds3;
                startActivity(i);
                break;
            case MENU_REPORTS_PAYEE /*9*/:
                PayeeReportDataSource ds4 = new PayeeReportDataSource(this._filter);
                i = new Intent(this, ReportsActivity.class);
                PMGlobal.datasource = ds4;
                startActivity(i);
                break;
            case MENU_TOOLS_FILETRANSFERS /*10*/:
                showFileTransfersDialog();
                break;
            case MENU_TOOLS_GOTODATE /*11*/:
                showDatePickerDialog();
                return true;
            case MENU_TOOLS_ADJUSTBALANCE /*13*/:
                adjustBalance();
                break;
            case MENU_TOOLS_MARKASCLEAR /*14*/:
                markAsClear();
                break;
            case MENU_TOOLS_ROLLUP /*15*/:
                rollupAction();
                return true;
            case MENU_SEARCH /*24*/:
                toggleSearch();
                return true;
        }
        return false;
    }

    private void showFileTransfersDialog() {
        CharSequence[] items = new CharSequence[]{Locales.kLOC_TOOLS_EMAILREGISTER, Locales.kLOC_TOOLS_FILETRANSFERS_SDCARD};
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_TOOLS_FILETRANSFERS)
                .setItems(items, (dialog, item) -> {
                    switch (item) {
                        case 0 /*Email register...*/:
                            TransactionsActivity.this.showEmailTransfersDialog();
                            return;
                        case 1 /*Local storage transfers...*/:
                            TransactionsActivity.this.showSdExportDialog();
                            return;
                        default:
                    }
                }).show();
    }

    private void showEmailTransfersDialog() {
        CharSequence[] items3 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX"};
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_TOOLS_FILETRANSFERS_EMAIL)
                .setItems(items3, (dialog, item) -> {
                    String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
                    switch (item) {
                        case EMAIL_QIF /*0*/:
                            TransactionsActivity.this.msgEmail = 0;
                            TransactionsActivity.this.shouldEmail = true;
                            TransactionsActivity.this.emailFileLocation = pmExternalPath + "SMMoney.qif";
                            Log.i("****** EMAIL", "EXPORTING QIF");
                            final String fl = TransactionsActivity.this.emailFileLocation;
                            new Thread() {
                                public void run() {
                                    ImportExportQIF exportqif = new ImportExportQIF(TransactionsActivity.this);
                                    exportqif.setFilter(TransactionsActivity.this._filter);
                                    exportqif.QIFPath = fl;
                                    exportqif.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                }
                            }.start();
                            return;
                        case EMAIL_TDF /*1*/:
                            TransactionsActivity.this.msgEmail = 1;
                            TransactionsActivity.this.shouldEmail = true;
                            TransactionsActivity.this.emailFileLocation = pmExternalPath + "SMMoney.txt";
                            final String fl2 = TransactionsActivity.this.emailFileLocation;
                            new Thread() {
                                public void run() {
                                    ImportExportTDF exporttdf = new ImportExportTDF(TransactionsActivity.this);
                                    exporttdf.CSVPath = fl2;
                                    exporttdf.setFilter(TransactionsActivity.this._filter);
                                    exporttdf.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                }
                            }.start();
                            return;
                        case EMAIL_CSV /*2*/:
                            TransactionsActivity.this.msgEmail = 2;
                            TransactionsActivity.this.shouldEmail = true;
                            TransactionsActivity.this.emailFileLocation = pmExternalPath + "SMMoney.csv";
                            final String fl3 = TransactionsActivity.this.emailFileLocation;
                            new Thread() {
                                public void run() {
                                    ImportExportCSV exportcsv = new ImportExportCSV(TransactionsActivity.this);
                                    exportcsv.CSVPath = fl3;
                                    exportcsv.setFilter(TransactionsActivity.this._filter);
                                    exportcsv.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                }
                            }.start();
                            return;
                        case SplitsActivity.REQUEST_EDIT /*3*/:
                            TransactionsActivity.this.generateOFXForEmail();
                            return;
                        default:
                    }
                }).show();
    }

    private void showLookupFileTransfersDialog() {
        CharSequence[] items4 = new CharSequence[]{"QIF", "TDF", "CSV"};
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_TOOLS_FILETRANSFERS)
                .setItems(items4, (dialog, item) -> {
                    switch (item) {
                        case EMAIL_QIF /*0*/:
                            ImportExportQIF exportqif = new ImportExportQIF(TransactionsActivity.this);
                            exportqif.setFilter(TransactionsActivity.this._filter);
                            exportqif.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                            return;
                        case SplitsActivity.RESULT_CHANGED /*1*/:
                            ImportExportTDF exporttdf = new ImportExportTDF(TransactionsActivity.this);
                            exporttdf.setFilter(TransactionsActivity.this._filter);
                            exporttdf.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                            return;
                        case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                            ImportExportCSV exportcsv = new ImportExportCSV(TransactionsActivity.this);
                            exportcsv.setFilter(TransactionsActivity.this._filter);
                            exportcsv.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                            return;
                        default:
                    }
                }).show();
    }

    private void showSdExportDialog() {
        CharSequence[] items6 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX"};
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_TOOLS_FILETRANSFERS)
                .setItems(items6, (dialog, item) -> {
                    switch (item) {
                        case EMAIL_QIF /*0*/:
                            new Thread() {
                                public void run() {
                                    ImportExportQIF exportqif = new ImportExportQIF(TransactionsActivity.this);
                                    exportqif.setFilter(TransactionsActivity.this._filter);
                                    exportqif.exportRecords(TransactionDB.queryWithFilterOrderByAccount(TransactionsActivity.this._filter));
                                }
                            }.start();
                            return;
                        case EMAIL_TDF /*1*/:
                            new Thread() {
                                public void run() {
                                    ImportExportTDF exporttdf = new ImportExportTDF(TransactionsActivity.this);
                                    exporttdf.setFilter(TransactionsActivity.this._filter);
                                    exporttdf.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                }
                            }.start();
                            return;
                        case EMAIL_CSV /*2*/:
                            new Thread() {
                                public void run() {
                                    ImportExportCSV exportcsv = new ImportExportCSV(TransactionsActivity.this);
                                    exportcsv.setFilter(TransactionsActivity.this._filter);
                                    exportcsv.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                }
                            }.start();
                            return;
                        case SplitsActivity.REQUEST_EDIT /*3*/:
                            TransactionsActivity.this.exportOFXToSD();
                            return;
                        default:
                    }
                }).show();
    }

    private void showDatePickerDialog() {
        GregorianCalendar theDate;
        LinearLayoutManager layoutManager = (LinearLayoutManager) this.recyclerView.getLayoutManager();
        int firstPosition = (layoutManager != null) ? layoutManager.findFirstVisibleItemPosition() : 0;
        
        if (this.adapter.getCount() == 0) {
            theDate = new GregorianCalendar();
        } else {
            TransactionClass trans = (TransactionClass) this.adapter.getItem(Math.max(0, firstPosition));
            theDate = (trans != null) ? trans.getDate() : new GregorianCalendar();
        }
        new DatePickerDialog(this, PocketMoneyThemes.datePickerTheme(), this.mDateSetListener, theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    @SuppressWarnings("unused")
    public void displayError(String msg) {
        AlertDialog alert = new AlertDialog.Builder(this.context, PocketMoneyThemes.dialogTheme()).create();
        alert.setTitle("Error");
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setButton(-1, "OK", (dialog, id) -> dialog.dismiss());
        alert.show();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }

    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    public Handler getHandler() {
        if (this.mHandler == null) {
            createHandler();
        }
        return this.mHandler;
    }

    @SuppressLint("HandlerLeak")
    private void createHandler() {
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case HandlerActivity.MSG_PROGRESS_UPDATE /*4*/:
                        if (TransactionsActivity.this.progressDialog == null) {
                            TransactionsActivity.this.progressDialog = new PocketMoneyProgressDialog(TransactionsActivity.this);
                            TransactionsActivity.this.progressDialog.setMessage("Exporting...\n\nWarning: This may take several minutes");
                            TransactionsActivity.this.progressDialog.setCancelable(true);
                            TransactionsActivity.this.progressDialog.show();
                            try {
                                TransactionsActivity.this.wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
                            } catch (Exception e) {
                                Log.e(com.example.smmoney.SMMoney.TAG, "Exception in handleMessage (MSG_PROGRESS_UPDATE) acquiring wakeLock", e);
                            }
                        }
                        if (TransactionsActivity.this.progressDialog != null && TransactionsActivity.this.progressDialog.isShowing()) {
                            TransactionsActivity.this.progressDialog.setProgress(msg.arg1);
                            return;
                        }
                        return;
                    case HandlerActivity.MSG_PROGRESS_FINISH /*5*/:
                        Log.i("*** MSG_PROGRESS_FINISH", "Finished");
                        if (!TransactionsActivity.this.shouldEmail && msg.obj.getClass().equals(String.class)) {
                            Toast.makeText(TransactionsActivity.this.context, (String) msg.obj, Toast.LENGTH_LONG).show();
                        }
                        try {
                            TransactionsActivity.this.wakeLock.release();
                        } catch (Exception e2) {
                            Log.e(com.example.smmoney.SMMoney.TAG, "Exception in handleMessage (MSG_PROGRESS_FINISH) releasing wakeLock", e2);
                        }
                        try {
                            TransactionsActivity.this.progressDialog.dismiss();
                            TransactionsActivity.this.progressDialog = null;
                        } catch (Exception e3) {
                            Log.e(com.example.smmoney.SMMoney.TAG, "Exception in handleMessage (MSG_PROGRESS_FINISH) dismissing progressDialog", e3);
                        }
                        if (TransactionsActivity.this.progressDialog != null) {
                            Log.i("*** MSG_PROGRESS_FINISH", "Dismissing");
                            TransactionsActivity.this.progressDialog.dismiss();
                            TransactionsActivity.this.progressDialog = null;
                        }
                        if (TransactionsActivity.this.shouldEmail) {
                            Intent emailIntent = new Intent("android.intent.action.SEND");
                            switch (TransactionsActivity.this.msgEmail) {
                                case EMAIL_QIF /*0*/:
                                    Log.i("****EMAIL", "Should Email");
                                    emailIntent.setType("text/qif");
                                    File qifFile = new File(TransactionsActivity.this.emailFileLocation);
                                    Uri qifUri = FileProvider.getUriForFile(TransactionsActivity.this, "com.example.fileprovider", qifFile);
                                    emailIntent.putExtra("android.intent.extra.STREAM", qifUri);
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT, "QIF"));
                                    emailIntent.putExtra("android.intent.extra.TEXT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_BODY, "QIF", CalExt.descriptionWithMediumDate(new GregorianCalendar())));
                                    emailLauncher.launch(emailIntent);
                                    break;
                                case EMAIL_TDF /*1*/:
                                    emailIntent.setType("text/txt");
                                    File tdfFile = new File(TransactionsActivity.this.emailFileLocation);
                                    Uri tdfUri = FileProvider.getUriForFile(TransactionsActivity.this, "com.example.fileprovider", tdfFile);
                                    emailIntent.putExtra("android.intent.extra.STREAM", tdfUri);
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT, "TDF"));
                                    emailIntent.putExtra("android.intent.extra.TEXT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_BODY, "TDF", CalExt.descriptionWithMediumDate(new GregorianCalendar())));
                                    emailLauncher.launch(emailIntent);
                                    break;
                                case EMAIL_CSV /*2*/:
                                    emailIntent.setType("text/csv");
                                    File csvFile = new File(TransactionsActivity.this.emailFileLocation);
                                    Uri csvUri = FileProvider.getUriForFile(TransactionsActivity.this, "com.example.fileprovider", csvFile);
                                    emailIntent.putExtra("android.intent.extra.STREAM", csvUri);
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT, "CSV"));
                                    emailIntent.putExtra("android.intent.extra.TEXT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_BODY, "CSV", CalExt.descriptionWithMediumDate(new GregorianCalendar())));
                                    emailLauncher.launch(emailIntent);
                                    break;
                            }
                            TransactionsActivity.this.shouldEmail = false;
                            return;
                        }
                        TransactionsActivity.this.reloadData();
                        return;
                    case HandlerActivity.MSG_ERROR /*6*/:
                        if (TransactionsActivity.this.progressDialog != null) {
                            TransactionsActivity.this.progressDialog.dismiss();
                        }
                        AlertDialog alert = new AlertDialog.Builder(TransactionsActivity.this.context, PocketMoneyThemes.dialogTheme()).create();
                        alert.setTitle("Error");
                        alert.setMessage((String) msg.obj);
                        alert.setCancelable(false);
                        alert.setButton(-1, "OK", (dialog, id) -> dialog.dismiss());
                        alert.show();
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message id " + msg.what);
                }
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 84) {
            return super.onKeyDown(keyCode, event);
        }
        toggleSearch();
        return true;
    }

    private MaterialButtonToggleGroup.OnButtonCheckedListener getRadioChangedListener() {
        return (group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.pendingbutton) {
                    TransactionsActivity.this._filter.setCleared(0);
                } else if (checkedId == R.id.clearedbutton) {
                    TransactionsActivity.this._filter.setCleared(1);
                } else if (checkedId == R.id.allbutton) {
                    TransactionsActivity.this._filter.setCleared(2);
                }
                TransactionsActivity.this.reloadData();
            }
        };
    }
}
