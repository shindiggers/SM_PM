package com.catamount.pocketmoney.views.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.database.AccountDB;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PMGlobal;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.records.SplitsClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.budgets.BudgetsHeaderHolder;
import com.catamount.pocketmoney.views.filters.FiltersMainActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.repeating.RepeatingActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;
import com.catamount.pocketmoney.views.transactions.TransactionEditActivity;
import com.catamount.pocketmoney.views.transactions.TransactionsActivity;
import java.util.ArrayList;
import java.util.List;

public class AccountRowAdapter extends BaseAdapter {
    private int NUMBER_OF_ACCOUNT_GROUPS = 7;
    private View allTransView;
    private Object customRowTag = new Object();
    private List<AccountClass> elements;
    private View filtersView;
    private OnClickListener headerClickListener = new OnClickListener() {
        public void onClick(View view) {
            String cat = ((BudgetsHeaderHolder) view).label;
            for (int i = 0; i < AccountRowAdapter.this.sectionedAccountStrings.length; i++) {
                if (cat.equals(AccountRowAdapter.this.sectionedAccountStrings[i])) {
                    AccountRowAdapter.this.setShowSection(i, !AccountRowAdapter.this.getShowSection(i));
                    AccountRowAdapter.this.notifyDataSetChanged();
                }
            }
        }
    };
    private Context mContext;
    private LayoutInflater mInflater;
    private LinearLayout previousRowLayout = null;
    private View repeatingView;
    private String[] sectionedAccountStrings = new String[]{Locales.kLOC_ACCOUNTSECTION_BANKS, Locales.kLOC_ACCOUNTSECTION_CASH, Locales.kLOC_ACCOUNTSECTION_CREDIT, Locales.kLOC_ACCOUNTSECTION_ASSETS, Locales.kLOC_ACCOUNTSECTION_LIABILITIES, Locales.kLOC_ACCOUNTSECTION_ONLINE, Locales.kLOC_FILTER_DATES_CUSTOM};
    private ArrayList<ArrayList<AccountClass>> sectionedAccounts;
    private String[] showSectionedAccountsPrefs = new String[]{Prefs.COLLAPSE_BANKS, Prefs.COLLAPSE_CASH, Prefs.COLLAPSE_CREDITCARDS, Prefs.COLLAPSE_ASSETS, Prefs.COLLAPSE_LIABILITIES, Prefs.COLLAPSE_ONLINE, Prefs.COLLAPSE_CUSTOM};
    private float textSize = 0.0f;

    public AccountRowAdapter(Context aContext) {
        this.mContext = aContext;
        this.elements = new ArrayList();
        this.mInflater = LayoutInflater.from(this.mContext);
        this.sectionedAccounts = new ArrayList();
    }

    public void setElements(List<AccountClass> aList) {
        this.elements.clear();
        notifyDataSetChanged();
        this.elements = aList;
        if (isSectioned()) {
            setupSections();
        }
    }

    public ArrayList<AccountClass> getElements() {
        return (ArrayList) this.elements;
    }

    public int getCount() {
        int count = 0;
        int customButtonCount = 0;
        if (Prefs.getBooleanPref(Prefs.ALLTRANSACTIONS)) {
            customButtonCount = 0 + 1;
        }
        if (Prefs.getBooleanPref(Prefs.FILTERS)) {
            customButtonCount++;
        }
        if (Prefs.getBooleanPref(Prefs.REPEATINGTRANSACTIONS)) {
            customButtonCount++;
        }
        if (isSectioned()) {
            if (customButtonCount > 0) {
                customButtonCount++;
                if (!getShowSection(this.showSectionedAccountsPrefs.length - 1)) {
                    customButtonCount = 1;
                }
            }
            for (int i = 0; i < this.sectionedAccounts.size(); i++) {
                ArrayList<AccountClass> section = this.sectionedAccounts.get(i);
                int size = section.isEmpty() ? 0 : getShowSection(i) ? section.size() + 1 : 1;
                count += size;
            }
        } else {
            count = 0 + this.elements.size();
        }
        if (count == 0) {
            return 0;
        }
        return count + customButtonCount;
    }

    public Object getItem(int position) {
        if (isSectioned()) {
            int currentIndex = 0;
            int i = 0;
            while (i < this.sectionedAccounts.size()) {
                if (currentIndex == position) {
                    if (this.sectionedAccounts.get(i).size() > 0 || i == this.sectionedAccounts.size() - 1) {
                        return new StringBuilder(String.valueOf('H')).append(this.sectionedAccountStrings[i]).toString();
                    }
                } else if (this.sectionedAccounts.get(i).size() + currentIndex >= position) {
                    if (getShowSection(i)) {
                        return ((ArrayList) this.sectionedAccounts.get(i)).get((position - currentIndex) - 1);
                    }
                    currentIndex++;
                } else if (getShowSection(i)) {
                    currentIndex += this.sectionedAccounts.get(i).size() + 1;
                } else if (this.sectionedAccounts.get(i).size() > 0 || i == this.sectionedAccounts.size() - 1) {
                    currentIndex++;
                }
                i++;
            }
            int newPos = position - currentIndex;
            if (newPos == 0) {
                if (Prefs.getBooleanPref(Prefs.ALLTRANSACTIONS)) {
                    return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_ALL_TRANSACTIONS).toString();
                }
                if (Prefs.getBooleanPref(Prefs.FILTERS)) {
                    return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_TOOLS_FILTERS).toString();
                }
                return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_REPEATING_TRANSACTIONS).toString();
            } else if (newPos != 1) {
                return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_REPEATING_TRANSACTIONS).toString();
            } else {
                if (Prefs.getBooleanPref(Prefs.FILTERS) && Prefs.getBooleanPref(Prefs.ALLTRANSACTIONS)) {
                    return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_TOOLS_FILTERS).toString();
                }
                if (Prefs.getBooleanPref(Prefs.REPEATINGTRANSACTIONS)) {
                    return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_REPEATING_TRANSACTIONS).toString();
                }
                return null;
            }
        }
        if (position == this.elements.size()) {
            if (Prefs.getBooleanPref(Prefs.ALLTRANSACTIONS)) {
                return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_ALL_TRANSACTIONS).toString();
            }
            if (Prefs.getBooleanPref(Prefs.FILTERS)) {
                return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_TOOLS_FILTERS).toString();
            }
            if (Prefs.getBooleanPref(Prefs.REPEATINGTRANSACTIONS)) {
                return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_REPEATING_TRANSACTIONS).toString();
            }
        } else if (position == this.elements.size() + 1) {
            if (Prefs.getBooleanPref(Prefs.FILTERS) && Prefs.getBooleanPref(Prefs.ALLTRANSACTIONS)) {
                return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_TOOLS_FILTERS).toString();
            }
            if (Prefs.getBooleanPref(Prefs.REPEATINGTRANSACTIONS)) {
                return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_REPEATING_TRANSACTIONS).toString();
            }
        } else if (position == this.elements.size() + 2) {
            return new StringBuilder(String.valueOf('C')).append(Locales.kLOC_REPEATING_TRANSACTIONS).toString();
        }
        return this.elements.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        AccountRowHolder holder;
        Object obj = getItem(position);
        if (obj.getClass() == String.class) {
            if (((String) obj).charAt(0) == 'H') {
                BudgetsHeaderHolder header = new BudgetsHeaderHolder(this.mContext, ((String) obj).substring(1), "");
                header.setTag(null);
                header.setOnClickListener(this.headerClickListener);
                return header;
            }
            String custom = ((String) obj).substring(1);
            if (custom.equals(Locales.kLOC_REPEATING_TRANSACTIONS)) {
                return getRepeatingTransView(position);
            }
            if (custom.equals(Locales.kLOC_TOOLS_FILTERS)) {
                return getFilterView(position);
            }
            if (custom.equals(Locales.kLOC_ALL_TRANSACTIONS)) {
                return getAllTransView(position);
            }
        }
        if (convertView == null || convertView.getTag() == null) {
            convertView = this.mInflater.inflate(R.layout.accounts_row, null);
            holder = new AccountRowHolder();
            holder.therow = convertView.findViewById(R.id.therow);
            ((Activity) this.mContext).registerForContextMenu(holder.therow);
            holder.therow.setOnClickListener(getBtnClickListener());
            holder.exchangeRate = convertView.findViewById(R.id.exchange_rate);
            holder.accountname = convertView.findViewById(R.id.account_name);
            holder.totalworth = convertView.findViewById(R.id.total_worth);
            holder.icon_image = convertView.findViewById(R.id.icon_image);
            holder.selected = convertView.findViewById(R.id.selected);
            holder.selected.setOnCheckedChangeListener(getCheckListener());
            holder.newtransbutton = convertView.findViewById(R.id.accountsnewtransbutton);
            holder.newtransbutton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AccountRowHolder holder = (AccountRowHolder) ((View) v.getParent().getParent()).getTag();
                    Intent i = new Intent(AccountRowAdapter.this.mContext, TransactionEditActivity.class);
                    TransactionClass trans = new TransactionClass();
                    AccountClass a1 = AccountDB.recordFor((String) holder.accountname.getText());
                    trans.setAccount(a1.getAccount());
                    trans.setCurrencyCode(a1.getCurrencyCode());
                    trans.getSplits().get(0).dirty = false;
                    trans.dirty = false;
                    i.putExtra("Transaction", trans);
                    AccountRowAdapter.this.mContext.startActivity(i);
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (AccountRowHolder) convertView.getTag();
        }
        if (position % 2 == 0) {
            holder.therow.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        } else {
            holder.therow.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        }
        holder.setAccount((AccountClass) obj, this.mContext);
        if (this.textSize == 0.0f) {
            this.textSize = holder.accountname.getTextSize();
        }
        return convertView;
    }

    private int indexForType(int type) {
        switch (type) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return 1;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                return 2;
            case SplitsActivity.REQUEST_EDIT /*3*/:
            case LookupsListActivity.FILTER_ACCOUNTS /*9*/:
                return 3;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                return 4;
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                return 5;
            default:
                return 0;
        }
    }

    private void setupSections() {
        this.sectionedAccounts = new ArrayList();
        for (int i = 0; i < this.NUMBER_OF_ACCOUNT_GROUPS; i++) {
            this.sectionedAccounts.add(new ArrayList());
        }
        for (AccountClass act : this.elements) {
            this.sectionedAccounts.get(indexForType(act.getType())).add(act);
        }
    }

    private OnCheckedChangeListener getCheckListener() {
        return new OnCheckedChangeListener() {
            final Context cxt;

            {
                this.cxt = AccountRowAdapter.this.mContext;
            }

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!PMGlobal.programaticUpdate) {
                    AccountRowHolder holder = (AccountRowHolder) ((View) buttonView.getParent()).getTag();
                    holder.account.setTotalWorth(isChecked);
                    holder.account.saveToDatabase();
                    ((AccountsActivity) AccountRowAdapter.this.mContext).clearBalanceCache();
                    ((AccountsActivity) AccountRowAdapter.this.mContext).reloadBalanceBar();
                    ((AccountsActivity) AccountRowAdapter.this.mContext).reloadCharts();
                }
            }
        };
    }

    private boolean isSectioned() {
        return Prefs.getBooleanPref(Prefs.GROUPBYACCOUNTTYPE);
    }

    private void setShowSection(int section, boolean show) {
        Prefs.setPref(this.showSectionedAccountsPrefs[section], show);
    }

    private boolean getShowSection(int section) {
        return Prefs.getBooleanPref(this.showSectionedAccountsPrefs[section]) && this.sectionedAccounts != null && this.sectionedAccounts.size() > 0 && (this.sectionedAccounts.get(section).size() > 0 || section == this.sectionedAccounts.size() - 1);
    }

    private OnClickListener getBtnClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(AccountRowAdapter.this.mContext, TransactionsActivity.class);
                AccountRowHolder holder = (AccountRowHolder) view.getTag();
                FilterClass aFilter = new FilterClass((String) holder.accountname.getText());
                aFilter.setFilterName(holder.account.getAccount());
                i.putExtra("Filter", aFilter);
                AccountRowAdapter.this.mContext.startActivity(i);
            }
        };
    }

    private View getRepeatingTransView(int position) {
        View v = this.mInflater.inflate(R.layout.simple_text_view, null);
        v.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        TextView tv = v.findViewById(R.id.text);
        tv.setGravity(17);
        tv.setText(Locales.kLOC_REPEATING_TRANSACTIONS);
        tv.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        v.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AccountRowAdapter.this.mContext.startActivity(new Intent(AccountRowAdapter.this.mContext, RepeatingActivity.class));
            }
        });
        v.setTag(null);
        return v;
    }

    private View getAllTransView(int position) {
        View v = this.mInflater.inflate(R.layout.simple_text_view, null);
        v.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        TextView tv = v.findViewById(R.id.text);
        tv.setGravity(17);
        tv.setText(Locales.kLOC_ALL_TRANSACTIONS);
        tv.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        v.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(AccountRowAdapter.this.mContext, TransactionsActivity.class);
                FilterClass f = new FilterClass();
                f.setFilterName(Locales.kLOC_ALL_TRANSACTIONS);
                f.setAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
                i.putExtra("Filter", new FilterClass());
                AccountRowAdapter.this.mContext.startActivity(i);
            }
        });
        v.setTag(null);
        return v;
    }

    private View getFilterView(int position) {
        View v = this.mInflater.inflate(R.layout.simple_text_view, null);
        v.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
        TextView tv = v.findViewById(R.id.text);
        tv.setGravity(17);
        tv.setText(Locales.kLOC_TOOLS_FILTERS);
        tv.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        v.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(AccountRowAdapter.this.mContext, FiltersMainActivity.class);
                FilterClass f = new FilterClass();
                f.setAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
                i.putExtra("Filter", f);
                i.putExtra("ONLY SAVED", 1);
                ((AccountsActivity) AccountRowAdapter.this.mContext).startActivityForResult(i, 1);
            }
        });
        v.setTag(null);
        return v;
    }
}
