package com.example.smmoney.views.filters;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.PocketMoneyActivity;

import java.util.ArrayList;
import java.util.Objects;

public class FiltersMainActivity extends PocketMoneyActivity {
    public static final int FILTER_EDIT_NEW = 1;
    public static final int FILTER_EDIT_OLD = 2;
    private static final int FILTER_RESULT_NOCHANGE = 2;
    private static final int FILTER_RESULT_SELECTED = 1;
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    private final int MENU_NEW = 1;
    private final int REQUEST_CURRENT_EDIT = 1;
    private FiltersMainActivity context;
    private FrameLayout currenctFilterView;
    private FilterClass filter;
    private ArrayList<FilterClass> filterList;
    private FilterRowAdapter theAdapter;
    private TextView titleTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.filter = (FilterClass) Objects.requireNonNull(getIntent().getExtras()).get("Filter");
        setContentView(R.layout.filter_main);
        this.context = this;
        setResult(FILTER_RESULT_NOCHANGE);
        if (getIntent().getExtras().get("ONLY SAVED") != null) {
            findViewById(R.id.filterreset).setVisibility(View.GONE);
            findViewById(R.id.filtercurrent).setVisibility(View.GONE);
        }
        setupButtons();
        setTitle();
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onStart() {
        super.onStart();
        this.theAdapter.reloadData();
    }

    private void setTitle() {
        this.titleTextView.setText(Locales.kLOC_TOOLS_FILTERS);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Locales.kLOC_TOOLS_FILTERS);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupButtons() {
        ListView theList = findViewById(R.id.filterlist);
        this.theAdapter = new FilterRowAdapter(this, this.filter);
        theList.setAdapter(this.theAdapter);
        findViewById(R.id.filterreset).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FilterClass newFilter = new FilterClass();
                newFilter.setFilterName(FiltersMainActivity.this.filter.getAccount());
                newFilter.setAccount(FiltersMainActivity.this.filter.getAccount());
                FiltersMainActivity.this.filterSelected(newFilter);
            }
        });
        this.currenctFilterView = findViewById(R.id.filtercurrent);
        this.currenctFilterView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (Objects.equals(FiltersMainActivity.this.filter.getAccount(), Locales.kLOC_FILTERS_CURRENT_ACCOUNT)) {
                    FiltersMainActivity.this.filter.setAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
                }
                FiltersMainActivity.this.filter.setCustomFilter(true);
                FiltersMainActivity.this.filterSelected(FiltersMainActivity.this.filter);
            }
        });
        FilterRowHolder holder = new FilterRowHolder();
        holder.filter = this.filter;
        this.currenctFilterView.setTag(holder);
        findViewById(R.id.filtercurrentedit).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(FiltersMainActivity.this.context, FilterEditActivity.class);
                i.putExtra("Filter", FiltersMainActivity.this.filter);
                FiltersMainActivity.this.context.startActivityForResult(i, FiltersMainActivity.FILTER_RESULT_SELECTED);
            }
        });
        theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ArrayList<View> theViews = new ArrayList<>();
        TextView tView = findViewById(R.id.reset_filter_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.current_filter_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.user_defined_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add((View) tView.getParent());
        View separatorView = findViewById(R.id.filter_separator);
        separatorView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        //theViews.add((View) separatorView);
        int i = 1;
        for (View theView : theViews) {
            (theView).setBackgroundResource(i % 2 == 0 ? PocketMoneyThemes.primaryRowSelector() : PocketMoneyThemes.alternatingRowSelector());
            i += 1;
        }
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FiltersMainActivity.this.openOptionsMenu();
            }
        });
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        //findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.actionBarColor());
    }

    public void filterSelected(FilterClass aFilter) {
        Intent i = new Intent();
        i.putExtra("Filter", aFilter);
        setResult(FILTER_RESULT_SELECTED, i);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            if (requestCode == FILTER_RESULT_SELECTED) {
                this.filter = (FilterClass) Objects.requireNonNull(data.getExtras()).get("Filter");
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_NEW, 0, Locales.kLOC_FILTER_NEW).setIcon(R.drawable.abouticon);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_NEW) {
            Intent i = new Intent(this.context, FilterEditActivity.class);
            FilterClass newFilter = new FilterClass();
            newFilter.setAccount(this.filter.getAccount());
            i.putExtra("Filter", newFilter);
            this.context.startActivity(i);
            return true;
        }
        return false;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Intent i = new Intent();
        i.putExtra("Filter", ((FilterRowHolder) v.getTag()).filter);
        MenuItem item = menu.add(0, CMENU_EDIT, 0, Locales.kLOC_GENERAL_EDIT);
        item.setIcon(R.drawable.abouticon);
        item.setIntent(i);
        if (v != this.currenctFilterView) {
            item = menu.add(0, CMENU_DELETE, 0, Locales.kLOC_GENERAL_DELETE);
            item.setIcon(R.drawable.abouticon);
            item.setIntent(i);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case CMENU_EDIT /*1*/:
                Intent intent = new Intent(this.context, FilterEditActivity.class);
                if (b != null) {
                    intent.putExtra("Filter", (FilterClass) b.get("Filter"));
                }
                startActivity(intent);
                return true;
            case CMENU_DELETE /*3*/:
                if (b != null) {
                    ((FilterClass) Objects.requireNonNull(b.get("Filter"))).deleteFromDatabase();
                }
                this.theAdapter.reloadData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
