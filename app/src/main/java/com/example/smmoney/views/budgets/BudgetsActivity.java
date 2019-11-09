package com.example.smmoney.views.budgets;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

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
import com.example.smmoney.views.PocketMoneyActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class BudgetsActivity extends PocketMoneyActivity {
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    private final int DIALOG_GOTODATE = 2;
    private final int DIALOG_PERIOD = 1;
    private final int MENU_GOTODATE = 3;
    private final int MENU_NEW = 1;
    private final int MENU_PREFS = 2;
    private final int MENU_QUIT = 5;
    private final int MENU_VIEW = 4;
    private BudgetsRowAdapter adapter;
    private BalanceBar balanceBar;
    private TextView budgetDisplay;
    private ProgressBar budgetProgressBar;
    private Context context;
    private OnDateSetListener mDateSetListener = new OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            BudgetsActivity.this.adapter.currentDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            BudgetsActivity.this.reloadData();
        }
    };
    private Button periodButton;
    private View progressiBeamBar;
    private ProgressBar reloadProgressBar;
    private ListView theList;
    private WakeLock wakeLock;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(26, "BudgetsActivity:DoNotDimScreen");
        this.context = this;
        FrameLayout layout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.budgets, null);
        setupView(layout);
        setContentView(layout);
        setTitle("SMMoney");
        getActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
    }

    public void onPause() {
        super.onPause();
        this.wakeLock.release();
    }

    public void onResume() {
        super.onResume();
        this.wakeLock.acquire(10*60*1000L /*10 minutes*/);
        reloadData();
    }

    private void setupView(FrameLayout layout) {
        TextView titleTextView = layout.findViewById(R.id.title_text_view);
        titleTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsActivity.this.openOptionsMenu();
            }
        });
        titleTextView.setText("SMMoney");
        ImageView leftArrow = layout.findViewById(R.id.lefttarrow);
        ImageView rightArrow = layout.findViewById(R.id.rightarrow);
        this.periodButton = layout.findViewById(R.id.periodbutton);
        this.periodButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsActivity.this.showDialog(DIALOG_PERIOD /*1*/);
            }
        });
        leftArrow.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsActivity.this.adapter.previousPeriod();
                BudgetsActivity.this.reloadData();
            }
        });
        rightArrow.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsActivity.this.adapter.nextPeriod();
                BudgetsActivity.this.reloadData();
            }
        });
        this.balanceBar = layout.findViewById(R.id.balancebar);
        this.balanceBar.nextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (Prefs.getIntPref(Prefs.BUDGETSAVEDBEAT) == 0) {
                    Prefs.setPref(Prefs.BUDGETSAVEDBEAT, 1);
                } else {
                    Prefs.setPref(Prefs.BUDGETSAVEDBEAT, 0);
                }
                BudgetsActivity.this.reloadData();
            }
        });
        this.budgetDisplay = layout.findViewById(R.id.budgetdisplaytextview);
        this.budgetDisplay.setVisibility(View.INVISIBLE);
        this.budgetDisplay.setTextColor(-1);
        this.budgetDisplay.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (Enums.kBudgetDisplayExpenseBudgeted == Prefs.getIntPref(Prefs.BUDGETDISPLAY)) {
                    Prefs.setPref(Prefs.BUDGETDISPLAY, Enums.kBudgetDisplayExpenseAvailable);
                } else if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) ==Enums.kBudgetDisplayExpenseAvailable) {
                    Prefs.setPref(Prefs.BUDGETDISPLAY, Enums.kBudgetDisplayExpenseOver);
                } else {
                    Prefs.setPref(Prefs.BUDGETDISPLAY, Enums.kBudgetDisplayExpenseBudgeted);
                }
                BudgetsActivity.this.budgetProgressBar.setVisibility(View.VISIBLE);
                BudgetsActivity.this.budgetDisplay.setVisibility(View.INVISIBLE);
                BudgetsActivity.this.reloadData();
            }
        });
        this.budgetProgressBar = layout.findViewById(R.id.budgetprogressbar);
        this.budgetProgressBar.setVisibility(View.INVISIBLE);
        this.reloadProgressBar = layout.findViewById(R.id.reloadprogressbar);
        this.theList = layout.findViewById(R.id.the_list);
        this.theList.setItemsCanFocus(true);
        this.theList.setVerticalScrollBarEnabled(false);
        this.adapter = new BudgetsRowAdapter(this, this.theList);
        this.theList.setAdapter(this.adapter);
        this.theList.setFocusable(false);
        this.theList.setVisibility(View.INVISIBLE);
        RadioGroup rg = layout.findViewById(R.id.radiogroup);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                BudgetsActivity.this.finish();
                BudgetsActivity.this.overridePendingTransition(0, 0);
            }
        });
        ((View) rg.getParent()).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.progressiBeamBar = layout.findViewById(R.id.progressbar);
        layout.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        FrameLayout theView = layout.findViewById(R.id.the_tool_bar);
        theView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        theView.setVisibility(View.GONE);
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
        this.balanceBar.balanceAmountTextView.setTextColor(-1);
        if (showCents()) {
            this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(savings));
        } else {
            this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrencyWithoutCents(savings));
        }
        if (savings >= 0.0d) {
            this.balanceBar.balanceTypeTextView.setTextColor(-1);
            if (Prefs.getIntPref(Prefs.BUDGETSAVEDBEAT) == 0) {
                this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_BUDGETS_SAVED);
                return;
            } else {
                this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_BUDGETS_BEATBUDGET);
                return;
            }
        }
        this.balanceBar.balanceTypeTextView.setTextColor(-1);
        if (Prefs.getIntPref(Prefs.BUDGETSAVEDBEAT) == 0) {
            this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_BUDGETS_DEFICIT);
        } else {
            this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_BUDGETS_OVERBUDGET);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void reloadData() {
        if (this.budgetProgressBar.getVisibility() == View.INVISIBLE) {
            this.reloadProgressBar.setVisibility(View.VISIBLE);
        }
        new AsyncTask() {
            protected Object doInBackground(Object... params) {
                BudgetsActivity.this.adapter.reloadData();
                return null;
            }

            protected void onPostExecute(Object result) {
                BudgetsActivity.this.reloadDataCallBack();
            }
        }.execute();
    }

    private void reloadDataCallBack() {
        this.periodButton.setText(this.adapter.rangeOfPeriodAsString());
        if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) == Enums.kBudgetDisplayExpenseBudgeted/*2*/) {
            this.budgetDisplay.setText(Locales.kLOC_BUDGETS_BUDGETED);
        } else if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) == Enums.kBudgetDisplayExpenseAvailable/*0*/) {
            this.budgetDisplay.setText(Locales.kLOC_BUDGETS_AVAILABLE);
        } else if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) == Enums.kBudgetDisplayExpenseOver/*3*/) {
            this.budgetDisplay.setText(Locales.kLOC_BUDGETS_BALANCE);
        }
        int newWidth = (int) TypedValue.applyDimension(1, 9.0f, getResources().getDisplayMetrics());
        LayoutParams lp = new LayoutParams(newWidth, this.theList.getHeight());
        lp.gravity = Gravity.LEFT/*3*/;
        int width = this.theList.getWidth();
        int leftMargin = (int) (((double) width) * this.adapter.getProgressPercent());
        if (leftMargin + newWidth > width) {
            leftMargin = width - newWidth;
        }
        lp.leftMargin = leftMargin;
        this.progressiBeamBar.setLayoutParams(lp);
        this.progressiBeamBar.bringToFront();
        this.progressiBeamBar.requestLayout();
        loadBalanceBar();
        this.budgetProgressBar.setVisibility(View.INVISIBLE);
        this.reloadProgressBar.setVisibility(View.INVISIBLE);
        this.budgetDisplay.setVisibility(View.VISIBLE);
        this.theList.setVisibility(View.VISIBLE);
    }

    private void newBudget() {
        Intent i = new Intent(this, BudgetsEditActivity.class);
        i.putExtra("Category", new CategoryClass());
        startActivity(i);
    }

    private void deleteBudget(final CategoryClass cat) {
        Builder b = new Builder(this);
        b.setTitle(Locales.kLOC_BUDGETCATEGORY_DELETE);
        b.setMessage(Locales.kLOC_BUDGETCATEGORY_DELETE_BODY);
        b.setPositiveButton(Locales.kLOC_BUDGETCATEGORY_DELETE_BUDGET, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CategoryClass c = new CategoryClass(cat.categoryID);
                c.hydrate();
                c.setBudgetLimit(0.0d);
                CategoryBudgetClass.deleteCategoryBudgetItemsForCateory(c.getCategory());
                c.saveToDatabase();
                BudgetsActivity.this.reloadData();
            }
        });
        b.setNegativeButton(Locales.kLOC_BUDGETCATEGORY_DELETE_CATBUD, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CategoryClass c = new CategoryClass(cat.categoryID);
                c.hydrate();
                c.deleteFromDatabase();
                BudgetsActivity.this.reloadData();
            }
        });
        b.create().show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_NEW, 0, Locales.kLOC_BUDGETS_NEW);
        menu.add(0, MENU_PREFS, 0, Locales.kLOC_GENERAL_PREFERENCES);
        menu.add(0, MENU_GOTODATE, 0, Locales.kLOC_TRANSACTIONS_OPTIONS_GOTO);
        menu.add(0, MENU_VIEW, 0, "View Options");
        menu.add(0, MENU_QUIT, 0, Locales.kLOC_GENERAL_QUIT);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_NEW /*1*/:
                newBudget();
                return true;
            case MENU_PREFS /*2*/:
                startActivity(new Intent(this, MainPrefsActivity.class));
                return true;
            case MENU_GOTODATE /*3*/:
                showDialog(DIALOG_GOTODATE /*2*/);
                return true;
            case MENU_VIEW /*4*/:
                startActivity(new Intent(this, BudgetsViewOptionsActivity.class));
                return true;
            case MENU_QUIT /*5*/:
                Prefs.setPref(Prefs.SHUTTINGDOWN, true);
                setResult(1);
                finish();
                break;
        }
        return false;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        BudgetsRowHolder aHolder = (BudgetsRowHolder) v;
        Intent i = new Intent();
        i.putExtra("Category", aHolder.category);
        menu.add(0, CMENU_EDIT, 0, Locales.kLOC_GENERAL_EDIT).setIntent(i);
        menu.add(0, CMENU_DELETE, 0, Locales.kLOC_GENERAL_DELETE).setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case CMENU_EDIT /*1*/:
                Intent anIntent = new Intent(this, BudgetsEditActivity.class);
                anIntent.putExtra("Category", (CategoryClass) b.get("Category"));
                startActivity(anIntent);
                return true;
            case CMENU_DELETE /*3*/:
                deleteBudget((CategoryClass) b.get("Category"));
                reloadData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PERIOD /*1*/:
                CharSequence[] items = new CharSequence[]{Locales.kLOC_REPEATING_FREQUENCY_DAILY, Locales.kLOC_REPEATING_FREQUENCY_WEEKLY, Locales.kLOC_BUDGETS_BIWEEKLY, Locales.kLOC_BUDGETS_4WEEKS, Locales.kLOC_REPEATING_FREQUENCY_MONTHLY, Locales.kLOC_BUDGETS_BIMONTHLY, Locales.kLOC_REPEATING_FREQUENCY_QUARTERLY, Locales.kLOC_BUDGETS_HALFYEAR, Locales.kLOC_REPEATING_FREQUENCY_YEARLY};
                Builder builder = new Builder(this);
                builder.setTitle(Locales.kLOC_BUDGETS_PERIOD);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        int periodType = -1;
                        switch (item) {
                            case 0 /*0*/:
                                periodType = Enums.kBudgetPeriodDay;
                                break;
                            case 1 /*1*/:
                                periodType = Enums.kBudgetPeriodWeek;
                                break;
                            case 2 /*2*/:
                                periodType = Enums.kBudgetPeriodBiweekly;
                                break;
                            case 3 /*3*/:
                                periodType = Enums.kBudgetPeriod4Weeks;
                                break;
                            case 4 /*4*/:
                                periodType = Enums.kBudgetPeriodMonth;
                                break;
                            case 5 /*5*/:
                                periodType = Enums.kBudgetPeriodBimonthly;
                                break;
                            case 6 /*6*/:
                                periodType = Enums.kBudgetPeriodQuarter;
                                break;
                            case 7 /*7*/:
                                periodType = Enums.kBudgetPeriodHalfYear;
                                break;
                            case 8 /*8*/:
                                periodType = Enums.kBudgetPeriodYear;
                                break;
                        }
                        Prefs.setPref(Prefs.DISPLAY_BUDGETPERIOD, periodType);
                        BudgetsActivity.this.adapter.currentPeriod = periodType;
                        dialog.dismiss();
                        BudgetsActivity.this.reloadData();
                    }
                });
                return builder.create();
            case DIALOG_GOTODATE /*2*/:
                GregorianCalendar theDate = this.adapter.currentDate;
                return new DatePickerDialog(this, this.mDateSetListener, theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH));
            default:
                return null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        Prefs.setPref(Prefs.SHUTTINGDOWN, true);
        setResult(1);
        finish();
        return true;
    }
}
