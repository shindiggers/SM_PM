package com.example.smmoney.views.lookups;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.PayeeClass;
import com.example.smmoney.views.PocketMoneyActivity;

import java.util.List;
import java.util.Objects;

public class CategoryLookupListActivity extends PocketMoneyActivity {
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    private final int CMENU_SUBCATEGORY = 4;
    private final int TYPE_ALL = 1;
    private final int TYPE_PAYEE = 2;
    private CatPayeeRowAdapter adapter;
    private int currentType = 2;
    private boolean isCategoryLookup = true;
    @SuppressWarnings("FieldCanBeLocal")
    private ListView listView;
    private LayoutInflater mInflater;
    private String payee;
    private boolean progUpdate = false;
    @SuppressWarnings("FieldCanBeLocal")
    private TextView titleTextView;

    private class CatPayeeRowAdapter extends BaseAdapter {
        List<String> allCategories;
        private final OnClickListener itemClickListener = new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("selection", (String) v.getTag());
                CategoryLookupListActivity.this.setResult(CategoryLookupListActivity.this.currentType, i);
                CategoryLookupListActivity.this.finish();
            }
        };
        List<String> payeeCategories;

        CatPayeeRowAdapter() {
            reloadData();
        }

        void reloadData() {
            if (CategoryLookupListActivity.this.isCategoryLookup) {
                this.payeeCategories = CategoryClass.allCategoryNamesInDatabaseForPayee(CategoryLookupListActivity.this.payee);
                this.allCategories = CategoryClass.allCategoryNamesInDatabase();
                return;
            }
            this.payeeCategories = PayeeClass.allPayeesInDatabaseForCategory(CategoryLookupListActivity.this.payee);
            this.allCategories = PayeeClass.allPayeesInDatabase();
        }

        public int getCount() {
            switch (CategoryLookupListActivity.this.currentType) {
                case TYPE_ALL /*1*/:
                    return this.allCategories.size();
                case TYPE_PAYEE /*2*/:
                    return this.payeeCategories.size();
                default:
                    return 0;
            }
        }

        public String getItem(int arg0) {
            switch (CategoryLookupListActivity.this.currentType) {
                case TYPE_ALL /*1*/:
                    return this.allCategories.get(arg0);
                case TYPE_PAYEE /*2*/:
                    return this.payeeCategories.get(arg0);
                default:
                    return "";
            }
        }

        public long getItemId(int arg0) {
            return arg0;
        }

        public View getView(int pos, View convertView, ViewGroup arg2) {
            String category = getItem(pos);
            if (convertView == null) {
                convertView = CategoryLookupListActivity.this.mInflater.inflate(PocketMoneyThemes.simpleListItem(), null);
                convertView.setOnClickListener(this.itemClickListener);
                //((TextView) convertView.findViewById(R.id.listView)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
            }
            ((TextView) convertView).setText(category);
            convertView.setTag(category);
            return convertView;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.payee = Objects.requireNonNull(getIntent().getExtras()).getString("payee");
        if (this.payee == null) {
            this.payee = getIntent().getExtras().getString("category");
            this.isCategoryLookup = false;
        }
        setContentView(R.layout.lookups_category);
        this.mInflater = LayoutInflater.from(this);
        setResult(0);
        setupView();
    }

    private void setupView() {
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        this.titleTextView.setText(this.isCategoryLookup ? Locales.kLOC_GENERAL_CATEGORY_TITLE : Locales.kLOC_GENERAL_PAYEE);
        this.titleTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CategoryLookupListActivity.this.openOptionsMenu();
            }
        });
        findViewById(R.id.the_tool_bar).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.adapter = new CatPayeeRowAdapter();
        this.listView = findViewById(R.id.listView);
        this.listView.setAdapter(this.adapter);
        registerForContextMenu(this.listView);
        final RadioButton payeeRadioButton = findViewById(R.id.payeebutton);
        payeeRadioButton.setText(this.payee);
        final RadioButton allRadioButton = findViewById(R.id.allbutton);
        RadioGroup rg = findViewById(R.id.radiogroup);
        if (this.payee.length() == 0) {
            this.currentType = TYPE_ALL/*1*/;
            allRadioButton.setChecked(true);
            payeeRadioButton.setText("<empty>");
        }
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!CategoryLookupListActivity.this.progUpdate) {
                    CategoryLookupListActivity.this.progUpdate = true;
                    if (checkedId == R.id.payeebutton) {
                        payeeRadioButton.setChecked(true);
                        CategoryLookupListActivity.this.currentType = 2;
                    } else if (checkedId == R.id.allbutton) {
                        allRadioButton.setChecked(true);
                        CategoryLookupListActivity.this.currentType = 1;
                    }
                    CategoryLookupListActivity.this.progUpdate = false;
                    CategoryLookupListActivity.this.reloadData();
                }
            }
        });
        ((View) rg.getParent()).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.listView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    private void reloadData() {
        this.adapter.reloadData();
        this.adapter.notifyDataSetChanged();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CMENU_DELETE, 0, Locales.kLOC_GENERAL_DELETE);
        menu.add(0, CMENU_EDIT, 0, Locales.kLOC_LOOKUPS_RENAMEITEM);
        if (this.isCategoryLookup) {
            menu.add(0, CMENU_SUBCATEGORY, 0, Locales.kLOC_ADDSUBCATEGORY);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final String originalString;
        Builder alert;
        final EditText input;
        switch (item.getItemId()) {
            case CMENU_EDIT /*1*/:
                // int theItem = this.currentType; ** Coded out as inspection showed theItem was never used
                originalString = this.adapter.getItem(info.position);
                alert = new Builder(this);
                input = new EditText(this);
                input.setText(originalString);
                alert.setTitle(Locales.kLOC_LOOKUPS_RENAMEITEM);
                alert.setView(input);
                alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final String value = input.getText().toString().trim();
                        Builder b = new Builder(CategoryLookupListActivity.this);
                        b.setTitle(Locales.kLOC_LOOKUPS_RENAMEITEM);
                        b.setMessage(Locales.kLOC_LOOKUPS_CHANGEBODY);
                        CharSequence charSequence = Locales.kLOC_LOOKUPS_POPUPLIST;
                        b.setPositiveButton(charSequence, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (CategoryLookupListActivity.this.isCategoryLookup) {
                                    CategoryClass.renameFromToInDatabase(originalString, value, false);
                                } else {
                                    PayeeClass.renameFromToInDatabase(originalString, value, false);
                                }
                                CategoryLookupListActivity.this.reloadData();
                            }
                        });
                        charSequence = Locales.kLOC_LOOKUPS_EVERYWHERE;
                        //str = originalString; str already created above as final
                        b.setNegativeButton(charSequence, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (CategoryLookupListActivity.this.isCategoryLookup) {
                                    CategoryClass.renameFromToInDatabase(originalString, value, true);
                                } else {
                                    PayeeClass.renameFromToInDatabase(originalString, value, true);
                                }
                                CategoryLookupListActivity.this.reloadData();
                            }
                        });
                    }
                });
                alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();
                ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE))).showSoftInput(input, 1);
                return true;
            case CMENU_DELETE /*3*/:
                originalString = this.adapter.getItem(info.position);
                if (this.isCategoryLookup) {
                    new CategoryClass(CategoryClass.idForCategory(originalString)).deleteFromDatabase();
                } else {
                    new PayeeClass(PayeeClass.idForPayee(originalString)).deleteFromDatabase();
                }
                reloadData();
                return true;
            case CMENU_SUBCATEGORY /*4*/:
                originalString = this.adapter.getItem(info.position);
                alert = new Builder(this);
                input = new EditText(this);
                alert.setTitle(Locales.kLOC_ADDSUBCATEGORY);
                alert.setView(input);
                alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        CategoryClass.insertIntoDatabase(originalString + ":" + input.getText().toString().trim());
                        CategoryLookupListActivity.this.reloadData();
                    }
                });
                alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();
                ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE))).showSoftInput(input, 1);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
