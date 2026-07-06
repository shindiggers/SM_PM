package com.example.smmoney.views.reports;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.budgets.BudgetsActivity;
import com.example.smmoney.views.charts.ChartsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ReportsPlaceholderActivity extends PocketMoneyActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.placeholder_layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_GENERAL_REPORTS);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        }

        findViewById(R.id.placeholder_text).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((TextView)findViewById(R.id.placeholder_text)).setTextColor(PocketMoneyThemes.primaryCellTextColor());

        this.bottomNav = findViewById(R.id.bottom_navigation);
        this.bottomNav.setSelectedItemId(R.id.nav_reports);
        this.bottomNav.setBackgroundColor(PocketMoneyThemes.bottomNavBackgroundColor());
        this.bottomNav.setItemIconTintList(PocketMoneyThemes.bottomNavColorStateList());
        this.bottomNav.setItemTextColor(PocketMoneyThemes.bottomNavColorStateList());
        
        this.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_accounts) {
                Intent intent = new Intent(this, AccountsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (itemId == R.id.nav_budgets) {
                Intent intent = new Intent(this, BudgetsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (itemId == R.id.nav_charts) {
                Intent intent = new Intent(this, ChartsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
            return itemId == R.id.nav_reports;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.bottomNav != null) {
            this.bottomNav.setSelectedItemId(R.id.nav_reports);
        }
    }
}
