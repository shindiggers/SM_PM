package com.example.smmoney.views.budgets;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.DialogFragment;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.prefs.MainPrefsActivity;
import com.example.smmoney.records.CategoryBudgetClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.charts.ChartsActivity;
import com.example.smmoney.views.reports.ReportsPlaceholderActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetsActivity extends PocketMoneyActivity implements BudgetScheduleDialog.ScheduleDialogListener, BudgetsPeriodDialog.BudgetsDialogListner, DatePickerDialog.OnDateSetListener {
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

    private BudgetsRowAdapter adapter;
    private ProgressBar reloadProgressBar;
    private androidx.recyclerview.widget.RecyclerView theList;
    private BottomNavigationView bottomNav;
    private View progressiBeamBar;
    private TextView periodDateRangeDisplay;
    private TextView periodAnchorDisplay;
    private ImageView btnToday;
    private WakeLock wakeLock;

    // Summary Card Views
    private TextView actualIncomeValue;
    private TextView actualIncomeLabel;
    private TextView incomeVarianceValue;
    private TextView budgetIncomeValue;
    private TextView budgetIncomeLabel;
    private TextView actualExpenseValue;
    private TextView actualExpenseLabel;
    private TextView expenseVarianceValue;
    private TextView budgetExpenseValue;
    private TextView budgetExpenseLabel;
    private TextView actualNetValue;
    private TextView actualNetLabel;
    private TextView netVarianceValue;
    private TextView budgetNetValue;
    private TextView budgetNetLabel;

    @Override
    @SuppressLint("InflateParams")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).newWakeLock(26, "BudgetsActivity:DoNotDimScreen");
        View layout = LayoutInflater.from(this).inflate(R.layout.budgets, null, false);
        setupView(layout);
        setContentView(layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.GENERAL_BUDGETS);
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
        this.btnToday = layout.findViewById(R.id.btn_today);
        leftArrow.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        rightArrow.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        this.btnToday.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);

        this.periodDateRangeDisplay = layout.findViewById(R.id.period_date_range_display);
        this.periodAnchorDisplay = layout.findViewById(R.id.period_anchor_display);
        this.periodDateRangeDisplay.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(this.periodDateRangeDisplay, ColorStateList.valueOf(fieldLabelColor));

        View periodContainer = layout.findViewById(R.id.period_anchor_container);
        periodContainer.setOnClickListener(v -> openBudgetPeriodDialog());

        leftArrow.setOnClickListener(v -> {
            BudgetsActivity.this.adapter.previousPeriod();
            BudgetsActivity.this.reloadData();
        });
        rightArrow.setOnClickListener(v -> {
            BudgetsActivity.this.adapter.nextPeriod();
            BudgetsActivity.this.reloadData();
        });
        this.btnToday.setOnClickListener(v -> {
            BudgetsActivity.this.adapter.currentDate = new GregorianCalendar();
            BudgetsActivity.this.reloadData();
        });

        // Initialize Summary Card views and theme colors
        com.google.android.material.card.MaterialCardView summaryCard = layout.findViewById(R.id.budget_summary_card);
        if (summaryCard != null) {
            summaryCard.setCardBackgroundColor(PocketMoneyThemes.cardBackgroundColor());
            summaryCard.setStrokeColor(PocketMoneyThemes.cardStrokeColor());
        }

        View summaryDivider = layout.findViewById(R.id.summary_divider);
        if (summaryDivider != null) {
            summaryDivider.setBackgroundColor(PocketMoneyThemes.cardDividerColor());
        }

        this.actualIncomeValue = layout.findViewById(R.id.summary_actual_income_value);
        this.actualIncomeLabel = layout.findViewById(R.id.summary_actual_income_label);
        this.incomeVarianceValue = layout.findViewById(R.id.summary_income_variance_value);
        this.budgetIncomeValue = layout.findViewById(R.id.summary_budget_income_value);
        this.budgetIncomeLabel = layout.findViewById(R.id.summary_budget_income_label);
        this.actualExpenseValue = layout.findViewById(R.id.summary_actual_expense_value);
        this.actualExpenseLabel = layout.findViewById(R.id.summary_actual_expense_label);
        this.expenseVarianceValue = layout.findViewById(R.id.summary_expense_variance_value);
        this.budgetExpenseValue = layout.findViewById(R.id.summary_budget_expense_value);
        this.budgetExpenseLabel = layout.findViewById(R.id.summary_budget_expense_label);
        this.actualNetValue = layout.findViewById(R.id.summary_actual_net_value);
        this.actualNetLabel = layout.findViewById(R.id.summary_actual_net_label);
        this.netVarianceValue = layout.findViewById(R.id.summary_net_variance_value);
        this.budgetNetValue = layout.findViewById(R.id.summary_budget_net_value);
        this.budgetNetLabel = layout.findViewById(R.id.summary_budget_net_label);

        this.reloadProgressBar = layout.findViewById(R.id.reloadprogressbar);
        this.theList = layout.findViewById(R.id.the_list);
        this.theList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        this.theList.setVerticalScrollBarEnabled(false);
        this.adapter = new BudgetsRowAdapter(this);
        this.theList.setAdapter(this.adapter);
        this.theList.setFocusable(false);
        this.theList.setVisibility(View.INVISIBLE);
        setupSwipeActions();
        
        this.bottomNav = layout.findViewById(R.id.bottom_navigation);
        this.bottomNav.setSelectedItemId(R.id.nav_budgets);
        this.bottomNav.setBackgroundColor(PocketMoneyThemes.bottomNavBackgroundColor());
        this.bottomNav.setItemIconTintList(PocketMoneyThemes.bottomNavColorStateList());
        this.bottomNav.setItemTextColor(PocketMoneyThemes.bottomNavColorStateList());
        this.bottomNav.setOnItemSelectedListener(item -> handleBottomNavigation(item.getItemId()));

        this.progressiBeamBar = layout.findViewById(R.id.progressbar);
        layout.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    private boolean handleBottomNavigation(int itemId) {
        if (itemId == R.id.nav_accounts) {
            Intent intent = new Intent(BudgetsActivity.this, AccountsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_left, R.anim.slide_out_right).toBundle());
            return true;
        } else if (itemId == R.id.nav_reports) {
            Intent intent = new Intent(BudgetsActivity.this, ReportsPlaceholderActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
            return true;
        } else if (itemId == R.id.nav_charts) {
            Intent intent = new Intent(BudgetsActivity.this, ChartsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
            return true;
        }
        return itemId == R.id.nav_budgets;
    }

    private boolean showCents() {
        return Prefs.getBooleanPref(Prefs.BUDGETSHOWCENTS);
    }

    private void loadSummaryCard() {
        if (this.adapter == null || this.actualIncomeValue == null) return;

        boolean showCents = showCents();

        // Income
        double actualIncome = this.adapter.totalIncomes();
        double budgetedIncome = this.adapter.budgetedIncomes();
        double incomeVariance = actualIncome - budgetedIncome;

        // Expenses
        double actualExpense = Math.abs(this.adapter.totalExpenses());
        if (Prefs.getBooleanPref(Prefs.BUDGETINCLUDEUNBUDGETED)) {
            actualExpense += Math.abs(this.adapter.totalNonBudgeted());
        }
        double budgetedExpense = Math.abs(this.adapter.budgetedExpenses());
        double expenseVariance = budgetedExpense - actualExpense; // Positive means under budget (ahead)

        // Net Totals (Surplus / Deficit)
        double actualNet = actualIncome - actualExpense;
        double budgetedNet = budgetedIncome - budgetedExpense;
        double netVariance = (actualIncome - budgetedIncome) + (budgetedExpense - actualExpense);

        // Theme colors
        int cardTextColor = PocketMoneyThemes.cardTextColor();
        int cardTextSecondaryColor = PocketMoneyThemes.cardTextSecondaryColor();
        int positiveColor = 0xFF2E7D32; // Green
        int negativeColor = PocketMoneyThemes.redLabelColor();

        // Apply secondary label colors
        this.actualIncomeLabel.setTextColor(cardTextSecondaryColor);
        this.budgetIncomeLabel.setTextColor(cardTextSecondaryColor);
        this.actualExpenseLabel.setTextColor(cardTextSecondaryColor);
        this.budgetExpenseLabel.setTextColor(cardTextSecondaryColor);

        // Bind Row 1: Income
        this.actualIncomeValue.setText(formatCurrency(actualIncome, showCents));
        this.actualIncomeValue.setTextColor(cardTextColor);
        this.budgetIncomeValue.setText(formatCurrency(budgetedIncome, showCents));
        this.budgetIncomeValue.setTextColor(cardTextColor);
        bindVariance(this.incomeVarianceValue, incomeVariance, showCents, positiveColor, negativeColor);

        // Bind Row 2: Expenses
        this.actualExpenseValue.setText(formatCurrency(actualExpense, showCents));
        this.actualExpenseValue.setTextColor(cardTextColor);
        this.budgetExpenseValue.setText(formatCurrency(budgetedExpense, showCents));
        this.budgetExpenseValue.setTextColor(cardTextColor);
        bindVariance(this.expenseVarianceValue, expenseVariance, showCents, positiveColor, negativeColor);

        // Bind Row 3: Net (Surplus / Deficit)
        this.actualNetValue.setText(formatCurrency(Math.abs(actualNet), showCents));
        this.actualNetValue.setTextColor(actualNet >= 0.0 ? positiveColor : negativeColor);
        this.actualNetLabel.setText(actualNet >= 0.0 ? R.string.BUDGET_SUMMARY_ACTUAL_SURPLUS : R.string.BUDGET_SUMMARY_ACTUAL_DEFICIT);
        this.actualNetLabel.setTextColor(cardTextColor);

        this.budgetNetValue.setText(formatCurrency(Math.abs(budgetedNet), showCents));
        this.budgetNetValue.setTextColor(budgetedNet >= 0.0 ? positiveColor : negativeColor);
        this.budgetNetLabel.setText(budgetedNet >= 0.0 ? R.string.BUDGET_SUMMARY_BUDGET_SURPLUS : R.string.BUDGET_SUMMARY_BUDGET_DEFICIT);
        this.budgetNetLabel.setTextColor(cardTextColor);

        bindVariance(this.netVarianceValue, netVariance, showCents, positiveColor, negativeColor);
    }

    private void bindVariance(TextView view, double variance, boolean showCents, int posColor, int negColor) {
        String formattedAmount = formatCurrency(Math.abs(variance), showCents);
        if (variance >= 0.0) {
            view.setText(getString(R.string.BUDGET_SUMMARY_AHEAD, formattedAmount));
            view.setTextColor(posColor);
        } else {
            view.setText(getString(R.string.BUDGET_SUMMARY_BEHIND, formattedAmount));
            view.setTextColor(negColor);
        }
    }

    private String formatCurrency(double amount, boolean showCents) {
        return showCents ? CurrencyExt.amountAsCurrency(amount) : CurrencyExt.amountAsCurrencyWithoutCents(amount);
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
        if (this.periodDateRangeDisplay != null) {
            this.periodDateRangeDisplay.setText(this.adapter.getFormattedPeriodRange());
        }
        if (this.periodAnchorDisplay != null) {
            this.periodAnchorDisplay.setText(this.adapter.getFormattedAnchorSummary());
        }

        if (this.btnToday != null) {
            this.btnToday.setVisibility(this.adapter.isCurrentPeriod() ? View.GONE : View.VISIBLE);
        }

        loadSummaryCard();
        this.reloadProgressBar.setVisibility(View.INVISIBLE);
        this.theList.setVisibility(View.VISIBLE);
        
        if (!this.adapter.isCurrentPeriod()) {
            this.progressiBeamBar.setVisibility(View.GONE);
        } else {
            this.progressiBeamBar.setVisibility(View.VISIBLE);
            android.graphics.drawable.Drawable bg = this.progressiBeamBar.getBackground();
            if (bg != null) {
                bg.mutate().setTint(PocketMoneyThemes.budgetProgressBeamColor());
            }
            int newWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12.0f, getResources().getDisplayMetrics());
            LayoutParams lp = new LayoutParams(newWidth, this.theList.getHeight());
            lp.gravity = Gravity.START;
            int width = this.theList.getWidth();
            int leftMargin = (int) (((double) width) * this.adapter.getProgressPercent()) - (newWidth / 2);
            if (leftMargin < 0) {
                leftMargin = 0;
            } else if (leftMargin + newWidth > width) {
                leftMargin = width - newWidth;
            }
            lp.leftMargin = leftMargin;
            this.progressiBeamBar.setLayoutParams(lp);
            this.progressiBeamBar.bringToFront();
            this.progressiBeamBar.requestLayout();
        }

    }

    private void newBudget() {
        Intent i = new Intent(this, BudgetsEditActivity.class);
        i.putExtra("Category", new CategoryClass());
        editLauncher.launch(i);
    }

    private void setupSwipeActions() {
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public int getSwipeDirs(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView recyclerView, @androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder) {
                // Disable swipe on headers
                if (viewHolder instanceof BudgetsRowHolder) {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
                return 0;
            }

            @Override
            public boolean onMove(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView recyclerView, @androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == androidx.recyclerview.widget.RecyclerView.NO_POSITION) return;

                Object item = adapter.getItem(position);
                if (item instanceof CategoryClass category) {
                    if (direction == androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                        // Swipe Right -> Edit
                        Intent anIntent = new Intent(BudgetsActivity.this, BudgetsEditActivity.class);
                        anIntent.putExtra("Category", category);
                        editLauncher.launch(anIntent);
                        adapter.notifyItemChanged(position);
                    } else if (direction == androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                        // Swipe Left -> Delete
                        deleteBudget(category);
                        adapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onChildDraw(@androidx.annotation.NonNull android.graphics.Canvas c, @androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView recyclerView, @androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    android.graphics.Paint paint = new android.graphics.Paint();

                    if (dX > 0) { // Swipe Right (Edit)
                        paint.setColor(android.graphics.Color.parseColor("#4CAF50")); // Green
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), paint);

                        android.graphics.drawable.Drawable icon = androidx.core.content.ContextCompat.getDrawable(BudgetsActivity.this, R.drawable.ic_edit_white_24dp);
                        if (icon != null) {
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }

                        paint.setColor(android.graphics.Color.WHITE);
                        paint.setTextSize(40);
                        paint.setAntiAlias(true);
                        c.drawText(getString(R.string.GENERAL_EDIT), (float) itemView.getLeft() + 140, (float) itemView.getTop() + (itemView.getHeight() / 2f) + 15, paint);

                    } else if (dX < 0) { // Swipe Left (Delete)
                        paint.setColor(android.graphics.Color.parseColor("#F44336")); // Red
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);

                        android.graphics.drawable.Drawable icon = androidx.core.content.ContextCompat.getDrawable(BudgetsActivity.this, R.drawable.ic_delete_white_24dp);
                        if (icon != null) {
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();
                            int iconRight = itemView.getRight() - iconMargin;
                            int iconLeft = iconRight - icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }

                        paint.setColor(android.graphics.Color.WHITE);
                        paint.setTextSize(40);
                        paint.setAntiAlias(true);
                        float textWidth = paint.measureText(getString(R.string.GENERAL_DELETE));
                        c.drawText(getString(R.string.GENERAL_DELETE), (float) itemView.getRight() - 140 - textWidth, (float) itemView.getTop() + (itemView.getHeight() / 2f) + 15, paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this.theList);
    }

    private void deleteBudget(final CategoryClass cat) {
        Builder b = new Builder(this, PocketMoneyThemes.dialogTheme());
        b.setTitle(R.string.BUDGETCATEGORY_DELETE);
        b.setMessage(R.string.BUDGETCATEGORY_DELETE_BODY);
        b.setPositiveButton(R.string.BUDGETCATEGORY_DELETE_BUDGET, (dialog, which) -> {
            CategoryClass c = new CategoryClass(cat.categoryID);
            c.hydrate();
            c.setBudgetLimit(0.0d);
            CategoryBudgetClass.deleteCategoryBudgetItemsForCateory(c.getCategory());
            c.saveToDatabase();
            BudgetsActivity.this.reloadData();
        });
        b.setNegativeButton(R.string.BUDGETCATEGORY_DELETE_CATBUD, (dialog, which) -> {
            CategoryClass c = new CategoryClass(cat.categoryID);
            c.hydrate();
            c.deleteFromDatabase();
            BudgetsActivity.this.reloadData();
        });
        b.create().show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_NEW, 0, SMMoney.getAppContext().getString(R.string.BUDGETS_NEW)).setIcon(R.drawable.ic_add_circle_outline_white_24dp_svg).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, MENU_SORT, 0, SMMoney.getAppContext().getString(R.string.TRANSACTIONS_OPTIONS_SORTON)).setIcon(R.drawable.ic_sort).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, MENU_PREFS, 0, SMMoney.getAppContext().getString(R.string.GENERAL_PREFERENCES));
        menu.add(0, MENU_GOTODATE, 0, SMMoney.getAppContext().getString(R.string.TRANSACTIONS_OPTIONS_GOTO));
        menu.add(0, MENU_QUIT, 0, SMMoney.getAppContext().getString(R.string.GENERAL_QUIT));
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
                .setTitle(R.string.TRANSACTIONS_OPTIONS_SORTON)
                .setView(dialogView)
                .setPositiveButton(R.string.GENERAL_OK, (dialog, which) -> {
                    int selectedPropertyId = propertyGroup.getCheckedRadioButtonId();
                    int newSort = getSortTypeFromId(selectedPropertyId);

                    int selectedDirectionId = directionGroup.getCheckedRadioButtonId();
                    int newDir = (selectedDirectionId == R.id.sort_asc) ? Enums.kBudgetsSortOrderAscending : Enums.kBudgetsSortOrderDescending;

                    Prefs.setPref(Prefs.BUDGETS_SORTON, newSort);
                    Prefs.setPref(Prefs.BUDGETS_SORT_ORDER_ASCENDING, newDir);
                    reloadData();
                })
                .setNegativeButton(R.string.GENERAL_CANCEL, null)
                .show();
    }

    private int getSortTypeFromId(int selectedPropertyId) {
        if (selectedPropertyId == R.id.sort_actual) return Enums.kBudgetsSortTypeActual;
        if (selectedPropertyId == R.id.sort_budget) return Enums.kBudgetsSortTypeBudgeted;
        if (selectedPropertyId == R.id.sort_percentage) return Enums.kBudgetsSortTypePercentage;
        if (selectedPropertyId == R.id.sort_variance) return Enums.kBudgetsSortTypeVariance;
        return Enums.kBudgetsSortTypeCategory;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        BudgetsActivity.this.adapter.currentDate = new GregorianCalendar(year, month, dayOfMonth);
        BudgetsActivity.this.reloadData();
    }

    public void openBudgetPeriodDialog() {
        BudgetScheduleDialog dialog = new BudgetScheduleDialog();
        dialog.show(getSupportFragmentManager(), "budgetScheduleDialog");
    }

    @Override
    public void onScheduleUpdated(int periodType) {
        if (this.adapter != null) {
            this.adapter.currentPeriod = periodType;
        }
        reloadData();
    }

    @Override
    public void applyPeriodType2(int periodType) {
        Prefs.setPref(Prefs.DISPLAY_BUDGETPERIOD, periodType);
        if (this.adapter != null) {
            this.adapter.currentPeriod = periodType;
        }
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
