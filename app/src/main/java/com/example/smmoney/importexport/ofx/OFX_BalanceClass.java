package com.example.smmoney.importexport.ofx;

import java.util.GregorianCalendar;

class OFX_BalanceClass {
    private double balance;
    private GregorianCalendar dateAsOf;
    private OFX_Tags tags;

    OFX_BalanceClass(String var1, OFX_Tags var2) {
        this.tags = var2;
        this.parse(var1);
    }

    public String description() {
        return "(balance=" + this.balance + "\tasOfDate=" + this.dateAsOf + ")";
    }

    private void parse(String var1) {
        this.balance = OFXClass.amountFromOFXAmount(OFXClass.stringBetween(var1, this.tags.balanceAmountBegin, this.tags.balanceAmountEnd, this.tags.lineEnding));
        this.dateAsOf = OFXClass.dateFromString(OFXClass.stringBetween(var1, this.tags.dateAsOfBegin, this.tags.dateAsOfEnd, this.tags.lineEnding));
    }
}
