package com.example.smmoney.importexport.ofx;

import androidx.annotation.NonNull;

import com.example.smmoney.records.TransactionClass;

import java.util.GregorianCalendar;
import java.util.List;

public class OFX_CreditCardStatement extends OFX_Statement {
    OFX_CreditCardStatement(String text, OFX_Tags tags) {
        super(text, tags);
    }

    OFX_CreditCardStatement(List<TransactionClass> transactions, OFX_Tags tags) {
        super(transactions, tags);
    }

    @Override
    String bankAccountMessage() {
        if (this.account == null) {
            return "";
        }
        String accountID = this.account.accountID != null ? this.account.accountID : "";
        return "\t\t\t\t" + this.tags.creditCardAccountBegin + "\n"
                + "\t\t\t\t\t" + this.tags.accountIDBegin + accountID + this.tags.accountIDEnd + "\n"
                + "\t\t\t\t" + this.tags.creditCardAccountEnd + "\n";
    }

    @Override
    void parse(String text) {
        super.parse(text);
        if (text != null && !text.isEmpty() && this.tags != null) {
            this.account = new OFX_AccountClass(OFXClass.stringBetween(text, this.tags.creditCardAccountBegin, this.tags.creditCardAccountEnd, this.tags.lineEnding), this.tags);
            this.account.accountType = OFX_AccountType.OFX_CREDITCARD;
        }
    }

    @Override
    @NonNull
    public String toString() {
        return "\t\t<CCSTMTTRNRS>\n" +
                "\t\t\t<TRNUID>PMA - " + OFXClass.dateAsString(new GregorianCalendar()) + "\n"
                + this.statusMessage("OK", "0", "INFO")
                + "\t\t\t" + this.tags.creditCardStatementTransmissionBegin + "\n"
                + "\t\t\t\t" + this.tags.currencyBegin + (this.defaultCurrency != null && !this.defaultCurrency.isEmpty() ? this.defaultCurrency : "USD") + this.tags.currencyEnd + "\n"
                + this.bankAccountMessage()
                + this.bankTransactionListMessage()
                + this.ledgerBalanceMessage()
                + this.availableBalanceMessage()
                + "\t\t\t" + this.tags.creditCardStatementTransmissionEnd + "\n"
                + "\t\t</CCSTMTTRNRS>\n";
    }
}
