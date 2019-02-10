package com.example.smmoney.importexport.ofx;

import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.TransactionClass;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

public class OFX_Statement {
    OFX_AccountClass account;
    OFX_BalanceClass availableBalance;
    GregorianCalendar dateEnd;
    GregorianCalendar dateStart;
    String defaultCurrency;
    OFX_BalanceClass ledgerBalance;
    ArrayList ofxtransactions;
    OFX_Tags tags;
    ArrayList transactions;

    public OFX_Statement(String var1, OFX_Tags var2) {
        this.tags = var2;
        this.parse(var1);
    }

    public OFX_Statement(List var1, OFX_Tags var2) {
        this.tags = var2;
        this.transactions = new ArrayList(var1);
        if(var1.size() > 0) {
            int var3 = AccountClass.idForAccount(((TransactionClass)var1.get(0)).getAccount());
            if(var3 != 0) {
                AccountClass var4 = new AccountClass(var3);
                var4.hydrate();
                this.account = new OFX_AccountClass(var4);
            }
        }

    }

    protected String availableBalanceMessage() {
        return "";
    }

    protected String bankAccountMessage() {
        String var1 = this.account.bankID;
        String var2 = this.account.accountID;
        String var3 = this.account.accountTypeAsString();
        return "\t\t\t\t" + this.tags.accountBegin + "\n" + "\t\t\t\t\t" + this.tags.bankIDBegin + var1 + this.tags.bankIDEnd + "\n" + "\t\t\t\t\t" + this.tags.accountIDBegin + var2 + this.tags.accountIDEnd + "\n" + "\t\t\t\t\t" + this.tags.accountTypeBegin + var3 + this.tags.accountTypeEnd + "\n" + "\t\t\t\t" + this.tags.accountEnd + "\n";
    }

    protected String bankTransactionListMessage() {
        String var1 = OFXClass.dateAsString(new GregorianCalendar());
        StringBuffer var2 = new StringBuffer(10000);
        Iterator var3 = this.transactions.iterator();

        while(var3.hasNext()) {
            var2.append((new OFX_TransactionClass((TransactionClass)var3.next(), this.tags)).toString());
        }

        return "\t\t\t\t" + this.tags.bankTransListBegin + "\n" + "\t\t\t\t\t" + this.tags.dateStartBegin + var1 + this.tags.dateStartEnd + "\n" + "\t\t\t\t\t" + this.tags.dateEndBegin + var1 + this.tags.dateEndEnd + "\n" + var2.toString() + "\t\t\t\t" + this.tags.bankTransListEnd + "\n";
    }

    public String description() {
        return "STATEMENT:\naccount=" + this.account + "\ndefaultCurrency=" + this.defaultCurrency + "\ndateStart=" + this.dateStart + "\ndateEnd=" + this.dateEnd + "\nledgerBalance=" + this.ledgerBalance + "\navailableBalance=" + this.availableBalance + "\ntransaction=" + this.transactions;
    }

    protected String ledgerBalanceMessage() {
        String var1 = OFXClass.amountAsOFXAmount(this.account.ledgerBalance);
        String var2 = OFXClass.dateAsString(new GregorianCalendar());
        return "\t\t\t\t" + this.tags.ledgerBalanceBegin + "\n" + "\t\t\t\t\t" + this.tags.balanceAmountBegin + var1 + this.tags.balanceAmountEnd + "\n" + "\t\t\t\t\t" + this.tags.dateAsOfBegin + var2 + this.tags.dateAsOfEnd + "\n" + "\t\t\t\t" + this.tags.ledgerBalanceEnd + "\n";
    }

    protected void parse(String var1) {
        this.account = new OFX_AccountClass(OFXClass.stringBetween(var1, this.tags.accountBegin, this.tags.accountEnd, this.tags.lineEnding), this.tags);
        this.defaultCurrency = OFXClass.stringBetween(var1, this.tags.currencyBegin, this.tags.currencyEnd, this.tags.lineEnding);
        this.dateStart = OFXClass.dateFromString(OFXClass.stringBetween(var1, this.tags.dateStartBegin, this.tags.dateStartEnd, this.tags.lineEnding));
        this.dateEnd = OFXClass.dateFromString(OFXClass.stringBetween(var1, this.tags.dateEndBegin, this.tags.dateEndEnd, this.tags.lineEnding));
        this.ledgerBalance = new OFX_BalanceClass(OFXClass.stringBetween(var1, this.tags.ledgerBalanceBegin, this.tags.ledgerBalanceEnd, this.tags.lineEnding), this.tags);
        this.availableBalance = new OFX_BalanceClass(OFXClass.stringBetween(var1, this.tags.availableBalanceBegin, this.tags.availableBalanceEnd, this.tags.lineEnding), this.tags);
        this.ofxtransactions = null;
        this.ofxtransactions = new ArrayList(50);
        String var2 = OFXClass.stringBetween(var1, this.tags.bankTransListBegin, this.tags.bankTransListEnd, this.tags.lineEnding);
        int var3 = 0;

        while(true) {
            int var4 = var2.indexOf(this.tags.transactionBegin, var3);
            if(var4 == -1) {
                break;
            }

            int var5 = var4 + this.tags.transactionBegin.length();
            int var6 = var2.indexOf(this.tags.transactionEnd, var5);
            String var7 = var2.substring(var5, var6);
            var3 = var6 + this.tags.transactionEnd.length();
            if(var7 == null || var7.length() <= 0) {
                break;
            }

            OFX_TransactionClass var8 = new OFX_TransactionClass(var7, this.tags);
            this.ofxtransactions.add(var8);
        }

    }

    protected String statusMessage(String var1, String var2, String var3) {
        return "\t\t\t" + this.tags.statusBegin + "\n" + "\t\t\t\t" + this.tags.statusCodeBegin + var2 + this.tags.statusCodeEnd + "\n" + "\t\t\t\t" + this.tags.statusSeverityBegin + var3 + this.tags.statusSeverityEnd + "\n" + "\t\t\t\t" + this.tags.statusMessageBegin + var1 + this.tags.statusMessageEnd + "\n" + "\t\t\t" + this.tags.statusEnd + "\n";
    }

    public String toString() {
        return "\t\t<STMTTRNRS>\n\t\t\t<TRNUID>PMA - " + OFXClass.dateAsString(new GregorianCalendar()) + "\n" + this.statusMessage("OK", "0", "INFO") + "\t\t\t" + this.tags.bankStatementTransmissionBegin + "\n" + "\t\t\t\t" + this.tags.currencyBegin + "USD" + this.tags.currencyEnd + "\n" + this.bankAccountMessage() + this.bankTransactionListMessage() + this.ledgerBalanceMessage() + this.availableBalanceMessage() + "\t\t\t" + this.tags.bankStatementTransmissionEnd + "\n" + "\t\t</STMTTRNRS>\n";
    }
}
