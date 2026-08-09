package com.example.smmoney.views.repeating;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.BalanceBar;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.transactions.TransactionEditActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class RepeatingActivity extends PocketMoneyActivity {
    private static final int MENU_NEW = 1;
    private static final int MENU_PROCESS = 2;
    private RepeatingRecyclerViewAdapter adapter;
    private BalanceBar balanceBar;
    private final FilterClass filter = new FilterClass();
    private boolean isProcessingToDate = false;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> reloadData()
    );

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (!isGranted) {
            Toast.makeText(this, "Notification permission is recommended for repeating transaction alerts", Toast.LENGTH_LONG).show();
        }
    });

    private WakeLock wakeLock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RepeatingActivity:DoNotDimScreen");
        this.filter.setType(Enums.kTransactionTypeRepeating/*5*/); // 5 = repeating in 'transactions' DB table
        setContentView(R.layout.repeating);
        setupView();
        setTitle();
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    public void onResume() {
        super.onResume();
        reloadData();
    }

    private void setTitle() {
        Objects.requireNonNull(getSupportActionBar()).setTitle(Locales.kLOC_REPEATING_TRANSACTIONS);
    }

    private void setupView() {
        RecyclerView recyclerView = findViewById(R.id.thelist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new RepeatingRecyclerViewAdapter(this);
        recyclerView.setAdapter(this.adapter);
        recyclerView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) recyclerView.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                TransactionClass transaction = adapter.getElements().get(position);
                if (direction == ItemTouchHelper.RIGHT) {
                    // Edit
                    Intent intent = new Intent(RepeatingActivity.this, TransactionEditActivity.class);
                    intent.putExtra("Transaction", transaction);
                    editLauncher.launch(intent);
                    adapter.notifyItemChanged(position);
                } else if (direction == ItemTouchHelper.LEFT) {
                    // Delete
                    deleteRepeatingTransaction(transaction);
                    adapter.notifyItemChanged(position);
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
                        
                        Drawable icon = ContextCompat.getDrawable(RepeatingActivity.this, R.drawable.ic_edit_white_24dp);
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
                        c.drawText(Locales.kLOC_GENERAL_EDIT, (float) itemView.getLeft() + 140, (float) itemView.getTop() + (itemView.getHeight() / 2f) + 15, paint);

                    } else if (dX < 0) { // Swipe Left (Delete)
                        paint.setColor(Color.parseColor("#F44336")); // Red
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                        
                        Drawable icon = ContextCompat.getDrawable(RepeatingActivity.this, R.drawable.ic_delete_white_24dp);
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
                        float textWidth = paint.measureText(Locales.kLOC_GENERAL_DELETE);
                        c.drawText(Locales.kLOC_GENERAL_DELETE, (float) itemView.getRight() - 140 - textWidth, (float) itemView.getTop() + (itemView.getHeight() / 2f) + 15, paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        this.balanceBar = findViewById(R.id.balancebar);
        this.balanceBar.setSecondBalanceEnabled(true);
        this.balanceBar.nextButton.setOnClickListener(v -> {
            int padding = (int) (20 * getResources().getDisplayMetrics().density);
            final EditText textView = new EditText(RepeatingActivity.this);
            textView.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            textView.setText(String.valueOf(Prefs.getIntPref(Prefs.PREFS_REPEATING_UPCOMING_PERIOD)));
            textView.setTextColor(PocketMoneyThemes.primaryEditTextColor());
            
            FrameLayout container = new FrameLayout(RepeatingActivity.this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = padding;
            params.rightMargin = padding;
            textView.setLayoutParams(params);
            container.addView(textView);

            AlertDialog.Builder b = new AlertDialog.Builder(RepeatingActivity.this, PocketMoneyThemes.dialogTheme());
            b.setView(container);
            b.setTitle(Locales.kLOC_REPEATING_UPCOMING_MESSAGE);
            b.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, which) -> {
                try {
                    Prefs.setPref(Prefs.PREFS_REPEATING_UPCOMING_PERIOD, Integer.parseInt(textView.getText().toString()));
                    RepeatingActivity.this.reloadData();
                } catch (NumberFormatException e) {
                    Log.e(com.example.smmoney.SMMoney.TAG, "NumberFormatException in RepeatingActivity upcoming period dialog", e);
                }
            });
            b.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null);
            AlertDialog dialog = b.create();
            
            textView.requestFocus();
            textView.selectAll();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
            
            dialog.show();
        });
    }

    private void reloadBalanceBar() {
        ArrayList<RepeatingTransactionClass> repeatingTransactions = TransactionDB.queryAllRepeatingTransactions();
        int days = Prefs.getIntPref(Prefs.PREFS_REPEATING_UPCOMING_PERIOD);
        String text = Locales.kLOC_REPEATING_UPCOMING_LABEL + " " + days + " " + Locales.kLOC_REPEATING_FREQUENCY_DAYS;
        
        GregorianCalendar today = CalExt.beginningOfToday();
        GregorianCalendar upcomingEndDate = CalExt.endOfDay(CalExt.addDays((GregorianCalendar) today.clone(), days));

        double overdueAmount = 0.0d;
        double upcomingAmount = 0.0d;
        for (RepeatingTransactionClass repeatingTransaction : repeatingTransactions) {
            if (repeatingTransaction.isOverdue()) {
                overdueAmount += repeatingTransaction.overdueAmount();
            }
            GregorianCalendar firstUpcoming = repeatingTransaction.getNextTransactionDateAfter(today);
            if (firstUpcoming != null) {
                upcomingAmount += repeatingTransaction.amountBetweenDate(firstUpcoming, upcomingEndDate);
            }
        }
        if (overdueAmount < 0.0d) {
            this.balanceBar.balanceAmountTextView.setTextColor(PocketMoneyThemes.redOnBlackLabelColor());
        } else {
            this.balanceBar.balanceAmountTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
        }
        if (upcomingAmount < 0.0d) {
            this.balanceBar.secondBalanceAmountTextView.setTextColor(PocketMoneyThemes.redOnBlackLabelColor());
        } else if (upcomingAmount > 0.0d) {
            this.balanceBar.secondBalanceAmountTextView.setTextColor(PocketMoneyThemes.greenDepositColor());
        } else {
            this.balanceBar.secondBalanceAmountTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
        }
        this.balanceBar.secondBalanceAmountTextView.setText(CurrencyExt.amountAsCurrency(upcomingAmount));
        this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(overdueAmount));
        this.balanceBar.secondBalanceTypeTextView.setText(text);
        this.balanceBar.secondBalanceTypeTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
        this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_REPEATING_OVERDUE_LABEL);
        this.balanceBar.balanceTypeTextView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
    }

    public void launchEdit(Intent intent) {
        editLauncher.launch(intent);
    }

    public void reloadData() {
        this.adapter.setElements(TransactionDB.queryWithFilter(this.filter));
        reloadBalanceBar();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_NEW, 0, Locales.kLOC_TRANSACTION_NEW).setIcon(R.drawable.ic_add_circle_outline_white_24dp_svg).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_PROCESS, 0, Locales.kLOC_REPEATING_PROCESSTODATE);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_NEW /*1*/:
                if (!AccountsActivity.isLite(this) || this.adapter.getItemCount() < 2) {
                    Intent i = new Intent(this, TransactionEditActivity.class);
                    TransactionClass trans = new TransactionClass();
                    trans.isRepeatingTransaction = true;
                    i.putExtra("Transaction", trans);
                    editLauncher.launch(i);
                    return true;
                }
                AccountsActivity.displayLiteDialog(this);
                return true;
            case MENU_PROCESS /*2*/:
                showDatePickerDialog();
                return true;
            default:
                return false;
        }
    }

    private void deleteRepeatingTransaction(final TransactionClass transaction) {
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_GENERAL_DELETE)
                .setMessage("Are you sure you want to delete this repeating transaction?")
                .setPositiveButton(Locales.kLOC_GENERAL_DELETE, (dialog, which) -> {
                    if (transaction != null) {
                        new RepeatingTransactionClass(transaction.transactionID, false).deleteFromDatabase();
                        transaction.deleteFromDatabase();
                    }
                    reloadData();
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null)
                .show();
    }

    private void showDatePickerDialog() {
        GregorianCalendar theDate = new GregorianCalendar();
        new DatePickerDialog(this, PocketMoneyThemes.datePickerTheme(), (view, year, monthOfYear, dayOfMonth) -> {
            if (!RepeatingActivity.this.isProcessingToDate) {
                RepeatingActivity.this.isProcessingToDate = true;
                final GregorianCalendar newCal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                new Thread() {
                    public void run() {
                        TransactionDB.addRepeatingEventsThroughDate(newCal, RepeatingActivity.this);
                        RepeatingActivity.this.runOnUiThread(() -> {
                            try {
                                RepeatingActivity.this.wakeLock.release();
                            } catch (Exception e) {
                                Log.e(com.example.smmoney.SMMoney.TAG, "Exception in RepeatingActivity runOnUiThread (wakeLock.release)", e);
                            }
                            RepeatingActivity.this.reloadData();
                            Toast.makeText(RepeatingActivity.this, "Process to date Completed", Toast.LENGTH_LONG).show();
                            RepeatingActivity.this.isProcessingToDate = false;
                        });
                    }
                }.start();
            }
        }, theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH)).show();
    }
}
