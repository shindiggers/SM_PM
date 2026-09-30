package com.example.smmoney.views.charts;

import android.content.Intent;
import android.os.Bundle;

import androidx.compose.ui.platform.ComposeView;
import androidx.core.app.ActivityOptionsCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.budgets.BudgetsActivity;
import com.example.smmoney.views.charts.compose.ChartsScreenKt;
import com.example.smmoney.views.reports.ReportsPlaceholderActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ChartsActivity extends PocketMoneyActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide the Action Bar to save screen real estate
        }

        ComposeView composeView = findViewById(R.id.compose_view);
        ChartsScreenKt.setChartsScreenContent(composeView);

        this.bottomNav = findViewById(R.id.bottom_navigation);
        this.bottomNav.setSelectedItemId(R.id.nav_charts);
        this.bottomNav.setBackgroundColor(PocketMoneyThemes.bottomNavBackgroundColor());
        this.bottomNav.setItemIconTintList(PocketMoneyThemes.bottomNavColorStateList());
        this.bottomNav.setItemTextColor(PocketMoneyThemes.bottomNavColorStateList());
        
        this.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_accounts) {
                Intent intent = new Intent(this, AccountsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_left, R.anim.slide_out_right).toBundle());
                return true;
            } else if (itemId == R.id.nav_budgets) {
                Intent intent = new Intent(this, BudgetsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_left, R.anim.slide_out_right).toBundle());
                return true;
            } else if (itemId == R.id.nav_reports) {
                Intent intent = new Intent(this, ReportsPlaceholderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
                return true;
            }
            return itemId == R.id.nav_charts;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.bottomNav != null) {
            this.bottomNav.setSelectedItemId(R.id.nav_charts);
        }
    }
}
