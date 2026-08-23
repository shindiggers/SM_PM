package com.example.smmoney.importexport.ofx;

import androidx.annotation.NonNull;

import com.example.smmoney.records.TransactionClass;

import java.util.GregorianCalendar;

class OFX_TransactionClass {
    double amount;
    String checknum;
    GregorianCalendar dtposted;
    GregorianCalendar dtuser;
    String fitID;
    String memo;
    String name;
    private final OFX_Tags tags;
    private OFX_TransactionType transactionType;

    OFX_TransactionClass(TransactionClass transaction, OFX_Tags tags) {
        this.tags = tags;
        this.amount = transaction.getAmount();
        this.checknum = transaction.getCheckNumber();
        this.name = transaction.getPayee();
        this.fitID = transaction.getOfxID();
        this.memo = transaction.getMemo();
        this.dtuser = transaction.getDate();
        this.setTransactionTypeFromTransaction(transaction);
    }

    OFX_TransactionClass(String text, OFX_Tags tags) {
        this.tags = tags;
        this.parse(text);
    }

    private void parse(String text) {
        if (text == null || text.isEmpty() || this.tags == null) {
            return;
        }
        this.setTransactionTypeFromString(OFXClass.stringBetween(text, this.tags.transactionTypeBegin, this.tags.transactionTypeEnd, this.tags.lineEnding));
        this.amount = OFXClass.amountFromOFXAmount(OFXClass.stringBetween(text, this.tags.transactionAmountBegin, this.tags.transactionAmountEnd, this.tags.lineEnding));
        this.checknum = OFXClass.stringBetween(text, this.tags.transactionCheckNumBegin, this.tags.transactionCheckNumEnd, this.tags.lineEnding);
        this.name = OFXClass.stringBetween(text, this.tags.transactionNameBegin, this.tags.transactionNameEnd, this.tags.lineEnding);
        this.fitID = OFXClass.stringBetween(text, this.tags.transactionFitIDBegin, this.tags.transactionFitIDEnd, this.tags.lineEnding);
        this.memo = OFXClass.stringBetween(text, this.tags.transactionMemoBegin, this.tags.transactionMemoEnd, this.tags.lineEnding);
        this.dtposted = OFXClass.dateFromString(OFXClass.stringBetween(text, this.tags.transactionDatePostedBegin, this.tags.transactionDatePostedEnd, this.tags.lineEnding));
        this.dtuser = OFXClass.dateFromString(OFXClass.stringBetween(text, this.tags.transactionDateUserEnteredBegin, this.tags.transactionDateUserEnteredEnd, this.tags.lineEnding));
    }

    @SuppressWarnings("unused")
    public String description() {
        return "TRANSACTION:\nfitID=" + this.fitID + "\ntransactionType=" + this.transactionType + "\ncheckNum=" + this.checknum + "\namount=" + this.amount + "\nname=" + this.name + "\nmemo=" + this.memo + "\ndtpost=" + this.dtposted + "\ndtuser=" + this.dtuser;
    }

    private void setTransactionTypeFromString(String text) {
        if ("CREDIT".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_CREDIT;
        } else if ("DEBIT".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_DEBIT;
        } else if ("INT".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_INT;
        } else if ("DIV".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_DIV;
        } else if ("FEE".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_FEE;
        } else if ("SRVCHG".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_SRVCHG;
        } else if ("DEP".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_DEP;
        } else if ("ATM".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_ATM;
        } else if ("POS".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_POS;
        } else if ("XFER".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_XFER;
        } else if ("CHECK".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_CHECK;
        } else if ("PAYMENT".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_PAYMENT;
        } else if ("CASH".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_CASH;
        } else if ("DIRECTDEP".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_DIRECTDEP;
        } else if ("DIRECTDEBIT".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_DIRECTDEBIT;
        } else if ("REPEATPMT".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_REPEATPMT;
        } else if ("OTHER".equalsIgnoreCase(text)) {
            this.transactionType = OFX_TransactionType.OFX_OTHER;
        } else {
            this.transactionType = OFX_TransactionType.OFX_UKNOWN;
        }
    }

    private void setTransactionTypeFromTransaction(TransactionClass transaction) {
        int type = transaction.getType();
        if (type == 1 || type == 3) {
            this.transactionType = OFX_TransactionType.OFX_CREDIT;
        } else {
            this.transactionType = OFX_TransactionType.OFX_DEBIT;
        }
    }

    @Override
    @NonNull
    public String toString() {
        String memoLine = "";
        String checkLine = "";
        if (this.memo != null && !this.memo.isEmpty()) {
            memoLine = "\t\t\t\t\t\t" + this.tags.transactionMemoBegin + this.memo + this.tags.transactionMemoEnd + "\n";
        }

        if (this.checknum != null && !this.checknum.isEmpty()) {
            checkLine = "\t\t\t\t\t\t" + this.tags.transactionCheckNumBegin + this.checknum + this.tags.transactionCheckNumEnd + "\n";
        }

        GregorianCalendar dateToUse = this.dtuser != null ? this.dtuser : (this.dtposted != null ? this.dtposted : new GregorianCalendar());

        StringBuilder append = (new StringBuilder("\t\t\t\t\t<STMTTRN>\n\t\t\t\t\t\t"))
                .append(this.tags.transactionTypeBegin)
                .append(this.transactionTypeAsString())
                .append(this.tags.transactionTypeEnd)
                .append("\n").append("\t\t\t\t\t\t")
                .append(this.tags.transactionDatePostedBegin)
                .append(OFXClass.dateAsString(dateToUse))
                .append(this.tags.transactionDatePostedEnd)
                .append("\n").append("\t\t\t\t\t\t")
                .append(this.tags.transactionAmountBegin)
                .append(OFXClass.amountAsOFXAmount(this.amount))
                .append(this.tags.transactionAmountEnd)
                .append("\n").append("\t\t\t\t\t\t")
                .append(this.tags.transactionFitIDBegin);
        String dateAsString;
        if (this.fitID != null && !this.fitID.isEmpty()) {
            dateAsString = this.fitID;
        } else {
            dateAsString = OFXClass.dateAsString(dateToUse);
        }

        return append
                .append(dateAsString)
                .append(this.tags.transactionFitIDEnd).append("\n").append("\t\t\t\t\t\t")
                .append(this.tags.transactionNameBegin).append(this.name != null ? this.name : "")
                .append(this.tags.transactionNameEnd).append("\n")
                .append(checkLine).append(memoLine).append("\t\t\t\t\t</STMTTRN>\n")
                .toString();
    }

    String transactionTypeAsString() {
        if (this.transactionType == null) {
            return "OTHER";
        }
        return switch (this.transactionType) {
            case OFX_CREDIT -> "CREDIT";
            case OFX_DEBIT -> "DEBIT";
            case OFX_INT -> "INT";
            case OFX_DIV -> "DIV";
            case OFX_FEE -> "FEE";
            case OFX_SRVCHG -> "SRVCHG";
            case OFX_DEP -> "DEP";
            case OFX_ATM -> "ATM";
            case OFX_POS -> "POS";
            case OFX_XFER -> "XFER";
            case OFX_CHECK -> "CHECK";
            case OFX_PAYMENT -> "PAYMENT";
            case OFX_CASH -> "CASH";
            case OFX_DIRECTDEP -> "DIRECTDEP";
            case OFX_DIRECTDEBIT -> "DIRECTDEBIT";
            case OFX_REPEATPMT -> "REPEATPMT";
            default -> "OTHER";
        };
    }
}
