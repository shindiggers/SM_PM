package com.catamount.pocketmoney.misc;

import android.net.Uri;
import com.catamount.pocketmoney.records.TransactionClass;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class iReceiptClass {
    public String callbackURL;
    public Uri data;
    public String showUI;
    public TransactionClass transaction;

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
        StringBuilder sb = new StringBuilder();
        sb.append("http://www.catamount.com/ireceiptredirect.php?ireceipt://hostlocation/post=?transaction=");
        sb.append(URLEncoder.encode(this.transaction.XMLStringWithImages(true)));
        sb.append("&showUI=ALWAYS");
        return sb.toString();
    }
}
