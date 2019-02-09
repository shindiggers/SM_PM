package com.catamount.pocketmoney.importexport.ofx;

import com.catamount.pocketmoney.importexport.ofx.OFX_Tags;

public class OFX_StatusCode {
    int code;
    String severity;
    OFX_Tags tags;

    public OFX_StatusCode(String var1) {
        this.parse(var1);
    }

    public void parse(String var1) {
    }
}
