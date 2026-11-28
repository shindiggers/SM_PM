package com.example.smmoney.views.filters;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.PocketMoneyActivity;

import java.util.Objects;

public class FiltersMainActivity extends PocketMoneyActivity {

    public final ActivityResultLauncher<Intent> filterEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != 0 && result.getData() != null) {
                    FilterClass appliedFilter = androidx.core.content.IntentCompat.getSerializableExtra(result.getData(), "Filter", FilterClass.class);
                    filterSelected(appliedFilter);
                } else if (this.theAdapter != null) {
                    this.theAdapter.reloadData();
                }
            }
    );

    private static final int FILTER_RESULT_NOCHANGE = 2;
    private static final int FILTER_RESULT_SELECTED = 1;
    private static final int MENU_NEW = 1;

    private FilterClass filter;
    private FilterRowAdapter theAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.filter = androidx.core.content.IntentCompat.getSerializableExtra(getIntent(), "Filter", FilterClass.class);
        setContentView(R.layout.filter_main);
        setResult(FILTER_RESULT_NOCHANGE);

        setupButtons();
        setTitle();
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.theAdapter != null) {
            this.theAdapter.reloadData();
        }
    }

    private void setTitle() {
        Objects.requireNonNull(getSupportActionBar()).setTitle(SMMoney.getAppContext().getString(R.string.FILTERS_SAVED));
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void setupButtons() {
        RecyclerView theList = findViewById(R.id.filterlist);
        theList.setLayoutManager(new LinearLayoutManager(this));
        this.theAdapter = new FilterRowAdapter(this, this.filter);
        theList.setAdapter(this.theAdapter);
        theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION || position >= theAdapter.getElements().size()) return;

                FilterClass filterItem = theAdapter.getElements().get(position);
                if (direction == ItemTouchHelper.RIGHT) {
                    // Edit
                    Intent intent = new Intent(FiltersMainActivity.this, FilterEditActivity.class);
                    intent.putExtra("Filter", filterItem);
                    filterEditLauncher.launch(intent);
                    theAdapter.notifyItemChanged(position);
                } else if (direction == ItemTouchHelper.LEFT) {
                    // Delete
                    deleteFilter(filterItem);
                    theAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();

                    if (dX > 0) { // Swipe Right (Edit)
                        paint.setColor(Color.parseColor("#4CAF50")); // Green
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), paint);

                        Drawable icon = ContextCompat.getDrawable(FiltersMainActivity.this, R.drawable.ic_edit_white_24dp);
                        if (icon != null) {
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }

                        paint.setColor(Color.WHITE);
                        paint.setTextSize(40);
                        paint.setAntiAlias(true);
                        c.drawText(SMMoney.getAppContext().getString(R.string.GENERAL_EDIT), (float) itemView.getLeft() + 140, (float) itemView.getTop() + (itemView.getHeight() / 2f) + 15, paint);

                    } else if (dX < 0) { // Swipe Left (Delete)
                        paint.setColor(Color.parseColor("#F44336")); // Red
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);

                        Drawable icon = ContextCompat.getDrawable(FiltersMainActivity.this, R.drawable.ic_delete_white_24dp);
                        if (icon != null) {
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();
                            int iconRight = itemView.getRight() - iconMargin;
                            int iconLeft = iconRight - icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }

                        paint.setColor(Color.WHITE);
                        paint.setTextSize(40);
                        paint.setAntiAlias(true);
                        float textWidth = paint.measureText(SMMoney.getAppContext().getString(R.string.GENERAL_DELETE));
                        c.drawText(SMMoney.getAppContext().getString(R.string.GENERAL_DELETE), (float) itemView.getRight() - 140 - textWidth, (float) itemView.getTop() + (itemView.getHeight() / 2f) + 15, paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(theList);
    }

    private void deleteFilter(final FilterClass filterItem) {
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(R.string.GENERAL_DELETE)
                .setMessage(R.string.FILTER_DELETE_CONFIRM)
                .setPositiveButton(R.string.GENERAL_DELETE, (dialog, which) -> {
                    if (filterItem != null) {
                        filterItem.deleteFromDatabase();
                    }
                    if (theAdapter != null) {
                        theAdapter.reloadData();
                    }
                })
                .setNegativeButton(R.string.GENERAL_CANCEL, (dialog, which) -> {
                    if (theAdapter != null) {
                        theAdapter.reloadData();
                    }
                })
                .setOnCancelListener(dialog -> {
                    if (theAdapter != null) {
                        theAdapter.reloadData();
                    }
                })
                .show();
    }

    public void filterSelected(FilterClass aFilter) {
        if (getIntent().hasExtra("ONLY SAVED")) {
            Intent i = new Intent(this, com.example.smmoney.views.transactions.TransactionsActivity.class);
            i.putExtra("Filter", aFilter);
            i.putExtra("FROM_SAVED_FILTERS", true);
            startActivity(i);
        } else {
            Intent i = new Intent();
            i.putExtra("Filter", aFilter);
            setResult(FILTER_RESULT_SELECTED, i);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem newItem = menu.add(0, MENU_NEW, 0, SMMoney.getAppContext().getString(R.string.FILTER_NEW));
        newItem.setIcon(R.drawable.ic_add_white_24dp);
        newItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_NEW) {
            Intent i = new Intent(this, FilterEditActivity.class);
            FilterClass newFilter = new FilterClass();
            if (this.filter != null) {
                newFilter.setAccount(this.filter.getAccount());
            }
            i.putExtra("Filter", newFilter);
            filterEditLauncher.launch(i);
            return true;
        }
        return false;
    }
}
