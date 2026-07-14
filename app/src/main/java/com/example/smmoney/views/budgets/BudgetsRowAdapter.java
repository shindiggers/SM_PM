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

import com.example.smmoney.R;
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
    private static final Comparator<CategoryClass> categoryComparator = (category1, category2) -> {
        double diff;
        switch (Prefs.getIntPref(Prefs.BUDGETS_SORTON)) {
            case Enums.kBudgetsSortTypeActual /*1*/:
                diff = category2.spent - category1.spent;
                if (diff < 0.0d) return -1;
                return diff > 0.0d ? 1 : 0;
            case Enums.kBudgetsSortTypeBudgeted /*2*/:
                diff = category2.budget - category1.budget;
                if (diff < 0.0d) return -1;
                return diff > 0.0d ? 1 : 0;
            default:
                return category1.getCategory().toUpperCase().compareTo(category2.getCategory().toUpperCase());
        }
    };

    GregorianCalendar currentDate;
    int currentPeriod;
    private final Context context;
    private List<CategoryClass> expenseCategories;
    private List<CategoryClass> incomeCategories;
    private final LayoutInflater inflater;
    private List<CategoryClass> nonBudgetedCategories;
    private final String showExpense = Prefs.COLLAPSE_EXPENSES;
    private final String showIncome = Prefs.COLLAPSE_INCOME;
    private final String showNonBudgeted = Prefs.COLLAPSE_UNBUDGETED;

    BudgetsRowAdapter(Context aContext, ListView theList) {
        this.context = aContext;
        this.inflater = LayoutInflater.from(this.context);
        this.currentDate = new GregorianCalendar();
        this.currentPeriod = Prefs.getIntPref(Prefs.DISPLAY_BUDGETPERIOD);
    }

    public int getCount() {
        int count = 3;
        if (this.incomeCategories != null && getShowSection(this.showIncome)) count += this.incomeCategories.size();
        if (this.expenseCategories != null && getShowSection(this.showExpense)) count += this.expenseCategories.size();
        if (Prefs.getBooleanPref(Prefs.BUDGETSHOWUNBUDGETED) && this.nonBudgetedCategories != null && getShowSection(this.showNonBudgeted)) count += this.nonBudgetedCategories.size();
        return count;
    }

    public Object getItem(int position) {
        int incomeSize = (this.incomeCategories == null || !getShowSection(this.showIncome)) ? 0 : this.incomeCategories.size();
        int expenseSize = (this.expenseCategories == null || !getShowSection(this.showExpense)) ? 0 : this.expenseCategories.size();
        if (position == 0) return Locales.kLOC_BUDGETS_INCOME;
        if (position < incomeSize + 1) return this.incomeCategories.get(position - 1);
        if (position == incomeSize + 1) return Locales.kLOC_BUDGETS_EXPENSES;
        if (position < (incomeSize + expenseSize) + 2) return this.expenseCategories.get((position - incomeSize) - 2);
        if (position == (incomeSize + expenseSize) + 2) return Locales.kLOC_BUDGETS_NONBUDGETED;
        return this.nonBudgetedCategories.get(((position - incomeSize) - expenseSize) - 3);
    }

    public long getItemId(int position) { return position; }

    public View getView(int position, View convertView, ViewGroup parent) {
        Object cat = getItem(position);
        if (cat instanceof String) {
            String label = (String) cat;
            BudgetsListHeaderHolder header = new BudgetsListHeaderHolder(this.context);
            header.setOnClickListener(getHeaderClickListener());
            
            double actual, target, diff;
            String targetLabel, sentence;
            
            if (label.equals(Locales.kLOC_BUDGETS_INCOME)) {
                actual = totalIncomes();
                target = budgetedIncomes();
                diff = actual - target;
                targetLabel = Locales.kLOC_BUDGET_LBL_BUDGETED;
                header.setExpanded(getShowSection(this.showIncome));
            } else if (label.equals(Locales.kLOC_BUDGETS_EXPENSES)) {
                actual = Math.abs(totalExpenses());
                target = Math.abs(budgetedExpenses());
                diff = target - actual;
                targetLabel = Locales.kLOC_BUDGET_LBL_BUDGETED;
                header.setExpanded(getShowSection(this.showExpense));
            } else { // Unbudgeted
                actual = Math.abs(totalNonBudgeted());
                target = 0.0;
                diff = -actual;
                targetLabel = Locales.kLOC_BUDGET_LBL_BUDGETED;
                header.setExpanded(getShowSection(this.showNonBudgeted));
            }

            sentence = (diff >= 0) ? Locales.kLOC_BUDGET_LBL_AHEAD.replace("%1$s", formatAmount(diff)) : 
                                     Locales.kLOC_BUDGET_LBL_BEHIND.replace("%1$s", formatAmount(diff));
            header.setData(label, actual, targetLabel, target, sentence);
            return header;
        }

        BudgetsRowHolder holder;
        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof BudgetsRowHolder)) {
            convertView = this.inflater.inflate(R.layout.budgets_row, parent, false);
            holder = new BudgetsRowHolder(convertView);
            convertView.setOnClickListener(getRowClickListener());
            ((Activity) this.context).registerForContextMenu(convertView);
            convertView.setTag(holder);
        } else {
            holder = (BudgetsRowHolder) convertView.getTag();
        }
        holder.setCategory((CategoryClass) cat);
        return convertView;
    }

    private String formatAmount(double amount) {
        String text = Prefs.getBooleanPref(Prefs.BUDGETSHOWCENTS) ? CurrencyExt.amountAsCurrency(Math.abs(amount)) : CurrencyExt.amountAsCurrencyWithoutCents(Math.abs(amount));
        return (amount < 0) ? "(" + text + ")" : text;
    }

    private OnClickListener getRowClickListener() {
        return view -> {
            Intent i = new Intent(BudgetsRowAdapter.this.context, TransactionsActivity.class);
            BudgetsRowHolder holder = (BudgetsRowHolder) view.getTag();
            FilterClass aFilter = new FilterClass();
            aFilter.setCategory(holder.category.getCategory() + (holder.category.getIncludeSubcategories() ? "%" : ""));
            aFilter.setDate(Locales.kLOC_FILTER_DATES_CUSTOM);
            aFilter.setDateFrom(BudgetsRowAdapter.this.startOfPeriod());
            aFilter.setDateTo(BudgetsRowAdapter.this.endOfPeriod());
            aFilter.setFilterName(holder.category.getCategory());
            aFilter.setCustomFilter(true);
            i.putExtra("subtitle", BudgetsRowAdapter.this.rangeOfPeriodAsString());
            i.putExtra("Filter", aFilter);
            BudgetsRowAdapter.this.context.startActivity(i);
        };
    }

    private OnClickListener getHeaderClickListener() {
        return view -> {
            String cat = ((BudgetsListHeaderHolder) view).label;
            if (cat.equals(Locales.kLOC_BUDGETS_INCOME)) setShowSection(this.showIncome, !getShowSection(this.showIncome));
            else if (cat.equals(Locales.kLOC_BUDGETS_EXPENSES)) setShowSection(this.showExpense, !getShowSection(this.showExpense));
            else if (cat.equals(Locales.kLOC_BUDGETS_NONBUDGETED)) setShowSection(this.showNonBudgeted, !getShowSection(this.showNonBudgeted));
            BudgetsRowAdapter.this.notifyDataSetChanged();
        };
    }

    public void reloadData() {
        boolean hideZeroActuals = Prefs.getBooleanPref(Prefs.BUDGETHIDEZEROSACTUALS);
        GregorianCalendar start = startOfPeriod();
        GregorianCalendar end = endOfPeriod();
        
        List<CategoryClass> incomes = CategoryClass.queryIncomeCategoriesWithBudgets();
        this.incomeCategories = new ArrayList<>();
        for (CategoryClass c : incomes) {
            c.spent = CategoryClass.querySpentInCategory(c.getCategory(), c.getIncludeSubcategories(), start, end);
            c.budget = round(c.budgetLimit(start, end));
            if (c.spent != 0.0d || c.budget != 0.0d || !hideZeroActuals) this.incomeCategories.add(c);
        }
        
        List<CategoryClass> expenses = CategoryClass.queryExpenseCategoriesWithBudgets();
        this.expenseCategories = new ArrayList<>();
        for (CategoryClass c : expenses) {
            c.spent = CategoryClass.querySpentInCategory(c.getCategory(), c.getIncludeSubcategories(), start, end);
            c.budget = round(c.budgetLimit(start, end));
            if (c.spent != 0.0d || c.budget != 0.0d || !hideZeroActuals) this.expenseCategories.add(c);
        }
        
        this.nonBudgetedCategories = new ArrayList<>();
        if (Prefs.getBooleanPref(Prefs.BUDGETSHOWUNBUDGETED)) {
            List<CategoryClass> unbudgeted = CategoryClass.queryNonBudgettedCategories();
            for (CategoryClass c : unbudgeted) {
                c.spent = CategoryClass.querySpentInCategory(c.getCategory(), c.getIncludeSubcategories(), start, end);
                if (c.spent != 0.0d || !hideZeroActuals) this.nonBudgetedCategories.add(c);
            }
        }
        
        Collections.sort(this.incomeCategories, categoryComparator);
        Collections.sort(this.expenseCategories, categoryComparator);
        Collections.sort(this.nonBudgetedCategories, categoryComparator);
        
        ((Activity) this.context).runOnUiThread(this::notifyDataSetChanged);
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }
    private void setShowSection(String s, boolean show) { Prefs.setPref(s, show); }
    private boolean getShowSection(String s) { return Prefs.getBooleanPref(s); }

    double totalIncomes() {
        double t = 0;
        if (incomeCategories != null) for (CategoryClass c : incomeCategories) t += c.spent;
        return t;
    }
    double totalExpenses() {
        double t = 0;
        if (expenseCategories != null) for (CategoryClass c : expenseCategories) t += c.spent;
        return t;
    }
    double totalNonBudgeted() {
        double t = 0;
        if (nonBudgetedCategories != null) for (CategoryClass c : nonBudgetedCategories) t += c.spent;
        return t;
    }
    double budgetedIncomes() {
        double t = 0;
        if (incomeCategories != null) for (CategoryClass c : incomeCategories) t += c.budget;
        return t;
    }
    double budgetedExpenses() {
        double t = 0;
        if (expenseCategories != null) for (CategoryClass c : expenseCategories) t += c.budget;
        return t;
    }

    public static GregorianCalendar startOfPeriod(GregorianCalendar inputDate, int budgetPeriod) {
        String budgetStartDateString = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
        GregorianCalendar budgetStartDate = CalExt.dateFromDescriptionWithMediumDate(budgetStartDateString);
        if (budgetStartDateString.equalsIgnoreCase(Locales.kLOC_GENERAL_DEFAULT)) {
            budgetStartDate = new GregorianCalendar();
            budgetStartDate.set(1989, 0, 1, 0, 0, 0);
        }
        GregorianCalendar clonedInputDate = (GregorianCalendar) inputDate.clone();
        switch (budgetPeriod) {
            case Enums.kBudgetPeriodDay -> { return CalExt.beginningOfDay(inputDate); }
            case Enums.kBudgetPeriodWeek -> {
                int dowStart = budgetStartDate.get(Calendar.DAY_OF_WEEK);
                int dowInput = inputDate.get(Calendar.DAY_OF_WEEK);
                return CalExt.subtractDays(inputDate, (dowInput < dowStart) ? 7 - (dowStart - dowInput) : dowInput - dowStart);
            }
            case Enums.kBudgetPeriodMonth -> {
                if (inputDate.get(Calendar.DAY_OF_MONTH) < budgetStartDate.get(Calendar.DAY_OF_MONTH)) clonedInputDate = CalExt.subtractMonth(clonedInputDate);
                clonedInputDate.set(Calendar.DAY_OF_MONTH, Math.min(CalExt.endOfMonth(clonedInputDate).get(Calendar.DAY_OF_MONTH), budgetStartDate.get(Calendar.DAY_OF_MONTH)));
                return clonedInputDate;
            }
            case Enums.kBudgetPeriodYear -> {
                if (clonedInputDate.get(Calendar.MONTH) < budgetStartDate.get(Calendar.MONTH) || (clonedInputDate.get(Calendar.MONTH) == budgetStartDate.get(Calendar.MONTH) && clonedInputDate.get(Calendar.DAY_OF_MONTH) < budgetStartDate.get(Calendar.DAY_OF_MONTH))) clonedInputDate = CalExt.subtractYear(clonedInputDate);
                clonedInputDate.set(Calendar.MONTH, budgetStartDate.get(Calendar.MONTH));
                clonedInputDate.set(Calendar.DAY_OF_MONTH, budgetStartDate.get(Calendar.DAY_OF_MONTH));
                return clonedInputDate;
            }
            default -> { return inputDate; }
        }
    }

    public GregorianCalendar startOfPeriod() { return startOfPeriod(this.currentDate, this.currentPeriod); }

    public GregorianCalendar endOfPeriod() {
        return switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay -> CalExt.endOfDay(this.currentDate);
            case Enums.kBudgetPeriodWeek -> CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 1));
            case Enums.kBudgetPeriodMonth -> CalExt.addMonth(CalExt.subtractDay(startOfPeriod()));
            case Enums.kBudgetPeriodYear -> CalExt.endOfDay(CalExt.addYear(CalExt.subtractDay(startOfPeriod())));
            default -> this.currentDate;
        };
    }

    void nextPeriod() {
        switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay -> this.currentDate = CalExt.addDays(this.currentDate, 1);
            case Enums.kBudgetPeriodWeek -> this.currentDate = CalExt.addWeeks(this.currentDate, 1);
            case Enums.kBudgetPeriodMonth -> this.currentDate = CalExt.addMonths(this.currentDate, 1);
            case Enums.kBudgetPeriodYear -> this.currentDate = CalExt.addYear(this.currentDate);
        }
    }

    void previousPeriod() {
        switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay -> this.currentDate = CalExt.addDays(this.currentDate, -1);
            case Enums.kBudgetPeriodWeek -> this.currentDate = CalExt.addWeeks(this.currentDate, -1);
            case Enums.kBudgetPeriodMonth -> this.currentDate = CalExt.addMonths(this.currentDate, -1);
            case Enums.kBudgetPeriodYear -> this.currentDate = CalExt.subtractYear(this.currentDate);
        }
    }

    double getProgressPercent() {
        GregorianCalendar today = new GregorianCalendar();
        if (startOfPeriod().after(today)) return 1.0;
        if (endOfPeriod().before(today)) return 0.0;
        return ((double) (today.getTimeInMillis() - startOfPeriod().getTimeInMillis())) / ((double) (endOfPeriod().getTimeInMillis() - startOfPeriod().getTimeInMillis()));
    }

    String rangeOfPeriodAsString() {
        return switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay -> CalExt.descriptionWithMediumDate(this.currentDate);
            case Enums.kBudgetPeriodMonth -> CalExt.descriptionWithMonth(this.currentDate) + " " + CalExt.descriptionWithYear(this.currentDate);
            case Enums.kBudgetPeriodYear -> CalExt.descriptionWithYear(this.currentDate);
            default -> CalExt.descriptionWithMediumDate(startOfPeriod()) + " - " + CalExt.descriptionWithMediumDate(endOfPeriod());
        };
    }
}
