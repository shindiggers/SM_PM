package com.example.smmoney.misc;

import android.net.Uri;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.records.TransactionClass;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class iReceiptClass {
    public final TransactionClass transaction;
    @SuppressWarnings("FieldCanBeLocal")
    private String callbackURL;
    @SuppressWarnings("FieldCanBeLocal")
    private Uri data;
    @SuppressWarnings("FieldCanBeLocal")
    private String showUI;

    public iReceiptClass(TransactionClass transaction) {
        this.transaction = transaction;
    }

    public iReceiptClass(Uri data) {
        this.data = data;
        String s = Uri.decode(data.toString());
        this.showUI = data.getQueryParameter("showUI");
        this.callbackURL = data.getQueryParameter("callbackURL");
        this.transaction = new TransactionClass();
        this.transaction.updateWithXML(URLDecoder.decode(data.getQueryParameter("transaction")));
        this.transaction.transactionID = 0;
    }

    public String postString() {
        try {
            return "http://www.catamount.com/ireceiptredirect.php?ireceipt://hostlocation/post=?transaction=" +
                    URLEncoder.encode(this.transaction.XMLStringWithImages(true), java.nio.charset.StandardCharsets.UTF_8.toString()) +
                    "&showUI=ALWAYS";
        } catch (UnsupportedEncodingException e) {
            Log.i(SMMoney.TAG, "Invalid tag parsing " + this.transaction.XMLStringWithImages(true) + " xml[");
        }
        return "";
    }
}
