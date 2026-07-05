package com.example.smmoney.views.charts;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.budgets.BudgetsActivity;
import com.example.smmoney.views.reports.ReportsPlaceholderActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChartsActivity extends PocketMoneyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.placeholder_layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Charts");
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        }

        findViewById(R.id.placeholder_text).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((TextView)findViewById(R.id.placeholder_text)).setTextColor(PocketMoneyThemes.primaryCellTextColor());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_charts);
        bottomNav.setBackgroundColor(PocketMoneyThemes.bottomNavBackgroundColor());
        bottomNav.setItemIconTintList(PocketMoneyThemes.bottomNavColorStateList());
        bottomNav.setItemTextColor(PocketMoneyThemes.bottomNavColorStateList());
        
        bottomNav.setOnItemSelectedListener(item -> {
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
            } else if (itemId == R.id.nav_reports) {
                Intent intent = new Intent(this, ReportsPlaceholderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return itemId == R.id.nav_charts;
        });
    }
}
