package com.example.smmoney.views.budgets;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.R;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.prefs.MainPrefsActivity;
import com.example.smmoney.records.CategoryBudgetClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.views.BalanceBar;
import com.example.smmoney.views.EndOnDateActivity;
import com.example.smmoney.views.PocketMoneyActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetsActivity extends PocketMoneyActivity implements BudgetsPeriodDialog.BudgetsDialogListner, DatePickerDialog.OnDateSetListener {
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    private final int MENU_GOTODATE = 3;
    private final int MENU_NEW = 1;
    private final int MENU_PREFS = 2;
    private final int MENU_QUIT = 5;
    private final int MENU_SORT = 4;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> reloadData()
    );

    private final ActivityResultLauncher<Intent> startDatePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == EndOnDateActivity.ENDONDATE_RESULT_DATESELECTED && result.getData() != null) {
                    String date = result.getData().getStringExtra("Date");
                    Prefs.setPref(Prefs.BUDGETSTARTDATE, date);
                    reloadData();
                } else if (result.getResultCode() == EndOnDateActivity.ENDONDATE_RESULT_NODATESELECTED) {
                    Prefs.setPref(Prefs.BUDGETSTARTDATE, Locales.kLOC_GENERAL_DEFAULT);
                    reloadData();
                }
            }
    );

    private BudgetsRowAdapter adapter;
    private BalanceBar balanceBar;
    private ProgressBar reloadProgressBar;
    private ListView theList;
    private BottomNavigationView bottomNav;
    private View progressiBeamBar;
    private MaterialButton periodButton;
    private TextView startDateDisplay;
    private WakeLock wakeLock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).newWakeLock(26, "BudgetsActivity:DoNotDimScreen");
        View layout = LayoutInflater.from(this).inflate(R.layout.budgets, null, false);
        setupView(layout);
        setContentView(layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_GENERAL_BUDGETS);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        }
    }

    public void onPause() {
        super.onPause();
        this.wakeLock.release();
    }

    public void onResume() {
        super.onResume();
        if (this.bottomNav != null) {
            this.bottomNav.setSelectedItemId(R.id.nav_budgets);
        }
        this.wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        reloadData();
    }

    private void setupView(View layout) {
        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();
        ImageView leftArrow = layout.findViewById(R.id.lefttarrow);
        ImageView rightArrow = layout.findViewById(R.id.rightarrow);
        leftArrow.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        rightArrow.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        this.periodButton = layout.findViewById(R.id.periodbutton);
        this.periodButton.setOnClickListener(v -> openBudgetPeriodDialog());
        
        this.startDateDisplay = layout.findViewById(R.id.start_date_display);
        OnClickListener startDateListener = v -> {
            Intent anIntent = new Intent(BudgetsActivity.this, com.example.smmoney.views.EndOnDateActivity.class);
            anIntent.putExtra("Date", BudgetsActivity.this.startDateDisplay.getText().toString());
            anIntent.putExtra(Prefs.BUDGETSTARTDATE, true);
            startDatePickerLauncher.launch(anIntent);
        };
        this.startDateDisplay.setOnClickListener(startDateListener);
        layout.findViewById(R.id.start_date_icon).setOnClickListener(startDateListener);
        layout.findViewById(R.id.start_date_label).setOnClickListener(startDateListener);

        leftArrow.setOnClickListener(v -> {
            BudgetsActivity.this.adapter.previousPeriod();
            BudgetsActivity.this.reloadData();
        });
        rightArrow.setOnClickListener(v -> {
            BudgetsActivity.this.adapter.nextPeriod();
            BudgetsActivity.this.reloadData();
        });
        this.balanceBar = layout.findViewById(R.id.balancebar);
        this.balanceBar.nextButton.setOnClickListener(v -> {
            Prefs.setPref(Prefs.BUDGETSAVEDBEAT, Prefs.getIntPref(Prefs.BUDGETSAVEDBEAT) == 0 ? 1 : 0);
            BudgetsActivity.this.reloadData();
        });
        this.balanceBar.previousButton.setOnClickListener(v -> {
            Prefs.setPref(Prefs.BUDGETSAVEDBEAT, Prefs.getIntPref(Prefs.BUDGETSAVEDBEAT) == 0 ? 1 : 0);
            BudgetsActivity.this.reloadData();
        });

        this.reloadProgressBar = layout.findViewById(R.id.reloadprogressbar);
        this.theList = layout.findViewById(R.id.the_list);
        this.theList.setItemsCanFocus(true);
        this.theList.setVerticalScrollBarEnabled(false);
        this.adapter = new BudgetsRowAdapter(this, this.theList);
        this.theList.setAdapter(this.adapter);
        this.theList.setFocusable(false);
        this.theList.setVisibility(View.INVISIBLE);
        
        this.bottomNav = layout.findViewById(R.id.bottom_navigation);
        this.bottomNav.setSelectedItemId(R.id.nav_budgets);
        this.bottomNav.setBackgroundColor(PocketMoneyThemes.bottomNavBackgroundColor());
        this.bottomNav.setItemIconTintList(PocketMoneyThemes.bottomNavColorStateList());
        this.bottomNav.setItemTextColor(PocketMoneyThemes.bottomNavColorStateList());
        this.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_accounts) {
                Intent intent = new Intent(BudgetsActivity.this, com.example.smmoney.views.accounts.AccountsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (itemId == R.id.nav_reports) {
                Intent intent = new Intent(BudgetsActivity.this, com.example.smmoney.views.reports.ReportsPlaceholderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemId == R.id.nav_charts) {
                Intent intent = new Intent(BudgetsActivity.this, com.example.smmoney.views.charts.ChartsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return itemId == R.id.nav_budgets;
        });

        this.progressiBeamBar = layout.findViewById(R.id.progressbar);
        layout.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((ImageView) layout.findViewById(R.id.start_date_icon)).setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        ((TextView) layout.findViewById(R.id.start_date_label)).setTextColor(fieldLabelColor);
        this.startDateDisplay.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.periodButton.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.periodButton.setBackgroundTintList(ColorStateList.valueOf(PocketMoneyThemes.highlightColor()));
        this.periodButton.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.0f, getResources().getDisplayMetrics()));
    }

    private boolean showCents() {
        return Prefs.getBooleanPref(Prefs.BUDGETSHOWCENTS);
    }

    private void loadBalanceBar() {
        double savings;
        double budgetedIncome = this.adapter.budgetedIncomes();
        double budgetedExpense = this.adapter.budgetedExpenses();
        double totalIncome = this.adapter.totalIncomes();
        double totalExpense = this.adapter.totalExpenses();
        if (totalExpense != 0.0d) {
            totalExpense *= -1.0d;
        }
        if (Prefs.getIntPref(Prefs.BUDGETSAVEDBEAT) == 0) {
            savings = totalIncome - totalExpense;
            if (Prefs.getBooleanPref(Prefs.BUDGETINCLUDEUNBUDGETED)) {
                savings += this.adapter.totalNonBudgeted();
            }
        } else {
            savings = (totalIncome - budgetedIncome) + (budgetedExpense - totalExpense);
        }
        
        String text = showCents() ? CurrencyExt.amountAsCurrency(Math.abs(savings)) : CurrencyExt.amountAsCurrencyWithoutCents(Math.abs(savings));
        int textColor = PocketMoneyThemes.headerTextColor();
        
        if (savings < 0.0d) {
            this.balanceBar.balanceAmountTextView.setText("(" + text + ")");
        } else {
            this.balanceBar.balanceAmountTextView.setText(text);
        }
        this.balanceBar.balanceAmountTextView.setTextColor(textColor);

        if (savings >= 0.0d) {
            if (Prefs.getIntPref(Prefs.BUDGETSAVEDBEAT) == 0) {
                this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_BUDGETS_SAVED);
            } else {
                this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_BUDGET_LBL_BEATING_BY);
            }
        } else {
            if (Prefs.getIntPref(Prefs.BUDGETSAVEDBEAT) == 0) {
                this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_BUDGETS_DEFICIT);
            } else {
                this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_BUDGET_LBL_MISSING_BY);
            }
        }
        this.balanceBar.balanceTypeTextView.setTextColor(textColor);
    }

    private void reloadData() {
        if (this.reloadProgressBar.getVisibility() == View.INVISIBLE) {
            this.reloadProgressBar.setVisibility(View.VISIBLE);
        }
        executor.execute(() -> {
            BudgetsActivity.this.adapter.reloadData();
            runOnUiThread(() -> {
                if (isFinishing()) return;
                BudgetsActivity.this.reloadDataCallBack();
            });
        });
    }

    private void reloadDataCallBack() {
        this.periodButton.setText(this.adapter.rangeOfPeriodAsString());
        this.startDateDisplay.setText(Prefs.getStringPref(Prefs.BUDGETSTARTDATE));
        
        loadBalanceBar();
        this.reloadProgressBar.setVisibility(View.INVISIBLE);
        this.theList.setVisibility(View.VISIBLE);
        
        int newWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9.0f, getResources().getDisplayMetrics());
        LayoutParams lp = new LayoutParams(newWidth, this.theList.getHeight());
        lp.gravity = Gravity.START;
        int width = this.theList.getWidth();
        int leftMargin = (int) (((double) width) * this.adapter.getProgressPercent());
        if (leftMargin + newWidth > width) {
            leftMargin = width - newWidth;
        }
        lp.leftMargin = leftMargin;
        this.progressiBeamBar.setLayoutParams(lp);
        this.progressiBeamBar.bringToFront();
        this.progressiBeamBar.requestLayout();

    }

    private void newBudget() {
        Intent i = new Intent(this, BudgetsEditActivity.class);
        i.putExtra("Category", new CategoryClass());
        editLauncher.launch(i);
    }

    private void deleteBudget(final CategoryClass cat) {
        Builder b = new Builder(this, PocketMoneyThemes.dialogTheme());
        b.setTitle(Locales.kLOC_BUDGETCATEGORY_DELETE);
        b.setMessage(Locales.kLOC_BUDGETCATEGORY_DELETE_BODY);
        b.setPositiveButton(Locales.kLOC_BUDGETCATEGORY_DELETE_BUDGET, (dialog, which) -> {
            CategoryClass c = new CategoryClass(cat.categoryID);
            c.hydrate();
            c.setBudgetLimit(0.0d);
            CategoryBudgetClass.deleteCategoryBudgetItemsForCateory(c.getCategory());
            c.saveToDatabase();
            BudgetsActivity.this.reloadData();
        });
        b.setNegativeButton(Locales.kLOC_BUDGETCATEGORY_DELETE_CATBUD, (dialog, which) -> {
            CategoryClass c = new CategoryClass(cat.categoryID);
            c.hydrate();
            c.deleteFromDatabase();
            BudgetsActivity.this.reloadData();
        });
        b.create().show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_NEW, 0, Locales.kLOC_BUDGETS_NEW).setIcon(R.drawable.ic_add_circle_outline_white_24dp_svg).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_SORT, 0, Locales.kLOC_TRANSACTIONS_OPTIONS_SORTON).setIcon(R.drawable.ic_sort).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_PREFS, 0, Locales.kLOC_GENERAL_PREFERENCES);
        menu.add(0, MENU_GOTODATE, 0, Locales.kLOC_TRANSACTIONS_OPTIONS_GOTO);
        menu.add(0, MENU_QUIT, 0, Locales.kLOC_GENERAL_QUIT);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_NEW -> {
                newBudget();
                return true;
            }
            case MENU_SORT -> {
                showSortOnDialog();
                return true;
            }
            case MENU_PREFS -> {
                startActivity(new Intent(this, MainPrefsActivity.class));
                return true;
            }
            case MENU_GOTODATE -> {
                DialogFragment datePicker = new BudgetsDatePickerDialog();
                datePicker.show(getSupportFragmentManager(), "date picker");
                return true;
            }
            case MENU_QUIT -> {
                Prefs.setPref(Prefs.SHUTTINGDOWN, true);
                setResult(1);
                finish();
                return true;
            }
        }
        return false;
    }

    private void showSortOnDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_budget_sort, null);
        RadioGroup propertyGroup = dialogView.findViewById(R.id.sort_property_group);
        RadioGroup directionGroup = dialogView.findViewById(R.id.sort_direction_group);

        // Theme the dialog
        int labelColor = PocketMoneyThemes.fieldLabelColor();
        int textColor = PocketMoneyThemes.primaryCellTextColor();
        ColorStateList tint = ColorStateList.valueOf(PocketMoneyThemes.currentTintColor());

        ((TextView) dialogView.findViewById(R.id.sort_by_label)).setTextColor(labelColor);
        ((TextView) dialogView.findViewById(R.id.order_label)).setTextColor(labelColor);

        for (int i = 0; i < propertyGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) propertyGroup.getChildAt(i);
            rb.setTextColor(textColor);
            rb.setButtonTintList(tint);
        }
        for (int i = 0; i < directionGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) directionGroup.getChildAt(i);
            rb.setTextColor(textColor);
            rb.setButtonTintList(tint);
        }

        // Set current state
        int currentSort = Prefs.getIntPref(Prefs.BUDGETS_SORTON);
        int currentDir = Prefs.getIntPref(Prefs.BUDGETS_SORT_ORDER_ASCENDING);

        switch (currentSort) {
            case Enums.kBudgetsSortTypeCategory -> propertyGroup.check(R.id.sort_category);
            case Enums.kBudgetsSortTypeActual -> propertyGroup.check(R.id.sort_actual);
            case Enums.kBudgetsSortTypeBudgeted -> propertyGroup.check(R.id.sort_budget);
            case Enums.kBudgetsSortTypePercentage -> propertyGroup.check(R.id.sort_percentage);
            case Enums.kBudgetsSortTypeVariance -> propertyGroup.check(R.id.sort_variance);
        }

        if (currentDir == Enums.kBudgetsSortOrderAscending) directionGroup.check(R.id.sort_asc);
        else directionGroup.check(R.id.sort_desc);

        new Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_TRANSACTIONS_OPTIONS_SORTON)
                .setView(dialogView)
                .setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, which) -> {
                    int selectedPropertyId = propertyGroup.getCheckedRadioButtonId();
                    int newSort = Enums.kBudgetsSortTypeCategory;
                    if (selectedPropertyId == R.id.sort_actual) newSort = Enums.kBudgetsSortTypeActual;
                    else if (selectedPropertyId == R.id.sort_budget) newSort = Enums.kBudgetsSortTypeBudgeted;
                    else if (selectedPropertyId == R.id.sort_percentage) newSort = Enums.kBudgetsSortTypePercentage;
                    else if (selectedPropertyId == R.id.sort_variance) newSort = Enums.kBudgetsSortTypeVariance;

                    int selectedDirectionId = directionGroup.getCheckedRadioButtonId();
                    int newDir = (selectedDirectionId == R.id.sort_asc) ? Enums.kBudgetsSortOrderAscending : Enums.kBudgetsSortOrderDescending;

                    Prefs.setPref(Prefs.BUDGETS_SORTON, newSort);
                    Prefs.setPref(Prefs.BUDGETS_SORT_ORDER_ASCENDING, newDir);
                    reloadData();
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null)
                .show();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        BudgetsRowHolder aHolder = (BudgetsRowHolder) v.getTag();
        Intent i = new Intent();
        i.putExtra("Category", aHolder.category);
        menu.add(0, CMENU_EDIT, 0, Locales.kLOC_GENERAL_EDIT).setIntent(i);
        menu.add(0, CMENU_DELETE, 0, Locales.kLOC_GENERAL_DELETE).setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case CMENU_EDIT -> {
                Intent anIntent = new Intent(this, BudgetsEditActivity.class);
                if (b != null) {
                    CategoryClass category;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        category = b.getSerializable("Category", CategoryClass.class);
                    } else {
                        //noinspection deprecation
                        category = (CategoryClass) b.get("Category");
                    }
                    anIntent.putExtra("Category", category);
                }
                editLauncher.launch(anIntent);
                return true;
            }
            case CMENU_DELETE -> {
                if (b != null) {
                    CategoryClass category;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        category = b.getSerializable("Category", CategoryClass.class);
                    } else {
                        //noinspection deprecation
                        category = (CategoryClass) b.get("Category");
                    }
                    deleteBudget(category);
                }
                reloadData();
                return true;
            }
            default -> {
                return super.onContextItemSelected(item);
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        BudgetsActivity.this.adapter.currentDate = new GregorianCalendar(year, month, dayOfMonth);
        BudgetsActivity.this.reloadData();
    }

    public void openBudgetPeriodDialog() {
        BudgetsPeriodDialog budgetsPeriodDialog = new BudgetsPeriodDialog();
        budgetsPeriodDialog.show(getSupportFragmentManager(), "budgetsPeriodDialog");
    }

    @Override
    public void applyPeriodType2(int periodType) {
        Prefs.setPref(Prefs.DISPLAY_BUDGETPERIOD, periodType);
        BudgetsActivity.this.adapter.currentPeriod = periodType;
        BudgetsActivity.this.reloadData();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            Prefs.setPref(Prefs.SHUTTINGDOWN, true);
            setResult(1);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
