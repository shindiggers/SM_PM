package com.catamount.pocketmoney.importexport.ofx;

import com.catamount.pocketmoney.importexport.ofx.OFXClass;
import com.catamount.pocketmoney.importexport.ofx.OFX_AccountClass;
import com.catamount.pocketmoney.importexport.ofx.OFX_AccountType;
import com.catamount.pocketmoney.importexport.ofx.OFX_Statement;
import com.catamount.pocketmoney.importexport.ofx.OFX_Tags;
import java.util.GregorianCalendar;
import java.util.List;

public class OFX_CreditCardStatement extends OFX_Statement {
    public OFX_CreditCardStatement(String var1, OFX_Tags var2) {
        super(var1, var2);
    }

    public OFX_CreditCardStatement(List var1, OFX_Tags var2) {
        super(var1, var2);
    }

    protected String bankAccountMessage() {
        String var1 = this.account.accountID;
        return "\t\t\t\t" + this.tags.creditCardAccountBegin + "\n" + "\t\t\t\t\t" + this.tags.accountIDBegin + var1 + this.tags.accountIDEnd + "\n" + "\t\t\t\t" + this.tags.creditCardAccountEnd + "\n";
    }

    public void parse(String var1) {
        super.parse(var1);
        this.account = new OFX_AccountClass(OFXClass.stringBetween(var1, this.tags.creditCardAccountBegin, this.tags.creditCardAccountEnd, this.tags.lineEnding), this.tags);
        this.account.accountType = OFX_AccountType.OFX_CREDITCARD;
    }

    public String toString() {
        return "\t\t<CCSTMTTRNRS>\n\t\t\t<TRNUID>PMA - " + OFXClass.dateAsString(new GregorianCalendar()) + "\n" + this.statusMessage("OK", "0", "INFO") + "\t\t\t" + this.tags.creditCardStatementTransmissionBegin + "\n" + "\t\t\t\t" + this.tags.currencyBegin + "USD" + this.tags.currencyEnd + "\n" + this.bankAccountMessage() + this.bankTransactionListMessage() + this.ledgerBalanceMessage() + this.availableBalanceMessage() + "\t\t\t" + this.tags.creditCardStatementTransmissionEnd + "\n" + "\t\t</CCSTMTTRNRS>\n";
    }
}
