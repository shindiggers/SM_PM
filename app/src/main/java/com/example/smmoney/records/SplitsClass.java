package com.example.smmoney.records;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import java.io.Serializable;

public class SplitsClass implements Serializable {
    private static String splitSelectionString = "SELECT transactionID, amount, xrate, categoryID, classID, memo, transferToAccountID, currencyCode FROM splits WHERE splitID=?";
    private double amount;
    private String category;
    private String className;
    private String currencyCode;
    private boolean deleted;
    public boolean dirty;
    public boolean hydrated;
    private String memo;
    int splitID;
    private int transactionID;
    private String transferToAccount;
    private int transferTransactionID;
    private double xrate;

    public int getTransactionID() {
        return this.transactionID;
    }

    public void setTransactionID(int id) {
        this.transactionID = id;
    }

    public void setAmount(double anAmount) {
        if (this.amount != anAmount) {
            this.dirty = true;
            this.amount = anAmount;
        }
    }

    public double getAmount() {
        hydrate();
        return this.amount;
    }

    public String amountAsString() {
        return CurrencyExt.amountAsString(getAmount());
    }

    public String amountAsCurrency() {
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            return CurrencyExt.amountAsCurrency(getAmount() / getXrate(), getCurrencyCode());
        }
        return CurrencyExt.amountAsCurrency(getAmount());
    }

    public void setXrate(double rate) {
        if (this.xrate != rate) {
            if (rate < 1.0E-8d) {
                rate = 1.0d;
            }
            this.dirty = true;
            this.xrate = rate;
        }
    }

    public double getXrate() {
        hydrate();
        if (this.xrate == 0.0d) {
            this.xrate = 1.0d;
        }
        return this.xrate;
    }

    private void setTransferTransactionID(int anID) {
        if (this.transferTransactionID != anID) {
            this.dirty = true;
            this.transferTransactionID = anID;
        }
    }

    int getTransferTransactionID() {
        hydrate();
        return this.transferTransactionID;
    }

    public void setCategory(String aString) {
        if (this.category != null || aString != null) {
            if (this.category != null && this.category.equals(aString)) {
                return;
            }
            if (aString == null || !aString.equals(Locales.kLOC_FILTERS_ALL_CATEGORIES)) {
                this.dirty = true;
                this.category = aString;
            }
        }
    }

    public String getCategory() {
        hydrate();
        return this.category;
    }

    public void setClassName(String aString) {
        if (this.className != null || aString != null) {
            if (this.className != null && this.className.equals(aString)) {
                return;
            }
            if (aString == null || !aString.equals(Locales.kLOC_FILTERS_ALL_CLASSES)) {
                this.dirty = true;
                this.className = aString;
            }
        }
    }

    public String getClassName() {
        hydrate();
        return this.className;
    }

    public void setMemo(String aString) {
        if (this.memo != null || aString != null) {
            if (this.memo == null || !this.memo.equals(aString)) {
                this.dirty = true;
                this.memo = aString;
            }
        }
    }

    public String getMemo() {
        hydrate();
        return this.memo;
    }

    public void setTransferToAccount(String aString) {
        if (this.transferToAccount != null || aString != null) {
            if (this.transferToAccount != null && this.transferToAccount.equals(aString)) {
                return;
            }
            if (aString == null || !aString.equals(Locales.kLOC_FILTERS_ALL_ACCOUNTS)) {
                this.dirty = true;
                this.transferToAccount = aString;
            }
        }
    }

    public String getTransferToAccount() {
        hydrate();
        return this.transferToAccount;
    }

    public void setCurrencyCode(String aString) {
        if (this.currencyCode != null || aString != null) {
            if (this.currencyCode == null || !this.currencyCode.equals(aString)) {
                this.dirty = true;
                this.currencyCode = aString;
            }
        }
    }

    public String getCurrencyCode() {
        hydrate();
        if (this.currencyCode == null || this.currencyCode.length() == 0) {
            setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE) == null ? "USD" : Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
        }
        while (this.currencyCode.length() < 3) {
            setCurrencyCode(this.currencyCode + " ");
        }
        return this.currencyCode;
    }

    public boolean isTransfer() {
        return (this.transferToAccount != null && this.transferToAccount.length() > 0) || this.transferTransactionID > 0;
    }

    public int getTransactionType() {
        if (isTransfer()) {
            if (getAmount() >= 0.0d) {
                return Enums.kTransactionTypeTransferFrom/*3*/;
            }
            return Enums.kTransactionTypeTransferTo/*2*/;
        } else if (getAmount() > 0.0d) {
            return Enums.kTransactionTypeDeposit/*1*/;
        } else {
            return Enums.kTransactionTypeWithdrawal/*0*/;
        }
    }

    public SplitsClass copy() {
        SplitsClass dup = new SplitsClass();
        dup.setTransactionID(getTransactionID());
        dup.setAmount(this.amount);
        dup.setXrate(this.xrate);
        dup.setTransferTransactionID(this.transferTransactionID);
        dup.setCategory(this.category);
        dup.setClassName(this.className);
        dup.setMemo(this.memo);
        dup.setTransferToAccount(this.transferToAccount);
        dup.setCurrencyCode(this.currencyCode);
        return dup;
    }

    public SplitsClass() {
        this.transactionID = 0;
        this.transferTransactionID = 0;
        this.xrate = 1.0d;
        this.category = "";
        this.className = "";
        this.memo = "";
        this.transferToAccount = "";
        this.currencyCode = "";
        this.hydrated = true;
        this.amount = 0.0d;
        this.category = "";
        this.transferToAccount = "";
        this.memo = "";
        this.className = "";
        this.currencyCode = "";
    }

    public SplitsClass(int pk) {
        this.transactionID = 0;
        this.transferTransactionID = 0;
        this.xrate = 1.0d;
        this.category = "";
        this.className = "";
        this.memo = "";
        this.transferToAccount = "";
        this.currencyCode = "";
        this.splitID = pk;
        this.dirty = false;
    }

    private static int insertNewRecordIntoDatabase() {
        ContentValues content = new ContentValues();
        content.put("transactionID", "''");
        long id = Database.insert(Database.SPLITS_TABLE_NAME, null, content);
        if (id == -1) {
            return 0;
        }
        return (int) id;
    }

    public void hydrate() {
        if (!this.hydrated) {
            new SQLiteQueryBuilder().setTables(Database.SPLITS_TABLE_NAME);
            String selection = "splitID=" + this.splitID;
            String[] projection = new String[]{"transactionID", "amount", "xrate", "categoryID", "classID", "memo", "transferToAccountID", "currencyCode"};
            Cursor curs = Database.rawQuery(splitSelectionString, new String[]{String.valueOf(this.splitID)});
            if (curs.getCount() > 0) {
                curs.moveToFirst();
                boolean wasDirty = this.dirty;
                int col = 1;
                setTransactionID(curs.getInt(0));
                int col2 = col + 1;
                setAmount(curs.getDouble(col));
                col = col2 + 1;
                setXrate(curs.getDouble(col2));
                col2 = col + 1;
                String str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setCategory(str);
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    str = "";
                }
                setClassName(str);
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setMemo(str);
                col = col2 + 1;
                setTransferToAccount(AccountClass.accountForID(curs.getInt(col2)));
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setCurrencyCode(str);
                if (!wasDirty && this.dirty) {
                    this.dirty = false;
                }
            } else {
                setAmount(0.0d);
                setXrate(1.0d);
            }
            curs.close();
            this.hydrated = true;
        }
    }

    private void dehydrate() {
        if (this.dirty) {
            ContentValues content = new ContentValues();
            int transferToAccountID = 0;
            if (this.transferToAccount != null && this.transferToAccount.length() > 0) {
                transferToAccountID = AccountClass.idForAccountElseAddIfMissing(this.transferToAccount, true);
            }
            content.put("transactionID", this.transactionID);
            content.put("amount", this.amount);
            content.put("xrate", this.xrate);
            content.put("categoryID", this.category);
            content.put("classID", this.className);
            content.put("memo", this.memo);
            content.put("transferToAccountID", transferToAccountID);
            content.put("currencyCode", this.currencyCode);
            Database.update(Database.SPLITS_TABLE_NAME, content, "splitID=" + this.splitID, null);
            this.dirty = false;
        }
        this.hydrated = false;
    }

    public void saveToDatabase() {
        if (this.dirty) {
            if (this.splitID == 0) {
                this.splitID = insertNewRecordIntoDatabase();
            }
            dehydrate();
        }
    }

    public void deleteFromDatabase() {
        if (this.splitID != 0) {
            Database.delete(Database.SPLITS_TABLE_NAME, "splitID=" + this.splitID, null);
        }
    }
}
