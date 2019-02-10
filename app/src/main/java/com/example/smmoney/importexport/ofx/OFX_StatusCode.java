package com.example.smmoney.importexport.ofx;

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
