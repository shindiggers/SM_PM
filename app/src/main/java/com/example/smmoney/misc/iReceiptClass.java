package com.example.smmoney.misc;

import android.net.Uri;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.records.TransactionClass;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        //noinspection unused
        String s = Uri.decode(data.toString());
        this.showUI = data.getQueryParameter("showUI");
        this.callbackURL = data.getQueryParameter("callbackURL");
        this.transaction = new TransactionClass();
        String transactionXml = data.getQueryParameter("transaction");
        if (transactionXml != null) {
            try {
                this.transaction.updateWithXML(URLDecoder.decode(transactionXml, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                Log.e(SMMoney.TAG, "Error decoding transaction XML", e);
            }
        }
        this.transaction.transactionID = 0;
    }

    public String postString() {
        try {
            return "http://www.catamount.com/ireceiptredirect.php?ireceipt://hostlocation/post=?transaction=" +
                    URLEncoder.encode(this.transaction.XMLStringWithImages(true), StandardCharsets.UTF_8.name()) +
                    "&showUI=ALWAYS";
        } catch (UnsupportedEncodingException e) {
            Log.e(SMMoney.TAG, "Error encoding transaction XML", e);
            return "";
        }
    }
}
