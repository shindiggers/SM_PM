package com.example.smmoney.views.reports;

import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.ColorExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.charts.ChartViewDataSource;
import com.example.smmoney.views.charts.items.ChartItem;
import com.example.smmoney.views.charts.items.ReportChartItem;
import com.example.smmoney.views.charts.views.ChartView;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;

public abstract class ReportDataSource implements ChartViewDataSource, Serializable {
    public ArrayList<ReportItem> data;
    int currentAction = 0;
    int currentPeriod;
    final FilterClass filter;
    int totalActions = 0;
    private final Comparator<ReportItem> comparator = new Comparator<ReportItem>() {
        public int compare(ReportItem o1, ReportItem o2) {
            int sortType = Prefs.getIntPref(Prefs.REPORTS_SORTON);
            double retVal = 0.0d;
            double flipIt = Prefs.getIntPref(Prefs.PREFS_REPORTS_SORTDIRECTION) == 0 ? 1.0d : -1.0d;
            switch (sortType) {
                case Enums.kReportsSortOnItem /*0*/:
                    retVal = ((double) o1.expense.compareToIgnoreCase(o2.expense)) * flipIt;
                    break;
                case Enums.kReportsSortOnAmount /*1*/:
                    retVal = (o1.amount - o2.amount) * flipIt;
                    break;
                case Enums.kReportsSortOnCount /*2*/:
                    retVal = ((double) (o1.count - o2.count)) * flipIt;
                    break;
            }
            if (retVal < 0.0d) {
                return -1;
            }
            return retVal > 0.0d ? 1 : 0;
        }
    };
    private GregorianCalendar currentDate;
    private ReportsActivity delegate;

    ReportDataSource(ArrayList<TransactionClass> theTrans, FilterClass theFilter) {
        this.filter = theFilter;
        this.currentPeriod = Prefs.getIntPref(Prefs.REPORTS_PERIOD);
        if (theTrans == null || theTrans.size() <= 0) {
            this.currentDate = new GregorianCalendar();
        } else {
            this.currentDate = theTrans.get(theTrans.size() - 1).getDate();
        }
    }

    protected abstract void generateReport();

    public abstract FilterClass newFilterBasedOnSelectedRow(String str);

    public abstract String title();

    private ArrayList<ReportItem> calculatePercentagesAndColors(ArrayList<ReportItem> array) {
        double negativeTotal = 0.0d;
        double positiveTotal = 0.0d;
        double negativeMaxValue = 0.0d;
        double positiveMaxValue = 0.0d;
        int index = 0;
        Iterator<ReportItem> it = array.iterator();
        while (it.hasNext()) {
            ReportItem item = it.next();
            if (item.checked) {
                if (item.amount < 0.0d) {
                    negativeTotal += item.amount;
                } else {
                    positiveTotal += item.amount;
                }
                negativeMaxValue = Math.min(negativeMaxValue, item.amount);
                positiveMaxValue = Math.max(positiveMaxValue, item.amount);
            }
        }
        it = array.iterator();
        while (it.hasNext()) {
            ReportItem item = it.next();
            if (item.checked) {
                if (item.amount < 0.0d) {
                    item.percent = (item.amount / negativeTotal) * 100.0d;
                } else {
                    item.percent = (item.amount / positiveTotal) * 100.0d;
                }
                item.color = ColorExt.getColorAtIndex(index);
            }
            index++;
        }
        return array;
    }

    public void reloadData() {
        if (this.data == null) {
            generateReport();
        }
        if (this.data != null) {
            Collections.sort(this.data, this.comparator);
            if (ReportsActivity.processData) {
                calculatePercentagesAndColors(this.data);
            }
        }
    }

    public void reloadData(ReportsActivity delegate) {
        this.delegate = delegate;
        reloadData();
    }

    @SuppressWarnings("unused")
    public int itemCount() {
        int count = 0;
        for (ReportItem item : this.data) {
            if (item.checked) {
                count += item.count;
            }
        }
        return count;
    }

    double expenseTotal() {
        double total = 0.0d;
        for (ReportItem item : this.data) {
            if (item.checked) {
                total += item.amount;
            }
        }
        return total;
    }

    String expenseTotalAsString() {
        AccountClass account = null;
        if (!this.filter.allAccounts()) {
            account = AccountDB.recordFor(this.filter.getAccount());
        }
        boolean mc = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        return CurrencyExt.amountAsCurrency(expenseTotal(), account != null ? account.getCurrencyCode() : Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
    }

    String rangeOfPeriodAsString() {
        switch (this.currentPeriod) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return CalExt.descriptionWithMonth(startOfPeriod()) + " " + CalExt.descriptionWithYear(endOfPeriod());
            case SplitsActivity.RESULT_CHANGED /*1*/:
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
            case SplitsActivity.REQUEST_EDIT /*3*/:
                return CalExt.descriptionWithMonth(startOfPeriod()) + " " + CalExt.descriptionWithYear(startOfPeriod()) + " - " + CalExt.descriptionWithMonth(endOfPeriod()) + " " + CalExt.descriptionWithYear(endOfPeriod());
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                return CalExt.descriptionWithYear(this.currentDate);
            default:
                return Locales.kLOC_PREFERENCES_SHOW_ALL;
        }
    }

    GregorianCalendar startOfPeriod() {
        switch (this.currentPeriod) {
            case Enums.kReportPeriodOneMonth /*0*/:
            case Enums.kReportPeriodTwoMonths /*1*/:
            case Enums.kReportPeriodThreeMonths /*2*/:
            case Enums.kReportPeriodSixMonths /*3*/:
                return CalExt.beginningOfMonth(this.currentDate);
            case Enums.kReportPeriodOneYear /*4*/:
                return CalExt.beginningOfYear(this.currentDate);
            default:
                return CalExt.distantPast();
        }
    }

    GregorianCalendar endOfPeriod() {
        switch (this.currentPeriod) {
            case Enums.kReportPeriodOneMonth /*0*/:
                return CalExt.endOfMonth(CalExt.addMonth(CalExt.subtractDay(startOfPeriod())));
            case Enums.kReportPeriodTwoMonths /*1*/:
                return CalExt.endOfMonth(CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 2));
            case Enums.kReportPeriodThreeMonths /*2*/:
                return CalExt.endOfMonth(CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 3));
            case Enums.kReportPeriodSixMonths /*3*/:
                return CalExt.endOfMonth(CalExt.addMonths(CalExt.subtractDay(startOfPeriod()), 6));
            case Enums.kReportPeriodOneYear /*4*/:
                return CalExt.endOfDay(CalExt.addYear(CalExt.subtractDay(startOfPeriod())));
            default:
                return CalExt.distantFuture();
        }
    }

    void nextPeriod() {
        switch (this.currentPeriod) {
            case Enums.kReportPeriodOneMonth /*0*/:
                this.currentDate = CalExt.addMonth(this.currentDate);
                return;
            case Enums.kReportPeriodTwoMonths /*1*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 2);
                return;
            case Enums.kReportPeriodThreeMonths /*2*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 3);
                return;
            case Enums.kReportPeriodSixMonths /*3*/:
                this.currentDate = CalExt.addMonths(this.currentDate, 6);
                return;
            case Enums.kReportPeriodOneYear /*4*/:
                this.currentDate = CalExt.addYear(this.currentDate);
                return;
            default:
        }
    }

    void previousPeriod() {
        switch (this.currentPeriod) {
            case Enums.kReportPeriodOneMonth /*0*/:
                this.currentDate = CalExt.subtractMonth(this.currentDate);
                return;
            case Enums.kReportPeriodTwoMonths /*1*/:
                this.currentDate = CalExt.subtractMonths(this.currentDate, 2);
                return;
            case Enums.kReportPeriodThreeMonths /*2*/:
                this.currentDate = CalExt.subtractMonths(this.currentDate, 3);
                return;
            case Enums.kReportPeriodSixMonths /*3*/:
                this.currentDate = CalExt.subtractMonths(this.currentDate, 6);
                return;
            case Enums.kReportPeriodOneYear /*4*/:
                this.currentDate = CalExt.subtractYear(this.currentDate);
                return;
            default:
        }
    }

    TransactionClass[] transactionsFromDateToDate(GregorianCalendar fromDate, GregorianCalendar toDate) {
        FilterClass tempFilter = this.filter.copy();
        tempFilter.setDate(Locales.kLOC_FILTER_DATES_CUSTOM);
        tempFilter.setDateFrom(fromDate);
        tempFilter.setDateTo(toDate);
        return TransactionDB.queryWithFilterToCArray(tempFilter);
    }

    void addCurrentPeriodToFilter(FilterClass modFilter) {
        String str = null;
        if (5 == this.currentPeriod) {
            modFilter.setDate(this.filter.getDate());
            modFilter.setDateFrom(this.filter.getDateFrom());
            modFilter.setDateTo(this.filter.getDateTo());
        } else {
            modFilter.setDate(Locales.kLOC_FILTER_DATES_CUSTOM);
            modFilter.setDateFrom(startOfPeriod());
            modFilter.setDateTo(endOfPeriod());
        }
        if (this.filter.getDate() != null && this.filter.getDate().length() > 0 && !this.filter.getDate().equals(Locales.kLOC_FILTER_DATES_ALL)) {
            GregorianCalendar fromDate;
            GregorianCalendar toDate;
            if (this.filter.isCustomDate()) {
                fromDate = this.filter.getDateFrom();
                toDate = this.filter.getDateTo();
            } else {
                fromDate = new GregorianCalendar();
                fromDate.setTimeInMillis(FilterClass.convertFilterDateIsFromDate(this.filter.getDate(), true));
                toDate = new GregorianCalendar();
                if (this.filter.getDate() != null) {
                    str = this.filter.getDate();
                }
                toDate.setTimeInMillis(FilterClass.convertFilterDateIsFromDate(str, false));
            }
            modFilter.setDate(Locales.kLOC_FILTER_DATES_CUSTOM);
            if (modFilter.getDateFrom() != null) {
                fromDate = laterDate(modFilter.getDateFrom(), fromDate);
            }
            modFilter.setDateFrom(fromDate);
            if (modFilter.getDateTo() != null) {
                toDate = earlierDate(modFilter.getDateTo(), toDate);
            }
            modFilter.setDateTo(toDate);
        }
    }

    private GregorianCalendar laterDate(GregorianCalendar date1, GregorianCalendar date2) {
        if (date2 != null && date1.before(date2)) {
            return date2;
        }
        return date1;
    }

    private GregorianCalendar earlierDate(GregorianCalendar date1, GregorianCalendar date2) {
        if (date2 != null && date1.after(date2)) {
            return date2;
        }
        return date1;
    }

    void updateProgress() {
        int percent = (this.currentAction * 100) / this.totalActions;
        int previousPercentSent = -1;
        if (previousPercentSent != percent && ReportsActivity.processData) {
            this.delegate.updateProgressBar(percent);
        }
    }

    @SuppressWarnings("unused")
    protected void progressFinished() {
        this.delegate.finishProgressBar();
    }

    public int numberOfDataPointsInSeries(ChartView chartView, int series) {
        int count = 0;
        for (ReportItem datum : this.data) {
            if ((datum).checked) {
                count++;
            }
        }
        return count;
    }

    public ChartItem itemForDataAtIndex(ChartView chartView, int row, int sections) {
        boolean displayCount = Prefs.getIntPref(Prefs.REPORTS_SORTON) == 2;
        int indexPos = 0;
        for (ReportItem item : this.data) {
            if (item.checked) {
                if (indexPos == row) {
                    ChartItem reportChartItem = new ReportChartItem(displayCount ? (double) item.count : item.amount, item.expense, item.color);
                    reportChartItem.reportItem = item;
                    return reportChartItem;
                }
                indexPos++;
            }
        }
        return null;
    }

    public int numberOfSeriesInChartView(ChartView chartView) {
        return 1;
    }

    public GregorianCalendar dateForRow(int row) {
        return null;
    }

    public double networthForRow(int row) {
        return 0.0d;
    }

    public void selectAllDataPointsForRow(int row) {
    }

    public int rowOfChartItem(ChartItem chartItem) {
        return 0;
    }
}
