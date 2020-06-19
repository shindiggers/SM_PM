package com.example.smmoney.importexport.ofx;

import androidx.annotation.NonNull;

import com.example.smmoney.records.TransactionClass;

import java.util.GregorianCalendar;

class OFX_TransactionClass {
    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType;
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

    // $FF: synthetic method
    private static int[] $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType() {
        int[] iArr = $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType;
        if (iArr == null) {
            iArr = new int[OFX_TransactionType.values().length];

            try {
                iArr[OFX_TransactionType.OFX_ATM.ordinal()] = 8;
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_CASH.ordinal()] = 13;
            } catch (NoSuchFieldError e2) {
                e2.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_CHECK.ordinal()] = 11;
            } catch (NoSuchFieldError e3) {
                e3.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_CREDIT.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
                e4.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_DEBIT.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
                e5.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_DEP.ordinal()] = 7;
            } catch (NoSuchFieldError e6) {
                e6.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_DIRECTDEBIT.ordinal()] = 15;
            } catch (NoSuchFieldError e7) {
                e7.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_DIRECTDEP.ordinal()] = 14;
            } catch (NoSuchFieldError e8) {
                e8.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_DIV.ordinal()] = 4;
            } catch (NoSuchFieldError e9) {
                e9.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_FEE.ordinal()] = 5;
            } catch (NoSuchFieldError e10) {
                e10.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_INT.ordinal()] = 3;
            } catch (NoSuchFieldError e11) {
                e11.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_OTHER.ordinal()] = 17;
            } catch (NoSuchFieldError e12) {
                e12.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_PAYMENT.ordinal()] = 12;
            } catch (NoSuchFieldError e13) {
                e13.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_POS.ordinal()] = 9;
            } catch (NoSuchFieldError e14) {
                e14.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_REPEATPMT.ordinal()] = 16;
            } catch (NoSuchFieldError e15) {
                e15.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_SRVCHG.ordinal()] = 6;
            } catch (NoSuchFieldError e16) {
                e16.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_UKNOWN.ordinal()] = 18;
            } catch (NoSuchFieldError e17) {
                e17.printStackTrace();
            }

            try {
                iArr[OFX_TransactionType.OFX_XFER.ordinal()] = 10;
            } catch (NoSuchFieldError e18) {
                e18.printStackTrace();
            }

            $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType = iArr;
        }
        return iArr;
    }

    private void parse(String text) {
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
        if (transaction.getType() != 0 && transaction.getType() != 2) {
            if (transaction.getType() == 1 || transaction.getType() == 3) {
                this.transactionType = OFX_TransactionType.OFX_CREDIT;
            }
        } else {
            this.transactionType = OFX_TransactionType.OFX_DEBIT;
        }

    }

    @Override
    @NonNull
    public String toString() {
        String memoLine = "";
        String checkLine = "";
        if (this.memo != null && this.memo.length() > 0) {
            memoLine = "\t\t\t\t\t\t" + this.tags.transactionMemoBegin + this.memo + this.tags.transactionMemoEnd + "\n";
        }

        if (this.checknum != null && this.checknum.length() > 0) {
            checkLine = "\t\t\t\t\t\t" + this.tags.transactionCheckNumBegin + this.checknum + this.tags.transactionCheckNumEnd + "\n";
        }

        StringBuilder append = (new StringBuilder("\t\t\t\t\t<STMTTRN>\n\t\t\t\t\t\t"))
                .append(this.tags.transactionTypeBegin)
                .append(this.transactionTypeAsString())
                .append(this.tags.transactionTypeEnd)
                .append("\n").append("\t\t\t\t\t\t")
                .append(this.tags.transactionDatePostedBegin)
                .append(OFXClass.dateAsString(this.dtuser))
                .append(this.tags.transactionDatePostedEnd)
                .append("\n").append("\t\t\t\t\t\t")
                .append(this.tags.transactionAmountBegin)
                .append(OFXClass.amountAsOFXAmount(this.amount))
                .append(this.tags.transactionAmountEnd)
                .append("\n").append("\t\t\t\t\t\t")
                .append(this.tags.transactionFitIDBegin);
        String dateAsString;
        if (this.fitID != null && this.fitID.length() != 0) {
            dateAsString = this.fitID;
        } else {
            dateAsString = OFXClass.dateAsString(this.dtuser);
        }

        return append
                .append(dateAsString)
                .append(this.tags.transactionFitIDEnd).append("\n").append("\t\t\t\t\t\t")
                .append(this.tags.transactionNameBegin).append(this.name)
                .append(this.tags.transactionNameEnd).append("\n")
                .append(checkLine).append(memoLine).append("\t\t\t\t\t</STMTTRN>\n")
                .toString();
    }

    String transactionTypeAsString() {
        switch ($SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType()[this.transactionType.ordinal()]) {
            case 1:
                return "CREDIT";
            case 2:
                return "DEBIT";
            case 3:
                return "INT";
            case 4:
                return "DIV";
            case 5:
                return "FEE";
            case 6:
                return "SRVCHG";
            case 7:
                return "DEP";
            case 8:
                return "ATM";
            case 9:
                return "POS";
            case 10:
                return "XFER";
            case 11:
                return "CHECK";
            case 12:
                return "PAYMENT";
            case 13:
                return "CASH";
            case 14:
                return "DIRECTDEP";
            case 15:
                return "DIRECTDEBIT";
            case 16:
                return "REPEATPMT";
            default:
                return "OTHER";
        }
    }
}
