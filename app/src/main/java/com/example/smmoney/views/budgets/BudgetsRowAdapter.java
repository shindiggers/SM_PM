package com.example.smmoney.views.budgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.transactions.TransactionsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

public class BudgetsRowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ROW = 1;

    private double getVariance(CategoryClass c, boolean isUnbudgeted) {
        double spent;
        if (c.getType() == Enums.kCategoryExpense || isUnbudgeted) {
            spent = ((double) Math.round(c.spent * -100.0d)) / 100.0d;
            if (spent == -0.0d) spent = 0.0d;
        } else {
            spent = ((double) Math.round(c.spent * 100.0d)) / 100.0d;
        }
        if (c.getType() == Enums.kCategoryExpense || isUnbudgeted) {
            return c.budget - spent;
        } else {
            return spent - c.budget;
        }
    }

    private Comparator<CategoryClass> getCategoryComparator(final boolean isUnbudgeted) {
        return (category1, category2) -> {
            int sortOn = Prefs.getIntPref(Prefs.BUDGETS_SORTON);
            boolean isAsc = Prefs.getIntPref(Prefs.BUDGETS_SORT_ORDER_ASCENDING) == Enums.kBudgetsSortOrderAscending;
            int result;

            switch (sortOn) {
                case Enums.kBudgetsSortTypeActual -> {
                    double v1 = Math.abs(category1.spent);
                    double v2 = Math.abs(category2.spent);
                    result = Double.compare(v1, v2);
                }
                case Enums.kBudgetsSortTypeBudgeted -> result = Double.compare(category1.budget, category2.budget);
                case Enums.kBudgetsSortTypePercentage -> {
                    double p1 = (category1.budget == 0) ? 0 : Math.abs(category1.spent) / category1.budget;
                    double p2 = (category2.budget == 0) ? 0 : Math.abs(category2.spent) / category2.budget;
                    result = Double.compare(p1, p2);
                }
                case Enums.kBudgetsSortTypeVariance -> {
                    double var1 = getVariance(category1, isUnbudgeted);
                    double var2 = getVariance(category2, isUnbudgeted);
                    result = Double.compare(var1, var2);
                }
                default -> {
                    result = category1.getCategory().toUpperCase().compareTo(category2.getCategory().toUpperCase());
                    return isAsc ? result : -result;
                }
            }

            if (result == 0) {
                result = category1.getCategory().toUpperCase().compareTo(category2.getCategory().toUpperCase());
            }

            return isAsc ? result : -result;
        };
    }

    GregorianCalendar currentDate;
    int currentPeriod;
    private final Context context;
    private List<CategoryClass> expenseCategories = new ArrayList<>();
    private List<CategoryClass> incomeCategories = new ArrayList<>();
    private final LayoutInflater inflater;
    private List<CategoryClass> nonBudgetedCategories = new ArrayList<>();
    private final String showExpense = Prefs.COLLAPSE_EXPENSES;
    private final String showIncome = Prefs.COLLAPSE_INCOME;
    private final String showNonBudgeted = Prefs.COLLAPSE_UNBUDGETED;

    BudgetsRowAdapter(Context aContext) {
        this.context = aContext;
        this.inflater = LayoutInflater.from(this.context);
        this.currentDate = new GregorianCalendar();
        this.currentPeriod = Prefs.getIntPref(Prefs.DISPLAY_BUDGETPERIOD);
    }

    @Override
    public int getItemCount() {
        int count = 3;
        if (this.incomeCategories != null && getShowSection(this.showIncome)) count += this.incomeCategories.size();
        if (this.expenseCategories != null && getShowSection(this.showExpense)) count += this.expenseCategories.size();
        if (Prefs.getBooleanPref(Prefs.BUDGETSHOWUNBUDGETED) && this.nonBudgetedCategories != null && getShowSection(this.showNonBudgeted)) count += this.nonBudgetedCategories.size();
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position) instanceof String) ? TYPE_HEADER : TYPE_ROW;
    }

    public Object getItem(int position) {
        int incomeSize = (this.incomeCategories == null || !getShowSection(this.showIncome)) ? 0 : this.incomeCategories.size();
        int expenseSize = (this.expenseCategories == null || !getShowSection(this.showExpense)) ? 0 : this.expenseCategories.size();
        if (position == 0) return context.getString(R.string.BUDGETS_INCOME);
        if (this.incomeCategories != null && position < incomeSize + 1) return this.incomeCategories.get(position - 1);
        if (position == incomeSize + 1) return context.getString(R.string.BUDGETS_EXPENSES);
        if (this.expenseCategories != null && position < (incomeSize + expenseSize) + 2) return this.expenseCategories.get((position - incomeSize) - 2);
        if (position == (incomeSize + expenseSize) + 2) return context.getString(R.string.BUDGETS_NONBUDGETED);
        int unbudgetedIndex = ((position - incomeSize) - expenseSize) - 3;
        if (this.nonBudgetedCategories != null && unbudgetedIndex >= 0 && unbudgetedIndex < this.nonBudgetedCategories.size()) {
            return this.nonBudgetedCategories.get(unbudgetedIndex);
        }
        return null;
    }

    @Override
    public long getItemId(int position) { return position; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            BudgetsListHeaderHolder header = new BudgetsListHeaderHolder(this.context);
            header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new BudgetsListHeaderViewHolder(header);
        } else {
            View convertView = this.inflater.inflate(R.layout.budgets_row, parent, false);
            return new BudgetsRowHolder(convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = getItem(position);
        if (holder instanceof BudgetsListHeaderViewHolder headerHolder && item instanceof String label) {
            BudgetsListHeaderHolder header = headerHolder.headerHolder;
            header.setOnClickListener(getHeaderClickListener());

            double actual, target, diff;
            String targetLabel;
            CharSequence sentence;

            if (label.equals(context.getString(R.string.BUDGETS_INCOME))) {
                actual = totalIncomes();
                target = budgetedIncomes();
                diff = actual - target;
                targetLabel = context.getString(R.string.BUDGET_LBL_BUDGETED);
                header.setExpanded(getShowSection(this.showIncome));
            } else if (label.equals(context.getString(R.string.BUDGETS_EXPENSES))) {
                actual = totalExpenses() * -1.0; 
                target = Math.abs(budgetedExpenses());
                diff = target - actual;
                targetLabel = context.getString(R.string.BUDGET_LBL_BUDGETED);
                header.setExpanded(getShowSection(this.showExpense));
            } else { // Unbudgeted
                actual = totalNonBudgeted() * -1.0;
                target = 0.0;
                diff = target - actual;
                targetLabel = context.getString(R.string.BUDGET_LBL_BUDGETED);
                header.setExpanded(getShowSection(this.showNonBudgeted));
            }

            sentence = (diff >= 0) ? context.getString(R.string.BUDGET_LBL_AHEAD).replace("%1$s", formatAmount(diff)) :
                                     context.getString(R.string.BUDGET_LBL_BEHIND).replace("%1$s", formatAmount(diff));
            header.setData(label, actual, targetLabel, target, sentence.toString());
        } else if (holder instanceof BudgetsRowHolder rowHolder && item instanceof CategoryClass cat) {
            rowHolder.therow.setTag(rowHolder);
            rowHolder.therow.setOnClickListener(getRowClickListener(rowHolder));

            // Identify unbudgeted items by position in list
            int incomeSize = (this.incomeCategories == null || !getShowSection(this.showIncome)) ? 0 : this.incomeCategories.size();
            int expenseSize = (this.expenseCategories == null || !getShowSection(this.showExpense)) ? 0 : this.expenseCategories.size();
            boolean isUnbudgeted = position > (incomeSize + expenseSize + 2);

            rowHolder.setCategory(cat, isUnbudgeted);
        }
    }

    private String formatAmount(double amount) {
        String text = Prefs.getBooleanPref(Prefs.BUDGETSHOWCENTS) ? CurrencyExt.amountAsCurrency(Math.abs(amount)) : CurrencyExt.amountAsCurrencyWithoutCents(Math.abs(amount));
        return (amount < 0) ? "(" + text + ")" : text;
    }

    private View.OnClickListener getRowClickListener(BudgetsRowHolder holder) {
        return view -> {
            Intent i = new Intent(BudgetsRowAdapter.this.context, TransactionsActivity.class);
            FilterClass aFilter = new FilterClass();
            aFilter.setCategory(holder.category.getCategory() + (holder.category.getIncludeSubcategories() ? "%" : ""));
            aFilter.setDate(context.getString(R.string.FILTER_DATES_CUSTOM));
            aFilter.setDateFrom(BudgetsRowAdapter.this.startOfPeriod());
            aFilter.setDateTo(BudgetsRowAdapter.this.endOfPeriod());
            aFilter.setFilterName(holder.category.getCategory());
            aFilter.setCustomFilter(true);
            i.putExtra("subtitle", BudgetsRowAdapter.this.rangeOfPeriodAsString());
            i.putExtra("Filter", aFilter);
            BudgetsRowAdapter.this.context.startActivity(i);
        };
    }

    private View.OnClickListener getHeaderClickListener() {
        return view -> {
            String cat = ((BudgetsListHeaderHolder) view).label;
            if (cat.equals(context.getString(R.string.BUDGETS_INCOME))) setShowSection(this.showIncome, !getShowSection(this.showIncome));
            else if (cat.equals(context.getString(R.string.BUDGETS_EXPENSES))) setShowSection(this.showExpense, !getShowSection(this.showExpense));
            else if (cat.equals(context.getString(R.string.BUDGETS_NONBUDGETED))) setShowSection(this.showNonBudgeted, !getShowSection(this.showNonBudgeted));
            // Toggling section expansion inserts/removes an entire section of rows and updates the header state
            // and item count/positions throughout the adapter, so a full data set update is appropriate here.
            //noinspection NotifyDataSetChanged
            BudgetsRowAdapter.this.notifyDataSetChanged();
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    public void reloadData() {
        boolean hideZeroActuals = Prefs.getBooleanPref(Prefs.BUDGETHIDEZEROSACTUALS);
        GregorianCalendar start = startOfPeriod();
        GregorianCalendar end = endOfPeriod();
        
        List<CategoryClass> incomesRaw = CategoryClass.queryIncomeCategoriesWithBudgets();
        final List<CategoryClass> newIncomeCategories = new ArrayList<>();
        for (CategoryClass c : incomesRaw) {
            c.spent = CategoryClass.querySpentInCategory(c.getCategory(), c.getIncludeSubcategories(), start, end);
            c.budget = round(c.budgetLimit(start, end));
            if (c.spent != 0.0d || c.budget != 0.0d || !hideZeroActuals) newIncomeCategories.add(c);
        }
        
        List<CategoryClass> expensesRaw = CategoryClass.queryExpenseCategoriesWithBudgets();
        final List<CategoryClass> newExpenseCategories = new ArrayList<>();
        for (CategoryClass c : expensesRaw) {
            c.spent = CategoryClass.querySpentInCategory(c.getCategory(), c.getIncludeSubcategories(), start, end);
            c.budget = round(c.budgetLimit(start, end));
            if (c.spent != 0.0d || c.budget != 0.0d || !hideZeroActuals) newExpenseCategories.add(c);
        }
        
        final List<CategoryClass> newNonBudgetedCategories = new ArrayList<>();
        if (Prefs.getBooleanPref(Prefs.BUDGETSHOWUNBUDGETED)) {
            List<CategoryClass> unbudgeted = CategoryClass.queryNonBudgettedCategories();
            for (CategoryClass c : unbudgeted) {
                c.spent = CategoryClass.querySpentInCategory(c.getCategory(), c.getIncludeSubcategories(), start, end);
                if (c.spent != 0.0d || !hideZeroActuals) newNonBudgetedCategories.add(c);
            }
        }
        
        newIncomeCategories.sort(getCategoryComparator(false));
        newExpenseCategories.sort(getCategoryComparator(false));
        newNonBudgetedCategories.sort(getCategoryComparator(true));
        
        ((Activity) this.context).runOnUiThread(() -> {
            BudgetsRowAdapter.this.incomeCategories = newIncomeCategories;
            BudgetsRowAdapter.this.expenseCategories = newExpenseCategories;
            BudgetsRowAdapter.this.nonBudgetedCategories = newNonBudgetedCategories;
            BudgetsRowAdapter.this.notifyDataSetChanged();
        });
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
        if (budgetStartDateString.equalsIgnoreCase(SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT))) {
            budgetStartDate = new GregorianCalendar();
            budgetStartDate.set(1989, 0, 1, 0, 0, 0);
        }

        boolean endOfMonth = budgetStartDate.get(Calendar.DAY_OF_MONTH) == CalExt.endOfMonth(budgetStartDate).get(Calendar.DAY_OF_MONTH);
        GregorianCalendar clonedInputDate = (GregorianCalendar) inputDate.clone();

        switch (budgetPeriod) {
            case Enums.kBudgetPeriodDay -> { return CalExt.beginningOfDay(inputDate); }
            case Enums.kBudgetPeriodWeek -> {
                int dowStart = budgetStartDate.get(Calendar.DAY_OF_WEEK);
                int dowInput = inputDate.get(Calendar.DAY_OF_WEEK);
                return CalExt.subtractDays(inputDate, (dowInput < dowStart) ? 7 - (dowStart - dowInput) : dowInput - dowStart);
            }
            case Enums.kBudgetPeriodBiweekly -> {
                int weeks = CalExt.weeksBetween(budgetStartDate, inputDate);
                int days = CalExt.daysBetween(budgetStartDate, inputDate) % 7;
                int adjustment = (weeks % 2 == 0) ? 0 : 7;
                if (weeks < 0 || days < 0) adjustment = (weeks % 2 == 0) ? 7 : 0;
                return CalExt.subtractDays(inputDate, (weeks >= 0 && days >= 0) ? adjustment + days : days + adjustment + 6);
            }
            case Enums.kBudgetPeriod4Weeks -> {
                int weeks = CalExt.weeksBetween(budgetStartDate, inputDate);
                int days = CalExt.daysBetween(budgetStartDate, inputDate) % 7;
                int adjustment = (weeks >= 0 && days >= 0) ? (weeks % 4) * 7 : (3 - Math.abs(weeks % 4)) * 7 + 6;
                return CalExt.subtractDays(inputDate, days + adjustment);
            }
            case Enums.kBudgetPeriodMonth -> {
                if (inputDate.get(Calendar.DAY_OF_MONTH) < budgetStartDate.get(Calendar.DAY_OF_MONTH)) clonedInputDate = CalExt.subtractMonth(clonedInputDate);
                if (endOfMonth) return CalExt.endOfMonth(clonedInputDate);
                clonedInputDate.set(Calendar.DAY_OF_MONTH, Math.min(CalExt.endOfMonth(clonedInputDate).get(Calendar.DAY_OF_MONTH), budgetStartDate.get(Calendar.DAY_OF_MONTH)));
                return clonedInputDate;
            }
            case Enums.kBudgetPeriodBimonthly -> {
                int monthsDiff = (inputDate.get(Calendar.YEAR) - budgetStartDate.get(Calendar.YEAR)) * 12 + inputDate.get(Calendar.MONTH) - budgetStartDate.get(Calendar.MONTH);
                if (monthsDiff % 2 != 0 || inputDate.get(Calendar.DAY_OF_MONTH) < budgetStartDate.get(Calendar.DAY_OF_MONTH)) {
                    int adj = (monthsDiff < 0) ? 2 - Math.abs(monthsDiff % 2) : Math.abs(monthsDiff % 2);
                    if (inputDate.get(Calendar.DAY_OF_MONTH) < budgetStartDate.get(Calendar.DAY_OF_MONTH) && monthsDiff % 2 == 0) adj = 2;
                    clonedInputDate = CalExt.subtractMonths(inputDate, adj);
                }
                if (endOfMonth) return CalExt.endOfMonth(clonedInputDate);
                clonedInputDate.set(Calendar.DAY_OF_MONTH, Math.min(CalExt.endOfMonth(clonedInputDate).get(Calendar.DAY_OF_MONTH), budgetStartDate.get(Calendar.DAY_OF_MONTH)));
                return clonedInputDate;
            }
            case Enums.kBudgetPeriodQuarter -> {
                int monthsDiff = (inputDate.get(Calendar.YEAR) - budgetStartDate.get(Calendar.YEAR)) * 12 + inputDate.get(Calendar.MONTH) - budgetStartDate.get(Calendar.MONTH);
                int adj = (monthsDiff % 3 == 0 && inputDate.get(Calendar.DAY_OF_MONTH) < budgetStartDate.get(Calendar.DAY_OF_MONTH)) ? 3 : (monthsDiff < 0) ? 3 - Math.abs(monthsDiff % 3) : Math.abs(monthsDiff % 3);
                clonedInputDate = CalExt.subtractMonths(inputDate, adj);
                if (endOfMonth) return CalExt.endOfMonth(clonedInputDate);
                clonedInputDate.set(Calendar.DAY_OF_MONTH, Math.min(CalExt.endOfMonth(clonedInputDate).get(Calendar.DAY_OF_MONTH), budgetStartDate.get(Calendar.DAY_OF_MONTH)));
                return clonedInputDate;
            }
            case Enums.kBudgetPeriodHalfYear -> {
                int monthsDiff = (inputDate.get(Calendar.YEAR) - budgetStartDate.get(Calendar.YEAR)) * 12 + inputDate.get(Calendar.MONTH) - budgetStartDate.get(Calendar.MONTH);
                int adj = (monthsDiff % 6 == 0 && inputDate.get(Calendar.DAY_OF_MONTH) < budgetStartDate.get(Calendar.DAY_OF_MONTH)) ? 6 : (monthsDiff < 0) ? 6 - Math.abs(monthsDiff % 6) : Math.abs(monthsDiff % 6);
                clonedInputDate = CalExt.subtractMonths(inputDate, adj);
                if (endOfMonth) return CalExt.endOfMonth(clonedInputDate);
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
        String budgetStartDateString = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
        GregorianCalendar budgetStartDate = CalExt.dateFromDescriptionWithMediumDate(budgetStartDateString);
        boolean firstOfMonth = budgetStartDateString.equalsIgnoreCase(context.getString(R.string.GENERAL_DEFAULT)) || budgetStartDate.get(Calendar.DAY_OF_MONTH) == 1;
        boolean endOfMonth = !budgetStartDateString.equalsIgnoreCase(context.getString(R.string.GENERAL_DEFAULT)) && budgetStartDate.get(Calendar.DAY_OF_MONTH) == CalExt.endOfMonth(budgetStartDate).get(Calendar.DAY_OF_MONTH);

        return switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay -> CalExt.endOfDay(this.currentDate);
            case Enums.kBudgetPeriodWeek -> CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 1));
            case Enums.kBudgetPeriodBiweekly -> CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 2));
            case Enums.kBudgetPeriod4Weeks -> CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(startOfPeriod()), 4));
            case Enums.kBudgetPeriodMonth -> {
                if (firstOfMonth) yield CalExt.endOfMonth(this.currentDate);
                GregorianCalendar end = CalExt.addMonth(CalExt.subtractDay(startOfPeriod()));
                yield endOfMonth ? CalExt.endOfMonth(end) : end;
            }
            case Enums.kBudgetPeriodBimonthly -> {
                if (firstOfMonth) yield CalExt.endOfMonth(CalExt.addMonth(startOfPeriod()));
                GregorianCalendar end = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 2);
                yield endOfMonth ? CalExt.endOfMonth(end) : end;
            }
            case Enums.kBudgetPeriodQuarter -> {
                if (firstOfMonth) yield CalExt.endOfMonth(CalExt.addMonths(startOfPeriod(), 2));
                GregorianCalendar end = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 3);
                yield endOfMonth ? CalExt.endOfMonth(end) : end;
            }
            case Enums.kBudgetPeriodHalfYear -> {
                if (firstOfMonth) yield CalExt.endOfMonth(CalExt.addMonths(startOfPeriod(), 5));
                GregorianCalendar end = CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 6);
                yield endOfMonth ? CalExt.endOfMonth(end) : end;
            }
            case Enums.kBudgetPeriodYear -> CalExt.endOfDay(CalExt.addYear(CalExt.subtractDay(startOfPeriod())));
            default -> this.currentDate;
        };
    }

    void nextPeriod() {
        switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay -> this.currentDate = CalExt.addDays(this.currentDate, 1);
            case Enums.kBudgetPeriodWeek -> this.currentDate = CalExt.addWeeks(this.currentDate, 1);
            case Enums.kBudgetPeriodBiweekly -> this.currentDate = CalExt.addWeeks(this.currentDate, 2);
            case Enums.kBudgetPeriod4Weeks -> this.currentDate = CalExt.addWeeks(this.currentDate, 4);
            case Enums.kBudgetPeriodMonth -> this.currentDate = CalExt.addMonths(this.currentDate, 1);
            case Enums.kBudgetPeriodBimonthly -> this.currentDate = CalExt.addMonths(this.currentDate, 2);
            case Enums.kBudgetPeriodQuarter -> this.currentDate = CalExt.addMonths(this.currentDate, 3);
            case Enums.kBudgetPeriodHalfYear -> this.currentDate = CalExt.addMonths(this.currentDate, 6);
            case Enums.kBudgetPeriodYear -> this.currentDate = CalExt.addYear(this.currentDate);
        }
    }

    void previousPeriod() {
        switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay -> this.currentDate = CalExt.addDays(this.currentDate, -1);
            case Enums.kBudgetPeriodWeek -> this.currentDate = CalExt.addWeeks(this.currentDate, -1);
            case Enums.kBudgetPeriodBiweekly -> this.currentDate = CalExt.addWeeks(this.currentDate, -2);
            case Enums.kBudgetPeriod4Weeks -> this.currentDate = CalExt.addWeeks(this.currentDate, -4);
            case Enums.kBudgetPeriodMonth -> this.currentDate = CalExt.addMonths(this.currentDate, -1);
            case Enums.kBudgetPeriodBimonthly -> this.currentDate = CalExt.addMonths(this.currentDate, -2);
            case Enums.kBudgetPeriodQuarter -> this.currentDate = CalExt.addMonths(this.currentDate, -3);
            case Enums.kBudgetPeriodHalfYear -> this.currentDate = CalExt.addMonths(this.currentDate, -6);
            case Enums.kBudgetPeriodYear -> this.currentDate = CalExt.subtractYear(this.currentDate);
        }
    }

    boolean isCurrentPeriod() {
        GregorianCalendar today = new GregorianCalendar();
        return !today.before(startOfPeriod()) && !today.after(endOfPeriod());
    }

    double getProgressPercent() {
        GregorianCalendar today = new GregorianCalendar();
        if (startOfPeriod().after(today)) return 0.0;
        if (endOfPeriod().before(today)) return 1.0;
        long start = startOfPeriod().getTimeInMillis();
        long end = endOfPeriod().getTimeInMillis();
        if (end <= start) return 0.0;
        return ((double) (today.getTimeInMillis() - start)) / ((double) (end - start));
    }

    String getFormattedPeriodRange() {
        GregorianCalendar start = startOfPeriod();
        GregorianCalendar end = endOfPeriod();

        if (this.currentPeriod == Enums.kBudgetPeriodDay) {
            return CalExt.descriptionWithMediumDate(this.currentDate);
        }

        String budgetStartDateString = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
        boolean isDefaultAnchor = budgetStartDateString.equalsIgnoreCase(context.getString(R.string.GENERAL_DEFAULT));

        // Format clean titles when aligned with calendar units
        if (isDefaultAnchor || (start.get(Calendar.DAY_OF_MONTH) == 1)) {
            if (this.currentPeriod == Enums.kBudgetPeriodMonth) {
                return new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(start.getTime());
            } else if (this.currentPeriod == Enums.kBudgetPeriodYear) {
                return String.valueOf(start.get(Calendar.YEAR));
            } else if (this.currentPeriod == Enums.kBudgetPeriodQuarter) {
                int quarter = (start.get(Calendar.MONTH) / 3) + 1;
                return "Q" + quarter + " " + start.get(Calendar.YEAR);
            }
        }

        return CalExt.descriptionWithMediumDate(start) + " – " + CalExt.descriptionWithMediumDate(end);
    }

    String getFormattedAnchorSummary() {
        String freq = switch (this.currentPeriod) {
            case Enums.kBudgetPeriodDay -> context.getString(R.string.REPEATING_FREQUENCY_DAILY);
            case Enums.kBudgetPeriodWeek -> context.getString(R.string.REPEATING_FREQUENCY_WEEKLY);
            case Enums.kBudgetPeriodBiweekly -> context.getString(R.string.BUDGETS_BIWEEKLY);
            case Enums.kBudgetPeriod4Weeks -> context.getString(R.string.BUDGETS_4WEEKS);
            case Enums.kBudgetPeriodMonth -> context.getString(R.string.REPEATING_FREQUENCY_MONTHLY);
            case Enums.kBudgetPeriodBimonthly -> context.getString(R.string.BUDGETS_BIMONTHLY);
            case Enums.kBudgetPeriodQuarter -> context.getString(R.string.REPEATING_FREQUENCY_QUARTERLY);
            case Enums.kBudgetPeriodHalfYear -> context.getString(R.string.BUDGETS_HALFYEAR);
            case Enums.kBudgetPeriodYear -> context.getString(R.string.REPEATING_FREQUENCY_YEARLY);
            default -> "";
        };

        String budgetStartDateString = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
        boolean isDefault = budgetStartDateString.isEmpty() || budgetStartDateString.equalsIgnoreCase(context.getString(R.string.GENERAL_DEFAULT));

        if (isDefault) {
            String defaultLabel = switch (this.currentPeriod) {
                case Enums.kBudgetPeriodMonth -> context.getString(R.string.BUDGET_ANCHOR_CALENDAR_MONTH);
                case Enums.kBudgetPeriodQuarter -> context.getString(R.string.BUDGET_ANCHOR_CALENDAR_QUARTER);
                case Enums.kBudgetPeriodHalfYear, Enums.kBudgetPeriodYear -> context.getString(R.string.BUDGET_ANCHOR_CALENDAR_YEAR);
                default -> context.getString(R.string.BUDGET_ANCHOR_CALENDAR_DEFAULT);
            };
            return freq + " • " + defaultLabel;
        }

        GregorianCalendar date = CalExt.dateFromDescriptionWithMediumDate(budgetStartDateString);
        if (this.currentPeriod == Enums.kBudgetPeriodMonth) {
            int day = date.get(Calendar.DAY_OF_MONTH);
            String suffix = getDayOfMonthSuffix(day);
            return freq + " • " + context.getString(R.string.BUDGET_ANCHOR_SUMMARY_MONTHLY_CUSTOM, day, suffix);
        } else if (this.currentPeriod == Enums.kBudgetPeriodWeek || this.currentPeriod == Enums.kBudgetPeriodBiweekly || this.currentPeriod == Enums.kBudgetPeriod4Weeks) {
            String dayName = new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(date.getTime());
            return freq + " • " + context.getString(R.string.BUDGET_ANCHOR_SUMMARY_WEEKLY_CUSTOM, dayName);
        } else {
            String shortDate = new java.text.SimpleDateFormat("d MMM", java.util.Locale.getDefault()).format(date.getTime());
            return freq + " • " + context.getString(R.string.BUDGET_ANCHOR_SUMMARY_CUSTOM_DATE, shortDate);
        }
    }

    private String getDayOfMonthSuffix(int n) {
        if (n >= 11 && n <= 13) return "th";
        return switch (n % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    CharSequence rangeOfPeriodAsString() {
        return getFormattedPeriodRange() + "\n" + getFormattedAnchorSummary();
    }
}
