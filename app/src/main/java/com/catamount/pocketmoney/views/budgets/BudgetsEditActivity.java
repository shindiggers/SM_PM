package com.catamount.pocketmoney.views.budgets;

import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.records.CategoryBudgetClass;
import com.catamount.pocketmoney.records.CategoryClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.CurrencyKeyboard;
import com.catamount.pocketmoney.views.PocketMoneyActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class BudgetsEditActivity extends PocketMoneyActivity {
    private final int CMENU_DELETE = 1;
    private final int DIALOG_BUDGET = 1;
    private final int DIALOG_PICKDATE = 2;
    View addBudgetCell;
    View budgetCell;
    EditText budgetEditText;
    TextView budgetTypeTextView;
    CategoryClass category;
    ArrayList<CategoryBudgetClass> categoryBudgetItems;
    EditText categoryEditText;
    CurrencyKeyboard currencyKeyboard;
    ArrayList<CategoryBudgetClass> deletedCategoryBudgetItems;
    Button enableVariableBudgetCell;
    View includeSubcategoriesCell;
    CheckBox includeSubcategoriesCheckBox;
    private OnDateSetListener mDateSetListener = new OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            GregorianCalendar newCal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            if (BudgetsEditActivity.this.selectedBudgetItem == null) {
                BudgetsEditActivity.this.categoryBudgetItems.get(0).setDate(newCal);
            } else {
                BudgetsEditActivity.this.selectedBudgetItem.setDate(newCal);
            }
            BudgetsEditActivity.this.reloadData();
        }
    };
    String oldCategory;
    TextView originalHistoryBudgetTextView;
    View originalHistoryCell;
    TextView originalHistoryDateTextView;
    ViewGroup outterView;
    TextView periodTextView;
    CheckBox rolloverCheckBox;
    CategoryBudgetClass selectedBudgetItem;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.category = (CategoryClass) getIntent().getExtras().get("Category");
        this.oldCategory = this.category.getCategory();
        setContentView(R.layout.budget_edit);
        this.categoryBudgetItems = CategoryBudgetClass.budgetItemsForCategory(this.category.getCategory());
        this.deletedCategoryBudgetItems = new ArrayList();
        setupButtons();
        loadCells();
        setTitle(Locales.kLOC_EDIT_TRANSACTION_TITLE);
        getActionBar().hide();
    }

    protected void onResume() {
        super.onResume();
        reloadData();
    }

    private void setupButtons() {
        ArrayList<View> theViews = new ArrayList();
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((TextView) findViewById(R.id.title_text_view)).setTextColor(PocketMoneyThemes.toolbarTextColor());
        findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.outterView = findViewById(R.id.outter_layout);
        View aView = this.outterView.findViewById(R.id.categorybutton);
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        aView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsEditActivity.this.getCells();
                Intent i = new Intent(BudgetsEditActivity.this, LookupsListActivity.class);
                i.putExtra("type", 5);
                BudgetsEditActivity.this.startActivityForResult(i, 5);
            }
        });
        this.categoryEditText = (AutoCompleteTextView) aView.findViewById(R.id.categoryedittext);
        this.categoryEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        ((TextView) aView.findViewById(R.id.categorytextview)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
        ((TextView) this.outterView.findViewById(R.id.category_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = findViewById(R.id.budgettypebutton);
        aView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsEditActivity.this.getCells();
                Intent i = new Intent(BudgetsEditActivity.this, LookupsListActivity.class);
                i.putExtra("type", 19);
                BudgetsEditActivity.this.startActivityForResult(i, 19);
            }
        });
        theViews.add(aView);
        this.budgetTypeTextView = findViewById(R.id.budgettexttextview);
        this.budgetTypeTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        ((TextView) aView.findViewById(R.id.budgettypelabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        aView = this.outterView.findViewById(R.id.periodbutton);
        aView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsEditActivity.this.getCells();
                Intent i = new Intent(BudgetsEditActivity.this, LookupsListActivity.class);
                i.putExtra("type", 20);
                BudgetsEditActivity.this.startActivityForResult(i, 20);
            }
        });
        theViews.add(aView);
        this.periodTextView = findViewById(R.id.periodtextview);
        this.periodTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        aView.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        ((TextView) aView.findViewById(R.id.periodlabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.budgetEditText = this.outterView.findViewById(R.id.budgetedittext);
        this.budgetEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.currencyKeyboard = findViewById(R.id.keyboardView);
        this.currencyKeyboard.setEditText(this.budgetEditText, null);
        this.budgetCell = (View) this.budgetEditText.getParent();
        theViews.add(this.budgetCell);
        this.budgetCell.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
        ((TextView) this.outterView.findViewById(R.id.budgetlabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.rolloverCheckBox = this.outterView.findViewById(R.id.rollovercheckbox);
        aView = (View) this.rolloverCheckBox.getParent();
        ((TextView) this.outterView.findViewById(R.id.rolloverlabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.rolloverCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BudgetsEditActivity.this.reloadData();
            }
        });
        this.includeSubcategoriesCheckBox = this.outterView.findViewById(R.id.includesubcategoriescheckbox);
        this.includeSubcategoriesCell = (View) this.includeSubcategoriesCheckBox.getParent();
        ((TextView) this.outterView.findViewById(R.id.includesubcategorieslabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        TextView button = findViewById(R.id.save_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsEditActivity.this.saveAction();
            }
        });
        button = findViewById(R.id.cancel_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsEditActivity.this.finish();
            }
        });
        ((TextView) findViewById(R.id.addnewbudgettextview)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.originalHistoryCell = findViewById(R.id.originalhistorycell);
        this.originalHistoryDateTextView = findViewById(R.id.originalhistorydate);
        this.originalHistoryDateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.originalHistoryBudgetTextView = findViewById(R.id.originalhistorybudget);
        this.originalHistoryBudgetTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.originalHistoryDateTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsEditActivity.this.selectedBudgetItem = null;
                BudgetsEditActivity.this.showDialog(2);
            }
        });
        this.originalHistoryBudgetTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BudgetsEditActivity.this.selectedBudgetItem = (CategoryBudgetClass) ((View) v.getParent()).getTag();
                BudgetsEditActivity.this.showDialog(1);
            }
        });
        this.enableVariableBudgetCell = findViewById(R.id.enablevariablebutton);
        this.enableVariableBudgetCell.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CategoryBudgetClass budgetItem = new CategoryBudgetClass();
                budgetItem.setCategoryName(BudgetsEditActivity.this.category.getCategory());
                budgetItem.setDate(new GregorianCalendar());
                budgetItem.setBudgetLimit(CurrencyExt.amountFromString(BudgetsEditActivity.this.budgetEditText.getText().toString()));
                BudgetsEditActivity.this.categoryBudgetItems.add(budgetItem);
                BudgetsEditActivity.this.reloadData();
            }
        });
        this.addBudgetCell = findViewById(R.id.addbudgetcell);
        this.addBudgetCell.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CategoryBudgetClass budgetItem = new CategoryBudgetClass();
                budgetItem.setCategoryName(BudgetsEditActivity.this.category.getCategory());
                budgetItem.setDate(new GregorianCalendar());
                budgetItem.setBudgetLimit(CurrencyExt.amountFromString(BudgetsEditActivity.this.budgetEditText.getText().toString()));
                BudgetsEditActivity.this.categoryBudgetItems.add(budgetItem);
                BudgetsEditActivity.this.reloadData();
            }
        });
    }

    public void reloadData() {
        this.outterView.removeViews(9, (this.outterView.getChildCount() - 9) - 2);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        Collections.sort(this.categoryBudgetItems, new Comparator<CategoryBudgetClass>() {
            public int compare(CategoryBudgetClass o1, CategoryBudgetClass o2) {
                return (int) (o2.getDate().getTimeInMillis() - o1.getDate().getTimeInMillis());
            }
        });
        if (this.categoryBudgetItems.size() > 0) {
            this.originalHistoryBudgetTextView.setText(CurrencyExt.amountAsCurrency(this.category.getBudgetLimit()));
            this.originalHistoryDateTextView.setText(CalExt.descriptionWithMediumDate(this.categoryBudgetItems.get(this.categoryBudgetItems.size() - 1).getDate()));
        }
        int i = 0;
        Iterator it = this.categoryBudgetItems.iterator();
        while (it.hasNext()) {
            CategoryBudgetClass budgetItem = (CategoryBudgetClass) it.next();
            View v = vi.inflate(R.layout.budgets_variable_row, null);
            registerForContextMenu(v);
            v.setTag(budgetItem);
            if (1 == i % 2) {
                v.setBackgroundResource(PocketMoneyThemes.alternatingRowSelector());
            } else {
                v.setBackgroundResource(PocketMoneyThemes.primaryRowSelector());
            }
            i++;
            TextView textView = v.findViewById(R.id.date);
            textView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            textView.setText(CalExt.descriptionWithMediumDate(budgetItem.getDate()));
            textView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    BudgetsEditActivity.this.selectedBudgetItem = (CategoryBudgetClass) ((View) v.getParent()).getTag();
                    GregorianCalendar theDate = BudgetsEditActivity.this.selectedBudgetItem == null ? BudgetsEditActivity.this.categoryBudgetItems.get(0).getDate() : BudgetsEditActivity.this.selectedBudgetItem.getDate();
                    new DatePickerDialog(BudgetsEditActivity.this, BudgetsEditActivity.this.mDateSetListener, theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
            textView = v.findViewById(R.id.budget);
            textView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            textView.setText(CurrencyExt.amountAsCurrency(budgetItem.getBudgetLimit()));
            textView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    BudgetsEditActivity.this.selectedBudgetItem = (CategoryBudgetClass) ((View) v.getParent()).getTag();
                    Builder alert = new Builder(BudgetsEditActivity.this);
                    final EditText input = new EditText(BudgetsEditActivity.this);
                    input.setText(CurrencyExt.amountAsString(BudgetsEditActivity.this.selectedBudgetItem == null ? BudgetsEditActivity.this.category.getBudgetLimit() : BudgetsEditActivity.this.selectedBudgetItem.getBudgetLimit()));
                    alert.setTitle(Locales.kLOC_BUDGETS_AMOUNT);
                    alert.setView(input);
                    alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString().trim();
                            if (BudgetsEditActivity.this.selectedBudgetItem == null) {
                                BudgetsEditActivity.this.category.setBudgetLimit(CurrencyExt.amountFromString(value));
                            } else {
                                BudgetsEditActivity.this.selectedBudgetItem.setBudgetLimit(CurrencyExt.amountFromString(value));
                            }
                            BudgetsEditActivity.this.reloadData();
                        }
                    });
                    alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
                    alert.create().show();
                }
            });
            CheckBox checkBox = v.findViewById(R.id.resetrollovercheckbox);
            checkBox.setChecked(budgetItem.getResetRollover());
            checkBox.setVisibility(this.rolloverCheckBox.isChecked() ? View.VISIBLE : View.INVISIBLE);
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((CategoryBudgetClass) ((View) buttonView.getParent()).getTag()).setResetRollover(isChecked);
                }
            });
            this.outterView.addView(v, 9, new LayoutParams(-1, -2));
        }
        if (this.categoryBudgetItems.size() > 0) {
            this.budgetCell.setVisibility(View.GONE);
            this.originalHistoryCell.setVisibility(View.VISIBLE);
            this.enableVariableBudgetCell.setVisibility(View.GONE);
            this.addBudgetCell.setVisibility(View.VISIBLE);
        } else {
            this.budgetCell.setVisibility(View.VISIBLE);
            this.enableVariableBudgetCell.setVisibility(View.VISIBLE);
            this.addBudgetCell.setVisibility(View.GONE);
            this.originalHistoryCell.setVisibility(View.GONE);
        }
        this.outterView.invalidate();
    }

    private void loadCells() {
        this.categoryEditText.setText(this.category.getCategory());
        this.budgetTypeTextView.setText(this.category.typeAsString());
        this.periodTextView.setText(this.category.periodAsString());
        this.budgetEditText.setText(CurrencyExt.amountAsCurrency(this.category.getBudgetLimit()));
        this.rolloverCheckBox.setChecked(this.category.getRollover());
        this.includeSubcategoriesCheckBox.setChecked(this.category.getIncludeSubcategories());
    }

    private void getCells() {
        this.category.setCategory(this.categoryEditText.getText().toString());
        this.category.setTypeFromString(this.budgetTypeTextView.getText().toString());
        this.category.setPeriodFromString(this.periodTextView.getText().toString());
        this.category.setBudgetLimit(CurrencyExt.amountFromString(this.budgetEditText.getText().toString()));
        this.category.setRollover(this.rolloverCheckBox.isChecked());
        this.category.setIncludeSubcategories(this.includeSubcategoriesCheckBox.isChecked());
    }

    private void saveAction() {
        getCells();
        int categoryID = CategoryClass.idForCategory(this.category.getCategory());
        if (this.oldCategory == null || this.oldCategory.length() == 0) {
            int catID = CategoryClass.idForCategory(this.category.getCategory());
            if (this.oldCategory == null || this.oldCategory.equalsIgnoreCase(this.category.getCategory())) {
                this.category = new CategoryClass(catID);
                getCells();
            }
            save();
            finish();
        } else if (this.category.getCategory().equalsIgnoreCase(this.oldCategory)) {
            save();
            finish();
        } else {
            CharSequence[] items = new CharSequence[]{Locales.kLOC_LOOKUPS_POPUPLIST, Locales.kLOC_LOOKUPS_EVERYWHERE};
            Builder builder = new Builder(this);
            builder.setTitle(Locales.kLOC_LOOKUPS_CHANGEBODY);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    int catID = CategoryClass.idForCategory(BudgetsEditActivity.this.category.getCategory());
                    int oldCatID = CategoryClass.idForCategory(BudgetsEditActivity.this.oldCategory);
                    if (catID != 0) {
                        BudgetsEditActivity.this.category = new CategoryClass(catID);
                        BudgetsEditActivity.this.getCells();
                    }
                    if (oldCatID != 0) {
                        new CategoryClass(oldCatID).deleteFromDatabase();
                        BudgetsEditActivity.this.category.categoryID = catID;
                    }
                    BudgetsEditActivity.this.save();
                    switch (item) {
                        case PocketMoneyThemes.kThemeBlack /*0*/:
                            BudgetsEditActivity.this.finish();
                            break;
                        case SplitsActivity.RESULT_CHANGED /*1*/:
                            TransactionClass.renameCategoryFromTo(BudgetsEditActivity.this.oldCategory, BudgetsEditActivity.this.category.getCategory());
                            BudgetsEditActivity.this.finish();
                            break;
                    }
                    BudgetsEditActivity.this.reloadData();
                }
            });
            builder.create().show();
        }
    }

    private void save() {
        this.category.saveToDatabase();
        Iterator it = this.categoryBudgetItems.iterator();
        while (it.hasNext()) {
            ((CategoryBudgetClass) it.next()).saveToDatabase();
        }
        it = this.deletedCategoryBudgetItems.iterator();
        while (it.hasNext()) {
            CategoryBudgetClass bItem = (CategoryBudgetClass) it.next();
            if (bItem.categoryBudgetID != 0) {
                bItem.deleteFromDatabase();
            }
        }
        if (this.category.getCategory() != null && this.category.getCategory() != this.oldCategory) {
            CategoryBudgetClass.renameBudgetItems(this.oldCategory, this.category.getCategory());
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Intent i = new Intent();
        i.putExtra("BudgetItem", (CategoryBudgetClass) v.getTag());
        menu.add(0, 1, 0, Locales.kLOC_GENERAL_DELETE).setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                this.categoryBudgetItems.remove(b.get("BudgetItem"));
                this.deletedCategoryBudgetItems.add((CategoryBudgetClass) b.get("BudgetItem"));
                reloadData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            String selection = "";
            try {
                if (data.getExtras() != null) {
                    selection = data.getExtras().getString("selection");
                    switch (requestCode) {
                        case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                            this.categoryEditText.setText(selection);
                            break;
                        case LookupsListActivity.BUDGET_TYPE /*19*/:
                            this.budgetTypeTextView.setText(selection);
                            break;
                        case LookupsListActivity.BUDGET_PERIOD /*20*/:
                            this.periodTextView.setText(selection);
                            break;
                    }
                    getCells();
                }
            } catch (NullPointerException e) {
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                double budgetLimit;
                Builder alert = new Builder(this);
                final EditText input = new EditText(this);
                if (this.selectedBudgetItem == null) {
                    budgetLimit = this.category.getBudgetLimit();
                } else {
                    budgetLimit = this.selectedBudgetItem.getBudgetLimit();
                }
                input.setText(CurrencyExt.amountAsString(budgetLimit));
                alert.setTitle(Locales.kLOC_BUDGETS_AMOUNT);
                alert.setView(input);
                alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString().trim();
                        if (BudgetsEditActivity.this.selectedBudgetItem == null) {
                            BudgetsEditActivity.this.category.setBudgetLimit(CurrencyExt.amountFromString(value));
                        } else {
                            BudgetsEditActivity.this.selectedBudgetItem.setBudgetLimit(CurrencyExt.amountFromString(value));
                        }
                        BudgetsEditActivity.this.reloadData();
                    }
                });
                alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                return alert.create();
            default:
                return null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.currencyKeyboard.hide()) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
