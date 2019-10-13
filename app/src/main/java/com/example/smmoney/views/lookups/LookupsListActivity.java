package com.example.smmoney.views.lookups;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.example.smmoney.R;
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
import java.util.HashMap;
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
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    private final int CMENU_SUBCATEGORY = 4;
    private int currentType;
    private ListView theList;
    private ArrayList<String> theStrings = null;
    private TextView titleTextView;

    private class LookupRowAdapter<T> extends BaseAdapter {
        LayoutInflater inflator;
        List<T> items;
        int layoutResId;

        LookupRowAdapter(int layoutResId, List<T> items) {
            this.items = items;
            this.inflator = LayoutInflater.from(LookupsListActivity.this);
            this.layoutResId = layoutResId;
        }

        public int getCount() {
            return this.items.size();
        }

        public T getItem(int arg0) {
            return this.items.get(arg0);
        }

        public long getItemId(int arg0) {
            return (long) arg0;
        }

        public View getView(int pos, View convertView, ViewGroup arg2) {
            String category = (String) getItem(pos);
            if (convertView == null) {
                convertView = this.inflator.inflate(this.layoutResId, null);
               // ((TextView) convertView.findViewById(R.id.the_list)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
            }
            ((TextView) convertView).setText(category);
            return convertView;
        }
    }

    class MyIndexerAdapter<T> extends LookupRowAdapter<T> implements SectionIndexer {
        HashMap<String, Integer> alphaIndexer = new HashMap<>();
        ArrayList<String> myElements;
        String[] sections;

        MyIndexerAdapter(Context context, int textViewResourceId, List<T> objects) {
            super(textViewResourceId, objects);
            this.myElements = (ArrayList) objects;
            int size = LookupsListActivity.this.theStrings.size();
            int i = size - 1;
            while (i >= 0) {
                String element = LookupsListActivity.this.theStrings.get(i);
                if (element == null || element.length() == 0) {
                    LookupsListActivity.this.theStrings.remove(i);
                    size--;
                    i--;
                } else {
                    this.alphaIndexer.put(element.substring(0, LookupsListActivity.ACCOUNT_TYPE_LOOKUP), i);
                }
                i--;
            }
            ArrayList<String> keyList = new ArrayList<>(this.alphaIndexer.keySet());
            Collections.sort(keyList);
            this.sections = new String[keyList.size()];
            keyList.toArray(this.sections);
        }

        public int getPositionForSection(int section) {
            return this.alphaIndexer.get(this.sections[section]);
        }

        public int getSectionForPosition(int position) {
            Log.v("getSectionForPosition", "called");
            return 0;
        }

        public Object[] getSections() {
            return this.sections;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookups);
        this.currentType = Objects.requireNonNull(getIntent().getExtras()).getInt("type");
        setupView();
        setupList();
    }

    public void onResume() {
        super.onResume();
        if (this.currentType == ACCOUNT_TYPE_LOOKUP && !Prefs.getBooleanPref(Prefs.HINT_ACCOUNT_TYPE_OPTIONS)) {
            Builder alert = new Builder(this);
            alert.setTitle(Locales.kLOC_ACCOUNTTYPES_URL_TITLE);
            alert.setMessage(Locales.kLOC_ACCOUNTTYPES_URL_BODY);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Prefs.setPref(Prefs.HINT_ACCOUNT_TYPE_OPTIONS, true);
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    private void setupView() {
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        this.titleTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LookupsListActivity.this.openOptionsMenu();
            }
        });
        findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.theList = findViewById(R.id.the_list);
        this.theList.setFastScrollEnabled(true);
        this.theList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                LookupsListActivity.this.onListItemClick(arg2);
            }
        });
        this.theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.theList.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    private void setupList() {
        int i = 0;
        boolean alphabetList = false;
        switch (this.currentType) {
            case ACCOUNT_TYPE_LOOKUP /*1*/:
                setTitle(Locales.kLOC_ACCOUNT_TYPE_LABEL);
                this.theStrings = AccountClass.accountTypes();
                break;
            case ACCOUNT_LOOKUP /*3*/:
            case ACCOUNT_LOOKUP_TRANS /*17*/:
                setTitle(Locales.kLOC_GENERAL_ACCOUNTS);
                this.theStrings = queryForAccounts();
                break;
            case PAYEE_LOOKUP /*4*/:
                alphabetList = true;
                setTitle(Locales.kLOC_GENERAL_PAYEE_TITLE);
                this.theStrings = PayeeClass.allPayeesInDatabase();
                break;
            case CATEGORY_LOOKUP /*5*/:
                alphabetList = true;
                setTitle(Locales.kLOC_GENERAL_CATEGORY_TITLE);
                this.theStrings = CategoryClass.allCategoryNamesInDatabase();
                break;
            case CLASS_LOOKUP /*6*/:
                setTitle(Locales.kLOC_GENERAL_CLASSES);
                this.theStrings = ClassNameClass.allClassNamesInDatabase();
                break;
            case ID_LOOKUP /*7*/:
            case FILTER_IDS /*12*/:
                setTitle(Locales.kLOC_GENERAL_ID_TITLE);
                this.theStrings = IDClass.allCategoriesInDatabase();
                break;
            case FILTER_TRANSACTION_TYPE /*8*/:
                setTitle(Locales.kLOC_ACCOUNT_TYPE_LABEL);
                this.theStrings = FilterClass.transactionTypes();
                break;
            case FILTER_ACCOUNTS /*9*/:
                setTitle(Locales.kLOC_GENERAL_ACCOUNTS);
                this.theStrings = queryForAccounts();
                this.theStrings.add(0, Locales.kLOC_FILTERS_ALL_ACCOUNTS);
                this.theStrings.add(0, Locales.kLOC_FILTERS_CURRENT_ACCOUNT);
                break;
            case FILTER_DATES /*10*/:
                setTitle(Locales.kLOC_FILTER_DATES);
                Bundle bundle = getIntent().getExtras();
                this.theStrings = FilterClass.dateRanges();
                if (bundle != null) {
                    this.theStrings.add(ACCOUNT_TYPE_LOOKUP, this.theStrings.remove(ACCOUNT_TYPE_LOOKUP) + "\n" + bundle.getString("FromDate") + "<->" + bundle.getString("ToDate"));
                }
                break;
            case FILTER_PAYEES /*11*/:
                setTitle(Locales.kLOC_GENERAL_PAYEE_TITLE);
                this.theStrings = PayeeClass.allPayeesInDatabase();
                break;
            case FILTER_CLEARED /*13*/:
                setTitle(Locales.kLOC_GENERAL_CLEARED);
                this.theStrings = FilterClass.clearedTypes();
                break;
            case FILTER_CATEGORIES /*14*/:
                setTitle(Locales.kLOC_GENERAL_CATEGORY_TITLE);
                this.theStrings = CategoryClass.allCategoryNamesInDatabase();
                this.theStrings.add(0, Locales.kLOC_FILTERS_UNFILED);
                this.theStrings.add(0, Locales.kLOC_FILTERS_ALL_CATEGORIES);
                break;
            case FILTER_CLASSES /*15*/:
                setTitle(Locales.kLOC_GENERAL_CLASSES);
                this.theStrings = ClassNameClass.allClassNamesInDatabase();
                this.theStrings.add(0, Locales.kLOC_FILTERS_UNFILED);
                this.theStrings.add(0, Locales.kLOC_FILTERS_ALL_CLASSES);
                break;
            case REPEAT_TYPE /*16*/:
                setTitle(Locales.kLOC_ACCOUNT_TYPE_LABEL);
                this.theStrings = new ArrayList<>();
                String[] types = RepeatingTransactionClass.types();
                int length = types.length;
                while (i < length) {
                    this.theStrings.add(types[i]);
                    i += ACCOUNT_TYPE_LOOKUP;
                }
                break;
            case ACCOUNT_LOOKUP_WITH_NONE /*18*/:
                setTitle(Locales.kLOC_GENERAL_ACCOUNTS);
                this.theStrings = queryForAccounts();
                this.theStrings.add(0, Locales.kLOC_GENERAL_NONE);
                break;
            case BUDGET_TYPE /*19*/:
                setTitle(Locales.kLOC_ACCOUNT_TYPE_LABEL);
                this.theStrings = CategoryClass.budgetTypes();
                break;
            case BUDGET_PERIOD /*20*/:
                setTitle(Locales.kLOC_BUDGETS_PERIOD);
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
            this.theList.setAdapter(new MyIndexerAdapter(getApplicationContext(), PocketMoneyThemes.simpleListItem(), this.theStrings));
        } else {
            ListView listView = this.theList;
            ListAdapter lookupRowAdapter = new LookupRowAdapter(PocketMoneyThemes.simpleListItem(), this.theStrings);
            ListAdapter adapter = lookupRowAdapter;
            listView.setAdapter(lookupRowAdapter);
        }
        registerForContextMenu(this.theList);
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
        Intent i = new Intent();
        i.putExtra("selection", (String) this.theList.getItemAtPosition(position));
        setResult(this.currentType, i);
        finish();
    }

    public String getTypeAsString() {
        switch (this.currentType) {
            case PAYEE_LOOKUP /*4*/:
                return Locales.kLOC_GENERAL_PAYEE;
            case CATEGORY_LOOKUP /*5*/:
                return Locales.kLOC_GENERAL_CATEGORY;
            case CLASS_LOOKUP /*6*/:
                return Locales.kLOC_GENERAL_CLASS;
            case ID_LOOKUP /*7*/:
                return Locales.kLOC_GENERAL_ID;
            default:
                return "";
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.currentType != PAYEE_LOOKUP && this.currentType != CATEGORY_LOOKUP && this.currentType != CLASS_LOOKUP && this.currentType != ID_LOOKUP) {
            return false;
        }
        int MENU_ADD = 1;
        menu.add(0, MENU_ADD, 0, "").setIcon(R.drawable.ic_arrow_drop_down_circle);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        final int theItem = this.currentType;
        Builder alert = new Builder(this);
        final EditText input = new EditText(this);
        alert.setTitle("");
        alert.setView(input);
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().trim();
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
            }
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
        return true;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (this.currentType == PAYEE_LOOKUP || this.currentType == CATEGORY_LOOKUP || this.currentType == CLASS_LOOKUP || this.currentType == ID_LOOKUP) {
            menu.add(0, CMENU_DELETE, 0, Locales.kLOC_GENERAL_DELETE);
            menu.add(0, CMENU_EDIT, 0, Locales.kLOC_LOOKUPS_RENAMEITEM);
            if (this.currentType == CATEGORY_LOOKUP) {
                menu.add(0, CMENU_SUBCATEGORY, 0, Locales.kLOC_ADDSUBCATEGORY);
            }
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final String originalString;
        Builder alert;
        final EditText input;
        switch (item.getItemId()) {
            case CMENU_EDIT /*1*/:
                final int theItem = this.currentType;
                originalString = this.theStrings.get(info.position);
                alert = new Builder(this);
                input = new EditText(this);
                input.setText(originalString);
                alert.setTitle(Locales.kLOC_LOOKUPS_RENAMEITEM);
                alert.setView(input);
                alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Builder b = new Builder(LookupsListActivity.this);
                        b.setTitle(Locales.kLOC_LOOKUPS_RENAMEITEM);
                        b.setMessage(Locales.kLOC_LOOKUPS_CHANGEBODY);
                        CharSequence charSequence = Locales.kLOC_LOOKUPS_POPUPLIST;
                        b.setPositiveButton(charSequence, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String value = input.getText().toString().trim();
                                switch (theItem) {
                                    case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                                        PayeeClass.renameFromToInDatabase(originalString, value, false);
                                        break;
                                    case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                                        CategoryClass.renameFromToInDatabase(originalString, value, false);
                                        break;
                                    case LookupsListActivity.CLASS_LOOKUP /*6*/:
                                        ClassNameClass.renameFromToInDatabase(originalString, value, false);
                                        break;
                                    case LookupsListActivity.ID_LOOKUP /*7*/:
                                        IDClass.renameFromToInDatabase(originalString, value, false);
                                        break;
                                }
                                LookupsListActivity.this.reloadData();
                            }
                        });
                        charSequence = Locales.kLOC_LOOKUPS_EVERYWHERE;
                        //editText = input;
                        //i = theItem;
                        //str = originalString;
                        b.setNegativeButton(charSequence, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String value = input.getText().toString().trim();
                                switch (theItem) {
                                    case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                                        PayeeClass.renameFromToInDatabase(originalString, value, true);
                                        break;
                                    case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                                        CategoryClass.renameFromToInDatabase(originalString, value, true);
                                        break;
                                    case LookupsListActivity.CLASS_LOOKUP /*6*/:
                                        ClassNameClass.renameFromToInDatabase(originalString, value, true);
                                        break;
                                    case LookupsListActivity.ID_LOOKUP /*7*/:
                                        IDClass.renameFromToInDatabase(originalString, value, true);
                                        break;
                                }
                                LookupsListActivity.this.reloadData();
                            }
                        });
                        b.create().show();
                    }
                });
                alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();
                ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE))).showSoftInput(input, ACCOUNT_TYPE_LOOKUP);
                return true;
            case CMENU_DELETE /*3*/:
                String value = this.theStrings.get(info.position);
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
                return true;
            case CMENU_SUBCATEGORY /*4*/:
                originalString = this.theStrings.get(info.position);
                alert = new Builder(this);
                input = new EditText(this);
                alert.setTitle(Locales.kLOC_ADDSUBCATEGORY);
                alert.setView(input);
                alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        CategoryClass.insertIntoDatabase(originalString + ":" + input.getText().toString().trim());
                        LookupsListActivity.this.reloadData();
                    }
                });
                alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();
                ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE))).showSoftInput(input, ACCOUNT_TYPE_LOOKUP);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
