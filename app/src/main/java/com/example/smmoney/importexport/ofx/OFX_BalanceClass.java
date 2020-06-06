package com.example.smmoney.importexport.ofx;

import java.util.GregorianCalendar;

class OFX_BalanceClass {
    private double balance;
    private GregorianCalendar dateAsOf;
    private OFX_Tags tags;

    OFX_BalanceClass(String text, OFX_Tags tags) {
        this.tags = tags;
        this.parse(text);
    }

    @SuppressWarnings("unused")
    public String description() {
        return "(balance=" + this.balance + "\tasOfDate=" + this.dateAsOf + ")";
    }

    private void parse(String text) {
        this.balance = OFXClass.amountFromOFXAmount(OFXClass.stringBetween(text, this.tags.balanceAmountBegin, this.tags.balanceAmountEnd, this.tags.lineEnding));
        this.dateAsOf = OFXClass.dateFromString(OFXClass.stringBetween(text, this.tags.dateAsOfBegin, this.tags.dateAsOfEnd, this.tags.lineEnding));
    }
}
