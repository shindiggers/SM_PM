package com.example.smmoney.records;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.iAP.util.Base64;
import com.example.smmoney.iAP.util.Base64DecoderException;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.misc.TransactionTransferRetVals;
import com.example.smmoney.views.desktopsync.PocketMoneySyncClass;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.SAXParserFactory;

public class TransactionClass extends PocketMoneyRecordClass implements Serializable {
    public static final String XML_LISTTAG_TRANSACTIONS = "TRANSACTIONS";
    public static final String XML_RECORDTAG_TRANSACTION = "TRANSACTION";
    private static String renameclass_statement = null;
    private static String renameclasstimestamp_statement = null;
    private static String renameid_statement = null;
    private static String renameidtimestamp_statement = null;
    private static String renamepayee_statement = null;
    private static String serverIDSelectionString = "SELECT transactionID FROM transactions WHERE serverID=?";
    private static String transactionSelectionString = "SELECT deleted, timestamp, type, date, cleared, accountID, payee, checkNumber, ofxID, image, subTotal, serverID FROM transactions WHERE transactionID=?";
    private String account;
    private String checkNumber;
    private boolean cleared;
    private String currentElementValue;
    private int currentImage;
    private int currentIndex;
    private byte[] data;
    private String dataString;
    private GregorianCalendar date;
    private boolean hydratedSplits;
    private int imageCount;
    private ArrayList<Integer> imageCounts;
    private String imageLocation;
    private int imageSize;
    private boolean isNew;
    public boolean isRepeatingTransaction;
    private String ofxID;
    private SplitsClass parserSplit;
    private String payee;
    private boolean readingInImage;
    public double runningBalance;
    private StringBuilder sb;
    private ArrayList<SplitsClass> splits;
    private ArrayList<SplitsClass> splitsDeleted;
    private double subTotal;
    public int transactionID;
    private int type;

    public boolean getDirty() {
        if (this.dirty) {
            return this.dirty;
        }
        for (SplitsClass split : this.splits) {
            if ((split).dirty) {
                return true;
            }
        }
        return false;
    }

    public int getTransactionID() {
        return this.transactionID;
    }

    public void setType(int atype) {
        if (this.type != atype) {
            this.dirty = true;
            this.type = atype;
        }
    }

    public int getType() {
        hydrate();
        return this.type;
    }

    public void initType() {
        if (getTransferToAccount() == null || getTransferToAccount().length() <= 0) {
            if (this.subTotal > 0.0d) {
                setType(Enums.kTransactionTypeDeposit); /*1*/
            } else {
                setType(Enums.kTransactionTypeWithdrawal); /*0*/
            }
        } else if (getSubTotal() > 0.0d) {
            setType(Enums.kTransactionTypeTransferFrom); /*3*/
        } else {
            setType(Enums.kTransactionTypeTransferTo); /*2*/
        }
    }

    public void setCleared(boolean clearIt) {
        if (this.cleared != clearIt) {
            this.dirty = true;
            this.cleared = clearIt;
        }
    }

    public boolean getCleared() {
        hydrate();
        return this.cleared;
    }

    public void setDate(GregorianCalendar aDate) {
        if (this.date != null || aDate != null) {
            if (this.date == null || !this.date.equals(aDate)) {
                this.dirty = true;
                this.date = aDate;
            }
        }
    }

    public GregorianCalendar getDate() {
        hydrate();
        return this.date;
    }

    public void setDateFromString(String aDate) {
        GregorianCalendar g = CalExt.dateFromDescriptionWithMediumDate(aDate);
        g.set(Calendar.MINUTE, this.date.get(Calendar.MINUTE));
        g.set(Calendar.HOUR_OF_DAY, this.date.get(Calendar.HOUR_OF_DAY));
        setDate(g);
    }

    public void updateDateWithTimeString(String str) {
        GregorianCalendar g = CalExt.dateFromDescriptionWithTime(str);
        GregorianCalendar date = getDate();
        date.set(Calendar.MINUTE, g.get(Calendar.MINUTE));
        date.set(Calendar.HOUR_OF_DAY, g.get(Calendar.HOUR_OF_DAY));
        setDate(date);
    }

    public void setAccount(String aString) {
        if (this.account != null || aString != null) {
            if (this.account != null && this.account.equals(aString)) {
                return;
            }
            if (aString == null || !aString.equals(Locales.kLOC_FILTERS_ALL_ACCOUNTS)) {
                this.dirty = true;
                this.account = aString;
            }
        }
    }

    public String getAccount() {
        hydrate();
        return this.account;
    }

    public void setCheckNumber(String aString) {
        if (this.checkNumber != null || aString != null) {
            if (this.checkNumber == null || !this.checkNumber.equals(aString)) {
                this.dirty = true;
                this.checkNumber = aString;
            }
        }
    }

    public String getCheckNumber() {
        hydrate();
        return this.checkNumber;
    }

    public void setOfxID(String aString) {
        if (this.ofxID != null || aString != null) {
            if (this.ofxID == null || !this.ofxID.equals(aString)) {
                this.dirty = true;
                this.ofxID = aString;
            }
        }
    }

    public String getOfxID() {
        hydrate();
        return this.ofxID;
    }

    public void setImageLocation(String aString) {
        if (this.imageLocation != null || aString != null) {
            if (this.imageLocation == null || !this.imageLocation.equals(aString)) {
                this.dirty = true;
                this.imageLocation = aString;
            }
        }
    }

    public String getImageLocation() {
        hydrate();
        return this.imageLocation;
    }

    public void setPayee(String aString) {
        if (this.payee != null || aString != null) {
            if (this.payee == null || !this.payee.equals(aString)) {
                this.dirty = true;
                this.payee = aString;
            }
        }
    }

    public String getPayee() {
        hydrate();
        return this.payee;
    }

    public void setSubTotal(double amount) {
        if (this.subTotal != amount) {
            this.dirty = true;
            this.subTotal = amount;
        }
    }

    public double getSubTotal() {
        if (this.subTotal == 0.0d) {
            hydrate();
        }
        return this.subTotal;
    }

    private void adjustSubTotal(double amount) {
        this.subTotal += amount;
    }

    public String subTotalAsString() {
        if (0.0d == this.subTotal) {
            return "";
        }
        return Double.toString(this.subTotal);
    }

    public String subTotalAsABSString() {
        if (this.subTotal == 0.0d) {
            return null;
        }
        return Double.toString(Math.abs(this.subTotal));
    }

    public String subTotalAsCurrency() {
        if (!Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            return CurrencyExt.amountAsCurrency(getSubTotal());
        }
        if (Prefs.getBooleanPref(Prefs.TRANSACTIONS_SHOW_FOREIGNAMOUNT)) {
            return CurrencyExt.amountAsCurrency(getSubTotal() / getXrate(), getCurrencyCode());
        }
        try {
            return CurrencyExt.amountAsCurrency(getSubTotal(), AccountDB.recordFor(getAccount()).getCurrencyCode());
        } catch (Exception e) {
            return CurrencyExt.amountAsCurrency(getSubTotal(), Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
        }
    }

    public String runningBalanceAsCurrency() {
        if (!Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            return CurrencyExt.amountAsCurrency(this.runningBalance);
        }
        AccountClass a1 = AccountDB.recordFor(getAccount());
        return CurrencyExt.amountAsCurrency(this.runningBalance, a1 == null ? Prefs.getStringPref(Prefs.HOMECURRENCYCODE) : a1.getCurrencyCode());
    }

    public int splitIndexOfSplitID(int splitID) {
        int retVal = 0;
        hydrate();
        for (SplitsClass split : this.splits) {
            if ((split).splitID == splitID) {
                return retVal;
            }
            retVal++;
        }
        return -1;
    }

    public void addSplit(SplitsClass aSplit) {
        this.splits.add(aSplit);
    }

    public ArrayList<SplitsClass> getSplits() {
        hydrate();
        return this.splits;
    }

    public void setSplits(ArrayList<SplitsClass> newSplits) {
        if (this.splits != newSplits) {
            this.dirty = true;
            this.splits = newSplits;
        }
    }

    public void setAmount(double amount) {
        setAmountAtIndex(amount, 0);
    }

    public void setAmountAtIndex(double amount, int index) {
        getSplits().get(index).setAmount(amount);
    }

    public double getAmount() {
        return getAmountAtIndex(0);
    }

    public double getAmountAtIndex(int index) {
        hydrate();
        if (getSplits().size() <= 0 || this.splits.size() <= index) {
            return 0.0d;
        }
        return this.splits.get(index).getAmount();
    }

    public void setXrate(double rate) {
        setXrateAtIndex(rate, 0);
    }

    public void setXrateAtIndex(double rate, int index) {
        getSplits().get(index).setXrate(rate);
    }

    public double getXrate() {
        return getXrateAtIndex(0);
    }

    public double getXrateAtIndex(int index) {
        hydrate();
        if (getSplits().size() <= 0 || this.splits.size() <= index) {
            return 1.0d;
        }
        return this.splits.get(index).getXrate();
    }

    public boolean isSingleXrate() {
        String currencyCode = getSplits().get(0).getCurrencyCode();
        for (SplitsClass splitsClass : getSplits()) {
            if (!(splitsClass).getCurrencyCode().equals(currencyCode)) {
                return false;
            }
        }
        return true;
    }

    public int getTransferTransactionID() {
        hydrate();
        if (getSplits().size() > 0) {
            return this.splits.get(0).getTransferTransactionID();
        }
        return 0;
    }

    public void setCategory(String category) {
        setCategoryAtIndex(category, 0);
    }

    public void setCategoryAtIndex(String category, int index) {
        getSplits().get(index).setCategory(category);
    }

    public String getCategory() {
        return getCategoryAtIndex(0);
    }

    public String getCategoryAtIndex(int index) {
        hydrate();
        if (getSplits() == null || this.splits.size() <= 0) {
            return null;
        }
        return this.splits.get(index).getCategory();
    }

    public void setClassName(String classname) {
        setClassNameAtIndex(classname, 0);
    }

    public void setClassNameAtIndex(String classname, int index) {
        getSplits().get(index).setClassName(classname);
    }

    public String getClassName() {
        return getClassNameAtIndex(0);
    }

    public String getClassNameAtIndex(int index) {
        hydrate();
        if (getSplits() == null || getSplits().size() <= 0 || this.splits.size() <= index) {
            return null;
        }
        return this.splits.get(index).getClassName();
    }

    public void setMemo(String memo) {
        setMemoAtIndex(memo, 0);
    }

    public void setMemoAtIndex(String memo, int index) {
        getSplits().get(index).setMemo(memo);
    }

    public String getMemo() {
        return getMemoAtIndex(0);
    }

    public String getMemoAtIndex(int index) {
        hydrate();
        if (getSplits() == null || getSplits().size() <= 0 || this.splits.size() <= index) {
            return null;
        }
        return this.splits.get(index).getMemo();
    }

    public void setTransferToAccount(String anAccount) {
        setTransferToAccountAtIndex(anAccount, 0);
    }

    public void setTransferToAccountAtIndex(String anAccount, int index) {
        getSplits().get(index).setTransferToAccount(anAccount);
    }

    public String getTransferToAccount() {
        return getTransferToAccountAtIndex(0);
    }

    public String getTransferToAccountAtIndex(int index) {
        hydrate();
        if (getSplits() == null || getSplits().size() <= 0 || this.splits.size() <= index) {
            return null;
        }
        return this.splits.get(index).getTransferToAccount();
    }

    public void setCurrencyCode(String code) {
        setCurrencyCodeAtIndex(code, 0);
    }

    public void setCurrencyCodeAtIndex(String code, int index) {
        getSplits().get(index).setCurrencyCode(code);
    }

    public String getCurrencyCode() {
        return getCurrencyCodeAtIndex(0);
    }

    public String getCurrencyCodeAtIndex(int index) {
        if (getSplits() == null || getSplits().size() <= 0 || this.splits.size() <= index) {
            return Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        return this.splits.get(index).getCurrencyCode();
    }

    public boolean multipleClassNames() {
        hydrate();
        if (this.splits.size() < 2) {
            return false;
        }
        int splitNumber = 0;
        for (SplitsClass split : this.splits) {
            if (!(split.getClassName() == null || split.getClassName().equals(""))) {
                splitNumber++;
            }
        }
        String firstClass = this.splits.get(0).getClassName();
        return splitNumber > 1 || firstClass == null || firstClass.equals("");
    }

    public int getNumberOfSplits() {
        return getSplits().size();
    }

    public double getSplitsTotal() {
        double splitsTotal = 0.0d;
        for (SplitsClass splitsClass : getSplits()) {
            splitsTotal += (splitsClass).getAmount();
        }
        return splitsTotal;
    }

    public void adjustSplits() {
        if (getSplitsTotal() == getSubTotal()) {
            return;
        }
        if (getNumberOfSplits() <= 1) {
            setAmount(getSubTotal());
            return;
        }
        SplitsClass split = new SplitsClass();
        split.setCategory("Miscellaneous");
        split.setAmount(getSubTotal() - getSplitsTotal());
        this.splits.add(split);
    }

    public void deleteSplitAtIndex(int index) {
        this.dirty = true;
        if (this.splitsDeleted == null) {
            this.splitsDeleted = new ArrayList<>();
        }
        this.splitsDeleted.add(getSplits().get(index));
        this.splits.remove(index);
        if (this.splits.size() == 0) {
            SplitsClass newSplit = new SplitsClass();
            newSplit.setCurrencyCode(this.splitsDeleted.get(0).getCurrencyCode());
            this.splits.add(newSplit);
        }
    }

    public boolean isTransfer() {
        return getType() == Enums.kTransactionTypeTransferTo /*2*/ || this.type == Enums.kTransactionTypeTransferFrom /*3*/;
    }

    public boolean isDeposit() {
        return getType() == Enums.kTransactionTypeDeposit /*1*/;
    }

    public boolean isWithdrawal() {
        return getType() == Enums.kTransactionTypeWithdrawal /*0*/;
    }

    public void checkAccountAddIfMissing() {
        if (AccountClass.idForAccount(this.account) == 0) {
            AccountClass.insertIntoDatabase(this.account);
        }
    }

    public ArrayList<String> imageFileNames() {
        if (getImageLocation() == null) {
            return null;
        }
        String[] strings = getImageLocation().split(";");
        ArrayList<String> retStrings = new ArrayList<>(strings.length);
        for (Object add : strings) {
            if (strings[0].length() > 0) {
                retStrings.add((String) add);
            }
        }
        return retStrings;
    }

    public TransactionClass copy() {
        TransactionClass dup = new TransactionClass();
        dup.setDeleted(getDeleted());
        dup.timestamp = this.timestamp;
        dup.setType(this.type);
        dup.isRepeatingTransaction = this.isRepeatingTransaction;
        dup.setDate((GregorianCalendar) this.date.clone());
        dup.setCleared(this.cleared);
        dup.setAccount(this.account);
        dup.setPayee(this.payee);
        dup.setCheckNumber(this.checkNumber);
        dup.setOfxID(this.ofxID);
        dup.setImageLocation(this.imageLocation);
        dup.setSubTotal(this.subTotal);
        dup.getSplits().clear();
        for (SplitsClass split : this.splits) {
            SplitsClass dupSplit = split.copy();
            dupSplit.setTransactionID(split.getTransactionID());
            dup.getSplits().add(dupSplit);
        }
        return dup;
    }

    public TransactionClass() {
        this.transactionID = 0;
        this.date = null;
        this.cleared = false;
        this.account = "";
        this.payee = "";
        this.checkNumber = "";
        this.ofxID = "";
        this.imageLocation = "";
        this.splits = new ArrayList<>();
        this.splitsDeleted = new ArrayList<>();
        this.currentImage = 0;
        this.readingInImage = false;
        this.currentIndex = 0;
        this.sb = new StringBuilder();
        this.hydrated = true;
        this.transactionID = 0;
        this.date = new GregorianCalendar();
        this.date.setTimeInMillis(System.currentTimeMillis());
        this.subTotal = 0.0d;
        this.account = null;
        this.payee = null;
        this.checkNumber = null;
        this.cleared = false;
        this.isRepeatingTransaction = false;
        this.splits = new ArrayList<>();
        this.splits.add(new SplitsClass());
        this.dirty = false;
        this.imageLocation = null;
    }

    public TransactionClass(int pk) {
        this.transactionID = 0;
        this.date = null;
        this.cleared = false;
        this.account = "";
        this.payee = "";
        this.checkNumber = "";
        this.ofxID = "";
        this.imageLocation = "";
        this.splits = new ArrayList<>();
        this.splitsDeleted = new ArrayList<>();
        this.currentImage = 0;
        this.readingInImage = false;
        this.currentIndex = 0;
        this.sb = new StringBuilder();
        this.transactionID = pk;
        this.hydrated = false;
        this.dirty = false;
    }

    public static void renameCategoryFromTo(String oldCat, String newCat) {
        if (oldCat == null) {
            oldCat = "";
        }
        if (newCat == null) {
            newCat = "";
        }
        Database.execSQL("UPDATE transactions SET timestamp=" + System.currentTimeMillis() + " WHERE transactionID IN (SELECT transactionID FROM splits WHERE categoryID LIKE " + Database.SQLFormat(oldCat) + " ESCAPE '\\')");
        Database.execSQL("UPDATE splits SET categoryID=" + Database.SQLFormat(newCat) + " WHERE categoryID LIKE " + Database.SQLFormat(oldCat) + " ESCAPE '\\'");
    }

    static void renamePayeeFromTo(String fromPayee, String toPayee) {
        if (fromPayee == null) {
            fromPayee = "";
        }
        if (toPayee == null) {
            toPayee = "";
        }
        if (renamepayee_statement == null) {
            renamepayee_statement = "UPDATE transactions SET payee=?, timestamp=? WHERE payee LIKE ?";
        }
        Database.execSQL(renamepayee_statement, new String[]{toPayee, String.valueOf((new GregorianCalendar().getTimeInMillis() / 1000)), fromPayee});
    }

    static void renameClassFromTo(String fromText, String toText) {
        if (fromText == null) {
            fromText = "";
        }
        if (toText == null) {
            toText = "";
        }
        if (renameclasstimestamp_statement == null) {
            renameclasstimestamp_statement = "UPDATE transactions SET timestamp=? WHERE transactionID IN (SELECT transactionID FROM splits WHERE classID LIKE ?)";
        }
        if (renameclass_statement == null) {
            renameclass_statement = "UPDATE splits SET classID=? WHERE classID LIKE ?";
        }
        Database.execSQL(renameclasstimestamp_statement, new String[]{String.valueOf((new GregorianCalendar().getTimeInMillis() / 1000)), fromText});
        Database.execSQL(renameclass_statement, new String[]{toText, fromText});
    }

    static void renameIDFromTo(String fromText, String toText) {
        if (fromText == null) {
            fromText = "";
        }
        if (toText == null) {
            toText = "";
        }
        if (renameidtimestamp_statement == null) {
            renameidtimestamp_statement = "UPDATE transactions SET timestamp=? WHERE transactionID IN (SELECT transactionID FROM splits WHERE checkNumber LIKE ?)";
        }
        if (renameid_statement == null) {
            renameid_statement = "UPDATE transactions SET checkNumber=?, timestamp=? WHERE checkNumber LIKE ?";
        }
        Database.execSQL(renameidtimestamp_statement, new String[]{String.valueOf((new GregorianCalendar().getTimeInMillis() / 1000)), fromText});
        Database.execSQL(renameid_statement, new String[]{toText, String.valueOf((new GregorianCalendar().getTimeInMillis() / 1000)), fromText});
    }

    private static int insertNewRecordIntoDatabase() {
        ContentValues content = new ContentValues();
        content.put("deleted", 0);
        content.put("serverID", Database.newServerID());
        long id = Database.insert(Database.TRANSACTIONS_TABLE_NAME, null, content);
        if (id == -1) {
            return 0;
        }
        return (int) id;
    }

    public static boolean importedTransactionExists(TransactionClass transaction) {
        String clearedString;
        new SQLiteQueryBuilder().setTables(Database.TRANSACTIONS_TABLE_NAME);
        int accountID = AccountDB.uniqueID(transaction.account);
        if (transaction.getCleared()) {
            clearedString = "1";
        } else {
            clearedString = "0";
        }
        Cursor c = Database.rawQuery("SELECT transactionID FROM transactions WHERE deleted=0 AND type=" + transaction.type + " AND accountID=" + accountID + " AND date >= " + CalExt.beginningOfDay(transaction.getDate()).getTimeInMillis() + " AND date <= " + CalExt.endOfDay(transaction.getDate()).getTimeInMillis() + " AND ((payee ISNULL AND (? ISNULL OR LENGTH(?)=0)) OR (payee LIKE ?) OR (? ISNULL AND LENGTH(payee)=0)) AND subtotal=" + transaction.getSubTotal() + " AND ((checkNumber ISNULL AND (? ISNULL OR LENGTH(?)=0)) OR (checkNumber LIKE ?) OR (? ISNULL AND LENGTH(checkNumber)=0)) AND cleared=" + clearedString, new String[]{transaction.getPayee(), transaction.getPayee(), transaction.getPayee(), transaction.getPayee(), transaction.getCheckNumber(), transaction.getCheckNumber(), transaction.getCheckNumber(), transaction.getCheckNumber()});
        boolean foundMatch = c.moveToFirst();
        c.close();
        return foundMatch;
    }

    public static TransactionClass recordWithServerID(String serverID) {
        TransactionClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery(serverIDSelectionString, new String[]{serverID});
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new TransactionClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public void deleteSplitsfromDatabasePermentantly() {
        if (this.transactionID != 0) {
            String deletionString = "DELETE FROM splits WHERE transactionID=?";
            Database.execSQL(deletionString, new String[]{String.valueOf(this.transactionID)});
        }
    }

    public void hydrate() {
        if (!this.hydrated) {
            new SQLiteQueryBuilder().setTables(Database.TRANSACTIONS_TABLE_NAME);
            String selection = "transactionID=" + this.transactionID;
            String[] projection = new String[]{"deleted", "timestamp", "type", "date", "cleared", "accountID", "payee", "checkNumber", "ofxID", "image", "subTotal", "serverID"};
            Cursor curs = Database.rawQuery(transactionSelectionString, new String[]{String.valueOf(this.transactionID)});
            if (curs.getCount() != 0) {
                curs.moveToFirst();
                boolean wasDirty = this.dirty;
                int col = 1;
                setDeleted(curs.getInt(0) == 1);
                this.timestamp = new GregorianCalendar();
                int col2 = col + 1;
                this.timestamp.setTimeInMillis(((long) curs.getDouble(col)) * 1000);
                col = col2 + 1;
                setType(curs.getInt(col2));
                this.date = new GregorianCalendar();
                col2 = col + 1;
                long tempdate = (long) curs.getDouble(col);
                Log.d("@@@@@@@@@@@@@@@@@@@"," before x 1000 = "+tempdate);
                Log.d("@@@@@@@@@@@@@@@@@@@"," after x 1000 = "+(tempdate*1000));
                this.date.setTimeInMillis(1000 * ((long) curs.getDouble(col)));
                col = col2 + 1;
                setCleared(curs.getInt(col2) == 1);
                col2 = col + 1;
                setAccount(AccountClass.accountForID(curs.getInt(col)));
                col = col2 + 1;
                String str = curs.getString(col2);
                if (str == null) {
                    str = "";
                }
                setPayee(str);
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setCheckNumber(str);
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    str = "";
                }
                setOfxID(str);
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setImageLocation(str);
                col = col2 + 1;
                setSubTotal(curs.getDouble(col2));
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setServerID(str);
                hydrateSplits();
                if (!wasDirty && this.dirty) {
                    this.dirty = false;
                }
            } else {
                if (this.timestamp == null) {
                    this.timestamp = new GregorianCalendar();
                }
                if (this.date == null) {
                    setDate(new GregorianCalendar());
                    setType(0);
                }
            }
            curs.close();
            this.hydrated = true;
            if (this.type == 5) {
                this.isRepeatingTransaction = true;
                initType(); // this changes type from '5' (ie repeating) to '0' (ie withdrawal) or whatever type it is
                return;
            }
            this.isRepeatingTransaction = false;
        }
    }

    private void hydrateSplits() {
        if (!this.hydratedSplits) {
            new SQLiteQueryBuilder().setTables(Database.SPLITS_TABLE_NAME);
            String selection = "transactionID=" + this.transactionID;
            String[] projection = new String[]{"splitID"};
            String hydrateSplitsString = "SELECT splitID FROM splits WHERE transactionID=?";
            Cursor curs = Database.rawQuery(hydrateSplitsString, new String[]{String.valueOf(this.transactionID)});
            if (curs.getCount() != 0) {
                if (this.splits == null) {
                    this.splits = new ArrayList<>();
                }
                while (curs.moveToNext()) {
                    SplitsClass split = new SplitsClass(curs.getInt(0));
                    split.hydrate();
                    this.splits.add(split);
                }
            }
            this.hydratedSplits = true;
            curs.close();
        }
    }

    public void dehydrateAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (getDirty()) {
            int id = AccountClass.idForAccount(this.account);
            if (id == 0) {
                if (getDeleted()) {
                    id = AccountClass.idForAccount(true, this.account);
                }
                if (id == 0) {
                    AccountClass.insertIntoDatabase(this.account);
                }
            }
            dehydratesplits();
            ContentValues content = new ContentValues();
            content.put("deleted", this.deleted);
            String str = "timestamp";
            long currentTimeMillis = (updateTimeStamp || this.timestamp == null) ? System.currentTimeMillis() / 1000 : this.timestamp.getTimeInMillis() / 1000;
            content.put(str, currentTimeMillis);
            content.put("type", this.type);
            content.put("date", this.date.getTimeInMillis() / 1000);
            content.put("cleared", this.cleared);
            content.put("accountID", id);
            content.put("payee", this.payee != null ? this.payee : "");
            content.put("checkNumber", this.checkNumber);
            content.put("ofxID", this.ofxID);
            content.put("image", this.imageLocation);
            content.put("subTotal", this.subTotal);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            content.put("serverID", this.serverID);
            Database.update(Database.TRANSACTIONS_TABLE_NAME, content, "transactionID=" + this.transactionID, null);
            this.dirty = false;
        }
        this.hydrated = false;
    }

    private void dehydratesplits() {
        for (SplitsClass split : this.splits) {
            split.setTransactionID(this.transactionID);
            split.dirty = true;
            split.saveToDatabase();
        }
        this.splits = null;
        if (this.splitsDeleted != null) {
            for (SplitsClass split : this.splitsDeleted) {
                if (split.splitID > 0) {
                    split.deleteFromDatabase();
                }
            }
            this.splitsDeleted = null;
        }
        this.hydratedSplits = false;
    }

    public void deleteFromDatabase() {
        if (this.transactionID != 0) {
            ContentValues content = new ContentValues();
            content.put("deleted", Boolean.TRUE);
            content.put("timestamp", ((double) System.currentTimeMillis()) / 1000.0d);
            if (Database.update(Database.TRANSACTIONS_TABLE_NAME, content, "transactionID=" + this.transactionID, null) != 1) {
                Log.e("SMMoney", "error delete transactionID=" + this.transactionID);
            }
            Database.delete(Database.SPLITS_TABLE_NAME, "transactionID=" + this.transactionID, null);
        }
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.transactionID == 0) {
            this.transactionID = insertNewRecordIntoDatabase();
        }
        dehydrateAndUpdateTimeStamp(updateTimeStamp);
    }

    public void transactionDelete() {
        int transferRecID;
        if (getTransactionID() != 0) {
            for (SplitsClass split : getSplits()) {
                AccountClass act = AccountDB.recordFor(split.getTransferToAccount() == null ? "" : split.getTransferToAccount());
                boolean regularTransfer = split.getCurrencyCode().equals(act == null ? "" : act.getCurrencyCode());
                int transferSplitItem;
                TransactionTransferRetVals ret = new TransactionTransferRetVals();
                TransactionDB.transactionGetTransfer(split.getTransferToAccount(), getAccount(), getDate(), regularTransfer ? (-1.0d * split.getAmount()) / split.getXrate() : -1.0d * split.getAmount(), regularTransfer ? null : split.getCurrencyCode(), ret);
                transferRecID = ret.transferRecID;
                transferSplitItem = ret.transferSplitItem;
                if (transferRecID != 0) {
                    TransactionClass transferRec = new TransactionClass(transferRecID);
                    if (transferRec.getNumberOfSplits() > 1) {
                        transferRec.setTransferToAccountAtIndex(null, transferSplitItem);
                        transferRec.adjustSubTotal(-1.0d * transferRec.getAmountAtIndex(transferSplitItem));
                        transferRec.setAmountAtIndex(0.0d, transferSplitItem);
                    } else {
                        transferRec.setDeleted(true);
                    }
                    transferRec.saveToDatabase();
                }
            }
            setDeleted(true);
            saveToDatabase();
        }
    }

    public void updateWithXML(String xmlTransaction) {
        if (xmlTransaction.startsWith("<images>")) {
            xmlTransaction = xmlTransaction.replaceAll("[\r\n]", "");
        }
        int index = xmlTransaction.indexOf(10);
        try {
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(new StringReader(xmlTransaction));
            xr.setContentHandler(this);
            xr.parse(is);
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error parsing xml");
        }
    }

    public void updateWithXMLFile(File f) {
        try {
            BufferedInputStream fi = new BufferedInputStream(new FileInputStream(f.getAbsolutePath()));
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(fi);
            xr.setContentHandler(this);
            xr.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        if (!localName.equals(XML_RECORDTAG_TRANSACTION)) {
            switch (localName) {
                case Database.SPLITS_TABLE_NAME:
                    if (this.splits == null) {
                        this.splits = new ArrayList<>();
                    }
                    this.splits.clear();
                    break;
                case "split":
                    this.parserSplit = new SplitsClass();
                    break;
                case "images":
                    setImageLocation("");
                    break;
                default:
                    localName.equals("image");
                    break;
            }
        }
        this.currentElementValue = null;
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        boolean z = false;
        if (this.currentElementValue == null) {
            this.currentElementValue = "";
        }
        switch (localName) {
            case "transactionid":
                this.transactionID = Integer.parseInt(this.currentElementValue);
                break;
            case "deleted":
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setDeleted(z);
                break;
            case "timestamp":
                this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
                break;
            case "date":
                setDate(CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue));
                break;
            case "subTotal":
            case "subtotal":
                setSubTotal(Double.parseDouble(this.currentElementValue));
                break;
            case "imagedata":
                try {
                    this.data = Base64.decode(this.currentElementValue);
                } catch (Base64DecoderException e) {
                    e.printStackTrace();
                }
                break;
            case "filename":
                if (this.data != null) {
                    try {
                        new File(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/").mkdirs();
                        FileOutputStream fos = new FileOutputStream(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/" + this.currentElementValue);
                        int length = this.data.length;
                        int loops = (length / 500000) + 1;
                        for (int i = 0; i < loops; i++) {
                            if ((i + 1) * 500000 > length) {
                                fos.write(this.data, i * 500000, length % 500000);
                            } else {
                                fos.write(this.data, i * 500000, 500000);
                            }
                        }
                        fos.flush();
                        fos.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if (this.currentElementValue != null) {
                    if (this.imageLocation == null) {
                        this.imageLocation = this.currentElementValue;
                    } else {
                        this.imageLocation += this.currentElementValue + ";";
                    }
                }
                this.data = null;
                break;
            case "cleared":
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setCleared(z);
                break;
            case "type":
                setType(Integer.parseInt(this.currentElementValue));
                break;
            case "split":
                getSplits().add(this.parserSplit);
                this.parserSplit = null;
                break;
            case "amount":
                this.parserSplit.setAmount(Double.parseDouble(this.currentElementValue));
                break;
            case "xrate":
                this.parserSplit.setXrate(Double.parseDouble(this.currentElementValue));
                break;
            case "image":
                if (!(this.currentElementValue == null || this.currentElementValue.length() <= 0 || this.currentElementValue.contains("\n"))) {
                    setImageLocation(URLDecoder.decode(this.currentElementValue));
                }
                break;
            case "serverID":
                setServerID(this.currentElementValue);
                break;
            case "account":
            case "checkNumber":
            case "ofxID":
            case "overdraftID":
            case "payee": {
                Class c = getClass();
                try {
                    Field f = c.getDeclaredField(localName);
                    f.setAccessible(true);
                    f.set(this, URLDecoder.decode(this.currentElementValue));
                } catch (Exception e3) {
                    Log.i(SMMoney.TAG, "Invalid tag parsing " + c.getName() + " xml[" + localName + "]");
                }
                break;
            }
            case "class":
                this.parserSplit.setClassName(URLDecoder.decode(this.currentElementValue));
                break;
            case "currencyCode":
            case "transferToAccount":
            case "memo":
            case "category": {
                Class c = this.parserSplit.getClass();
                try {
                    Field f = c.getDeclaredField(localName);
                    f.setAccessible(true);
                    f.set(this.parserSplit, URLDecoder.decode(this.currentElementValue));
                } catch (Exception e4) {
                    Log.i(SMMoney.TAG, "Invalid tag parsing " + c.getName() + " xml[" + localName + "]");
                }
                break;
            }
        }
        this.currentElementValue = null;
    }

    public void characters(char[] ch, int start, int length) {
        if (this.currentElementValue == null) {
            this.currentElementValue = new String(ch, start, length);
            return;
        }
        String buffer = this.currentElementValue.concat(new String(ch, start, length));
        this.currentElementValue = null;
        this.currentElementValue = buffer;
    }

    private void addText(XmlSerializer body, String text) throws IOException {
        if (text == null) {
            text = "";
        }
        body.text(text);
    }

    private void addTextWithEncoding(XmlSerializer body, String text) throws IOException {
        body.text(text == null ? "" : encode(text));
    }

    public static boolean writeXMLStringWithFileNameToStream(BufferedOutputStream output, String fileName, boolean withImages) {
        return false;
    }

    public static String XMLStringWithFileNameToString(String fileName, boolean withImages) {
        StringBuilder sb = new StringBuilder();
        sb.append("<image>");
        if (withImages) {
            try {
                String imgdata = "<imagedata>";
                File f = new File(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/" + fileName);
                if (f.exists()) {
                    FileInputStream fin = new FileInputStream(f.getAbsolutePath());
                    int totalRead = 0;
                    int read = 0;
                    int size = (int) f.length();
                    byte[] data = new byte[size];
                    while (totalRead < size && read != -1) {
                        read = fin.read(data, totalRead, size - totalRead);
                        totalRead += read;
                    }
                    if (data != null) {
                        imgdata = imgdata + Base64.encode(data) + "</imagedata>";
                    }
                    sb.append(imgdata);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sb.append("<filename>");
        sb.append(fileName);
        sb.append("</filename>");
        sb.append("</image>");
        return sb.toString();
    }

    private static void XMLStringWithFileName(XmlSerializer body, String fileName, boolean withImages) {
        try {
            body.startTag(null, "image");
            if (withImages) {
                File f = new File(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/" + fileName);
                if (f.exists()) {
                    FileInputStream fin = new FileInputStream(f.getAbsolutePath());
                    int totalRead = 0;
                    int read = 0;
                    int size = (int) f.length();
                    byte[] data = new byte[size];
                    while (totalRead < size && read != -1) {
                        read = fin.read(data, totalRead, size - totalRead);
                        totalRead += read;
                    }
                    if (data != null) {
                        body.startTag(null, "imagedata");
                        String hmmm = Base64.encode(data);
                        PocketMoneySyncClass.printToFile(hmmm, "image.out");
                        body.text(hmmm);
                        body.endTag(null, "imagedata");
                    }
                }
            }
            body.startTag(null, "filename");
            body.text(fileName);
            body.endTag(null, "filename");
            body.endTag(null, "image");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String XMLStringWithImages(boolean withImages) {
        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            public void write(int b) {
                this.string.append((char) b);
            }

            public String toString() {
                return this.string.toString();
            }
        };
        XmlSerializer body = Xml.newSerializer();
        try {
            String descriptionWithISO861Date;
            body.setOutput(output, "UTF-8");
            body.startTag(null, XML_RECORDTAG_TRANSACTION);
            body.startTag(null, "transactionID");
            addText(body, Integer.toString(this.transactionID));
            body.endTag(null, "transactionID");
            body.startTag(null, "serverID");
            addText(body, getServerID());
            body.endTag(null, "serverID");
            body.startTag(null, "timestamp");
            addText(body, this.timestamp == null ? CalExt.descriptionWithISO861Date(new GregorianCalendar()) : CalExt.descriptionWithISO861Date(this.timestamp));
            body.endTag(null, "timestamp");
            if (getDeleted()) {
                body.startTag(null, "deleted");
                addText(body, getDeleted() ? "Y" : "N");
                body.endTag(null, "deleted");
            }
            body.startTag(null, "date");
            if (getDate() == null) {
                descriptionWithISO861Date = CalExt.descriptionWithISO861Date(new GregorianCalendar());
            } else {
                descriptionWithISO861Date = CalExt.descriptionWithISO861Date(getDate());
            }
            addText(body, descriptionWithISO861Date);
            body.endTag(null, "date");
            body.startTag(null, "account");
            addTextWithEncoding(body, getAccount());
            body.endTag(null, "account");
            if (getPayee() != null && getPayee().length() > 0) {
                body.startTag(null, "payee");
                addTextWithEncoding(body, getPayee());
                body.endTag(null, "payee");
            }
            if (getCheckNumber() != null && getCheckNumber().length() > 0) {
                body.startTag(null, "checkNumber");
                addTextWithEncoding(body, getCheckNumber());
                body.endTag(null, "checkNumber");
            }
            body.startTag(null, "subTotal");
            addText(body, Double.toString(getSubTotal()));
            body.endTag(null, "subTotal");
            if (getOfxID() != null && getOfxID().length() > 0) {
                body.startTag(null, "ofxID");
                addText(body, getOfxID());
                body.endTag(null, "ofxID");
            }
            if (getCleared()) {
                body.startTag(null, "cleared");
                addText(body, getCleared() ? "Y" : "N");
                body.endTag(null, "cleared");
            }
            body.startTag(null, "type");
            addText(body, Integer.toString(this.isRepeatingTransaction ? 5 : getType()));
            body.endTag(null, "type");
            body.startTag(null, Database.SPLITS_TABLE_NAME);
            for (SplitsClass split : getSplits()) {
                body.startTag(null, "split");
                body.startTag(null, "amount");
                addText(body, Double.toString(split.getAmount()));
                body.endTag(null, "amount");
                body.startTag(null, "xrate");
                if (split.getXrate() != 1.0d) {
                    addText(body, Double.toString(split.getXrate()));
                } else {
                    addText(body, "1");
                }
                body.endTag(null, "xrate");
                body.startTag(null, "currencyCode");
                addTextWithEncoding(body, split.getCurrencyCode());
                body.endTag(null, "currencyCode");
                if (split.getCategory() != null && split.getCategory().length() > 0) {
                    body.startTag(null, "category");
                    addTextWithEncoding(body, split.getCategory());
                    body.endTag(null, "category");
                }
                if (split.getTransferToAccount() != null && split.getTransferToAccount().length() > 0) {
                    body.startTag(null, "transferToAccount");
                    addTextWithEncoding(body, split.getTransferToAccount());
                    body.endTag(null, "transferToAccount");
                }
                if (split.getClassName() != null && split.getClassName().length() > 0) {
                    body.startTag(null, "class");
                    addTextWithEncoding(body, split.getClassName());
                    body.endTag(null, "class");
                }
                if (split.getMemo() != null && split.getMemo().length() > 0) {
                    body.startTag(null, "memo");
                    addTextWithEncoding(body, split.getMemo());
                    body.endTag(null, "memo");
                }
                body.endTag(null, "split");
            }
            body.endTag(null, Database.SPLITS_TABLE_NAME);
            if (this.imageLocation != null && this.imageLocation.length() > 0) {
                body.startTag(null, "images");
                for (String file : this.imageLocation.split(";")) {
                    XMLStringWithFileName(body, file, withImages);
                }
                body.endTag(null, "images");
            }
            body.endTag(null, XML_RECORDTAG_TRANSACTION);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}
