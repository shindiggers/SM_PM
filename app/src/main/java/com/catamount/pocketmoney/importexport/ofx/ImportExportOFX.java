package com.catamount.pocketmoney.importexport.ofx;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.text.format.DateFormat;
import android.util.Log;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.importexport.ofx.OFXClass;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.FilterClass;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ImportExportOFX {
    public String accountNameBeingImported;
    Context context;
    int currentLine;
    List data;
    DateFormat dateFormatter;
    String defaultCurrencyCode;
    public FilterClass filter;
    boolean isURL;
    List lines;
    NumberFormat numberFormatter;
    int numberOfLines;
    OFXClass ofxData;
    public String path;

    public ImportExportOFX(Context var1, FilterClass var2) {
        this.context = var1;
        this.filter = var2;
        this.defaultCurrencyCode = Prefs.getStringPref("prefscurrencyhomecurrency");
        this.dateFormatter = null;
        this.numberFormatter = null;
    }

    public ImportExportOFX(Context var1, String var2) {
        this.context = var1;
        this.path = var2;
        this.defaultCurrencyCode = Prefs.getStringPref("prefscurrencyhomecurrency");
        this.dateFormatter = null;
        this.numberFormatter = null;
    }

    public ImportExportOFX(Context var1, String var2, boolean var3) {
        this.context = var1;
        this.isURL = true;
        this.path = var2;
        this.defaultCurrencyCode = Prefs.getStringPref("prefscurrencyhomecurrency");
        this.dateFormatter = null;
        this.numberFormatter = null;
    }

    private int OFXTypeToAccountType(String var1) {
        byte var2 = 1;
        if(var1.equals("Bank")) {
            var2 = 0;
        } else if(!var1.equals("Cash")) {
            if(var1.equals("CCard")) {
                return 2;
            }

            if(var1.equals("Oth A")) {
                return 3;
            }

            if(var1.equals("Oth L")) {
                return 4;
            }
        }

        return var2;
    }

    public static List dateFormats() {
        ArrayList var0 = new ArrayList();
        var0.add(Locales.kLOC_GENERAL_DEFAULT);
        var0.add("mm/dd\'yy");
        var0.add("mm/dd\'yyyy");
        var0.add("mm/dd/yy");
        var0.add("mm/dd/yyyy");
        var0.add("dd/mm\'yy");
        var0.add("dd/mm\'yyyy");
        var0.add("dd/mm/yy");
        var0.add("dd/mm/yyyy");
        var0.add("yyyy/mm/dd");
        return var0;
    }

    public static List dateSeparators() {
        ArrayList var0 = new ArrayList();
        var0.add(Locales.kLOC_GENERAL_DEFAULT);
        var0.add("/");
        var0.add(".");
        var0.add("-");
        return var0;
    }

    private void displayError(String var1) {
        Builder var2 = new Builder(PocketMoney.getAppContext());
        var2.setTitle(var1);
        var2.setPositiveButton(Locales.kLOC_GENERAL_OK, null);
        var2.create().show();
    }

    private String fileName() {
        if(this.path.endsWith("/")) {
            this.path = this.path.substring(0, -1 + this.path.length());
        }

        return this.path.substring(1 + this.path.lastIndexOf("/"));
    }

    public static List numberFormats() {
        ArrayList var0 = new ArrayList();
        var0.add(Locales.kLOC_GENERAL_DEFAULT);
        var0.add("1,000.00");
        var0.add("1.000,00");
        var0.add("1\'000.00");
        var0.add("1\'000,00");
        var0.add("1 000,00");
        return var0;
    }

    private void processAccounts() {
        int var1 = AccountClass.idForAccountNumber(this.ofxData.statement.account.accountID, this.ofxData.statement.account.bankID);
        if(var1 != 0) {
            this.accountNameBeingImported = (new AccountClass(var1)).getAccount();
        } else {
            String var2;
            if(this.ofxData.statement.account.bankID != null && this.ofxData.statement.account.bankID.length() > 0) {
                var2 = this.ofxData.statement.account.bankID;
            } else {
                var2 = "";
            }

            StringBuilder var3 = new StringBuilder(String.valueOf(var2));
            String var4;
            if(this.ofxData.statement.account.bankID == null && this.ofxData.statement.account.bankID.length() <= 0) {
                var4 = "";
            } else {
                var4 = "-";
            }

            this.accountNameBeingImported = var3.append(var4).append(this.ofxData.statement.account.accountID).toString();
            AccountClass var5 = new AccountClass(var1);
            var5.setAccount(this.accountNameBeingImported);
            var5.setTotalWorth(true);
            var5.setNoLimit(true);
            var5.setLimit(0.0D);
            var5.setType(this.ofxData.statement.account.ofxAccountTypeAsPocketMoneyAccountType());
            var5.setAccountNumber(this.ofxData.statement.account.accountID);
            var5.setRoutingNumber(this.ofxData.statement.account.bankID);
            var5.setCurrencyCode(this.ofxData.statement.defaultCurrency);
            var5.saveToDatabase();
        }
    }

    private void processTransactions() {
        // $FF: Couldn't be decompiled
    }

    public boolean exportRecords(List var1) {
        String var2 = this.generateData(var1);
        String var3 = this.path;

        IOException var4;
        label26: {
            BufferedWriter var8;
            try {
                String var6 = Prefs.getStringPref("prefsdatatransfersfileencoding");
                File var7 = new File(var3.substring(0, 1 + var3.lastIndexOf("/")));
                if(!var7.exists()) {
                    var7.mkdirs();
                }

                var8 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(var3), var6));
            } catch (IOException var10) {
                var4 = var10;
                break label26;
            }

            try {
                var8.write(var2);
                var8.close();
                return true;
            } catch (IOException var9) {
                var4 = var9;
            }
        }

        Log.v("Export writing error", var4.toString());
        this.displayError(var4.toString());
        return false;
    }

    public String generateData(List var1) {
        if(var1.size() > 0) {
            OFXClass var2 = new OFXClass();
            var2.transactions = var1;
            return var2.toString();
        } else {
            return "";
        }
    }

    public void importIntoDatabase() {
        // $FF: Couldn't be decompiled
    }
}
