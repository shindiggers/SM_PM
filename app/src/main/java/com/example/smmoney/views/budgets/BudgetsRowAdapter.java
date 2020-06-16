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
    GregorianCalendar currentDate;
    int currentPeriod;
    private Context context;
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

    public static GregorianCalendar startOfPeriod(GregorianCalendar inputDate, int budgetPeriod) {
        String budgetStartDateString = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
        GregorianCalendar budgetStartDate = CalExt.dateFromDescriptionWithMediumDate(budgetStartDateString);
        if (budgetStartDateString.equalsIgnoreCase(Locales.kLOC_GENERAL_DEFAULT)) {
            budgetStartDate = new GregorianCalendar();
            budgetStartDate.set(Calendar.YEAR/*1*/, 1989);
            budgetStartDate.set(Calendar.MONTH/*2*/, 0);
            budgetStartDate.set(Calendar.DAY_OF_MONTH/*5*/, 1);
            budgetStartDate.set(Calendar.HOUR_OF_DAY/*11*/, 0);
            budgetStartDate.set(Calendar.MINUTE/*12*/, 0);
            budgetStartDate.set(Calendar.SECOND/*13*/, 0);
        }

        GregorianCalendar eomForBudgetStartDate = CalExt.endOfMonth(budgetStartDate);
        boolean endOfMonth;
        endOfMonth = budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/) == eomForBudgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/);

        GregorianCalendar clonedInputDate;
        label175:
        {
            clonedInputDate = (GregorianCalendar) inputDate.clone();
            switch (budgetPeriod) {
                case Enums.kBudgetPeriodDay/*0*/:
                    return CalExt.beginningOfDay(inputDate);
                case Enums.kBudgetPeriodWeek/*1*/:
                    int dowBudgetStartDate = budgetStartDate.get(Calendar.DAY_OF_WEEK/*7*/);
                    int dowInputDate = inputDate.get(Calendar.DAY_OF_WEEK/*7*/);
                    GregorianCalendar returnDate;
                    if (inputDate.get(Calendar.DAY_OF_WEEK/*7*/) < budgetStartDate.get(Calendar.DAY_OF_WEEK/*7*/)) {
                        returnDate = CalExt.subtractDays(inputDate, 7 - (dowBudgetStartDate - dowInputDate));
                    } else {
                        returnDate = CalExt.subtractDays(inputDate, dowInputDate - dowBudgetStartDate);
                    }

                    return returnDate;
                case Enums.kBudgetPeriodMonth/*2*/:
                    CalExt.daysBetween(budgetStartDate, inputDate);
                    if (inputDate.get(Calendar.DAY_OF_MONTH/*5*/) < budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/)) {
                        clonedInputDate = CalExt.subtractMonth(clonedInputDate);
                    }

                    if (endOfMonth) {
                        return CalExt.endOfMonth(clonedInputDate);
                    }

                    clonedInputDate.set(Calendar.DAY_OF_MONTH/*5*/, Math.min(CalExt.endOfMonth(clonedInputDate).get(Calendar.DAY_OF_MONTH/*5*/), budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/)));
                    return clonedInputDate;
                case Enums.kBudgetPeriodYear/*4*/:
                    break label175;
                case Enums.kBudgetPeriodBiweekly/*5*/:
                    int weeksBetweenStartDateAndInputDate = CalExt.weeksBetween(budgetStartDate, inputDate);
                    int weekdaysBetweenStartDateAndInputDate = CalExt.daysBetween(budgetStartDate, inputDate) % 7;
                    boolean twoWeeksBetweenDays;
                    twoWeeksBetweenDays = weeksBetweenStartDateAndInputDate % 2 == 0;

                    //GregorianCalendar returnDate;
                    if (weeksBetweenStartDateAndInputDate >= 0 && weekdaysBetweenStartDateAndInputDate >= 0) {
                        byte twoWeekAdjustments;
                        if (twoWeeksBetweenDays) {
                            twoWeekAdjustments = 0;
                        } else {
                            twoWeekAdjustments = 7;
                        }

                        returnDate = CalExt.subtractDays(inputDate, twoWeekAdjustments + weekdaysBetweenStartDateAndInputDate);
                    } else {
                        byte twoWeekAdjustments;
                        if (twoWeeksBetweenDays) {
                            twoWeekAdjustments = 7;
                        } else {
                            twoWeekAdjustments = 0;
                        }

                        returnDate = CalExt.subtractDays(inputDate, weekdaysBetweenStartDateAndInputDate + twoWeekAdjustments + 6);
                    }

                    return returnDate;
                case Enums.kBudgetPeriodBimonthly/*6*/:
                    int monthOfBudgetStartDate = budgetStartDate.get(Calendar.MONTH/*2*/);
                    int domOfBudgetStartDate = budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/);
                    int monthOfInputDate = inputDate.get(Calendar.MONTH/*2*/);
                    int domOfInputDate = inputDate.get(Calendar.DAY_OF_MONTH/*5*/);
                    //GregorianCalendar returnDate;
                    if ((monthOfInputDate - monthOfBudgetStartDate) % 2 == 0 && domOfInputDate < domOfBudgetStartDate) {
                        returnDate = CalExt.subtractMonths(inputDate, 2);
                    } else if (monthOfInputDate < monthOfBudgetStartDate) {
                        returnDate = CalExt.subtractMonths(inputDate, 2 - Math.abs((monthOfInputDate - monthOfBudgetStartDate) % 2));
                    } else {
                        returnDate = CalExt.subtractMonths(inputDate, Math.abs((monthOfInputDate - monthOfBudgetStartDate) % 2));
                    }

                    if (!endOfMonth) {
                        returnDate.set(Calendar.DAY_OF_MONTH/*5*/, Math.min(CalExt.endOfMonth(returnDate).get(Calendar.DAY_OF_MONTH/*5*/), budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/)));
                        return returnDate;
                    }

                    CalExt.endOfMonth(returnDate);
                case Enums.kBudgetPeriodQuarter/*3*/:
                    monthOfBudgetStartDate = budgetStartDate.get(Calendar.MONTH/*2*/);
                    domOfBudgetStartDate = budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/);
                    monthOfInputDate = inputDate.get(Calendar.MONTH/*2*/);
                    domOfInputDate = inputDate.get(Calendar.DAY_OF_MONTH/*5*/);
                    //GregorianCalendar returnDate;
                    if ((monthOfInputDate - monthOfBudgetStartDate) % 3 == 0 && domOfInputDate < domOfBudgetStartDate) {
                        returnDate = CalExt.subtractMonths(inputDate, 3);
                    } else if (monthOfInputDate < monthOfBudgetStartDate) {
                        returnDate = CalExt.subtractMonths(inputDate, 3 - Math.abs((monthOfInputDate - monthOfBudgetStartDate) % 3));
                    } else {
                        returnDate = CalExt.subtractMonths(inputDate, Math.abs((monthOfInputDate - monthOfBudgetStartDate) % 3));
                    }

                    if (!endOfMonth) {
                        returnDate.set(Calendar.DAY_OF_MONTH/*5*/, Math.min(CalExt.endOfMonth(returnDate).get(Calendar.DAY_OF_MONTH/*5*/), budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/)));
                        return returnDate;
                    }

                    CalExt.endOfMonth(returnDate);
                case Enums.kBudgetPeriodHalfYear/*7*/:
                    break;
                case Enums.kBudgetPeriod4Weeks/*8*/:
                    weeksBetweenStartDateAndInputDate = CalExt.weeksBetween(budgetStartDate, inputDate);
                    weekdaysBetweenStartDateAndInputDate = CalExt.daysBetween(budgetStartDate, inputDate) % 7;
                    //GregorianCalendar returnDate;
                    if (weeksBetweenStartDateAndInputDate >= 0 && weekdaysBetweenStartDateAndInputDate >= 0) {
                        returnDate = CalExt.subtractDays(inputDate, weekdaysBetweenStartDateAndInputDate + weeksBetweenStartDateAndInputDate * 7 % 4);
                    } else {
                        returnDate = CalExt.subtractDays(inputDate, weekdaysBetweenStartDateAndInputDate + 6 + 7 * (3 - Math.abs(weeksBetweenStartDateAndInputDate % 4)));
                    }

                    return returnDate;
                default:
                    return null;
            }

            int monthOfBudgetStartDate = budgetStartDate.get(Calendar.MONTH/*2*/);
            int domOfBudgetStartDate = budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/);
            int monthOfInputDate = inputDate.get(Calendar.MONTH/*2*/);
            int domOfInputDate = inputDate.get(Calendar.DAY_OF_MONTH/*5*/);
            GregorianCalendar returnDate;
            if ((monthOfInputDate - monthOfBudgetStartDate) % 6 == 0 && domOfInputDate < domOfBudgetStartDate) {
                returnDate = CalExt.subtractMonths(inputDate, 6);
            } else if (monthOfInputDate < monthOfBudgetStartDate) {
                returnDate = CalExt.subtractMonths(inputDate, 6 - Math.abs((monthOfInputDate - monthOfBudgetStartDate) % 6));
            } else {
                returnDate = CalExt.subtractMonths(inputDate, Math.abs((monthOfInputDate - monthOfBudgetStartDate) % 6));
            }

            if (!endOfMonth) {
                returnDate.set(Calendar.DAY_OF_MONTH/*5*/, Math.min(CalExt.endOfMonth(returnDate).get(Calendar.DAY_OF_MONTH/*5*/), budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/)));
                return returnDate;
            }

            clonedInputDate = CalExt.endOfMonth(returnDate);
        }

        int monthOfBudgetStartDate = budgetStartDate.get(Calendar.MONTH/*2*/);
        int domOfBudgetStartDate = budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/);
        int monthOfInputDate = inputDate.get(Calendar.MONTH/*2*/);
        int domOfInputDate = inputDate.get(Calendar.DAY_OF_MONTH/*5*/);
        if (monthOfInputDate < monthOfBudgetStartDate || monthOfInputDate == monthOfBudgetStartDate && domOfInputDate < domOfBudgetStartDate) {
            clonedInputDate = CalExt.subtractYear(inputDate);
        }

        clonedInputDate.set(Calendar.DAY_OF_MONTH/*5*/, budgetStartDate.get(Calendar.DAY_OF_MONTH/*5*/));
        clonedInputDate.set(Calendar.MONTH/*2*/, budgetStartDate.get(Calendar.MONTH/*2*/));
        return clonedInputDate;
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
                return CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 4));
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
