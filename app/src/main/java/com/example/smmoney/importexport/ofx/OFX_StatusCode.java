package com.example.smmoney.importexport.ofx;

class OFX_StatusCode {
    int code;
    String severity;
    OFX_Tags tags;

    public OFX_StatusCode(String var1) {
        this.parse(var1);
    }

    private void parse(String var1) {
    }
}
