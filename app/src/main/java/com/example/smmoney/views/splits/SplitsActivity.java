package com.example.smmoney.views.splits;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.smmoney.R;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.PocketMoneyActivity;

import java.util.Objects;

public class SplitsActivity extends PocketMoneyActivity {
    public static final int REQUEST_EDIT = 3;
    public static final int RESULT_CHANGED = -1; // Activity.RESULT_OK

    public static final int RESULT_NO_CHANGE = 0;
    final ActivityResultLauncher<Intent> editSplitLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_CHANGED && result.getData() != null) {
                    Intent data = result.getData();
                    SplitsClass split;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        split = data.getSerializableExtra("Split", SplitsClass.class);
                    } else {
                        //noinspection deprecation
                        split = (SplitsClass) Objects.requireNonNull(data.getExtras()).get("Split");
                    }
                    int index = data.getIntExtra("SplitIndex", -1);
                    if (index != -1) {
                        this.transaction.getSplits().remove(index);
                        this.transaction.getSplits().add(index, split);
                    } else {
                        this.transaction.addSplit(split);
                    }
                    reloadData();
                }
            }
    );
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MENU_ADJUST = 3;
    private final int MENU_CLEAR = 4;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MENU_NEW = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MENU_REMAINDER = 2;
    private final int REQUEST_NEW = 1;
    private final int REQUEST_REMAINDER = 2;
    private SplitsRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    @SuppressWarnings("FieldCanBeLocal")
    private Context context;
    private double originalSubtotal = 0.0d;
    private TextView remainderTextView;
    private TextView remainderTitleTextView;
    private TextView splitsTotalTextView;
    private TextView splitsTotalTitleTextView;
    private TextView totalTextView;
    private TextView totalTitleTextView;
    private TransactionClass transaction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                this.transaction = intent.getSerializableExtra("Transaction", TransactionClass.class);
            } else {
                //noinspection deprecation
                this.transaction = (TransactionClass) intent.getExtras().get("Transaction");
            }
            if (this.transaction != null) {
                this.transaction.hydrated = true;
                this.transaction.dirty = true;

                // Clean up auto-populated single empty split
                if (this.transaction.getSplits().size() == 1) {
                    SplitsClass first = this.transaction.getSplits().get(0);
                    if (first.getCategory().isEmpty() && (first.getMemo() == null || first.getMemo().isEmpty())) {
                        this.transaction.getSplits().clear();
                    }
                }
            }
        }
        if (this.transaction == null) {
            finish();
            return;
        }
        this.originalSubtotal = this.transaction.getSubTotal();
        setContentView(R.layout.splits);
        this.splitsTotalTextView = findViewById(R.id.splitssplitstotal);
        this.remainderTextView = findViewById(R.id.splitsremainder);
        this.totalTextView = findViewById(R.id.splitstotal);
        this.splitsTotalTitleTextView = findViewById(R.id.splitssplitstotaltitle);
        this.splitsTotalTitleTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.totalTitleTextView = findViewById(R.id.splitstotaltitle);
        
        // Theme headers and card for high contrast
        findViewById(R.id.status_card).setBackgroundColor(0xFF1A1A1A); // Force Dark Gray
        
        int headerTitleColor = 0xFFBDBDBD; // Light Gray
        int headerValueColor = 0xFFFFFFFF; // White
        
        ((TextView) findViewById(R.id.splitstotaltitle)).setTextColor(headerTitleColor);
        this.totalTextView.setTextColor(headerValueColor);
        
        ((TextView) findViewById(R.id.splitssplitstotaltitle)).setTextColor(headerTitleColor);
        this.splitsTotalTextView.setTextColor(headerValueColor);
        
        this.remainderTitleTextView = findViewById(R.id.splitsremaindertitle);
        this.remainderTitleTextView.setTextColor(headerTitleColor);
        this.remainderTextView.setTextColor(headerValueColor);
        
        TextView editBtn = findViewById(R.id.edit_total_button);
        editBtn.setTextColor(PocketMoneyThemes.currentTintColor());
        editBtn.setOnClickListener(v -> showEditTotalDialog());

        this.recyclerView = findViewById(R.id.the_list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new SplitsRecyclerViewAdapter(this, this.transaction);
        this.recyclerView.setAdapter(this.adapter);
        this.recyclerView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.recyclerView.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < transaction.getSplits().size()) {
                    transaction.getSplits().remove(position);
                    adapter.notifyItemRemoved(position);
                    reloadData();
                } else {
                    adapter.notifyItemChanged(position); // Snap back if it's the placeholder
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof SplitsRecyclerViewAdapter.SplitViewHolder) {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
                return 0;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    paint.setColor(Color.parseColor("#F44336"));
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                    
                    Drawable icon = ContextCompat.getDrawable(SplitsActivity.this, R.drawable.ic_delete_white_24dp);
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();
                        int iconRight = itemView.getRight() - iconMargin;
                        int iconLeft = iconRight - icon.getIntrinsicWidth();
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this.recyclerView);

        setResult(RESULT_NO_CHANGE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_EDIT_SPLITS_TITLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        setTransactionAsResult();
        finish();
        return true;
    }

    protected void onResume() {
        super.onResume();
        reloadData();
    }

    private void reloadData() {
        this.adapter.setItems(this.transaction.getSplits());
        
        double splitsTotal = splitsSum();
        double remainderTotal = this.transaction.getSubTotal() - splitsTotal;
        double totalTotal = this.transaction.getSubTotal();

        boolean singleXrate = this.transaction.isSingleXrate();
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            this.splitsTotalTextView.setText(CurrencyExt.amountAsCurrency(splitsTotal / (singleXrate ? this.transaction.getXrate() : 1.0d), this.transaction.getCurrencyCode()));
            this.remainderTextView.setText(CurrencyExt.amountAsCurrency(remainderTotal / (singleXrate ? this.transaction.getXrate() : 1.0d), this.transaction.getCurrencyCode()));
            this.totalTextView.setText(CurrencyExt.amountAsCurrency(totalTotal / (singleXrate ? this.transaction.getXrate() : 1.0d), this.transaction.getCurrencyCode()));
        } else {
            this.splitsTotalTextView.setText(CurrencyExt.amountAsCurrency(splitsTotal, Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
            this.remainderTextView.setText(CurrencyExt.amountAsCurrency(remainderTotal, Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
            this.totalTextView.setText(CurrencyExt.amountAsCurrency(totalTotal, Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
        }

        int greenColor = 0xFF4D9C26; // Success Green
        int redColor = 0xFFBC5A5A;   // Warning Red
        
        if (Math.abs(remainderTotal) < 0.01) {
            this.remainderTextView.setTextColor(greenColor);
        } else {
            this.remainderTextView.setTextColor(redColor);
        }
    }

    private void showEditTotalDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setText(CurrencyExt.amountAsString(this.transaction.getSubTotal()));
        input.setSelection(input.getText().length());
        
        alert.setTitle("Edit Total Value");
        alert.setView(input);
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                this.transaction.setSubTotal(CurrencyExt.amountFromString(value));
                this.transaction.initType();
                reloadData();
            }
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null);
        alert.show();
    }

    private void setTransactionAsResult() {
        Intent i = new Intent();
        double splitsSum = splitsSum();
        if ((this.originalSubtotal == 0.0d || this.transaction.getNumberOfSplits() == 1) && splitsSum != 0.0d) {
            this.transaction.setSubTotal(splitsSum);
        }
        this.transaction.initType();
        i.putExtra("Transaction", this.transaction);
        setResult(RESULT_CHANGED, i);
    }

    private double splitsSum() {
        double splitsTotal = 0.0d;
        for (SplitsClass splitsClass : this.transaction.getSplits()) {
            splitsTotal += (splitsClass).getAmount();
        }
        return splitsTotal;
    }

    public void newSplitAction() {
        newSplitAction(false);
    }

    public void newSplitAction(boolean autoPopulateRemainder) {
        SplitsClass split = new SplitsClass();
        AccountClass act = AccountDB.recordFor(this.transaction.getAccount());
        split.setCurrencyCode(act == null ? Prefs.getStringPref(Prefs.HOMECURRENCYCODE) : act.getCurrencyCode());
        
        if (autoPopulateRemainder) {
            double remainder = this.transaction.getSubTotal() - splitsSum();
            if (Math.abs(remainder) > 0.009) {
                split.setAmount(remainder);
            }
        }
        
        split.dirty = false;
        Intent i = new Intent(this, SplitsEditActivity.class);
        i.putExtra("Transaction", this.transaction);
        i.putExtra("Split", split);
        i.putExtra("SplitIndex", -1);
        editSplitLauncher.launch(i);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_NEW, 0, Locales.kLOC_SPLITS_NEW)
            .setIcon(R.drawable.ic_add_circle_outline_white_24dp_svg)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_NEW) {
            newSplitAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }

    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setTransactionAsResult();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
