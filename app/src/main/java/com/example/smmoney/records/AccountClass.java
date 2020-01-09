package com.example.smmoney.records;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.accounts.AccountTypeIconGridActivity;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

import javax.xml.parsers.SAXParserFactory;

public class AccountClass extends PocketMoneyRecordClass implements Serializable {
    public static final String XML_LISTTAG_ACCOUNTS = "ACCOUNTS";
    public static final String XML_RECORDTAG_ACCOUNT = "ACCOUNTCLASS";
    private static String select_accountNumBankID_statement = null;
    private String account;
    public int accountID;
    private String accountNumber;
    private double balanceAvailableCreditCached;
    private double balanceAvailableFundsCached;
    private double balanceClearedCached;
    private double balanceCurrentCached;
    private double balanceOverallCached;
    private int balanceType;
    private String checkNumber;
    private String currencyCode;
    private String currentElementValue;
    private int displayOrder;
    private double exchangeRate;
    private String expirationDate;
    private double fee;
    private int fixedPercent;
    private String iconFileName;
    private Bitmap iconImage;
    private String institution;
    private double keepChangeRoundTo;
    private String keepTheChangeAccount;
    private double lastSyncTime;
    private double limit;
    private boolean noLimit;
    private String notes;
    private String overdraftAccount;
    private String phone;
    private String routingNumber;
    private boolean totalWorth;
    private int type;
    private int uniqueID;
    private String url;

    private int getAccountId() {
        return this.accountID;
    }

    private void setDisplayOrder(int order) {
        if (this.displayOrder != order) {
            this.dirty = true;
            this.displayOrder = order;
        }
    }

    private int getDisplayOrder() {
        hydrate();
        return this.displayOrder;
    }

    private void setBalanceType(int atype) {
        if (this.balanceType != atype) {
            this.dirty = true;
            this.balanceType = atype;
        }
    }

    private int getBalanceType() {
        hydrate();
        return this.balanceType;
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

    public void setAccount(String aString) {
        if (this.account != null || aString != null) {
            if (this.account == null || !this.account.equals(aString)) {
                this.dirty = true;
                this.account = aString;
            }
        }
    }

    public String getAccount() {
        hydrate();
        return this.account;
    }

    public void setAccountNumber(String aString) {
        if (this.accountNumber != null || aString != null) {
            if (this.accountNumber == null || !this.accountNumber.equals(aString)) {
                this.dirty = true;
                this.accountNumber = aString;
            }
        }
    }

    public String getAccountNumber() {
        hydrate();
        return this.accountNumber;
    }

    public void setRoutingNumber(String aString) {
        if (this.routingNumber != null || aString != null) {
            if (this.routingNumber == null || !this.routingNumber.equals(aString)) {
                this.dirty = true;
                this.routingNumber = aString;
            }
        }
    }

    public String getRoutingNumber() {
        hydrate();
        return this.routingNumber;
    }

    public void setInstitution(String aString) {
        if (this.institution != null || aString != null) {
            if (this.institution == null || !this.institution.equals(aString)) {
                this.dirty = true;
                this.institution = aString;
            }
        }
    }

    public String getInstitution() {
        hydrate();
        return this.institution;
    }

    public void setPhone(String aString) {
        if (this.phone != null || aString != null) {
            if (this.phone == null || !this.phone.equals(aString)) {
                this.dirty = true;
                this.phone = aString;
            }
        }
    }

    public String getPhone() {
        hydrate();
        return this.phone;
    }

    public void setExpirationDate(String aString) {
        if (this.expirationDate != null || aString != null) {
            if (this.expirationDate == null || !this.expirationDate.equals(aString)) {
                this.dirty = true;
                this.expirationDate = aString;
            }
        }
    }

    public String getExpirationDate() {
        hydrate();
        return this.expirationDate;
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

    public void setNotes(String aString) {
        if (this.notes != null || aString != null) {
            if (this.notes == null || !this.notes.equals(aString)) {
                this.dirty = true;
                this.notes = aString;
            }
        }
    }

    public String getNotes() {
        hydrate();
        return this.notes;
    }

    public void setIconFileName(String aString) {
        if (this.iconFileName != null || aString != null) {
            if (this.iconFileName == null || !this.iconFileName.equals(aString)) {
                this.dirty = true;
                this.iconFileName = aString;
            }
        }
    }

    public String getIconFileName() {
        hydrate();
        if (this.iconFileName == null || this.iconFileName.length() == 0) {
            switch (this.type) {
                case Enums.kAccountTypeChecking /*0*/:
                    this.iconFileName = "checkbook.png";
                    break;
                case Enums.kAccountTypeCreditCard /*2*/:
                    this.iconFileName = "ic_visa.xml";
                    break;
                case Enums.kAccountTypeAsset /*3*/:
                    this.iconFileName = "asset.png";
                    break;
                case Enums.kAccountTypeLiability /*4*/:
                    this.iconFileName = "liability.png";
                    break;
                case Enums.kAccountTypeSavings /*6*/:
                    this.iconFileName = "savings.png";
                    break;
                default:
                    this.iconFileName = "cash.png";
                    break;
            }
        }
        return this.iconFileName;
    }

    public void setIconFileNameFromResourceWithContext(int res, Context aContext) {
        //setIconFileName(AccountTypeIconGridActivity.replaceIconNameWithUppercase(aContext.getResources().getResourceEntryName(res).concat(".png")));
        setIconFileName(AccountTypeIconGridActivity.replaceIconNameWithUppercase(aContext.getResources().getResourceEntryName(res).concat(getIconResourceFileNameExtension(res))));
    }

    public String getIconResourceFileNameExtension(int res) {
        Resources resources = SMMoney.getAppContext().getResources();
        TypedValue value = new TypedValue();
        resources.getValue(res, value, true);
        String[] fullFileName = value.string.toString().split("\\.(?=[^\\.]+$)");
        return "." + fullFileName[1];
    }

    public int getIconFileNameResourceIDUsingContext(Context aContext) {
        String modifiedFileName = getIconFileName().replace(".png", "").replace("&", "").replace("-", "").replace(" ", "").toLowerCase();
        String modifiedFileName2 = modifiedFileName.replace(".xml", "").replace("&", "").replace("-", "").replace(" ", "").toLowerCase();
        return aContext.getResources().getIdentifier(modifiedFileName2, "drawable", aContext.getPackageName());
        //return aContext.getResources().getIdentifier(getIconFileName().replace("([^\\s]+(\\.(?i)(.xml|.png))$)", "").replace("&", "").replace("-", "").replace(" ", "").toLowerCase(), "drawable", aContext.getPackageName());
    }

    public void setUrl(String aString) {
        if (this.url != null || aString != null) {
            if (this.url == null || !this.url.equals(aString)) {
                this.dirty = true;
                this.url = aString;
            }
        }
    }

    public String getUrl() {
        hydrate();
        return this.url;
    }

    private void setFee(double amount) {
        if (this.fee != amount) {
            this.dirty = true;
            this.fee = amount;
        }
    }

    public double getFee() {
        hydrate();
        return this.fee;
    }

    public String getKeepTheChangeAccount() {
        hydrate();
        return this.keepTheChangeAccount;
    }

    public void setKeepTheChangeAccount(String aString) {
        if (this.keepTheChangeAccount != null || aString != null) {
            if (this.keepTheChangeAccount == null || !this.keepTheChangeAccount.equals(aString)) {
                this.dirty = true;
                this.keepTheChangeAccount = aString;
            }
        }
    }

    public double getKeepChangeRoundTo() {
        hydrate();
        return this.keepChangeRoundTo;
    }

    private void setKeepChangeRoundTo(double aKeepChangeRoundTo) {
        if (this.keepChangeRoundTo != aKeepChangeRoundTo) {
            this.dirty = true;
            this.keepChangeRoundTo = aKeepChangeRoundTo;
        }
    }

    public String keepChangeRoundToAsString() {
        return formatAmountAsCurrency(getKeepChangeRoundTo());
    }

    public void setKeepChangeRoundToFromString(String str) {
        setKeepChangeRoundTo(numberFromString(str));
    }

    private double numberFromString(String str) {
        try {
            return CurrencyExt.amountFromStringWithCurrency(str, getCurrencyCode());
        } catch (Exception e) {
            return 0.0d;
        }
    }

    public void setFeeFromString(String str) {
        setFee(numberFromString(str));
    }

    public String feeAsString() {
        return formatAmountAsCurrency(getFee());
    }

    public void setLimitFromString(String str) {
        setLimit(numberFromString(str));
        if (str == null || str.length() <= 0) {
            setNoLimit(true);
        } else {
            setNoLimit(false);
        }
    }

    public String limitAsString() {
        if (this.noLimit) {
            return "";
        }
        return formatAmountAsCurrency(getLimit());
    }

    public void setLimit(double amount) {
        if (this.limit != amount) {
            this.dirty = true;
            this.limit = amount;
        }
    }

    public double getLimit() {
        hydrate();
        return this.limit;
    }

    public void setExchangeRate(double amount) {
        if (amount == 0.0d) {
            amount = 1.0d;
        }
        if (this.exchangeRate != amount) {
            this.dirty = true;
            this.exchangeRate = amount;
        }
    }

    public double getExchangeRate() {
        hydrate();
        if (this.exchangeRate == 0.0d) {
            this.exchangeRate = 1.0d;
        }
        return this.exchangeRate;
    }

    public String exchangeRateAsString() {
        return CurrencyExt.exchangeRateAsString(getExchangeRate());
    }

    private void setFixedPercent(int value) {
        if (this.fixedPercent != value) {
            this.dirty = true;
            this.fixedPercent = value;
        }
    }

    private int getFixedPercent() {
        hydrate();
        return this.fixedPercent;
    }

    public void setNoLimit(boolean value) {
        if (this.noLimit != value) {
            this.dirty = true;
            this.noLimit = value;
        }
    }

    public boolean getNoLimit() {
        hydrate();
        return this.noLimit;
    }

    public void setTotalWorth(boolean value) {
        if (this.totalWorth != value) {
            this.dirty = true;
            this.totalWorth = value;
        }
    }

    public boolean getTotalWorth() {
        hydrate();
        return this.totalWorth;
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
            this.currencyCode = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        while (this.currencyCode.length() < 3) {
            this.currencyCode += " ";
        }
        return this.currencyCode;
    }

    public void setLastSyncTime(double time) {
        if (this.lastSyncTime != time) {
            this.dirty = true;
            this.lastSyncTime = time;
        }
    }

    public double getLastSyncTime() {
        hydrate();
        return this.lastSyncTime;
    }

    public void setOverdraftAccount(String aString) {
        if (this.overdraftAccount != null || aString != null) {
            if (this.overdraftAccount == null || !this.overdraftAccount.equals(aString)) {
                this.dirty = true;
                this.overdraftAccount = aString;
            }
        }
    }

    public String getOverdraftAccount() {
        hydrate();
        return this.overdraftAccount;
    }

    public void timeStampIt() {
        this.timestamp = new GregorianCalendar();
    }

    public AccountClass() {
        this.account = "";
        this.accountNumber = "";
        this.routingNumber = "";
        this.institution = "";
        this.phone = "";
        this.expirationDate = "";
        this.checkNumber = "";
        this.notes = "";
        this.iconFileName = "";
        this.url = "";
        this.currencyCode = "";
        this.overdraftAccount = "";
        this.keepTheChangeAccount = "";
        this.accountID = 0;
        this.displayOrder = 0;
        this.keepChangeRoundTo = 1.0d;
        this.noLimit = true;
        this.type = 0;
        this.totalWorth = true;
        this.currencyCode = Prefs.getStringPref(Prefs.HOMECURRENCYCODE) == null ? "USD" : Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        this.lastSyncTime = 0.0d;
        this.displayOrder = 0;
        this.overdraftAccount = "";
        this.deleted = false;
    }

    public String formatAmountAsCurrency(double amount) {
        return CurrencyExt.amountAsCurrency(amount, Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES) ? getCurrencyCode() : null);
    }

    public boolean balanceExceedsLimit() {
        if (!hasLimit()) {
            return false;
        }
        switch (getType()) {
            case Enums.kAccountTypeChecking /*0*/:
            case Enums.kAccountTypeCash /*1*/:
            case Enums.kAccountTypeAsset /*3*/:
            case Enums.kAccountTypeOnline /*5*/:
            case Enums.kAccountTypeSavings /*6*/:
            case Enums.kAccountTypeMoneyMarket /*7*/:
            case Enums.kAccountTypeInvestment /*9*/:
                return getLimit() >= balanceCurrent();
            case Enums.kAccountTypeCreditCard /*2*/:
            case Enums.kAccountTypeLiability /*4*/:
            case Enums.kAccountTypeCreditLine /*8*/:
                return -1.0d * getLimit() > balanceCurrent();
            default:
                return false;
        }
    }

    public boolean balanceExceedsLimitWithRunningBalance(double amount) {
        if (!hasLimit()) {
            return false;
        }
        switch (getType()) {
            case Enums.kAccountTypeChecking /*0*/:
            case Enums.kAccountTypeCash /*1*/:
            case Enums.kAccountTypeAsset /*3*/:
            case Enums.kAccountTypeOnline /*5*/:
            case Enums.kAccountTypeSavings /*6*/:
            case Enums.kAccountTypeMoneyMarket /*7*/:
            case Enums.kAccountTypeInvestment /*9*/:
                return getLimit() >= amount;
            case Enums.kAccountTypeCreditCard /*2*/:
            case Enums.kAccountTypeLiability /*4*/:
            case Enums.kAccountTypeCreditLine /*8*/:
                return -1.0d * getLimit() > amount;
            default:
                return false;
        }
    }

    private boolean hasLimit() {
        return !getNoLimit();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isLiability() {
        switch (getType()) {
            case Enums.kAccountTypeCreditCard /*2*/:
            case Enums.kAccountTypeLiability /*4*/:
            case Enums.kAccountTypeCreditLine /*8*/:
                return true;
            default:
                return false;
        }
    }

    public boolean isAsset() {
        return !isLiability();
    }

    public double balanceOfType(int balanceType) {
        double retBalance;
        switch (balanceType) {
            case Enums.kBalanceTypeFuture /*0*/:
                if (this.balanceOverallCached == 0.0d) {
                    retBalance = balanceOverall();
                    this.balanceOverallCached = retBalance;
                    break;
                }
                return this.balanceOverallCached;
            case Enums.kBalanceTypeCleared /*1*/:
                if (this.balanceClearedCached == 0.0d) {
                    retBalance = balanceCleared();
                    this.balanceClearedCached = retBalance;
                    break;
                }
                return this.balanceClearedCached;
            case Enums.kBalanceTypeCurrent /*2*/:
                if (this.balanceCurrentCached == 0.0d) {
                    retBalance = balanceCurrent();
                    this.balanceCurrentCached = retBalance;
                    break;
                }
                return this.balanceCurrentCached;
            case Enums.kBalanceTypeAvailableFunds /*3*/:
                if (this.balanceAvailableFundsCached == 0.0d) {
                    if (!isLiability()) {
                        if (hasLimit()) {
                            retBalance = balanceOverall() - this.limit;
                        } else {
                            double balanceOverallCache = balanceCurrent();
                            if (balanceOverallCache < 0.0d) {
                                retBalance = 0.0d;
                            } else {
                                retBalance = balanceOverallCache;
                            }
                        }
                        if (retBalance < 0.0d) {
                            retBalance = 0.0d;
                        }
                    } else if (hasLimit()) {
                        retBalance = this.limit + balanceCurrent();
                    } else {
                        retBalance = 0.0d;
                    }
                    this.balanceAvailableFundsCached = retBalance;
                    break;
                }
                return this.balanceAvailableFundsCached;
            case Enums.kBalanceTypeAvailableCredit /*4*/:
                if (this.balanceAvailableCreditCached == 0.0d) {
                    retBalance = 0.0d;
                    if ((Enums.kAccountTypeCreditCard/*2*/ == getType() || Enums.kAccountTypeCreditLine /*8*/ == getType()) && hasLimit()) {
                        retBalance = this.limit + balanceCurrent();
                    }
                    this.balanceAvailableCreditCached = retBalance;
                    break;
                }
                return this.balanceAvailableCreditCached;
            default:
                retBalance = 0.0d;
                break;
        }
        return retBalance;
    }

    public void setTypeFromString(String aType) {
        if (aType.equals(Locales.kLOC_ACCOUNTTYPE_ASSET)) {
            setType(3);
        } else if (aType.equals(Locales.kLOC_ACCOUNTTYPE_CASH)) {
            setType(1);
        } else if (aType.equals(Locales.kLOC_ACCOUNTTYPE_CHECKING)) {
            setType(0);
        } else if (aType.equals(Locales.kLOC_ACCOUNTTYPE_CREDITCARD)) {
            setType(2);
        } else if (aType.equals(Locales.kLOC_ACCOUNTTYPE_CREDITLINE)) {
            setType(8);
        } else if (aType.equals(Locales.kLOC_ACCOUNTTYPE_LIABILITY)) {
            setType(4);
        } else if (aType.equals(Locales.kLOC_ACCOUNTTYPE_MONEYMARKET)) {
            setType(7);
        } else if (aType.equals(Locales.kLOC_ACCOUNTTYPE_ONLINE)) {
            setType(5);
        } else if (aType.equals(Locales.kLOC_ACCOUNTTYPE_SAVINGS)) {
            setType(6);
        }
    }

    public String typeAsString() {
        switch (getType()) {
            case Enums.kAccountTypeChecking /*0*/:
                return Locales.kLOC_ACCOUNTTYPE_CHECKING;
            case Enums.kAccountTypeCreditCard /*2*/:
                return Locales.kLOC_ACCOUNTTYPE_CREDITCARD;
            case Enums.kAccountTypeAsset /*3*/:
                return Locales.kLOC_ACCOUNTTYPE_ASSET;
            case Enums.kAccountTypeLiability /*4*/:
                return Locales.kLOC_ACCOUNTTYPE_LIABILITY;
            case Enums.kAccountTypeOnline /*5*/:
                return Locales.kLOC_ACCOUNTTYPE_ONLINE;
            case Enums.kAccountTypeSavings /*6*/:
                return Locales.kLOC_ACCOUNTTYPE_SAVINGS;
            case Enums.kAccountTypeMoneyMarket /*7*/:
                return Locales.kLOC_ACCOUNTTYPE_MONEYMARKET;
            case Enums.kAccountTypeCreditLine /*8*/:
                return Locales.kLOC_ACCOUNTTYPE_CREDITLINE;
            default:
                return Locales.kLOC_ACCOUNTTYPE_CASH;
        }
    }

    public AccountClass(int pk) {
        this.account = "";
        this.accountNumber = "";
        this.routingNumber = "";
        this.institution = "";
        this.phone = "";
        this.expirationDate = "";
        this.checkNumber = "";
        this.notes = "";
        this.iconFileName = "";
        this.url = "";
        this.currencyCode = "";
        this.overdraftAccount = "";
        this.keepTheChangeAccount = "";
        this.accountID = 0;
        this.displayOrder = 0;
        this.keepChangeRoundTo = 1.0d;
        this.accountID = pk;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.ACCOUNTS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"account"}, "accountID=" + pk, null, null, null, null);
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            String act = curs.getString(0);
            if (act != null) {
                this.account = act;
            } else {
                this.account = "";
            }
        } else {
            this.account = "*accountID " + pk + "  has no account name*";
            this.exchangeRate = 1.0d;
        }
        this.dirty = false;
        curs.close();
    }

    public void deleteFromDatabase() {
        ContentValues values = new ContentValues();
        values.put("timestamp", System.currentTimeMillis());
        values.put("deleted", Boolean.TRUE);
        Database.update(Database.ACCOUNTS_TABLE_NAME, values, "accountID=" + this.accountID, null);
    }

    public void hydrate() {
        if (!this.hydrated) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Database.ACCOUNTS_TABLE_NAME);
            Cursor curs = Database.query(qb, new String[]{"deleted", "timestamp", "displayOrder", "account", "type", "accountNumber", "routingNumber", "institution", "phone", "expirationDate", "checkNumber", "notes", "iconFileName", "url", "fee", "fixedPercent", "limitAmount", "noLimit", "totalWorth", "exchangeRate", "currencyCode", "lastSyncTime", "overdraftAccountID", "serverID", "keepTheChangeAccountID", "keepChangeRoundTo"}, "accountID=" + this.accountID, null, null, null, null);
            if (curs.getCount() != 0) {
                curs.moveToFirst();
                boolean wasDirty = this.dirty;
                int col = 1;
                setDeleted(curs.getInt(0) == 1);
                this.timestamp = new GregorianCalendar();
                int col2 = col + 1;
                this.timestamp.setTimeInMillis(1000 * ((long) curs.getDouble(col)));
                col = col2 + 1;
                setDisplayOrder(curs.getInt(col2));
                col2 = col + 1;
                String str = curs.getString(col);
                if (str == null) {
                    setAccount("");
                } else {
                    setAccount(str);
                }
                col = col2 + 1;
                setType(curs.getInt(col2));
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    setAccountNumber("");
                } else {
                    setAccountNumber(str);
                }
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    setRoutingNumber("");
                } else {
                    setRoutingNumber(str);
                }
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    setInstitution("");
                } else {
                    setInstitution(str);
                }
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    setPhone("");
                } else {
                    setPhone(str);
                }
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    setExpirationDate("");
                } else {
                    setExpirationDate(str);
                }
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    setCheckNumber("");
                } else {
                    setCheckNumber(str);
                }
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    setNotes("");
                } else {
                    setNotes(str);
                }
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    setIconFileName("");
                } else {
                    setIconFileName(str);
                }
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    setUrl("");
                } else {
                    setUrl(str);
                }
                col = col2 + 1;
                setFee(curs.getDouble(col2));
                col2 = col + 1;
                setFixedPercent((int) curs.getDouble(col));
                col = col2 + 1;
                setLimit(curs.getDouble(col2));
                col2 = col + 1;
                setNoLimit(curs.getDouble(col) == 1.0d);
                col = col2 + 1;
                setTotalWorth(curs.getDouble(col2) == 1.0d);
                col2 = col + 1;
                double xrate = curs.getDouble(col);
                if (xrate == 0.0d) {
                    xrate = 1.0d;
                }
                setExchangeRate(xrate);
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    setCurrencyCode("");
                } else {
                    setCurrencyCode(str);
                }
                col2 = col + 1;
                setLastSyncTime(curs.getDouble(col));
                col = col2 + 1;
                setOverdraftAccount(accountForID(curs.getInt(col2)));
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setServerID(str);
                col = col2 + 1;
                setKeepTheChangeAccount(accountForID(curs.getInt(col2)));
                col2 = col + 1;
                double keep = curs.getDouble(col);
                if (keep == 0.0d) {
                    keep = 1.0d;
                }
                setKeepChangeRoundTo(keep);
                if (!wasDirty && this.dirty) {
                    this.dirty = false;
                }
            }
            this.hydrated = true;
            curs.close();
        }
    }

    public void dehydrateAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            ContentValues content = new ContentValues();
            content.put("deleted", this.deleted);
            String str = "timestamp";
            long currentTimeMillis = (updateTimeStamp || this.timestamp == null) ? System.currentTimeMillis() / 1000 : this.timestamp.getTimeInMillis() / 1000;
            content.put(str, currentTimeMillis);
            content.put("displayOrder", this.displayOrder);
            content.put("account", this.account);
            content.put("type", this.type);
            content.put("accountNumber", this.accountNumber);
            content.put("routingNumber", this.routingNumber);
            content.put("institution", this.institution);
            content.put("phone", this.phone);
            content.put("expirationDate", this.expirationDate);
            content.put("checkNumber", this.checkNumber);
            content.put("notes", this.notes);
            content.put("iconFileName", this.iconFileName);
            content.put("url", this.url);
            content.put("fee", this.fee);
            content.put("limitAmount", this.limit);
            content.put("noLimit", this.noLimit);
            content.put("totalWorth", this.totalWorth);
            content.put("exchangeRate", this.exchangeRate);
            content.put("currencyCode", this.currencyCode);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            content.put("serverID", this.serverID);
            content.put("keepTheChangeAccountID", idForAccount(this.keepTheChangeAccount));
            content.put("keepChangeRoundTo", this.keepChangeRoundTo);
            content.put("lastSyncTime", this.lastSyncTime);
            content.put("overdraftAccountID", idForAccount(this.overdraftAccount));
            if (Database.update(Database.ACCOUNTS_TABLE_NAME, content, "accountID=" + this.accountID, null) != 1) {
                Log.e("PockeyMoney", "Problem updating accountID=" + this.accountID);
            }
            this.dirty = false;
        }
        this.hydrated = false;
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            if (this.accountID == 0) {
                this.accountID = insertIntoDatabase(this.account);
            }
            dehydrateAndUpdateTimeStamp(updateTimeStamp);
        }
    }

    public static ArrayList<String> accountTypes() {
        ArrayList<String> accountTypes = new ArrayList<>();
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_ASSET);
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_CASH);
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_CHECKING);
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_CREDITCARD);
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_CREDITLINE);
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_LIABILITY);
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_MONEYMARKET);
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_ONLINE);
        accountTypes.add(Locales.kLOC_ACCOUNTTYPE_SAVINGS);
        Collections.sort(accountTypes);
        return accountTypes;
    }

    public static int insertIntoDatabase(String newAccount) {
        if (newAccount == null || newAccount.length() == 0) {
            return 0;
        }
        ContentValues content = new ContentValues();
        content.put("deleted", 0);
        content.put("account", newAccount);
        content.put("exchangeRate", 1.0f);
        content.put("totalWorth", 1);
        content.put("noLimit", 1);
        content.put("serverID", Database.newServerID());
        content.put("timestamp", System.currentTimeMillis() / 1000);
        long id = Database.insert(Database.ACCOUNTS_TABLE_NAME, null, content);
        if (id != -1) {
            return (int) id;
        }
        return 0;
    }

    public static int idForAccountElseAddIfMissing(String anAccount, boolean addIt) {
        int id = idForAccount(anAccount);
        if (id == 0 && addIt) {
            return insertIntoDatabase(anAccount);
        }
        return id;
    }

    public static int idForAccount(String account) {
        if (account == null || account.length() == 0) {
            return 0;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.ACCOUNTS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"accountID"}, "deleted=0 AND account LIKE " + Database.SQLFormat(account), null, null, null, null);
        int accountID = 0;
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            accountID = curs.getInt(0);
        }
        curs.close();
        return accountID;
    }

    static int idForAccount(boolean deleted, String account) {
        int i = 1;
        if (account == null || account.length() == 0) {
            return 0;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.ACCOUNTS_TABLE_NAME);
        String[] projection = new String[]{"accountID"};
        StringBuilder stringBuilder = new StringBuilder("deleted=");
        if (!deleted) {
            i = 0;
        }
        Cursor curs = Database.query(qb, projection, stringBuilder.append(i).append(" AND account LIKE ").append(Database.SQLFormat(account)).toString(), null, null, null, null);
        int accountID = 0;
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            accountID = curs.getInt(0);
        }
        curs.close();
        return accountID;
    }

    static String accountForID(int pk) {
        if (pk == 0) {
            return null;
        }
        return new AccountClass(pk).getAccount();
    }

    public static AccountClass recordWithServerID(String serverID) {
        AccountClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery("SELECT accountID FROM accounts WHERE serverID=" + Database.SQLFormat(serverID), null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new AccountClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public static int idForAccountNumber(String accountNumber, String bankID) {
        int accountID = 0;
        if (select_accountNumBankID_statement == null) {
            select_accountNumBankID_statement = "SELECT accountID FROM accounts WHERE deleted=0 AND ((accountNumber LIKE ?  ESCAPE '\\' AND routingNumber LIKE ?  ESCAPE '\\') OR (accountNumber LIKE ?  ESCAPE '\\'))";
        }
        Cursor c = Database.rawQuery(select_accountNumBankID_statement, new String[]{accountNumber, bankID, accountNumber});
        if (c.getCount() > 0) {
            c.moveToFirst();
            accountID = c.getInt(0);
        }
        c.close();
        return accountID;
    }

    private double balanceCleared() {
        Cursor curs;
        double balance = 0.0d;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.TRANSACTIONS_TABLE_NAME);
        if (this.accountID != 0) {
            curs = Database.query(qb, new String[]{"sum(subTotal)"}, "deleted=0 AND accountID=" + this.accountID + " AND cleared=1 AND type<>" + 5, null, null, null, null);
        } else {
            curs = Database.query(qb, new String[]{"sum(subTotal)"}, "deleted=0 AND cleared=1 AND type<>5 AND transactions.accountID IN (SELECT accountID FROM accounts WHERE deleted=0 AND totalWorth=1)", null, null, null, null);
        }
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            balance = curs.getDouble(0);
        }
        curs.close();
        return balance;
    }

    public double balanceAsOfDate(GregorianCalendar cal) {
        Cursor curs;
        double balance = 0.0d;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.TRANSACTIONS_TABLE_NAME);
        if (this.accountID != 0) {
            curs = Database.query(qb, new String[]{"sum(subTotal)"}, "deleted=0 AND accountID=" + this.accountID + " AND date<=" + (CalExt.endOfDay(cal).getTimeInMillis() / 1000) + " AND type<>" + 5, null, null, null, null);
        } else {
            curs = Database.query(qb, new String[]{"sum(subTotal)"}, "deleted=0 AND date<=" + (CalExt.endOfDay(cal).getTimeInMillis() / 1000) + " AND type<>" + 5 + " AND transactions.accountID IN (SELECT accountID FROM accounts WHERE deleted=0 AND totalWorth=1)", null, null, null, null);
        }
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            balance = curs.getDouble(0);
        }
        curs.close();
        return balance;
    }

    private double balanceOverall() {
        if (Prefs.getLongPref(Prefs.BALANCEONDATE) <= 0) {
            return balanceAsOfDate(CalExt.distantFuture());
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(Prefs.getLongPref(Prefs.BALANCEONDATE));
        return balanceAsOfDate(cal);
    }

    private double balanceCurrent() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        return balanceAsOfDate(cal);
    }

    public void updateWithXML(String xmlTransaction) {
        try {
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(new StringReader(xmlTransaction));
            xr.setContentHandler(this);
            xr.parse(is);
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error parsing xml");
        }
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        this.currentElementValue = null;
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        boolean z = false;
        if (this.currentElementValue == null) {
            this.currentElementValue = "";
        }
        if (localName.equals("accountID")) {
            this.accountID = Integer.valueOf(this.currentElementValue);
        } else if (localName.equals("timestamp")) {
            this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
        } else if (localName.equals("deleted")) {
            if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                z = true;
            }
            setDeleted(z);
        } else if (localName.equals("displayOrder")) {
            setDisplayOrder(Integer.valueOf(this.currentElementValue));
        } else if (localName.equals(Prefs.BALANCETYPE)) {
            setBalanceType(Integer.valueOf(this.currentElementValue));
        } else if (localName.equals("type")) {
            setType(Integer.valueOf(this.currentElementValue));
        } else if (localName.equals("fee")) {
            setFee(Double.valueOf(this.currentElementValue));
        } else if (localName.equals("fixedPercent")) {
            setFixedPercent(Integer.valueOf(this.currentElementValue));
        } else if (localName.equals("limitAmount")) {
            setLimit(Double.valueOf(this.currentElementValue));
        } else if (localName.equals("keepChangeRoundTo")) {
            setKeepChangeRoundTo(Double.valueOf(this.currentElementValue));
        } else if (localName.equals("noLimit")) {
            if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                z = true;
            }
            setNoLimit(z);
        } else if (localName.equals("totalWorth")) {
            if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                z = true;
            }
            setTotalWorth(z);
        } else if (localName.equals("exchangeRate")) {
            setExchangeRate(Double.valueOf(this.currentElementValue));
        } else if (!localName.equals("customIcon")) {
            switch (localName) {
                case "iconFileName":
                    setIconFileName(this.currentElementValue);
                    break;
                case "serverID":
                    setServerID(this.currentElementValue);
                    break;
                case "keepChangeRoundTo":
                    try {
                        setKeepChangeRoundTo(Double.parseDouble(this.currentElementValue));
                    } catch (Exception e) {
                        setKeepChangeRoundTo(1.0d);
                    }
                    break;
                case "keepTheChangeAccount":
                    setKeepTheChangeAccount(this.currentElementValue);
                    break;
                case "category":
                case "account":
                case "accountNumber":
                case "routingNumber":
                case "institution":
                case "phone":
                case "expirationDate":
                case "checkNumber":
                case "notes":
                case "currencyCode":
                case "url":
                case "overdraftAccount":
                    Class<?> c = getClass();
                    try {
                        c.getDeclaredField(localName).set(this, URLDecoder.decode(this.currentElementValue));
                    } catch (Exception e2) {
                        Log.i(SMMoney.TAG, "Invalid tag parsing " + c.getName() + " xml [" + localName + "]");
                    }
                    break;
            }
        }
        this.currentElementValue = null;
    }

    public void characters(char[] ch, int start, int length) {
        if (this.currentElementValue == null) {
            this.currentElementValue = new String(ch, start, length);
        } else {
            this.currentElementValue += new String(ch, start, length);
        }
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

    public String XMLString() {
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
            body.setOutput(output, "UTF-8");
            body.startTag(null, XML_RECORDTAG_ACCOUNT);
            body.startTag(null, "accountid");
            addText(body, Integer.toString(getAccountId()));
            body.endTag(null, "accountid");
            body.startTag(null, "serverID");
            addText(body, getServerID());
            body.endTag(null, "serverID");
            body.startTag(null, "timestamp");
            addText(body, this.timestamp == null ? "0" : CalExt.descriptionWithISO861Date(this.timestamp));
            body.endTag(null, "timestamp");
            body.startTag(null, "deleted");
            addText(body, getDeleted() ? "Y" : "N");
            body.endTag(null, "deleted");
            body.startTag(null, "displayOrder");
            addText(body, Integer.toString(getDisplayOrder()));
            body.endTag(null, "displayOrder");
            body.startTag(null, Prefs.BALANCETYPE);
            addText(body, Integer.toString(getBalanceType()));
            body.endTag(null, Prefs.BALANCETYPE);
            body.startTag(null, "account");
            addTextWithEncoding(body, getAccount());
            body.endTag(null, "account");
            body.startTag(null, "type");
            addText(body, Integer.toString(getType()));
            body.endTag(null, "type");
            body.startTag(null, "accountNumber");
            addTextWithEncoding(body, getAccountNumber());
            body.endTag(null, "accountNumber");
            body.startTag(null, "routingNumber");
            addTextWithEncoding(body, getRoutingNumber());
            body.endTag(null, "routingNumber");
            body.startTag(null, "institution");
            addTextWithEncoding(body, getInstitution());
            body.endTag(null, "institution");
            body.startTag(null, "phone");
            addTextWithEncoding(body, getPhone());
            body.endTag(null, "phone");
            body.startTag(null, "expirationDate");
            addTextWithEncoding(body, getExpirationDate());
            body.endTag(null, "expirationDate");
            body.startTag(null, "checkNumber");
            addTextWithEncoding(body, getCheckNumber());
            body.endTag(null, "checkNumber");
            body.startTag(null, "notes");
            addTextWithEncoding(body, getNotes());
            body.endTag(null, "notes");
            body.startTag(null, "fee");
            addText(body, Double.toString(getFee()));
            body.endTag(null, "fee");
            body.startTag(null, "fixedPercent");
            addText(body, Integer.toString(getFixedPercent()));
            body.endTag(null, "fixedPercent");
            body.startTag(null, "limitAmount");
            addText(body, Double.toString(getLimit()));
            body.endTag(null, "limitAmount");
            body.startTag(null, "noLimit");
            addText(body, getNoLimit() ? "Y" : "N");
            body.endTag(null, "noLimit");
            body.startTag(null, "totalWorth");
            addText(body, getTotalWorth() ? "Y" : "N");
            body.endTag(null, "totalWorth");
            body.startTag(null, "exchangeRate");
            addText(body, Double.toString(getExchangeRate()));
            body.endTag(null, "exchangeRate");
            body.startTag(null, "currencyCode");
            addTextWithEncoding(body, getCurrencyCode());
            body.endTag(null, "currencyCode");
            body.startTag(null, "url");
            addTextWithEncoding(body, getUrl());
            body.endTag(null, "url");
            body.startTag(null, "overdraftAccount");
            addTextWithEncoding(body, getOverdraftAccount());
            body.endTag(null, "overdraftAccount");
            body.startTag(null, "iconFileName");
            addTextWithEncoding(body, getIconFileName());
            body.endTag(null, "iconFileName");
            body.startTag(null, "keepTheChangeAccount");
            addTextWithEncoding(body, getKeepTheChangeAccount());
            body.endTag(null, "keepTheChangeAccount");
            body.startTag(null, "keepChangeRoundTo");
            addTextWithEncoding(body, Double.toString(getKeepChangeRoundTo()));
            body.endTag(null, "keepChangeRoundTo");
            body.endTag(null, XML_RECORDTAG_ACCOUNT);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}
