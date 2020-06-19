package com.example.smmoney.importexport.ofx;

import androidx.annotation.NonNull;

import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.TransactionClass;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

class OFX_Statement {
    OFX_AccountClass account;
    private OFX_BalanceClass availableBalance;
    private GregorianCalendar dateEnd;
    private GregorianCalendar dateStart;
    String defaultCurrency;
    private OFX_BalanceClass ledgerBalance;
    ArrayList<OFX_TransactionClass> ofxtransactions;
    final OFX_Tags tags;
    private ArrayList<TransactionClass> transactions;

    OFX_Statement(String text, OFX_Tags tags) {
        this.tags = tags;
        this.parse(text);
    }

    OFX_Statement(List<TransactionClass> transactions, OFX_Tags tags) {
        this.tags = tags;
        this.transactions = new ArrayList<>(transactions);
        if (transactions.size() > 0) {
            int accountID = AccountClass.idForAccount((transactions.get(0)).getAccount());
            if (accountID != 0) {
                AccountClass accountClassRecord = new AccountClass(accountID);
                accountClassRecord.hydrate();
                this.account = new OFX_AccountClass(accountClassRecord);
            }
        }

    }

    String availableBalanceMessage() {
        return "";
    }

    String bankAccountMessage() {
        String bankID = this.account.bankID;
        String accountID = this.account.accountID;
        String accountTypeAsString = this.account.accountTypeAsString();
        return "\t\t\t\t" + this.tags.accountBegin + "\n"
                + "\t\t\t\t\t" + this.tags.bankIDBegin + bankID + this.tags.bankIDEnd + "\n"
                + "\t\t\t\t\t" + this.tags.accountIDBegin + accountID + this.tags.accountIDEnd + "\n"
                + "\t\t\t\t\t" + this.tags.accountTypeBegin + accountTypeAsString + this.tags.accountTypeEnd + "\n"
                + "\t\t\t\t" + this.tags.accountEnd + "\n";
    }

    String bankTransactionListMessage() {
        String dateAsOf = OFXClass.dateAsString(new GregorianCalendar());
        StringBuilder str = new StringBuilder(10000);

        for (TransactionClass transaction : this.transactions) {
            str.append((new OFX_TransactionClass(transaction, this.tags)).toString());
        }

        return "\t\t\t\t" + this.tags.bankTransListBegin + "\n"
                + "\t\t\t\t\t" + this.tags.dateStartBegin + dateAsOf + this.tags.dateStartEnd + "\n"
                + "\t\t\t\t\t" + this.tags.dateEndBegin + dateAsOf + this.tags.dateEndEnd + "\n"
                + str.toString()
                + "\t\t\t\t" + this.tags.bankTransListEnd + "\n";
    }

    @SuppressWarnings("unused")
    public String description() {
        return "STATEMENT:\naccount=" + this.account + "\ndefaultCurrency=" + this.defaultCurrency + "\ndateStart=" + this.dateStart + "\ndateEnd=" + this.dateEnd + "\nledgerBalance=" + this.ledgerBalance + "\navailableBalance=" + this.availableBalance + "\ntransaction=" + this.transactions;
    }

    String ledgerBalanceMessage() {
        String balAmt = OFXClass.amountAsOFXAmount(this.account.ledgerBalance);
        String dateAsOfEnd = OFXClass.dateAsString(new GregorianCalendar());
        return "\t\t\t\t" + this.tags.ledgerBalanceBegin + "\n"
                + "\t\t\t\t\t" + this.tags.balanceAmountBegin + balAmt + this.tags.balanceAmountEnd + "\n"
                + "\t\t\t\t\t" + this.tags.dateAsOfBegin + dateAsOfEnd + this.tags.dateAsOfEnd + "\n"
                + "\t\t\t\t" + this.tags.ledgerBalanceEnd + "\n";
    }

    void parse(String text) {
        this.account = new OFX_AccountClass(OFXClass.stringBetween(text, this.tags.accountBegin, this.tags.accountEnd, this.tags.lineEnding), this.tags);
        this.defaultCurrency = OFXClass.stringBetween(text, this.tags.currencyBegin, this.tags.currencyEnd, this.tags.lineEnding);
        this.dateStart = OFXClass.dateFromString(OFXClass.stringBetween(text, this.tags.dateStartBegin, this.tags.dateStartEnd, this.tags.lineEnding));
        this.dateEnd = OFXClass.dateFromString(OFXClass.stringBetween(text, this.tags.dateEndBegin, this.tags.dateEndEnd, this.tags.lineEnding));
        this.ledgerBalance = new OFX_BalanceClass(OFXClass.stringBetween(text, this.tags.ledgerBalanceBegin, this.tags.ledgerBalanceEnd, this.tags.lineEnding), this.tags);
        this.availableBalance = new OFX_BalanceClass(OFXClass.stringBetween(text, this.tags.availableBalanceBegin, this.tags.availableBalanceEnd, this.tags.lineEnding), this.tags);
        this.ofxtransactions = null;
        this.ofxtransactions = new ArrayList<>(50);
        String bankTransList = OFXClass.stringBetween(text, this.tags.bankTransListBegin, this.tags.bankTransListEnd, this.tags.lineEnding);
        int currentEndTagIndex = 0;

        while (true) {
            int currentStartTagIndex = bankTransList.indexOf(this.tags.transactionBegin, currentEndTagIndex);
            if (currentStartTagIndex == -1) {
                break;
            }

            int currentStartTagIndex2 = currentStartTagIndex + this.tags.transactionBegin.length();
            int currentEndTagIndex2 = bankTransList.indexOf(this.tags.transactionEnd, currentStartTagIndex2);
            String transactionString = bankTransList.substring(currentStartTagIndex2, currentEndTagIndex2);
            currentEndTagIndex = currentEndTagIndex2 + this.tags.transactionEnd.length();
            if (transactionString == null || transactionString.length() <= 0) {
                break;
            }

            OFX_TransactionClass ofxTransactionToAdd = new OFX_TransactionClass(transactionString, this.tags);
            this.ofxtransactions.add(ofxTransactionToAdd);
        }

    }

    String statusMessage(@SuppressWarnings("SameParameterValue") String msg,
                         @SuppressWarnings("SameParameterValue") String code,
                         @SuppressWarnings("SameParameterValue") String severity) {
        return "\t\t\t" + this.tags.statusBegin + "\n"
                + "\t\t\t\t" + this.tags.statusCodeBegin + code + this.tags.statusCodeEnd + "\n"
                + "\t\t\t\t" + this.tags.statusSeverityBegin + severity + this.tags.statusSeverityEnd + "\n"
                + "\t\t\t\t" + this.tags.statusMessageBegin + msg + this.tags.statusMessageEnd + "\n"
                + "\t\t\t" + this.tags.statusEnd + "\n";
    }

    @Override
    @NonNull
    public String toString() {
        return "\t\t<STMTTRNRS>\n" +
                "\t\t\t<TRNUID>PMA - " + OFXClass.dateAsString(new GregorianCalendar()) + "\n"
                + this.statusMessage("OK", "0", "INFO")
                + "\t\t\t" + this.tags.bankStatementTransmissionBegin + "\n"
                + "\t\t\t\t" + this.tags.currencyBegin + "USD" + this.tags.currencyEnd + "\n"
                + this.bankAccountMessage()
                + this.bankTransactionListMessage()
                + this.ledgerBalanceMessage()
                + this.availableBalanceMessage()
                + "\t\t\t" + this.tags.bankStatementTransmissionEnd + "\n"
                + "\t\t</STMTTRNRS>\n";
    }
}
