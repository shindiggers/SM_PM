package com.example.smmoney.views.accounts;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PMGlobal;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.CheckBoxTint;
import com.example.smmoney.views.budgets.BudgetsHeaderHolder;
import com.example.smmoney.views.filters.FiltersMainActivity;
import com.example.smmoney.views.repeating.RepeatingActivity;
import com.example.smmoney.views.transactions.TransactionEditActivity;
import com.example.smmoney.views.transactions.TransactionsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private ArrayList<AccountClass> elements = new ArrayList<>();
    private final List<AccountListItem> items = new ArrayList<>();
    private ArrayList<ArrayList<AccountClass>> sectionedAccounts = new ArrayList<>();
    
    private final String[] sectionedAccountStrings = new String[]{
            Locales.kLOC_ACCOUNTSECTION_BANKS, 
            Locales.kLOC_ACCOUNTSECTION_CASH, 
            Locales.kLOC_ACCOUNTSECTION_CREDIT, 
            Locales.kLOC_ACCOUNTSECTION_ASSETS, 
            Locales.kLOC_ACCOUNTSECTION_LIABILITIES, 
            Locales.kLOC_ACCOUNTSECTION_ONLINE, 
            Locales.kLOC_FILTER_DATES_CUSTOM
    };
    
    private final String[] showSectionedAccountsPrefs = new String[]{
            Prefs.COLLAPSE_BANKS, 
            Prefs.COLLAPSE_CASH, 
            Prefs.COLLAPSE_CREDITCARDS, 
            Prefs.COLLAPSE_ASSETS, 
            Prefs.COLLAPSE_LIABILITIES, 
            Prefs.COLLAPSE_ONLINE, 
            Prefs.COLLAPSE_CUSTOM
    };

    public AccountRecyclerViewAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setElements(ArrayList<AccountClass> aList) {
        this.elements = aList;
        if (isSectioned()) {
            setupSections();
        }
        calculateItems();
    }

    public ArrayList<AccountClass> getElements() {
        return this.elements;
    }

    public AccountListItem getItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    private boolean isSectioned() {
        return Prefs.getBooleanPref(Prefs.GROUPBYACCOUNTTYPE);
    }

    private void setupSections() {
        this.sectionedAccounts = new ArrayList<>();
        for (int i = 0; i < 7; i++) { // NUMBER_OF_ACCOUNT_GROUPS
            this.sectionedAccounts.add(new ArrayList<>());
        }
        for (AccountClass act : this.elements) {
            this.sectionedAccounts.get(indexForType(act.getType())).add(act);
        }
    }

    private int indexForType(int type) {
        return switch (type) {
            case 1 -> 1; // Cash
            case 2, 8 -> 2; // Credit
            case 3, 9 -> 3; // Assets
            case 4 -> 4; // Liabilities
            case 5 -> 5; // Online
            default -> 0; // Checking
        };
    }

    public int typeForIndex(int sectionIndex) {
        return switch (sectionIndex) {
            case 1 -> 1; // Cash
            case 2 -> 2; // Credit
            case 3 -> 3; // Assets
            case 4 -> 4; // Liabilities
            case 5 -> 5; // Online
            default -> 0; // Checking
        };
    }

    public int getSectionIndexForPosition(int position) {
        if (position < 0 || position >= items.size()) return -1;
        
        // Walk backwards from position to find the nearest header
        for (int i = position; i >= 0; i--) {
            if (items.get(i).type == AccountListItem.TYPE_HEADER) {
                return items.get(i).sectionIndex;
            }
        }
        
        // If we didn't find one by walking backwards (rare), walk forwards
        for (int i = position; i < items.size(); i++) {
            if (items.get(i).type == AccountListItem.TYPE_HEADER) {
                return items.get(i).sectionIndex;
            }
        }
        return -1;
    }

    private void calculateItems() {
        items.clear();
        if (isSectioned()) {
            for (int i = 0; i < sectionedAccounts.size(); i++) {
                ArrayList<AccountClass> section = sectionedAccounts.get(i);
                if (!section.isEmpty() || i == sectionedAccounts.size() - 1) {
                    items.add(AccountListItem.createHeader(sectionedAccountStrings[i], i));
                    if (getShowSection(i)) {
                        for (AccountClass account : section) {
                            items.add(AccountListItem.createAccount(account));
                        }
                    }
                }
            }
        } else {
            for (AccountClass account : elements) {
                items.add(AccountListItem.createAccount(account));
            }
        }

        // Custom rows
        if (Prefs.getBooleanPref(Prefs.ALLTRANSACTIONS)) {
            items.add(AccountListItem.createCustom(Locales.kLOC_ALL_TRANSACTIONS));
        }
        if (Prefs.getBooleanPref(Prefs.FILTERS)) {
            items.add(AccountListItem.createCustom(Locales.kLOC_TOOLS_FILTERS));
        }
        if (Prefs.getBooleanPref(Prefs.REPEATINGTRANSACTIONS)) {
            items.add(AccountListItem.createCustom(Locales.kLOC_REPEATING_TRANSACTIONS));
        }
        notifyDataSetChanged();
    }

    private boolean getShowSection(int section) {
        return Prefs.getBooleanPref(this.showSectionedAccountsPrefs[section]);
    }

    private void setShowSection(int section, boolean show) {
        Prefs.setPref(this.showSectionedAccountsPrefs[section], show);
        calculateItems();
    }

    private String balanceForSection(int sectionIndex) {
        int balanceType = Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED) ? Prefs.getIntPref(Prefs.BALANCETYPE) : Prefs.getIntPref(Prefs.BALANCEBARREGISTER);
        double total = 0.0;
        if (this.sectionedAccounts != null && sectionIndex < this.sectionedAccounts.size()) {
            for (AccountClass act : this.sectionedAccounts.get(sectionIndex)) {
                if (act.getTotalWorth()) {
                    total += act.balanceOfType(balanceType);
                }
            }
        }
        return CurrencyExt.amountAsCurrency(total);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case AccountListItem.TYPE_HEADER:
                BudgetsHeaderHolder header = new BudgetsHeaderHolder(mContext, "", "");
                return new HeaderViewHolder(header);
            case AccountListItem.TYPE_CUSTOM:
                View customView = mInflater.inflate(R.layout.simple_text_view, parent, false);
                return new CustomViewHolder(customView);
            default:
                View accountView = mInflater.inflate(R.layout.accounts_row, parent, false);
                return new AccountViewHolder(accountView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AccountListItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder h = (HeaderViewHolder) holder;
            String balance = balanceForSection(item.sectionIndex);
            h.headerView.setData(item.label, balance);
            h.headerView.setExpanded(getShowSection(item.sectionIndex));
            h.headerView.setOnClickListener(v -> setShowSection(item.sectionIndex, !getShowSection(item.sectionIndex)));
        } else if (holder instanceof AccountViewHolder) {
            AccountViewHolder h = (AccountViewHolder) holder;
            h.bind(item.account);
        } else if (holder instanceof CustomViewHolder) {
            CustomViewHolder h = (CustomViewHolder) holder;
            h.bind(item.label);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void onDragFinished() {
        // Persist the new order to the database
        int order = 0;
        for (AccountListItem item : items) {
            if (item.type == AccountListItem.TYPE_ACCOUNT) {
                item.account.hydrate();
                // Set the displayOrder based on the current position in the flattened list
                item.account.setDisplayOrder(order++); 
                item.account.saveToDatabase();
            }
        }
        // No need to reload everything, but might need to update sectionedAccounts if grouped
        if (isSectioned()) {
             // If sectioned, we need to rebuild the sections from the flattened list
             // or just reload from DB which is safer
             setElements(AccountDB.queryOnViewType(Prefs.getIntPref(Prefs.VIEWACCOUNTS)));
        }
    }
    
    // Helper to check if a position is draggable
    public boolean canDrag(int position) {
        if (position < 0 || position >= items.size()) return false;
        return items.get(position).type == AccountListItem.TYPE_ACCOUNT;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        BudgetsHeaderHolder headerView;
        HeaderViewHolder(BudgetsHeaderHolder view) {
            super(view);
            headerView = view;
        }
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        AccountClass account;
        View therow;
        TextView accountname;
        TextView totalworth;
        TextView exchangeRate;
        ImageView icon_image;
        ImageView newtransbutton;
        AppCompatCheckBox selected;

        AccountViewHolder(View v) {
            super(v);
            therow = v.findViewById(R.id.therow);
            accountname = v.findViewById(R.id.account_name);
            totalworth = v.findViewById(R.id.total_worth);
            exchangeRate = v.findViewById(R.id.exchange_rate);
            icon_image = v.findViewById(R.id.icon_image);
            newtransbutton = v.findViewById(R.id.accountsnewtransbutton);
            selected = v.findViewById(R.id.selected);
            
            therow.setOnClickListener(v1 -> {
                Intent i = new Intent(mContext, TransactionsActivity.class);
                FilterClass aFilter = new FilterClass(account.getAccount());
                aFilter.setFilterName(account.getAccount());
                i.putExtra("Filter", aFilter);
                mContext.startActivity(i);
            });

            selected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!PMGlobal.programaticUpdate) {
                    account.setTotalWorth(isChecked);
                    account.saveToDatabase();
                    if (mContext instanceof AccountsActivity) {
                        ((AccountsActivity) mContext).clearBalanceCache();
                        ((AccountsActivity) mContext).reloadBalanceBar();
                        ((AccountsActivity) mContext).reloadCharts();
                    }
                    notifyDataSetChanged();
                }
            });

            newtransbutton.setOnClickListener(v1 -> {
                Intent i = new Intent(mContext, TransactionEditActivity.class);
                TransactionClass trans = new TransactionClass();
                trans.setAccount(account.getAccount());
                trans.setCurrencyCode(account.getCurrencyCode());
                trans.getSplits().get(0).dirty = false;
                trans.dirty = false;
                i.putExtra("Transaction", trans);
                if (mContext instanceof AccountsActivity) {
                    ((AccountsActivity) mContext).editLauncher.launch(i);
                } else {
                    mContext.startActivity(i);
                }
            });
        }

        void bind(AccountClass act) {
            this.account = act;
            itemView.setTag(this); // Store ViewHolder in tag for context menu
            accountname.setText(account.getAccount());
            accountname.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            icon_image.setImageResource(account.getIconFileNameResourceIDUsingContext(mContext));
            
            PMGlobal.programaticUpdate = true;
            selected.setChecked(account.getTotalWorth());
            PMGlobal.programaticUpdate = false;
            
            CheckBoxTint.colorCheckBox(selected);
            updateBalanceLabel();
            
            boolean multi = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
            exchangeRate.setVisibility((multi && account.getExchangeRate() != 1.0d) ? View.VISIBLE : View.GONE);
            if (exchangeRate.getVisibility() == View.VISIBLE) {
                exchangeRate.setText(CurrencyExt.exchangeRateAsString(account.getExchangeRate()));
            }
            exchangeRate.setTextColor(PocketMoneyThemes.alternateCellTextColor());
            newtransbutton.setVisibility(View.VISIBLE);
            newtransbutton.setColorFilter(PocketMoneyThemes.currentTintColor());
            
            if (getAdapterPosition() % 2 == 0) {
                therow.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            } else {
                therow.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            }
        }

        private void updateBalanceLabel() {
            int balanceType = Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED) ? Prefs.getIntPref(Prefs.BALANCETYPE) : Prefs.getIntPref(Prefs.BALANCEBARREGISTER);
            double balance = account.balanceOfType(balanceType);
            totalworth.setText(account.formatAmountAsCurrency(balance));
            if (account.balanceExceedsLimit()) {
                totalworth.setTextColor(PocketMoneyThemes.redLabelColor());
            } else if (balance < 0.0d) {
                totalworth.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            } else {
                totalworth.setTextColor(PocketMoneyThemes.greenDepositColor());
            }
        }
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        CustomViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.text);
            textView.setGravity(17);
        }

        void bind(String label) {
            textView.setText(label);
            textView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            itemView.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            itemView.setOnClickListener(v -> {
                if (label.equals(Locales.kLOC_REPEATING_TRANSACTIONS)) {
                    mContext.startActivity(new Intent(mContext, RepeatingActivity.class));
                } else if (label.equals(Locales.kLOC_ALL_TRANSACTIONS)) {
                    Intent i = new Intent(mContext, TransactionsActivity.class);
                    FilterClass f = new FilterClass();
                    f.setFilterName(Locales.kLOC_ALL_TRANSACTIONS);
                    f.setAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
                    i.putExtra("Filter", f);
                    mContext.startActivity(i);
                } else if (label.equals(Locales.kLOC_TOOLS_FILTERS)) {
                    Intent i = new Intent(mContext, FiltersMainActivity.class);
                    FilterClass f = new FilterClass();
                    f.setAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
                    i.putExtra("Filter", f);
                    i.putExtra("ONLY SAVED", 1);
                    if (mContext instanceof AccountsActivity) {
                        ((AccountsActivity) mContext).filterLauncher.launch(i);
                    }
                }
            });
        }
    }
}
