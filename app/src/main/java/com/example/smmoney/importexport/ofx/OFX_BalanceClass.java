package com.example.smmoney.importexport.ofx;

import androidx.annotation.NonNull;

import java.util.GregorianCalendar;

class OFX_BalanceClass {
    private double balance;
    private GregorianCalendar dateAsOf;
    private final OFX_Tags tags;

    OFX_BalanceClass(String text, OFX_Tags tags) {
        this.tags = tags;
        this.parse(text);
    }

    // Retained for convention as standard model accessors even though currently not used in the app
    @SuppressWarnings("unused")
    public double getBalance() {
        return balance;
    }

    // Retained for convention as standard model accessors even though currently not used in the app
    @SuppressWarnings("unused")
    public GregorianCalendar getDateAsOf() {
        return dateAsOf;
    }

    public String description() {
        return "(balance=" + this.balance + "\tasOfDate=" + this.dateAsOf + ")";
    }

    private void parse(String text) {
        if (text == null || text.isEmpty() || this.tags == null) {
            return;
        }
        this.balance = OFXClass.amountFromOFXAmount(OFXClass.stringBetween(text, this.tags.balanceAmountBegin, this.tags.balanceAmountEnd, this.tags.lineEnding));
        this.dateAsOf = OFXClass.dateFromString(OFXClass.stringBetween(text, this.tags.dateAsOfBegin, this.tags.dateAsOfEnd, this.tags.lineEnding));
    }

    @NonNull
    @Override
    public String toString() {
        return description();
    }
}
