package com.catamount.pocketmoney.importexport.ofx;

import com.catamount.pocketmoney.importexport.ofx.OFXClass;
import com.catamount.pocketmoney.importexport.ofx.OFX_Tags;
import java.util.GregorianCalendar;

public class OFX_BalanceClass {
    double balance;
    GregorianCalendar dateAsOf;
    OFX_Tags tags;

    public OFX_BalanceClass(String var1, OFX_Tags var2) {
        this.tags = var2;
        this.parse(var1);
    }

    public String description() {
        return "(balance=" + this.balance + "\tasOfDate=" + this.dateAsOf + ")";
    }

    protected void parse(String var1) {
        this.balance = OFXClass.amountFromOFXAmount(OFXClass.stringBetween(var1, this.tags.balanceAmountBegin, this.tags.balanceAmountEnd, this.tags.lineEnding));
        this.dateAsOf = OFXClass.dateFromString(OFXClass.stringBetween(var1, this.tags.dateAsOfBegin, this.tags.dateAsOfEnd, this.tags.lineEnding));
    }
}
