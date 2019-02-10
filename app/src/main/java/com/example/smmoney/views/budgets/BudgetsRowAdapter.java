package com.example.smmoney.views.budgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;
import com.example.smmoney.views.transactions.TransactionsActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

public class BudgetsRowAdapter extends BaseAdapter {
    static final Comparator<CategoryClass> categoryComparator = new Comparator<CategoryClass>() {
        public int compare(CategoryClass category1, CategoryClass category2) {
            double diff;
            switch (Prefs.getIntPref(Prefs.BUDGETS_SORTON)) {
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    diff = category2.spent - category1.spent;
                    if (diff < 0.0d) {
                        return -1;
                    }
                    return diff > 0.0d ? 1 : 0;
                case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                    diff = category2.budget - category1.budget;
                    if (diff < 0.0d) {
                        return -1;
                    }
                    return diff > 0.0d ? 1 : 0;
                case SplitsActivity.REQUEST_EDIT /*3*/:
                    diff = (category1.spent / category1.budget) - (category2.spent / category2.budget);
                    if (diff < 0.0d) {
                        return -1;
                    }
                    return diff > 0.0d ? 1 : 0;
                default:
                    return category1.getCategory().toUpperCase().compareTo(category2.getCategory().toUpperCase());
            }
        }
    };
    private Context context;
    public GregorianCalendar currentDate;
    public int currentPeriod;
    private List<CategoryClass> elements;
    private List<CategoryClass> expenseCategories;
    private List<CategoryClass> incomeCategories;
    private LayoutInflater inflater;
    private ListView listView;
    private List<CategoryClass> nonBudgetedCategories;
    String showExpense = Prefs.COLLAPSE_EXPENSES;
    String showIncome = Prefs.COLLAPSE_INCOME;
    String showNonBudgeted = Prefs.COLLAPSE_UNBUDGETED;

    public BudgetsRowAdapter(Context aContext, ListView theList) {
        this.context = aContext;
        this.listView = theList;
        this.inflater = LayoutInflater.from(this.context);
        this.currentDate = new GregorianCalendar();
        this.currentPeriod = Prefs.getIntPref(Prefs.DISPLAY_BUDGETPERIOD);
    }

    public int getCount() {
        int count = 3;
        if (this.incomeCategories != null && getShowSection(this.showIncome)) {
            count = 3 + this.incomeCategories.size();
        }
        if (this.expenseCategories != null && getShowSection(this.showExpense)) {
            count += this.expenseCategories.size();
        }
        if (Prefs.getBooleanPref(Prefs.BUDGETSHOWUNBUDGETED) && this.nonBudgetedCategories != null && getShowSection(this.showNonBudgeted)) {
            return count + this.nonBudgetedCategories.size();
        }
        if (Prefs.getBooleanPref(Prefs.BUDGETSHOWUNBUDGETED)) {
            return count;
        }
        return count - 1;
    }

    public Object getItem(int position) {
        int incomeSize = (this.incomeCategories == null || !getShowSection(this.showIncome)) ? 0 : this.incomeCategories.size();
        int expenseSize = (this.expenseCategories == null || !getShowSection(this.showExpense)) ? 0 : this.expenseCategories.size();
        int nonBudgetedSize = (this.nonBudgetedCategories == null || !getShowSection(this.showNonBudgeted)) ? 0 : this.nonBudgetedCategories.size();
        if (position == 0) {
            return Locales.kLOC_BUDGETS_INCOME;
        }
        if (position < incomeSize + 1) {
            return this.incomeCategories.get(position - 1);
        }
        if (position == incomeSize + 1) {
            return Locales.kLOC_BUDGETS_EXPENSES;
        }
        if (position < (incomeSize + expenseSize) + 2) {
            return this.expenseCategories.get((position - incomeSize) - 2);
        }
        if (position == (incomeSize + expenseSize) + 2) {
            return Locales.kLOC_BUDGETS_NONBUDGETED;
        }
        if (position < ((incomeSize + expenseSize) + nonBudgetedSize) + 3) {
            return this.nonBudgetedCategories.get(((position - incomeSize) - expenseSize) - 3);
        }
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Object cat = getItem(position);
        if (cat.getClass() == String.class) {
            BudgetsHeaderHolder header = new BudgetsHeaderHolder(this.context, (String) cat, balanceForCategory((String) cat));
            header.setOnClickListener(getHeaderClickListener());
            return header;
        }
        if (convertView == null || convertView.getClass() != BudgetsRowHolder.class) {
            convertView = new BudgetsRowHolder(this.context);
            convertView.setOnClickListener(getRowClickListener());
            ((Activity) this.context).registerForContextMenu(convertView);
        }
        if (position % 2 == 0) {
            convertView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        } else {
            convertView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        }
        ((BudgetsRowHolder) convertView).setCategory((CategoryClass) cat);
        return convertView;
    }

    private OnClickListener getRowClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(BudgetsRowAdapter.this.context, TransactionsActivity.class);
                BudgetsRowHolder holder = (BudgetsRowHolder) view;
                FilterClass aFilter = new FilterClass();
                aFilter.setCategory(new StringBuilder(String.valueOf(holder.category.getCategory())).append(holder.category.getIncludeSubcategories() ? "%" : "").toString());
                aFilter.setDate(Locales.kLOC_FILTER_DATES_CUSTOM);
                aFilter.setDateFrom(BudgetsRowAdapter.this.startOfPeriod());
                aFilter.setDateTo(BudgetsRowAdapter.this.endOfPeriod());
                i.putExtra("Filter", aFilter);
                BudgetsRowAdapter.this.context.startActivity(i);
            }
        };
    }

    private OnClickListener getHeaderClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                boolean z = false;
                String cat = ((BudgetsHeaderHolder) view).label;
                BudgetsRowAdapter budgetsRowAdapter;
                String str;
                if (cat.equals(Locales.kLOC_BUDGETS_INCOME)) {
                    budgetsRowAdapter = BudgetsRowAdapter.this;
                    str = BudgetsRowAdapter.this.showIncome;
                    if (!BudgetsRowAdapter.this.getShowSection(BudgetsRowAdapter.this.showIncome)) {
                        z = true;
                    }
                    budgetsRowAdapter.setShowSection(str, z);
                } else if (cat.equals(Locales.kLOC_BUDGETS_EXPENSES)) {
                    budgetsRowAdapter = BudgetsRowAdapter.this;
                    str = BudgetsRowAdapter.this.showExpense;
                    if (!BudgetsRowAdapter.this.getShowSection(BudgetsRowAdapter.this.showExpense)) {
                        z = true;
                    }
                    budgetsRowAdapter.setShowSection(str, z);
                } else if (cat.equals(Locales.kLOC_BUDGETS_NONBUDGETED)) {
                    budgetsRowAdapter = BudgetsRowAdapter.this;
                    str = BudgetsRowAdapter.this.showNonBudgeted;
                    if (!BudgetsRowAdapter.this.getShowSection(BudgetsRowAdapter.this.showNonBudgeted)) {
                        z = true;
                    }
                    budgetsRowAdapter.setShowSection(str, z);
                }
                BudgetsRowAdapter.this.notifyDataSetChanged();
            }
        };
    }

    public void reloadData() {
        List<CategoryClass> nonBudgetedCategoriesHolder;
        boolean hideZeroActuals = Prefs.getBooleanPref(Prefs.BUDGETHIDEZEROSACTUALS);
        GregorianCalendar startDate = startOfPeriod();
        GregorianCalendar endDate = endOfPeriod();
        List<CategoryClass> tempIncomeCategories = CategoryClass.queryIncomeCategoriesWithBudgets();
        ArrayList<CategoryClass> afterZeroIncomes = new ArrayList();
        for (CategoryClass category : tempIncomeCategories) {
            category.spent = CategoryClass.querySpentInCategory(category.getCategory(), category.getIncludeSubcategories(), startDate, endDate);
            if (category.spent != 0.0d || !hideZeroActuals) {
                afterZeroIncomes.add(category);
                category.budget = ((double) Math.round(100.0d * category.budgetLimit(startDate, endDate))) / 100.0d;
            }
        }
        List<CategoryClass> incomeCategoriesHolder = afterZeroIncomes;
        List<CategoryClass> tempExpenseCategories = CategoryClass.queryExpenseCategoriesWithBudgets();
        ArrayList<CategoryClass> afterZeroExpenses = new ArrayList();
        for (CategoryClass category2 : tempExpenseCategories) {
            category2.spent = CategoryClass.querySpentInCategory(category2.getCategory(), category2.getIncludeSubcategories(), startDate, endDate);
            if (category2.spent != 0.0d || !hideZeroActuals) {
                afterZeroExpenses.add(category2);
                category2.budget = ((double) Math.round(100.0d * category2.budgetLimit(startDate, endDate))) / 100.0d;
            }
        }
        List<CategoryClass> expenseCategoriesHolder = afterZeroExpenses;
        if (Prefs.getBooleanPref(Prefs.BUDGETSHOWUNBUDGETED)) {
            List<CategoryClass> tempNonBudgetdCategories = CategoryClass.queryNonBudgettedCategories();
            ArrayList<CategoryClass> afterZeroNonBudgeted = new ArrayList();
            for (CategoryClass category22 : tempNonBudgetdCategories) {
                category22.spent = CategoryClass.querySpentInCategory(category22.getCategory(), category22.getIncludeSubcategories(), startDate, endDate);
                if (category22.spent != 0.0d || !hideZeroActuals) {
                    afterZeroNonBudgeted.add(category22);
                }
            }
            nonBudgetedCategoriesHolder = afterZeroNonBudgeted;
        } else {
            nonBudgetedCategoriesHolder = null;
        }
        Collections.sort(incomeCategoriesHolder, categoryComparator);
        Collections.sort(expenseCategoriesHolder, categoryComparator);
        if (nonBudgetedCategoriesHolder != null) {
            Collections.sort(nonBudgetedCategoriesHolder, categoryComparator);
        }
        final List<CategoryClass> list = nonBudgetedCategoriesHolder;
        final List<CategoryClass> list2 = expenseCategoriesHolder;
        final List<CategoryClass> list3 = incomeCategoriesHolder;
        ((Activity) this.context).runOnUiThread(new Runnable() {
            public void run() {
                BudgetsRowAdapter.this.nonBudgetedCategories = list;
                BudgetsRowAdapter.this.expenseCategories = list2;
                BudgetsRowAdapter.this.incomeCategories = list3;
                BudgetsRowAdapter.this.notifyDataSetChanged();
            }
        });
    }

    private void setShowSection(String section, boolean show) {
        Prefs.setPref(section, show);
    }

    private boolean getShowSection(String section) {
        return Prefs.getBooleanPref(section);
    }

    public double totalIncomes() {
        double earned = 0.0d;
        if (this.incomeCategories != null) {
            for (CategoryClass category : this.incomeCategories) {
                earned += category.spent;
            }
        }
        return earned;
    }

    public double totalExpenses() {
        double spent = 0.0d;
        if (this.expenseCategories != null) {
            for (CategoryClass category : this.expenseCategories) {
                spent += category.spent;
            }
        }
        return spent;
    }

    public double totalNonBudgeted() {
        double spent = 0.0d;
        if (this.nonBudgetedCategories != null) {
            for (CategoryClass category : this.nonBudgetedCategories) {
                spent += category.spent;
            }
        }
        return spent;
    }

    public double budgetedIncomes() {
        double budget = 0.0d;
        if (this.incomeCategories != null) {
            for (CategoryClass category : this.incomeCategories) {
                budget += category.budget;
            }
        }
        return budget;
    }

    public double budgetedExpenses() {
        double budget = 0.0d;
        if (this.expenseCategories != null) {
            for (CategoryClass category : this.expenseCategories) {
                budget += category.budget;
            }
        }
        return budget;
    }

    private boolean showCents() {
        return Prefs.getBooleanPref(Prefs.BUDGETSHOWCENTS);
    }

    public String xOfy(double x, double y) {
        String xofy = Locales.kLOC_BUDGETS_XOFY;
        if (showCents()) {
            return xofy.replace("%1$s", CurrencyExt.amountAsCurrency(x)).replace("%2$s", CurrencyExt.amountAsCurrency(y));
        }
        return xofy.replace("%1$s", CurrencyExt.amountAsCurrencyWithoutCents(x)).replace("%2$s", CurrencyExt.amountAsCurrencyWithoutCents(y));
    }

    public String balanceForCategory(String cat) {
        String retVal = "";
        if (cat.equals(Locales.kLOC_BUDGETS_INCOME)) {
            return xOfy(totalIncomes(), budgetedIncomes());
        }
        if (cat.equals(Locales.kLOC_BUDGETS_EXPENSES)) {
            double totalExpense = totalExpenses();
            double budgetedExpense = Math.abs(budgetedExpenses());
            if (totalExpense != 0.0d) {
                totalExpense *= -1.0d;
            }
            return xOfy(totalExpense, budgetedExpense);
        }
        if (!cat.equals(Locales.kLOC_BUDGETS_NONBUDGETED)) {
            return retVal;
        }
        double totalUnbudgeted = totalNonBudgeted();
        double unbudgetedAvailable = budgetedIncomes() - Math.abs(budgetedExpenses());
        if (totalUnbudgeted != 0.0d) {
            totalUnbudgeted *= -1.0d;
        }
        return xOfy(totalUnbudgeted, unbudgetedAvailable);
    }

    public String rangeOfPeriodAsString() {
        switch (this.currentPeriod) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return CalExt.descriptionWithMediumDate(this.currentDate);
            case SplitsActivity.RESULT_CHANGED /*1*/:
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                return new StringBuilder(String.valueOf(CalExt.descriptionWithMediumDate(startOfPeriod()))).append(" - ").append(CalExt.descriptionWithMediumDate(endOfPeriod())).toString();
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return new StringBuilder(String.valueOf(CalExt.descriptionWithMonth(this.currentDate))).append(" ").append(CalExt.descriptionWithYear(this.currentDate)).toString();
                }
                return new StringBuilder(String.valueOf(CalExt.descriptionWithMediumDate(startOfPeriod()))).append(" - ").append(CalExt.descriptionWithMediumDate(endOfPeriod())).toString();
            case SplitsActivity.REQUEST_EDIT /*3*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return new StringBuilder(String.valueOf(CalExt.descriptionWithMonth(CalExt.beginningOfQuarter(this.currentDate)))).append(" ").append(CalExt.descriptionWithYear(CalExt.beginningOfQuarter(this.currentDate))).append(" - ").append(CalExt.descriptionWithMonth(CalExt.endOfQuarter(this.currentDate))).append(" ").append(CalExt.descriptionWithYear(CalExt.endOfQuarter(this.currentDate))).toString();
                }
                return new StringBuilder(String.valueOf(CalExt.descriptionWithMediumDate(startOfPeriod()))).append(" - ").append(CalExt.descriptionWithMediumDate(endOfPeriod())).toString();
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return CalExt.descriptionWithYear(this.currentDate);
                }
                return new StringBuilder(String.valueOf(CalExt.descriptionWithMediumDate(startOfPeriod()))).append(" - ").append(CalExt.descriptionWithMediumDate(endOfPeriod())).toString();
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return new StringBuilder(String.valueOf(CalExt.descriptionWithMonth(startOfPeriod()))).append(" ").append(CalExt.descriptionWithYear(startOfPeriod())).append(" - ").append(CalExt.descriptionWithMonth(endOfPeriod())).append(" ").append(CalExt.descriptionWithYear(endOfPeriod())).toString();
                }
                return new StringBuilder(String.valueOf(CalExt.descriptionWithMediumDate(startOfPeriod()))).append(" - ").append(CalExt.descriptionWithMediumDate(endOfPeriod())).toString();
            case LookupsListActivity.ID_LOOKUP /*7*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return new StringBuilder(String.valueOf(CalExt.descriptionWithMonth(startOfPeriod()))).append(" ").append(CalExt.descriptionWithYear(startOfPeriod())).append(" - ").append(CalExt.descriptionWithMonth(endOfPeriod())).append(" ").append(CalExt.descriptionWithYear(endOfPeriod())).toString();
                }
                return new StringBuilder(String.valueOf(CalExt.descriptionWithMediumDate(startOfPeriod()))).append(" - ").append(CalExt.descriptionWithMediumDate(endOfPeriod())).toString();
            default:
                return "All";
        }
    }

    public GregorianCalendar startOfPeriod() {
        return startOfPeriod(this.currentDate, this.currentPeriod);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.util.GregorianCalendar startOfPeriod(java.util.GregorianCalendar r18, int r19) {
        /*
        r15 = "budgetStartDate";
        r5 = com.catamount.pocketmoney.misc.Prefs.getStringPref(r15);
        r4 = com.catamount.pocketmoney.misc.CalExt.dateFromDescriptionWithMediumDate(r5);
        r15 = com.catamount.pocketmoney.misc.Locales.kLOC_GENERAL_DEFAULT;
        r15 = r5.equalsIgnoreCase(r15);
        if (r15 == 0) goto L_0x004a;
    L_0x0012:
        r4 = new java.util.GregorianCalendar;
        r4.<init>();
        r15 = 1;
        r16 = 1989; // 0x7c5 float:2.787E-42 double:9.827E-321;
        r0 = r16;
        r4.set(r15, r0);
        r15 = 2;
        r16 = 0;
        r0 = r16;
        r4.set(r15, r0);
        r15 = 5;
        r16 = 1;
        r0 = r16;
        r4.set(r15, r0);
        r15 = 11;
        r16 = 0;
        r0 = r16;
        r4.set(r15, r0);
        r15 = 12;
        r16 = 0;
        r0 = r16;
        r4.set(r15, r0);
        r15 = 13;
        r16 = 0;
        r0 = r16;
        r4.set(r15, r0);
    L_0x004a:
        r10 = 0;
        r11 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r4);
        r15 = 5;
        r15 = r4.get(r15);
        r16 = 5;
        r0 = r16;
        r16 = r11.get(r0);
        r0 = r16;
        if (r15 != r0) goto L_0x006c;
    L_0x0060:
        r10 = 1;
    L_0x0061:
        r13 = r18.clone();
        r13 = (java.util.GregorianCalendar) r13;
        switch(r19) {
            case 0: goto L_0x006e;
            case 1: goto L_0x0073;
            case 2: goto L_0x0111;
            case 3: goto L_0x0181;
            case 4: goto L_0x01d7;
            case 5: goto L_0x00a7;
            case 6: goto L_0x0156;
            case 7: goto L_0x01ac;
            case 8: goto L_0x00dd;
            default: goto L_0x006a;
        };
    L_0x006a:
        r15 = 0;
    L_0x006b:
        return r15;
    L_0x006c:
        r10 = 0;
        goto L_0x0061;
    L_0x006e:
        r15 = com.catamount.pocketmoney.misc.CalExt.beginningOfDay(r18);
        goto L_0x006b;
    L_0x0073:
        r15 = 7;
        r2 = r4.get(r15);
        r15 = 7;
        r0 = r18;
        r7 = r0.get(r15);
        r15 = 7;
        r0 = r18;
        r15 = r0.get(r15);
        r16 = 7;
        r0 = r16;
        r16 = r4.get(r0);
        r0 = r16;
        if (r15 >= r0) goto L_0x009e;
    L_0x0092:
        r15 = r2 - r7;
        r15 = 7 - r15;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, r15);
    L_0x009c:
        r15 = r13;
        goto L_0x006b;
    L_0x009e:
        r15 = r7 - r2;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, r15);
        goto L_0x009c;
    L_0x00a7:
        r0 = r18;
        r14 = com.catamount.pocketmoney.misc.CalExt.weeksBetween(r4, r0);
        r0 = r18;
        r15 = com.catamount.pocketmoney.misc.CalExt.daysBetween(r4, r0);
        r9 = r15 % 7;
        r15 = r14 % 2;
        if (r15 != 0) goto L_0x00cc;
    L_0x00b9:
        r12 = 1;
    L_0x00ba:
        if (r14 < 0) goto L_0x00be;
    L_0x00bc:
        if (r9 >= 0) goto L_0x00d0;
    L_0x00be:
        if (r12 == 0) goto L_0x00ce;
    L_0x00c0:
        r15 = 7;
    L_0x00c1:
        r15 = r15 + 6;
        r15 = r15 + r9;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, r15);
    L_0x00ca:
        r15 = r13;
        goto L_0x006b;
    L_0x00cc:
        r12 = 0;
        goto L_0x00ba;
    L_0x00ce:
        r15 = 0;
        goto L_0x00c1;
    L_0x00d0:
        if (r12 == 0) goto L_0x00db;
    L_0x00d2:
        r15 = 0;
    L_0x00d3:
        r15 = r15 + r9;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, r15);
        goto L_0x00ca;
    L_0x00db:
        r15 = 7;
        goto L_0x00d3;
    L_0x00dd:
        r0 = r18;
        r14 = com.catamount.pocketmoney.misc.CalExt.weeksBetween(r4, r0);
        r0 = r18;
        r15 = com.catamount.pocketmoney.misc.CalExt.daysBetween(r4, r0);
        r9 = r15 % 7;
        if (r14 < 0) goto L_0x00ef;
    L_0x00ed:
        if (r9 >= 0) goto L_0x0105;
    L_0x00ef:
        r15 = r14 % 4;
        r15 = java.lang.Math.abs(r15);
        r15 = 3 - r15;
        r15 = r15 * 7;
        r15 = r15 + 6;
        r15 = r15 + r9;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, r15);
    L_0x0102:
        r15 = r13;
        goto L_0x006b;
    L_0x0105:
        r15 = r14 * 7;
        r15 = r15 % 4;
        r15 = r15 + r9;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, r15);
        goto L_0x0102;
    L_0x0111:
        r0 = r18;
        r9 = com.catamount.pocketmoney.misc.CalExt.daysBetween(r4, r0);
        r15 = 5;
        r0 = r18;
        r15 = r0.get(r15);
        r16 = 5;
        r0 = r16;
        r16 = r4.get(r0);
        r0 = r16;
        if (r15 >= r0) goto L_0x012e;
    L_0x012a:
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonth(r13);
    L_0x012e:
        if (r10 == 0) goto L_0x0137;
    L_0x0130:
        r13 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r15 = r13;
        goto L_0x006b;
    L_0x0137:
        r15 = 5;
        r16 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r17 = 5;
        r16 = r16.get(r17);
        r17 = 5;
        r0 = r17;
        r17 = r4.get(r0);
        r16 = java.lang.Math.min(r16, r17);
        r0 = r16;
        r13.set(r15, r0);
        r15 = r13;
        goto L_0x006b;
    L_0x0156:
        r15 = 2;
        r3 = r4.get(r15);
        r15 = 5;
        r1 = r4.get(r15);
        r15 = 2;
        r0 = r18;
        r8 = r0.get(r15);
        r15 = 5;
        r6 = r4.get(r15);
        r15 = r8 - r3;
        r15 = r15 % 2;
        if (r15 != 0) goto L_0x0216;
    L_0x0172:
        if (r6 >= r1) goto L_0x0216;
    L_0x0174:
        r15 = 2;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
    L_0x017b:
        if (r10 == 0) goto L_0x023a;
    L_0x017d:
        r13 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
    L_0x0181:
        r15 = 2;
        r3 = r4.get(r15);
        r15 = 5;
        r1 = r4.get(r15);
        r15 = 2;
        r0 = r18;
        r8 = r0.get(r15);
        r15 = 5;
        r6 = r4.get(r15);
        r15 = r8 - r3;
        r15 = r15 % 3;
        if (r15 != 0) goto L_0x0259;
    L_0x019d:
        if (r6 >= r1) goto L_0x0259;
    L_0x019f:
        r15 = 3;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
    L_0x01a6:
        if (r10 == 0) goto L_0x027d;
    L_0x01a8:
        r13 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
    L_0x01ac:
        r15 = 2;
        r3 = r4.get(r15);
        r15 = 5;
        r1 = r4.get(r15);
        r15 = 2;
        r0 = r18;
        r8 = r0.get(r15);
        r15 = 5;
        r6 = r4.get(r15);
        r15 = r8 - r3;
        r15 = r15 % 6;
        if (r15 != 0) goto L_0x029c;
    L_0x01c8:
        if (r6 >= r1) goto L_0x029c;
    L_0x01ca:
        r15 = 6;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
    L_0x01d1:
        if (r10 == 0) goto L_0x02c0;
    L_0x01d3:
        r13 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
    L_0x01d7:
        r15 = 2;
        r3 = r4.get(r15);
        r15 = 5;
        r1 = r4.get(r15);
        r15 = 2;
        r0 = r18;
        r8 = r0.get(r15);
        r15 = 5;
        r6 = r4.get(r15);
        if (r8 < r3) goto L_0x01f3;
    L_0x01ef:
        if (r8 != r3) goto L_0x01f7;
    L_0x01f1:
        if (r6 >= r1) goto L_0x01f7;
    L_0x01f3:
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractYear(r18);
    L_0x01f7:
        r15 = 5;
        r16 = 5;
        r0 = r16;
        r16 = r4.get(r0);
        r0 = r16;
        r13.set(r15, r0);
        r15 = 2;
        r16 = 2;
        r0 = r16;
        r16 = r4.get(r0);
        r0 = r16;
        r13.set(r15, r0);
        r15 = r13;
        goto L_0x006b;
    L_0x0216:
        if (r8 >= r3) goto L_0x022a;
    L_0x0218:
        r15 = r8 - r3;
        r15 = r15 % 2;
        r15 = java.lang.Math.abs(r15);
        r15 = 2 - r15;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
        goto L_0x017b;
    L_0x022a:
        r15 = r8 - r3;
        r15 = r15 % 2;
        r15 = java.lang.Math.abs(r15);
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
        goto L_0x017b;
    L_0x023a:
        r15 = 5;
        r16 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r17 = 5;
        r16 = r16.get(r17);
        r17 = 5;
        r0 = r17;
        r17 = r4.get(r0);
        r16 = java.lang.Math.min(r16, r17);
        r0 = r16;
        r13.set(r15, r0);
        r15 = r13;
        goto L_0x006b;
    L_0x0259:
        if (r8 >= r3) goto L_0x026d;
    L_0x025b:
        r15 = r8 - r3;
        r15 = r15 % 3;
        r15 = java.lang.Math.abs(r15);
        r15 = 3 - r15;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
        goto L_0x01a6;
    L_0x026d:
        r15 = r8 - r3;
        r15 = r15 % 3;
        r15 = java.lang.Math.abs(r15);
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
        goto L_0x01a6;
    L_0x027d:
        r15 = 5;
        r16 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r17 = 5;
        r16 = r16.get(r17);
        r17 = 5;
        r0 = r17;
        r17 = r4.get(r0);
        r16 = java.lang.Math.min(r16, r17);
        r0 = r16;
        r13.set(r15, r0);
        r15 = r13;
        goto L_0x006b;
    L_0x029c:
        if (r8 >= r3) goto L_0x02b0;
    L_0x029e:
        r15 = r8 - r3;
        r15 = r15 % 6;
        r15 = java.lang.Math.abs(r15);
        r15 = 6 - r15;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
        goto L_0x01d1;
    L_0x02b0:
        r15 = r8 - r3;
        r15 = r15 % 6;
        r15 = java.lang.Math.abs(r15);
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, r15);
        goto L_0x01d1;
    L_0x02c0:
        r15 = 5;
        r16 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r17 = 5;
        r16 = r16.get(r17);
        r17 = 5;
        r0 = r17;
        r17 = r4.get(r0);
        r16 = java.lang.Math.min(r16, r17);
        r0 = r16;
        r13.set(r15, r0);
        r15 = r13;
        goto L_0x006b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.catamount.pocketmoney.views.budgets.BudgetsRowAdapter.startOfPeriod(java.util.GregorianCalendar, int):java.util.GregorianCalendar");
    }

    private GregorianCalendar endOfPeriod() {
        boolean firstOfMonth;
        String budgetStartDateString = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
        GregorianCalendar budgetStartDate = CalExt.dateFromDescriptionWithMediumDate(budgetStartDateString);
        boolean endOfMonth = false;
        if (budgetStartDateString.equalsIgnoreCase(Locales.kLOC_GENERAL_DEFAULT)) {
            budgetStartDate = null;
        } else
            endOfMonth = budgetStartDate.get(Calendar.DAY_OF_MONTH) == CalExt.endOfMonth(budgetStartDate).get(Calendar.DAY_OF_MONTH);
        firstOfMonth = budgetStartDate == null || budgetStartDate.get(Calendar.DAY_OF_MONTH) == 1;
        GregorianCalendar budgetEndDate;
        switch (this.currentPeriod) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return CalExt.endOfDay(this.currentDate);
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 1));
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                if (firstOfMonth) {
                    budgetEndDate = CalExt.endOfMonth(this.currentDate);
                } else {
                    budgetEndDate = CalExt.addMonth(CalExt.subtractDay(startOfPeriod()));
                    if (endOfMonth) {
                        budgetEndDate = CalExt.endOfMonth(CalExt.endOfMonth(budgetEndDate));
                    }
                }
                return budgetEndDate;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                if (firstOfMonth) {
                    budgetEndDate = CalExt.endOfMonth(CalExt.addMonths(startOfPeriod(), 2));
                } else {
                    budgetEndDate = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 3);
                    if (endOfMonth) {
                        budgetEndDate = CalExt.subtractDay(CalExt.endOfMonth(budgetEndDate));
                    }
                }
                return budgetEndDate;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                return CalExt.endOfDay(CalExt.addYear(CalExt.subtractDay(startOfPeriod())));
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                return CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 2));
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                if (firstOfMonth) {
                    budgetEndDate = CalExt.endOfMonth(CalExt.addMonth(startOfPeriod()));
                } else {
                    budgetEndDate = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 2);
                    if (endOfMonth) {
                        budgetEndDate = CalExt.subtractDay(CalExt.endOfMonth(budgetEndDate));
                    }
                }
                return budgetEndDate;
            case LookupsListActivity.ID_LOOKUP /*7*/:
                if (firstOfMonth) {
                    budgetEndDate = CalExt.endOfMonth(CalExt.addMonths(startOfPeriod(), 5));
                } else {
                    budgetEndDate = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 6);
                    if (endOfMonth) {
                        budgetEndDate = CalExt.subtractDay(CalExt.endOfMonth(budgetEndDate));
                    }
                }
                return budgetEndDate;
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                return CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 2));
            default:
                return null;
        }
    }

    public void nextPeriod() {
        switch (this.currentPeriod) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                this.currentDate = CalExt.addDays(this.currentDate, 1);
                return;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, 1);
                return;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 1);
                return;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 3);
                return;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                this.currentDate = CalExt.addYear(this.currentDate);
                return;
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, 2);
                return;
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 2);
                return;
            case LookupsListActivity.ID_LOOKUP /*7*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 6);
                return;
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, 4);
                return;
            default:
                return;
        }
    }

    public void previousPeriod() {
        switch (this.currentPeriod) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                this.currentDate = CalExt.addDays(this.currentDate, -1);
                return;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, -1);
                return;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                this.currentDate = CalExt.addMonths(this.currentDate, -1);
                return;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                this.currentDate = CalExt.addMonths(this.currentDate, -3);
                return;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                this.currentDate = CalExt.subtractYear(this.currentDate);
                return;
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, -2);
                return;
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                this.currentDate = CalExt.addMonths(this.currentDate, -2);
                return;
            case LookupsListActivity.ID_LOOKUP /*7*/:
                this.currentDate = CalExt.addMonths(this.currentDate, -6);
                return;
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, -4);
                return;
            default:
                return;
        }
    }

    public double getProgressPercent() {
        GregorianCalendar today = new GregorianCalendar();
        switch (this.currentPeriod) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                if (this.currentDate.after(today)) {
                    return 0.0d;
                }
                return 1.0d;
            default:
                double percentIntoPeriod = ((double) (today.getTimeInMillis() - startOfPeriod().getTimeInMillis())) / ((double) (endOfPeriod().getTimeInMillis() - startOfPeriod().getTimeInMillis()));
                if (startOfPeriod().after(today)) {
                    return 1.0d;
                }
                if (endOfPeriod().before(today)) {
                    return 0.0d;
                }
                return percentIntoPeriod;
        }
    }
}
