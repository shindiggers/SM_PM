package com.catamount.pocketmoney.views.budgets;

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
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.prefs.MainPrefsActivity;
import com.catamount.pocketmoney.records.CategoryBudgetClass;
import com.catamount.pocketmoney.records.CategoryClass;
import com.catamount.pocketmoney.views.BalanceBar;
import com.catamount.pocketmoney.views.PocketMoneyActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;

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
        setTitle("PocketMoney");
        getActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
    }

    public void onPause() {
        super.onPause();
        this.wakeLock.release();
    }

    public void onResume() {
        super.onResume();
        this.wakeLock.acquire();
        reloadData();
    }

    private void setupView(FrameLayout layout) {
        TextView titleTextView = layout.findViewById(R.id.title_text_view);
        titleTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsActivity.this.openOptionsMenu();
            }
        });
        titleTextView.setText("PocketMoney");
        ImageView leftArrow = layout.findViewById(R.id.lefttarrow);
        ImageView rightArrow = layout.findViewById(R.id.rightarrow);
        this.periodButton = layout.findViewById(R.id.periodbutton);
        this.periodButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsActivity.this.showDialog(1);
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
                if (2 == Prefs.getIntPref(Prefs.BUDGETDISPLAY)) {
                    Prefs.setPref(Prefs.BUDGETDISPLAY, 0);
                } else if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) == 0) {
                    Prefs.setPref(Prefs.BUDGETDISPLAY, 3);
                } else {
                    Prefs.setPref(Prefs.BUDGETDISPLAY, 2);
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

    public void loadBalanceBar() {
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

    public void reloadData() {
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
        if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) == 2) {
            this.budgetDisplay.setText(Locales.kLOC_BUDGETS_BUDGETED);
        } else if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) == 0) {
            this.budgetDisplay.setText(Locales.kLOC_BUDGETS_AVAILABLE);
        } else if (Prefs.getIntPref(Prefs.BUDGETDISPLAY) == 3) {
            this.budgetDisplay.setText(Locales.kLOC_BUDGETS_BALANCE);
        }
        int newWidth = (int) TypedValue.applyDimension(1, 9.0f, getResources().getDisplayMetrics());
        LayoutParams lp = new LayoutParams(newWidth, this.theList.getHeight());
        lp.gravity = 3;
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
        menu.add(0, 1, 0, Locales.kLOC_BUDGETS_NEW);
        menu.add(0, 2, 0, Locales.kLOC_GENERAL_PREFERENCES);
        menu.add(0, 3, 0, Locales.kLOC_TRANSACTIONS_OPTIONS_GOTO);
        menu.add(0, 4, 0, "View Options");
        menu.add(0, 5, 0, Locales.kLOC_GENERAL_QUIT);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                newBudget();
                return true;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                startActivity(new Intent(this, MainPrefsActivity.class));
                return true;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                showDialog(2);
                return true;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                startActivity(new Intent(this, BudgetsViewOptionsActivity.class));
                return true;
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
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
        menu.add(0, 1, 0, Locales.kLOC_GENERAL_EDIT).setIntent(i);
        menu.add(0, 3, 0, Locales.kLOC_GENERAL_DELETE).setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                Intent anIntent = new Intent(this, BudgetsEditActivity.class);
                anIntent.putExtra("Category", (CategoryClass) b.get("Category"));
                startActivity(anIntent);
                return true;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                deleteBudget((CategoryClass) b.get("Category"));
                reloadData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                CharSequence[] items = new CharSequence[]{Locales.kLOC_REPEATING_FREQUENCY_DAILY, Locales.kLOC_REPEATING_FREQUENCY_WEEKLY, Locales.kLOC_BUDGETS_BIWEEKLY, Locales.kLOC_BUDGETS_4WEEKS, Locales.kLOC_REPEATING_FREQUENCY_MONTHLY, Locales.kLOC_BUDGETS_BIMONTHLY, Locales.kLOC_REPEATING_FREQUENCY_QUARTERLY, Locales.kLOC_BUDGETS_HALFYEAR, Locales.kLOC_REPEATING_FREQUENCY_YEARLY};
                Builder builder = new Builder(this);
                builder.setTitle(Locales.kLOC_BUDGETS_PERIOD);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        int periodType = -1;
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                periodType = 0;
                                break;
                            case SplitsActivity.RESULT_CHANGED /*1*/:
                                periodType = 1;
                                break;
                            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                                periodType = 5;
                                break;
                            case SplitsActivity.REQUEST_EDIT /*3*/:
                                periodType = 8;
                                break;
                            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                                periodType = 2;
                                break;
                            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                                periodType = 6;
                                break;
                            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                                periodType = 3;
                                break;
                            case LookupsListActivity.ID_LOOKUP /*7*/:
                                periodType = 7;
                                break;
                            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                                periodType = 4;
                                break;
                        }
                        Prefs.setPref(Prefs.DISPLAY_BUDGETPERIOD, periodType);
                        BudgetsActivity.this.adapter.currentPeriod = periodType;
                        dialog.dismiss();
                        BudgetsActivity.this.reloadData();
                    }
                });
                return builder.create();
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
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
