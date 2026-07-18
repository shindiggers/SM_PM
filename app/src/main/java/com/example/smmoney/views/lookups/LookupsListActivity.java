package com.example.smmoney.views.lookups;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.google.android.material.snackbar.Snackbar;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.ClassNameClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.IDClass;
import com.example.smmoney.records.PayeeClass;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.views.PocketMoneyActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LookupsListActivity extends PocketMoneyActivity {
    public static final int ACCOUNT_ICON_LOOKUP = 2;
    private static final int ACCOUNT_LOOKUP = 3;
    public static final int ACCOUNT_LOOKUP_TRANS = 17;
    public static final int ACCOUNT_LOOKUP_WITH_NONE = 18;
    private static final int ACCOUNT_TYPE_LOOKUP = 1;
    public static final int BUDGET_PERIOD = 20;
    public static final int BUDGET_TYPE = 19;
    public static final int CATEGORY_LOOKUP = 5;
    public static final int CLASS_LOOKUP = 6;
    public static final int FILTER_ACCOUNTS = 9;
    public static final int FILTER_CATEGORIES = 14;
    public static final int FILTER_CLASSES = 15;
    public static final int FILTER_CLEARED = 13;
    public static final int FILTER_DATES = 10;
    public static final int FILTER_IDS = 12;
    public static final int FILTER_PAYEES = 11;
    public static final int FILTER_TRANSACTION_TYPE = 8;
    public static final int ID_LOOKUP = 7;
    public static final int PAYEE_LOOKUP = 4;
    public static final int REPEAT_TYPE = 16;
    private int currentType;
    private boolean isMultiSelect = false;
    private boolean mIsUpdatingCheckState = false;
    private RecyclerView recyclerView;
    private LookupRecyclerViewAdapter adapter;
    private ArrayList<String> theStrings = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookups);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.currentType = extras.getInt("type");
            this.isMultiSelect = extras.getBoolean("isMultiSelect", false);
        }
        setupView();
        setupList();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getTypeAsString());
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (this.isMultiSelect) {
            returnDoneResult();
        } else {
            getOnBackPressedDispatcher().onBackPressed();
        }
        return true;
    }

    public void onResume() {
        super.onResume();
        if (this.currentType == ACCOUNT_TYPE_LOOKUP && !Prefs.getBooleanPref(Prefs.HINT_ACCOUNT_TYPE_OPTIONS)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
            alert.setTitle(Locales.kLOC_ACCOUNTTYPES_URL_TITLE);
            alert.setMessage(Locales.kLOC_ACCOUNTTYPES_URL_BODY);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
                Prefs.setPref(Prefs.HINT_ACCOUNT_TYPE_OPTIONS, true);
                dialog.dismiss();
            });
            alert.show();
        }
    }

    private void setupView() {
        this.recyclerView = findViewById(R.id.the_list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new LookupRecyclerViewAdapter(this, this.isMultiSelect);
        this.recyclerView.setAdapter(this.adapter);
        
        this.adapter.setOnItemClickListener(position -> {
            if (!LookupsListActivity.this.isMultiSelect) {
                LookupsListActivity.this.onListItemClick(position);
            } else {
                LookupsListActivity.this.handleMultiClick(position);
            }
        });

        if (canEdit()) {
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    if (direction == ItemTouchHelper.RIGHT) {
                        renameItem(position);
                        adapter.notifyItemChanged(position);
                    } else if (direction == ItemTouchHelper.LEFT) {
                        deleteItem(position);
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
                            
                            Drawable icon = ContextCompat.getDrawable(LookupsListActivity.this, R.drawable.ic_edit_white_24dp);
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
                            
                            Drawable icon = ContextCompat.getDrawable(LookupsListActivity.this, R.drawable.ic_delete_white_24dp);
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
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this.recyclerView);
        }

        this.recyclerView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.recyclerView.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    private boolean canEdit() {
        return this.currentType == PAYEE_LOOKUP || this.currentType == CATEGORY_LOOKUP || this.currentType == CLASS_LOOKUP || this.currentType == ID_LOOKUP;
    }

    private void handleMultiClick(int position) {
        if (mIsUpdatingCheckState) return;
        
        String clickedItem = this.theStrings.get(position);
        boolean isNowChecked = !this.adapter.isItemChecked(position);
        
        mIsUpdatingCheckState = true;
        this.adapter.setItemChecked(position, isNowChecked);

        if (isAllItem(clickedItem)) {
            if (isNowChecked) {
                // Uncheck everything else if "All" is selected
                for (int i = 0; i < this.theStrings.size(); i++) {
                    if (i != position) {
                        this.adapter.setItemChecked(i, false);
                    }
                }
            }
        } else if (isNowChecked) {
            // If a specific item is selected, uncheck any "All" items
            for (int i = 0; i < this.theStrings.size(); i++) {
                if (isAllItem(this.theStrings.get(i))) {
                    this.adapter.setItemChecked(i, false);
                }
            }
        }
        mIsUpdatingCheckState = false;
    }

    private boolean isAllItem(String item) {
        return item.equals(Locales.kLOC_FILTERS_ALL_ACCOUNTS) ||
               item.equals(Locales.kLOC_FILTERS_ALL_CATEGORIES) ||
               item.equals(Locales.kLOC_FILTERS_ALL_CLASSES) ||
               item.equals(Locales.kLOC_FILTERS_CURRENT_ACCOUNT);
    }

    private void setupList() {
        int i = 0;
        boolean alphabetList = false;
        switch (this.currentType) {
            case ACCOUNT_TYPE_LOOKUP /*1*/:
                this.theStrings = AccountClass.accountTypes();
                break;
            case ACCOUNT_LOOKUP /*3*/:
            case ACCOUNT_LOOKUP_TRANS /*17*/:
                this.theStrings = queryForAccounts();
                break;
            case PAYEE_LOOKUP /*4*/:
                alphabetList = true;
                this.theStrings = PayeeClass.allPayeesInDatabase();
                break;
            case CATEGORY_LOOKUP /*5*/:
                alphabetList = true;
                this.theStrings = CategoryClass.allCategoryNamesInDatabase();
                break;
            case CLASS_LOOKUP /*6*/:
                this.theStrings = ClassNameClass.allClassNamesInDatabase();
                break;
            case ID_LOOKUP /*7*/:
            case FILTER_IDS /*12*/:
                this.theStrings = IDClass.allCategoriesInDatabase();
                break;
            case FILTER_TRANSACTION_TYPE /*8*/:
                this.theStrings = FilterClass.transactionTypes();
                break;
            case FILTER_ACCOUNTS /*9*/:
                this.theStrings = queryForAccounts();
                this.theStrings.add(0, Locales.kLOC_FILTERS_ALL_ACCOUNTS);
                this.theStrings.add(0, Locales.kLOC_FILTERS_CURRENT_ACCOUNT);
                break;
            case FILTER_DATES /*10*/:
                Bundle bundle = getIntent().getExtras();
                this.theStrings = FilterClass.dateRanges();
                if (bundle != null) {
                    this.theStrings.add(ACCOUNT_TYPE_LOOKUP, this.theStrings.remove(ACCOUNT_TYPE_LOOKUP) + "\n" + bundle.getString("FromDate") + "<->" + bundle.getString("ToDate"));
                }
                break;
            case FILTER_PAYEES /*11*/:
                this.theStrings = PayeeClass.allPayeesInDatabase();
                break;
            case FILTER_CLEARED /*13*/:
                this.theStrings = FilterClass.clearedTypes();
                break;
            case FILTER_CATEGORIES /*14*/:
                this.theStrings = CategoryClass.allCategoryNamesInDatabase();
                this.theStrings.add(0, Locales.kLOC_FILTERS_UNFILED);
                this.theStrings.add(0, Locales.kLOC_FILTERS_ALL_CATEGORIES);
                break;
            case FILTER_CLASSES /*15*/:
                this.theStrings = ClassNameClass.allClassNamesInDatabase();
                this.theStrings.add(0, Locales.kLOC_FILTERS_UNFILED);
                this.theStrings.add(0, Locales.kLOC_FILTERS_ALL_CLASSES);
                break;
            case REPEAT_TYPE /*16*/:
                this.theStrings = new ArrayList<>();
                String[] types = RepeatingTransactionClass.types();
                int length = types.length;
                while (i < length) {
                    this.theStrings.add(types[i]);
                    i += ACCOUNT_TYPE_LOOKUP;
                }
                break;
            case ACCOUNT_LOOKUP_WITH_NONE /*18*/:
                this.theStrings = queryForAccounts();
                this.theStrings.add(0, Locales.kLOC_GENERAL_NONE);
                break;
            case BUDGET_TYPE /*19*/:
                this.theStrings = CategoryClass.budgetTypes();
                break;
            case BUDGET_PERIOD /*20*/:
                this.theStrings = CategoryClass.periods();
                break;
            default:
                this.theStrings = new ArrayList<>();
                this.theStrings.add("Invalid Type Passed to LookupList");
                break;
        }
        int size = this.theStrings.size();
        int i2 = 0;
        while (i2 < size) {
            if (this.theStrings.get(i2) == null) {
                this.theStrings.remove(i2);
                size--;
                i2--;
            }
            i2 += ACCOUNT_TYPE_LOOKUP;
        }
        
        if (alphabetList) {
            Collections.sort(this.theStrings, String.CASE_INSENSITIVE_ORDER);
        }
        
        this.adapter.setItems(this.theStrings);
        
        if (this.isMultiSelect) {
            preCheckItems();
        }
    }

    private void preCheckItems() {
        String currentSelection = getIntent().getStringExtra("currentSelection");
        if (currentSelection == null || currentSelection.isEmpty() || currentSelection.equals(Locales.kLOC_FILTER_DATES_ALL)) {
            // Default check "All" items if nothing selected
            for (int i = 0; i < this.theStrings.size(); i++) {
                if (isAllItem(this.theStrings.get(i))) {
                    this.adapter.setItemChecked(i, true);
                    break;
                }
            }
            return;
        }
        String[] selected = currentSelection.split(";");
        for (int i = 0; i < this.theStrings.size(); i++) {
            String item = this.theStrings.get(i);
            for (String s : selected) {
                if (item.equals(s)) {
                    this.adapter.setItemChecked(i, true);
                    break;
                }
            }
        }
    }

    private void reloadData() {
        setupList();
    }

    private ArrayList<String> queryForAccounts() {
        ArrayList<String> names = new ArrayList<>();
        for (AccountClass accountClass : AccountDB.queryOnViewType(0)) {
            names.add((accountClass).getAccount());
        }
        return names;
    }

    private void onListItemClick(int position) {
        if (canEdit()) {
            Snackbar.make(this.recyclerView, "Swipe right to rename, left to delete", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent();
        i.putExtra("selection", this.theStrings.get(position));
        setResult(this.currentType, i);
        finish();
    }

    public String getTypeAsString() {
        return switch (this.currentType) {
            case ACCOUNT_TYPE_LOOKUP, FILTER_TRANSACTION_TYPE, REPEAT_TYPE, BUDGET_TYPE /*19*/ ->
                    Locales.kLOC_ACCOUNT_TYPE_LABEL;
            case ACCOUNT_LOOKUP, ACCOUNT_LOOKUP_TRANS, FILTER_ACCOUNTS,
                 ACCOUNT_LOOKUP_WITH_NONE /*18*/ -> Locales.kLOC_GENERAL_ACCOUNTS;
            case PAYEE_LOOKUP, FILTER_PAYEES /*11*/ -> Locales.kLOC_GENERAL_PAYEE_TITLE;
            case CATEGORY_LOOKUP, FILTER_CATEGORIES /*14*/ ->
                    Locales.kLOC_GENERAL_CATEGORY_TITLE;
            case CLASS_LOOKUP, FILTER_CLASSES /*15*/ -> Locales.kLOC_GENERAL_CLASSES;
            case ID_LOOKUP, FILTER_IDS /*12*/ -> Locales.kLOC_GENERAL_ID_TITLE;
            case FILTER_DATES /*10*/ -> Locales.kLOC_FILTER_DATES;
            case FILTER_CLEARED /*13*/ -> Locales.kLOC_GENERAL_CLEARED;
            case BUDGET_PERIOD /*20*/ -> Locales.kLOC_BUDGETS_PERIOD;
            default -> "Select item";
        };
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.isMultiSelect) {
            menu.add(0, 2 /* MENU_DONE */, 0, Locales.kLOC_GENERAL_DONE).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        if (this.currentType != PAYEE_LOOKUP && this.currentType != CATEGORY_LOOKUP && this.currentType != CLASS_LOOKUP && this.currentType != ID_LOOKUP) {
            return this.isMultiSelect;
        }
        int MENU_ADD = 1;
        menu.add(0, MENU_ADD, 0, Locales.kLOC_TRANSACTION_NEW).setIcon(R.drawable.ic_add_circle_outline_white_24dp_svg).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (this.isMultiSelect) {
                returnDoneResult();
            } else {
                getOnBackPressedDispatcher().onBackPressed();
            }
            return true;
        }
        if (item.getItemId() == 2 /* MENU_DONE */) {
            returnDoneResult();
            return true;
        }
        if (item.getItemId() != 1 /* MENU_ADD */) {
            return super.onOptionsItemSelected(item);
        }

        final int theItem = this.currentType;
        AlertDialog.Builder alert = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
        final EditText input = new EditText(this);
        alert.setTitle(Locales.kLOC_TRANSACTION_NEW);
        alert.setView(input);
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) return;
            switch (theItem) {
                case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                    PayeeClass.insertIntoDatabase(value);
                    break;
                case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                    CategoryClass.insertIntoDatabase(value);
                    break;
                case LookupsListActivity.CLASS_LOOKUP /*6*/:
                    ClassNameClass.insertIntoDatabase(value);
                    break;
                case LookupsListActivity.ID_LOOKUP /*7*/:
                    IDClass.insertIntoDatabase(value);
                    break;
            }
            LookupsListActivity.this.reloadData();
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, whichButton) -> dialog.cancel());
        alert.show();
        return true;
    }

    private void renameItem(int position) {
        final String originalString = this.theStrings.get(position);
        final int theItem = this.currentType;
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rename_lookup, null);
        EditText input = dialogView.findViewById(R.id.edit_new_name);
        RadioGroup scopeGroup = dialogView.findViewById(R.id.scope_group);
        RadioButton scopePopup = dialogView.findViewById(R.id.scope_popup_only);
        RadioButton scopeEverywhere = dialogView.findViewById(R.id.scope_everywhere);
        TextView helperText = dialogView.findViewById(R.id.helper_text);

        // Theme the dialog elements
        int labelColor = PocketMoneyThemes.fieldLabelColor();
        int textColor = PocketMoneyThemes.primaryCellTextColor();
        ColorStateList tint = ColorStateList.valueOf(PocketMoneyThemes.currentTintColor());

        ((TextView) dialogView.findViewById(R.id.label_new_name)).setTextColor(labelColor);
        ((TextView) dialogView.findViewById(R.id.label_scope)).setTextColor(labelColor);
        helperText.setTextColor(textColor);
        
        input.setText(originalString);
        input.setTextColor(textColor);
        
        View underline = dialogView.findViewById(R.id.edit_underline);
        underline.setBackgroundColor(PocketMoneyThemes.currentTintColor());
        
        scopePopup.setTextColor(textColor);
        scopePopup.setButtonTintList(tint);
        scopePopup.setText(Locales.kLOC_LOOKUPS_POPUPLIST);
        
        scopeEverywhere.setTextColor(textColor);
        scopeEverywhere.setButtonTintList(tint);
        scopeEverywhere.setText(Locales.kLOC_LOOKUPS_EVERYWHERE);
        
        helperText.setText(getString(R.string.dialog_rename_lookup_helper_text));

        AlertDialog dialog = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_LOOKUPS_RENAMEITEM)
                .setView(dialogView)
                .setPositiveButton(Locales.kLOC_GENERAL_OK, (d, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) return;
                    
                    boolean updateEverywhere = (scopeGroup.getCheckedRadioButtonId() == R.id.scope_everywhere);
                    
                    switch (theItem) {
                        case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                            PayeeClass.renameFromToInDatabase(originalString, value, updateEverywhere);
                            break;
                        case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                            CategoryClass.renameFromToInDatabase(originalString, value, updateEverywhere);
                            break;
                        case LookupsListActivity.CLASS_LOOKUP /*6*/:
                            ClassNameClass.renameFromToInDatabase(originalString, value, updateEverywhere);
                            break;
                        case LookupsListActivity.ID_LOOKUP /*7*/:
                            IDClass.renameFromToInDatabase(originalString, value, updateEverywhere);
                            break;
                    }
                    reloadData();
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null)
                .create();

        // Ensure keyboard and focus are handled when dialog is shown
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        
        dialog.setOnShowListener(d -> {
            input.requestFocus();
            input.selectAll();
            // Force keyboard via IMM as a secondary measure
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        
        dialog.show();
    }

    private void deleteItem(int position) {
        String value = this.theStrings.get(position);
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setTitle(Locales.kLOC_GENERAL_DELETE)
                .setMessage(Locales.kLOC_GENERAL_DELETE + " '" + value + "'?")
                .setPositiveButton(Locales.kLOC_GENERAL_DELETE, (dialog, which) -> {
                    switch (this.currentType) {
                        case PAYEE_LOOKUP /*4*/:
                            new PayeeClass(PayeeClass.idForPayee(value)).deleteFromDatabase();
                            break;
                        case CATEGORY_LOOKUP /*5*/:
                            new CategoryClass(CategoryClass.idForCategory(value)).deleteFromDatabase();
                            break;
                        case CLASS_LOOKUP /*6*/:
                            new ClassNameClass(ClassNameClass.idForClass(value)).deleteFromDatabase();
                            break;
                        case ID_LOOKUP /*7*/:
                            new IDClass(IDClass.idForID(value)).deleteFromDatabase();
                            break;
                    }
                    reloadData();
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null)
                .show();
    }

    private void returnDoneResult() {
        StringBuilder sb = new StringBuilder();
        java.util.Set<Integer> checked = this.adapter.getCheckedPositions();
        boolean hasSpecificSelection = false;
        String allItemValue = "";

        for (int i = 0; i < this.theStrings.size(); i++) {
            if (checked.contains(i)) {
                String val = this.theStrings.get(i);
                if (isAllItem(val)) {
                    allItemValue = val;
                } else {
                    if (sb.length() > 0) sb.append(";");
                    sb.append(val);
                    hasSpecificSelection = true;
                }
            }
        }
        
        String result;
        if (hasSpecificSelection) {
            result = sb.toString();
        } else {
            result = allItemValue;
        }
        
        Intent i = new Intent();
        i.putExtra("selection", result);
        setResult(this.currentType, i);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK && this.isMultiSelect) {
            returnDoneResult();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
