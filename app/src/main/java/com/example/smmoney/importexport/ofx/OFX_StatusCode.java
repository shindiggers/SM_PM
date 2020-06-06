package com.example.smmoney.importexport.ofx;

@SuppressWarnings("unused")
class OFX_StatusCode {
    int code;
    String severity;
    OFX_Tags tags;

    public OFX_StatusCode(String text) {
        this.parse(text);
    }

    private void parse(String text) {
    }
}
