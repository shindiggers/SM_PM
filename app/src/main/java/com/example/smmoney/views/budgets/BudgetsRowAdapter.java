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
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.transactions.TransactionsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

public class BudgetsRowAdapter extends BaseAdapter {
    private static final Comparator<CategoryClass> categoryComparator = new Comparator<CategoryClass>() {
        public int compare(CategoryClass category1, CategoryClass category2) {
            double diff;
            switch (Prefs.getIntPref(Prefs.BUDGETS_SORTON)) {
                case Enums.kBudgetsSortTypeActual /*1*/:
                    diff = category2.spent - category1.spent;
                    if (diff < 0.0d) {
                        return -1;
                    }
                    return diff > 0.0d ? 1 : 0;
                case Enums.kBudgetsSortTypeBudgeted /*2*/:
                    diff = category2.budget - category1.budget;
                    if (diff < 0.0d) {
                        return -1;
                    }
                    return diff > 0.0d ? 1 : 0;
                case Enums.kBudgetsSortTypePercentage /*3*/:
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
    GregorianCalendar currentDate;
    int currentPeriod;
    private List<CategoryClass> elements;
    private List<CategoryClass> expenseCategories;
    private List<CategoryClass> incomeCategories;
    private LayoutInflater inflater;
    private ListView listView;
    private List<CategoryClass> nonBudgetedCategories;
    private String showExpense = Prefs.COLLAPSE_EXPENSES;
    private String showIncome = Prefs.COLLAPSE_INCOME;
    private String showNonBudgeted = Prefs.COLLAPSE_UNBUDGETED;

    BudgetsRowAdapter(Context aContext, ListView theList) {
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
                aFilter.setCategory(holder.category.getCategory() + (holder.category.getIncludeSubcategories() ? "%" : ""));
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
        ArrayList<CategoryClass> afterZeroIncomes = new ArrayList<>();
        for (CategoryClass category : tempIncomeCategories) {
            category.spent = CategoryClass.querySpentInCategory(category.getCategory(), category.getIncludeSubcategories(), startDate, endDate);
            if (category.spent != 0.0d || !hideZeroActuals) {
                afterZeroIncomes.add(category);
                category.budget = ((double) Math.round(100.0d * category.budgetLimit(startDate, endDate))) / 100.0d;
            }
        }
        List<CategoryClass> incomeCategoriesHolder = afterZeroIncomes;
        List<CategoryClass> tempExpenseCategories = CategoryClass.queryExpenseCategoriesWithBudgets();
        ArrayList<CategoryClass> afterZeroExpenses = new ArrayList<>();
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
            ArrayList<CategoryClass> afterZeroNonBudgeted = new ArrayList<>();
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

    double totalIncomes() {
        double earned = 0.0d;
        if (this.incomeCategories != null) {
            for (CategoryClass category : this.incomeCategories) {
                earned += category.spent;
            }
        }
        return earned;
    }

    double totalExpenses() {
        double spent = 0.0d;
        if (this.expenseCategories != null) {
            for (CategoryClass category : this.expenseCategories) {
                spent += category.spent;
            }
        }
        return spent;
    }

    double totalNonBudgeted() {
        double spent = 0.0d;
        if (this.nonBudgetedCategories != null) {
            for (CategoryClass category : this.nonBudgetedCategories) {
                spent += category.spent;
            }
        }
        return spent;
    }

    double budgetedIncomes() {
        double budget = 0.0d;
        if (this.incomeCategories != null) {
            for (CategoryClass category : this.incomeCategories) {
                budget += category.budget;
            }
        }
        return budget;
    }

    double budgetedExpenses() {
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

    private String xOfy(double x, double y) {
        String xofy = Locales.kLOC_BUDGETS_XOFY;
        if (showCents()) {
            return xofy.replace("%1$s", CurrencyExt.amountAsCurrency(x)).replace("%2$s", CurrencyExt.amountAsCurrency(y));
        }
        return xofy.replace("%1$s", CurrencyExt.amountAsCurrencyWithoutCents(x)).replace("%2$s", CurrencyExt.amountAsCurrencyWithoutCents(y));
    }

    private String balanceForCategory(String cat) {
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

    String rangeOfPeriodAsString() {
        switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                return CalExt.descriptionWithMediumDate(this.currentDate);
            case Enums.kBudgetPeriodWeek /*1*/:
            case Enums.kBudgetPeriodBiweekly /*5*/:
            case Enums.kBudgetPeriod4Weeks /*8*/:
                return CalExt.descriptionWithMediumDate(startOfPeriod()) + " - " + CalExt.descriptionWithMediumDate(endOfPeriod());
            case Enums.kBudgetPeriodMonth /*2*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return CalExt.descriptionWithMonth(this.currentDate) + " " + CalExt.descriptionWithYear(this.currentDate);
                }
                return CalExt.descriptionWithMediumDate(startOfPeriod()) + " - " + CalExt.descriptionWithMediumDate(endOfPeriod());
            case Enums.kBudgetPeriodQuarter /*3*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return CalExt.descriptionWithMonth(CalExt.beginningOfQuarter(this.currentDate)) + " " + CalExt.descriptionWithYear(CalExt.beginningOfQuarter(this.currentDate)) + " - " + CalExt.descriptionWithMonth(CalExt.endOfQuarter(this.currentDate)) + " " + CalExt.descriptionWithYear(CalExt.endOfQuarter(this.currentDate));
                }
                return CalExt.descriptionWithMediumDate(startOfPeriod()) + " - " + CalExt.descriptionWithMediumDate(endOfPeriod());
            case Enums.kBudgetPeriodYear /*4*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return CalExt.descriptionWithYear(this.currentDate);
                }
                return CalExt.descriptionWithMediumDate(startOfPeriod()) + " - " + CalExt.descriptionWithMediumDate(endOfPeriod());
            case Enums.kBudgetPeriodBimonthly /*6*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return CalExt.descriptionWithMonth(startOfPeriod()) + " " + CalExt.descriptionWithYear(startOfPeriod()) + " - " + CalExt.descriptionWithMonth(endOfPeriod()) + " " + CalExt.descriptionWithYear(endOfPeriod());
                }
                return CalExt.descriptionWithMediumDate(startOfPeriod()) + " - " + CalExt.descriptionWithMediumDate(endOfPeriod());
            case Enums.kBudgetPeriodHalfYear /*7*/:
                if (Prefs.getStringPref(Prefs.BUDGETSTARTDATE).equals(Locales.kLOC_GENERAL_DEFAULT)) {
                    return CalExt.descriptionWithMonth(startOfPeriod()) + " " + CalExt.descriptionWithYear(startOfPeriod()) + " - " + CalExt.descriptionWithMonth(endOfPeriod()) + " " + CalExt.descriptionWithYear(endOfPeriod());
                }
                return CalExt.descriptionWithMediumDate(startOfPeriod()) + " - " + CalExt.descriptionWithMediumDate(endOfPeriod());
            default:
                return "All";
        }
    }

    private GregorianCalendar startOfPeriod() {
        return startOfPeriod(this.currentDate, this.currentPeriod);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    //public static java.util.GregorianCalendar startOfPeriod(java.util.GregorianCalendar r18, int r19) {
        /*
        Prefs.BUDGETSTARTDATE r15 = "budgetStartDate";
        String r5 = com.catamount.pocketmoney.misc.Prefs.getStringPref(Prefs.BUDGETSTARTDATE r15);
        GregorianCalendar budgetStartDate r4 = com.catamount.pocketmoney.misc.CalExt.dateFromDescriptionWithMediumDate(r5);
        Prefs.BUDGETSTARTDATE r15 = com.catamount.pocketmoney.misc.Locales.kLOC_GENERAL_DEFAULT;
        Prefs.BUDGETSTARTDATE r15 = String r5.equalsIgnoreCase(Prefs.BUDGETSTARTDATE r15);
        if (Prefs.BUDGETSTARTDATE r15 == 0) goto L_0x004a;
    L_0x0012:
        GregorianCalendar budgetStartDate r4 = new java.util.GregorianCalendar;
        GregorianCalendar budgetStartDate r4.<init>();
        Prefs.BUDGETSTARTDATE r15 = 1;
        r16 = 1989; // 0x7c5 float:2.787E-42 double:9.827E-321;
        r0 = r16;
        GregorianCalendar budgetStartDate r4.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = 2;
        r16 = 0;
        r0 = r16;
        GregorianCalendar budgetStartDate r4.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r16 = 1;
        r0 = r16;
        GregorianCalendar budgetStartDate r4.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = 11;
        r16 = 0;
        r0 = r16;
        GregorianCalendar budgetStartDate r4.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = 12;
        r16 = 0;
        r0 = r16;
        GregorianCalendar budgetStartDate r4.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = 13;
        r16 = 0;
        r0 = r16;
        GregorianCalendar budgetStartDate r4.set(Prefs.BUDGETSTARTDATE r15, r0);
    L_0x004a:
        r10 = 0;
        r11 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(GregorianCalendar budgetStartDate r4);
        Prefs.BUDGETSTARTDATE r15 = 5;
        Prefs.BUDGETSTARTDATE r15 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        r16 = 5; // MONTH
        r0 = r16;
        r16 = r11.get(r0);
        r0 = r16;
        if (Prefs.BUDGETSTARTDATE r15 != r0) goto L_0x006c;
    L_0x0060:
        r10 = 1;
    L_0x0061:
        r13 = r18.clone();
        r13 = (java.util.GregorianCalendar) r13;
        switch(r19) {
            case 0: goto L_0x006e;  Enums.kBudgetPeriodDay
            case 1: goto L_0x0073;  Enums.kBudgetPeriodWeek
            case 2: goto L_0x0111;  Enums.kBudgetPeriodMonth
            case 3: goto L_0x0181;  Enums.kBudgetPeriodQuarter
            case 4: goto L_0x01d7;
            case 5: goto L_0x00a7;
            case 6: goto L_0x0156;
            case 7: goto L_0x01ac;
            case 8: goto L_0x00dd;
            default: goto L_0x006a;
        };
    L_0x006a:
        Prefs.BUDGETSTARTDATE r15 = 0;
    L_0x006b:
        return Prefs.BUDGETSTARTDATE r15;
    L_0x006c:
        r10 = 0;
        goto L_0x0061;
    L_0x006e: // todo CASE 0  Enums.kBudgetPeriodDay
        Prefs.BUDGETSTARTDATE r15 = com.catamount.pocketmoney.misc.CalExt.beginningOfDay(r18);
        goto L_0x006b;
    L_0x0073: // todo CASE 1  Enums.kBudgetPeriodWeek
        Prefs.BUDGETSTARTDATE r15 = 7;
        r2 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 7;
        r0 = r18;
        r7 = r0.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 7;
        r0 = r18;
        Prefs.BUDGETSTARTDATE r15 = r0.get(Prefs.BUDGETSTARTDATE r15);
        r16 = 7;
        r0 = r16;
        r16 = GregorianCalendar budgetStartDate r4.get(r0);
        r0 = r16;
        if (Prefs.BUDGETSTARTDATE r15 >= r0) goto L_0x009e;
    L_0x0092:
        Prefs.BUDGETSTARTDATE r15 = r2 - r7;
        Prefs.BUDGETSTARTDATE r15 = 7 - Prefs.BUDGETSTARTDATE r15;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, Prefs.BUDGETSTARTDATE r15);
    L_0x009c:
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
    L_0x009e:
        Prefs.BUDGETSTARTDATE r15 = r7 - r2;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x009c;
    L_0x00a7: // todo CASE 5 Enums.kBudgetPeriodBiweekly
        r0 = r18;
        r14 = com.catamount.pocketmoney.misc.CalExt.weeksBetween(GregorianCalendar budgetStartDate r4, r0);
        r0 = r18;
        Prefs.BUDGETSTARTDATE r15 = com.catamount.pocketmoney.misc.CalExt.daysBetween(GregorianCalendar budgetStartDate r4, r0);
        r9 = Prefs.BUDGETSTARTDATE r15 % 7;
        Prefs.BUDGETSTARTDATE r15 = r14 % 2;
        if (Prefs.BUDGETSTARTDATE r15 != 0) goto L_0x00cc;
    L_0x00b9:
        r12 = 1;
    L_0x00ba:
        if (r14 < 0) goto L_0x00be;
    L_0x00bc:
        if (r9 >= 0) goto L_0x00d0;
    L_0x00be:
        if (r12 == 0) goto L_0x00ce;
    L_0x00c0:
        Prefs.BUDGETSTARTDATE r15 = 7;
    L_0x00c1:
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 + 6;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 + r9;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, Prefs.BUDGETSTARTDATE r15);
    L_0x00ca:
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
    L_0x00cc:
        r12 = 0;
        goto L_0x00ba;
    L_0x00ce:
        Prefs.BUDGETSTARTDATE r15 = 0;
        goto L_0x00c1;
    L_0x00d0:
        if (r12 == 0) goto L_0x00db;
    L_0x00d2:
        Prefs.BUDGETSTARTDATE r15 = 0;
    L_0x00d3:
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 + r9;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x00ca;
    L_0x00db:
        Prefs.BUDGETSTARTDATE r15 = 7;
        goto L_0x00d3;
    L_0x00dd: // todo CASE 8 Enums.kBudgetPeriod4Weeks
        r0 = r18;
        r14 = com.catamount.pocketmoney.misc.CalExt.weeksBetween(GregorianCalendar budgetStartDate r4, r0);
        r0 = r18;
        Prefs.BUDGETSTARTDATE r15 = com.catamount.pocketmoney.misc.CalExt.daysBetween(GregorianCalendar budgetStartDate r4, r0);
        r9 = Prefs.BUDGETSTARTDATE r15 % 7;
        if (r14 < 0) goto L_0x00ef;
    L_0x00ed:
        if (r9 >= 0) goto L_0x0105;
    L_0x00ef:
        Prefs.BUDGETSTARTDATE r15 = r14 % 4;
        Prefs.BUDGETSTARTDATE r15 = java.lang.Math.abs(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 3 - Prefs.BUDGETSTARTDATE r15;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 * 7;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 + 6;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 + r9;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, Prefs.BUDGETSTARTDATE r15);
    L_0x0102:
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
    L_0x0105:
        Prefs.BUDGETSTARTDATE r15 = r14 * 7;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 4;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 + r9;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractDays(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x0102;
    L_0x0111: // todo CASE 2  Enums.kBudgetPeriodMonth
        r0 = r18;
        r9 = com.catamount.pocketmoney.misc.CalExt.daysBetween(GregorianCalendar budgetStartDate r4, r0);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r0 = r18;
        Prefs.BUDGETSTARTDATE r15 = r0.get(Prefs.BUDGETSTARTDATE r15);
        r16 = 5;
        r0 = r16;
        r16 = GregorianCalendar budgetStartDate r4.get(r0);
        r0 = r16;
        if (Prefs.BUDGETSTARTDATE r15 >= r0) goto L_0x012e;
    L_0x012a:
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonth(r13);
    L_0x012e:
        if (r10 == 0) goto L_0x0137;
    L_0x0130:
        r13 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
    L_0x0137:
        Prefs.BUDGETSTARTDATE r15 = 5;
        r16 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r17 = 5;
        r16 = r16.get(r17);
        r17 = 5;
        r0 = r17;
        r17 = GregorianCalendar budgetStartDate r4.get(r0);
        r16 = java.lang.Math.min(r16, r17);
        r0 = r16;
        r13.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
    L_0x0156: // todo CASE 6 Enums.kBudgetPeriodBimonthly
        Prefs.BUDGETSTARTDATE r15 = 2;
        r3 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r1 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 2;
        r0 = r18;
        r8 = r0.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r6 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 2;
        if (Prefs.BUDGETSTARTDATE r15 != 0) goto L_0x0216;
    L_0x0172:
        if (r6 >= r1) goto L_0x0216;
    L_0x0174:
        Prefs.BUDGETSTARTDATE r15 = 2;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
    L_0x017b:
        if (r10 == 0) goto L_0x023a;
    L_0x017d:
        r13 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
    L_0x0181: // todo CASE 3  Enums.kBudgetPeriodQuarter
        Prefs.BUDGETSTARTDATE r15 = 2; MONTH OF YEAR
        r3 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 5; DAY OF MONTH
        r1 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 2;
        r0 = r18;
        r8 = r0.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r6 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 3;
        if (Prefs.BUDGETSTARTDATE r15 != 0) goto L_0x0259;
    L_0x019d:
        if (r6 >= r1) goto L_0x0259;
    L_0x019f:
        Prefs.BUDGETSTARTDATE r15 = 3;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
    L_0x01a6:
        if (r10 == 0) goto L_0x027d;
    L_0x01a8:
        r13 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
    L_0x01ac: // todo CASE 7 Enums.kBudgetPeriodHalfYear
        Prefs.BUDGETSTARTDATE r15 = 2;
        r3 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r1 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 2;
        r0 = r18;
        r8 = r0.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r6 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 6;
        if (Prefs.BUDGETSTARTDATE r15 != 0) goto L_0x029c;
    L_0x01c8:
        if (r6 >= r1) goto L_0x029c;
    L_0x01ca:
        Prefs.BUDGETSTARTDATE r15 = 6;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
    L_0x01d1:
        if (r10 == 0) goto L_0x02c0;
    L_0x01d3:
        r13 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
    L_0x01d7: // todo CASE 4 Enums.kBudgetPeriodYear
        Prefs.BUDGETSTARTDATE r15 = 2;
        r3 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r1 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 2;
        r0 = r18;
        r8 = r0.get(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 5;
        r6 = GregorianCalendar budgetStartDate r4.get(Prefs.BUDGETSTARTDATE r15);
        if (r8 < r3) goto L_0x01f3;
    L_0x01ef:
        if (r8 != r3) goto L_0x01f7;
    L_0x01f1:
        if (r6 >= r1) goto L_0x01f7;
    L_0x01f3:
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractYear(r18);
    L_0x01f7:
        Prefs.BUDGETSTARTDATE r15 = 5;
        r16 = 5;
        r0 = r16;
        r16 = GregorianCalendar budgetStartDate r4.get(r0);
        r0 = r16;
        r13.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = 2;
        r16 = 2;
        r0 = r16;
        r16 = GregorianCalendar budgetStartDate r4.get(r0);
        r0 = r16;
        r13.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
    L_0x0216:
        if (r8 >= r3) goto L_0x022a;
    L_0x0218:
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 2;
        Prefs.BUDGETSTARTDATE r15 = java.lang.Math.abs(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 2 - Prefs.BUDGETSTARTDATE r15;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x017b;
    L_0x022a:
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 2;
        Prefs.BUDGETSTARTDATE r15 = java.lang.Math.abs(Prefs.BUDGETSTARTDATE r15);
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x017b;
    L_0x023a:
        Prefs.BUDGETSTARTDATE r15 = 5;
        r16 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r17 = 5;
        r16 = r16.get(r17);
        r17 = 5;
        r0 = r17;
        r17 = GregorianCalendar budgetStartDate r4.get(r0);
        r16 = java.lang.Math.min(r16, r17);
        r0 = r16;
        r13.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
    L_0x0259:
        if (r8 >= r3) goto L_0x026d;
    L_0x025b:
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 3;
        Prefs.BUDGETSTARTDATE r15 = java.lang.Math.abs(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 3 - Prefs.BUDGETSTARTDATE r15;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x01a6;
    L_0x026d:
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 3;
        Prefs.BUDGETSTARTDATE r15 = java.lang.Math.abs(Prefs.BUDGETSTARTDATE r15);
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x01a6;
    L_0x027d:
        Prefs.BUDGETSTARTDATE r15 = 5;
        r16 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r17 = 5;
        r16 = r16.get(r17);
        r17 = 5;
        r0 = r17;
        r17 = GregorianCalendar budgetStartDate r4.get(r0);
        r16 = java.lang.Math.min(r16, r17);
        r0 = r16;
        r13.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
    L_0x029c:
        if (r8 >= r3) goto L_0x02b0;
    L_0x029e:
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 6;
        Prefs.BUDGETSTARTDATE r15 = java.lang.Math.abs(Prefs.BUDGETSTARTDATE r15);
        Prefs.BUDGETSTARTDATE r15 = 6 - Prefs.BUDGETSTARTDATE r15;
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x01d1;
    L_0x02b0:
        Prefs.BUDGETSTARTDATE r15 = r8 - r3;
        Prefs.BUDGETSTARTDATE r15 = Prefs.BUDGETSTARTDATE r15 % 6;
        Prefs.BUDGETSTARTDATE r15 = java.lang.Math.abs(Prefs.BUDGETSTARTDATE r15);
        r0 = r18;
        r13 = com.catamount.pocketmoney.misc.CalExt.subtractMonths(r0, Prefs.BUDGETSTARTDATE r15);
        goto L_0x01d1;
    L_0x02c0:
        Prefs.BUDGETSTARTDATE r15 = 5;
        r16 = com.catamount.pocketmoney.misc.CalExt.endOfMonth(r13);
        r17 = 5;
        r16 = r16.get(r17);
        r17 = 5;
        r0 = r17;
        r17 = GregorianCalendar budgetStartDate r4.get(r0);
        r16 = java.lang.Math.min(r16, r17);
        r0 = r16;
        r13.set(Prefs.BUDGETSTARTDATE r15, r0);
        Prefs.BUDGETSTARTDATE r15 = r13;
        goto L_0x006b;
        */
    //throw new UnsupportedOperationException("Method not decompiled: com.catamount.pocketmoney.views.budgets.BudgetsRowAdapter.startOfPeriod(java.util.GregorianCalendar, int):java.util.GregorianCalendar");
    //}

    public static GregorianCalendar startOfPeriod(GregorianCalendar calendar, int budgetPeriod) {
        boolean firstOfMonth;
        String budgetStartDateString /*r5*/ = Prefs.getStringPref(Prefs.BUDGETSTARTDATE /*r15*/);
        GregorianCalendar budgetStartDate /*r4*/ = CalExt.dateFromDescriptionWithMediumDate(budgetStartDateString /*r5*/);
        boolean endOfMonth = false;
        if (budgetStartDateString /*r5*/.equalsIgnoreCase(Locales.kLOC_GENERAL_DEFAULT)) {
            budgetStartDate = CalExt.beginningOfMonth(calendar) /*r11*/;
        } else
            endOfMonth /*r11*/ = budgetStartDate /*r4*/.get(Calendar.DAY_OF_MONTH /*5*/) == CalExt.endOfMonth(budgetStartDate).get(Calendar.DAY_OF_MONTH);
        firstOfMonth = budgetStartDate == null || budgetStartDate.get(Calendar.DAY_OF_MONTH) == 1;


        switch (budgetPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                return CalExt.beginningOfDay(calendar);
            case Enums.kBudgetPeriodWeek /*1*/:
                return CalExt.beginningOfWeek(calendar);
            case Enums.kBudgetPeriodMonth /*2*/:
                if (firstOfMonth) {
                    budgetStartDate = CalExt.beginningOfDay(calendar);
                } else {
                    budgetStartDate = CalExt.beginningOfDay(calendar);
                    if (endOfMonth) {
                        budgetStartDate = CalExt.beginningOfMonth(CalExt.beginningOfMonth(budgetStartDate)); // Calls beginningOfMonth twice? Surely only need to call once??
                    }
                }
                return budgetStartDate;
            case Enums.kBudgetPeriodQuarter /*3*/:
                if (endOfMonth) {
                    budgetStartDate = CalExt.beginningOfMonth(CalExt.subtractMonths(CalExt.addDays(calendar, 1), 3));
                } else {
                    budgetStartDate = CalExt.subtractMonths(CalExt.addDays(calendar, 1), 3);
                    if (firstOfMonth) {
                        budgetStartDate = CalExt.subtractDay(CalExt.endOfMonth(budgetStartDate));
                    }
                }
                return budgetStartDate;
            case Enums.kBudgetPeriodYear /*4*/:
                return CalExt.beginningOfYear(calendar);
            case Enums.kBudgetPeriodBiweekly /*5*/:
                // get budget start date
                // count weeks (lweekdbetween) between budget start date and current date (will be positive if budget start date preceeds current date)
                // counts days between budget start date and current date (will be positive if budget start date preceeds current date)
                // if modulus of lweeksbetween = 0 that means that we can use the DAYOFTHEMONTH of the budget start date
                int lWeeksBetween /*r14*/ = CalExt.weeksBetween(budgetStartDate, calendar);
                int lDaysBetween /*r15*/ = CalExt.daysBetween(budgetStartDate, calendar);
                int lDaysRemainder /*r9*/ = lDaysBetween % 7;
                int lWeeksRemainder /*r15(revised)*/ = lWeeksBetween % 2;
                if (lWeeksRemainder /*r15(revised)*/ != 0) {
                    int r12 = 0;
                }
                if (lWeeksBetween/*r14*/ > 0) {
                    if (lWeeksRemainder == 0) {
                        budgetStartDate = CalExt.subtractDays(budgetStartDate, lDaysRemainder);
                    } else {
                        budgetStartDate = CalExt.subtractDays(budgetStartDate, lDaysRemainder + 7);
                    }
                } else {
                    if (lWeeksRemainder == 0) {
                        budgetStartDate = CalExt.addDays(budgetStartDate, lDaysRemainder);
                    } else {
                        budgetStartDate = CalExt.addDays(budgetStartDate, lDaysRemainder + 7);
                    }
                }
                return budgetStartDate;
            case Enums.kBudgetPeriodBimonthly /*6*/:
                if (endOfMonth) {
                    budgetStartDate = CalExt.beginningOfMonth(CalExt.addDays(CalExt.subtractMonths(calendar, 2), 1));
                } else {
                    budgetStartDate = CalExt.subtractMonths(CalExt.subtractDay(calendar), 2);
                    if (firstOfMonth) {
                        budgetStartDate = CalExt.subtractDay(CalExt.endOfMonth(budgetStartDate));
                    }
                }
                return budgetStartDate;
            case Enums.kBudgetPeriodHalfYear /*7*/:
                if (endOfMonth) {
                    budgetStartDate = CalExt.beginningOfMonth(CalExt.addMonths(calendar, 6));
                } else {
                    budgetStartDate = CalExt.subtractMonths(calendar, 6);
                    if (firstOfMonth) {
                        budgetStartDate = CalExt.addDays(CalExt.beginningOfDay(budgetStartDate), 1);
                    }
                }
                return budgetStartDate;
            case Enums.kBudgetPeriod4Weeks /*8*/:
                return CalExt.beginningOfDay(CalExt.subtractDays(CalExt.subtractDay(calendar), 4 * 7));
            default:
                return null;
        }
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
            case Enums.kBudgetPeriodDay /*0*/:
                return CalExt.endOfDay(this.currentDate);
            case Enums.kBudgetPeriodWeek /*1*/:
                return CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 1));
            case Enums.kBudgetPeriodMonth /*2*/:
                if (firstOfMonth) {
                    budgetEndDate = CalExt.endOfMonth(this.currentDate);
                } else {
                    budgetEndDate = CalExt.addMonth(CalExt.subtractDay(startOfPeriod()));
                    if (endOfMonth) {
                        budgetEndDate = CalExt.endOfMonth(CalExt.endOfMonth(budgetEndDate));
                    }
                }
                return budgetEndDate;
            case Enums.kBudgetPeriodQuarter /*3*/:
                if (firstOfMonth) {
                    budgetEndDate = CalExt.endOfMonth(CalExt.addMonths(startOfPeriod(), 2));
                } else {
                    budgetEndDate = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 3);
                    if (endOfMonth) {
                        budgetEndDate = CalExt.subtractDay(CalExt.endOfMonth(budgetEndDate));
                    }
                }
                return budgetEndDate;
            case Enums.kBudgetPeriodYear /*4*/:
                return CalExt.endOfDay(CalExt.addYear(CalExt.subtractDay(startOfPeriod())));
            case Enums.kBudgetPeriodBiweekly /*5*/:
                return CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 2));
            case Enums.kBudgetPeriodBimonthly /*6*/:
                if (firstOfMonth) {
                    budgetEndDate = CalExt.endOfMonth(CalExt.addMonth(startOfPeriod()));
                } else {
                    budgetEndDate = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 2);
                    if (endOfMonth) {
                        budgetEndDate = CalExt.subtractDay(CalExt.endOfMonth(budgetEndDate));
                    }
                }
                return budgetEndDate;
            case Enums.kBudgetPeriodHalfYear /*7*/:
                if (firstOfMonth) {
                    budgetEndDate = CalExt.endOfMonth(CalExt.addMonths(startOfPeriod(), 5));
                } else {
                    budgetEndDate = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 6);
                    if (endOfMonth) {
                        budgetEndDate = CalExt.subtractDay(CalExt.endOfMonth(budgetEndDate));
                    }
                }
                return budgetEndDate;
            case Enums.kBudgetPeriod4Weeks /*8*/:
                return CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 2));
            default:
                return null;
        }
    }

    void nextPeriod() {
        switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                this.currentDate = CalExt.addDays(this.currentDate, 1);
                return;
            case Enums.kBudgetPeriodWeek /*1*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, 1);
                return;
            case Enums.kBudgetPeriodMonth /*2*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 1);
                return;
            case Enums.kBudgetPeriodQuarter /*3*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 3);
                return;
            case Enums.kBudgetPeriodYear /*4*/:
                this.currentDate = CalExt.addYear(this.currentDate);
                return;
            case Enums.kBudgetPeriodBiweekly /*5*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, 2);
                return;
            case Enums.kBudgetPeriodBimonthly /*6*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 2);
                return;
            case Enums.kBudgetPeriodHalfYear /*7*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 6);
                return;
            case Enums.kBudgetPeriod4Weeks /*8*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, 4);
                return;
            default:
        }
    }

    void previousPeriod() {
        switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                this.currentDate = CalExt.addDays(this.currentDate, -1);
                return;
            case Enums.kBudgetPeriodWeek /*1*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, -1);
                return;
            case Enums.kBudgetPeriodMonth /*2*/:
                this.currentDate = CalExt.addMonths(this.currentDate, -1);
                return;
            case Enums.kBudgetPeriodQuarter /*3*/:
                this.currentDate = CalExt.addMonths(this.currentDate, -3);
                return;
            case Enums.kBudgetPeriodYear /*4*/:
                this.currentDate = CalExt.subtractYear(this.currentDate);
                return;
            case Enums.kBudgetPeriodBiweekly /*5*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, -2);
                return;
            case Enums.kBudgetPeriodBimonthly /*6*/:
                this.currentDate = CalExt.addMonths(this.currentDate, -2);
                return;
            case Enums.kBudgetPeriodHalfYear /*7*/:
                this.currentDate = CalExt.addMonths(this.currentDate, -6);
                return;
            case Enums.kBudgetPeriod4Weeks /*8*/:
                this.currentDate = CalExt.addWeeks(this.currentDate, -4);
                return;
            default:
        }
    }

    double getProgressPercent() {
        GregorianCalendar today = new GregorianCalendar();
        if (this.currentPeriod == Enums.kBudgetPeriodDay/*0*/) {
            if (this.currentDate.after(today)) {
                return 0.0d;
            }
            return 1.0d;
        }
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
