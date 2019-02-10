package com.example.smmoney.importexport.ofx;

import com.example.smmoney.records.TransactionClass;
import java.util.GregorianCalendar;

public class OFX_TransactionClass {
    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType;
    double amount;
    String checknum;
    GregorianCalendar dtposted;
    GregorianCalendar dtuser;
    String fitID;
    String memo;
    String name;
    OFX_Tags tags;
    OFX_TransactionType transactionType;

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType() {
        int[] var0 = $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType;
        if(var0 != null) {
            return var0;
        } else {
            int[] var1 = new int[OFX_TransactionType.values().length];

            try {
                var1[OFX_TransactionType.OFX_ATM.ordinal()] = 8;
            } catch (NoSuchFieldError var37) {
            }

            try {
                var1[OFX_TransactionType.OFX_CASH.ordinal()] = 13;
            } catch (NoSuchFieldError var36) {
            }

            try {
                var1[OFX_TransactionType.OFX_CHECK.ordinal()] = 11;
            } catch (NoSuchFieldError var35) {
            }

            try {
                var1[OFX_TransactionType.OFX_CREDIT.ordinal()] = 1;
            } catch (NoSuchFieldError var34) {
            }

            try {
                var1[OFX_TransactionType.OFX_DEBIT.ordinal()] = 2;
            } catch (NoSuchFieldError var33) {
            }

            try {
                var1[OFX_TransactionType.OFX_DEP.ordinal()] = 7;
            } catch (NoSuchFieldError var32) {
            }

            try {
                var1[OFX_TransactionType.OFX_DIRECTDEBIT.ordinal()] = 15;
            } catch (NoSuchFieldError var31) {
            }

            try {
                var1[OFX_TransactionType.OFX_DIRECTDEP.ordinal()] = 14;
            } catch (NoSuchFieldError var30) {
            }

            try {
                var1[OFX_TransactionType.OFX_DIV.ordinal()] = 4;
            } catch (NoSuchFieldError var29) {
            }

            try {
                var1[OFX_TransactionType.OFX_FEE.ordinal()] = 5;
            } catch (NoSuchFieldError var28) {
            }

            try {
                var1[OFX_TransactionType.OFX_INT.ordinal()] = 3;
            } catch (NoSuchFieldError var27) {
            }

            try {
                var1[OFX_TransactionType.OFX_OTHER.ordinal()] = 17;
            } catch (NoSuchFieldError var26) {
            }

            try {
                var1[OFX_TransactionType.OFX_PAYMENT.ordinal()] = 12;
            } catch (NoSuchFieldError var25) {
            }

            try {
                var1[OFX_TransactionType.OFX_POS.ordinal()] = 9;
            } catch (NoSuchFieldError var24) {
            }

            try {
                var1[OFX_TransactionType.OFX_REPEATPMT.ordinal()] = 16;
            } catch (NoSuchFieldError var23) {
            }

            try {
                var1[OFX_TransactionType.OFX_SRVCHG.ordinal()] = 6;
            } catch (NoSuchFieldError var22) {
            }

            try {
                var1[OFX_TransactionType.OFX_UKNOWN.ordinal()] = 18;
            } catch (NoSuchFieldError var21) {
            }

            try {
                var1[OFX_TransactionType.OFX_XFER.ordinal()] = 10;
            } catch (NoSuchFieldError var20) {
            }

            $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType = var1;
            return var1;
        }
    }

    public OFX_TransactionClass(TransactionClass var1, OFX_Tags var2) {
        this.tags = var2;
        this.amount = var1.getAmount();
        this.checknum = var1.getCheckNumber();
        this.name = var1.getPayee();
        this.fitID = var1.getOfxID();
        this.memo = var1.getMemo();
        this.dtuser = var1.getDate();
        this.setTransactionTypeFromTransaction(var1);
    }

    public OFX_TransactionClass(String var1, OFX_Tags var2) {
        this.tags = var2;
        this.parse(var1);
    }

    private void parse(String var1) {
        this.setTransactionTypeFromString(OFXClass.stringBetween(var1, this.tags.transactionTypeBegin, this.tags.transactionTypeEnd, this.tags.lineEnding));
        this.amount = OFXClass.amountFromOFXAmount(OFXClass.stringBetween(var1, this.tags.transactionAmountBegin, this.tags.transactionAmountEnd, this.tags.lineEnding));
        this.checknum = OFXClass.stringBetween(var1, this.tags.transactionCheckNumBegin, this.tags.transactionCheckNumEnd, this.tags.lineEnding);
        this.name = OFXClass.stringBetween(var1, this.tags.transactionNameBegin, this.tags.transactionNameEnd, this.tags.lineEnding);
        this.fitID = OFXClass.stringBetween(var1, this.tags.transactionFitIDBegin, this.tags.transactionFitIDEnd, this.tags.lineEnding);
        this.memo = OFXClass.stringBetween(var1, this.tags.transactionMemoBegin, this.tags.transactionMemoEnd, this.tags.lineEnding);
        this.dtposted = OFXClass.dateFromString(OFXClass.stringBetween(var1, this.tags.transactionDatePostedBegin, this.tags.transactionDatePostedEnd, this.tags.lineEnding));
        this.dtuser = OFXClass.dateFromString(OFXClass.stringBetween(var1, this.tags.transactionDateUserEnteredBegin, this.tags.transactionDateUserEnteredEnd, this.tags.lineEnding));
    }

    public String description() {
        return "TRANSACTION:\nfitID=" + this.fitID + "\ntransactionType=" + this.transactionType + "\ncheckNum=" + this.checknum + "\namount=" + this.amount + "\nname=" + this.name + "\nmemo=" + this.memo + "\ndtpost=" + this.dtposted + "\ndtuser=" + this.dtuser;
    }

    public void setTransactionTypeFromString(String var1) {
        if("CREDIT".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_CREDIT;
        } else if("DEBIT".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_DEBIT;
        } else if("INT".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_INT;
        } else if("DIV".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_DIV;
        } else if("FEE".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_FEE;
        } else if("SRVCHG".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_SRVCHG;
        } else if("DEP".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_DEP;
        } else if("ATM".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_ATM;
        } else if("POS".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_POS;
        } else if("XFER".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_XFER;
        } else if("CHECK".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_CHECK;
        } else if("PAYMENT".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_PAYMENT;
        } else if("CASH".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_CASH;
        } else if("DIRECTDEP".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_DIRECTDEP;
        } else if("DIRECTDEBIT".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_DIRECTDEBIT;
        } else if("REPEATPMT".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_REPEATPMT;
        } else if("OTHER".equalsIgnoreCase(var1)) {
            this.transactionType = OFX_TransactionType.OFX_OTHER;
        } else {
            this.transactionType = OFX_TransactionType.OFX_UKNOWN;
        }
    }

    public void setTransactionTypeFromTransaction(TransactionClass var1) {
        if(var1.getType() != 0 && var1.getType() != 2) {
            if(var1.getType() == 1 || var1.getType() == 3) {
                this.transactionType = OFX_TransactionType.OFX_CREDIT;
                return;
            }
        } else {
            this.transactionType = OFX_TransactionType.OFX_DEBIT;
        }

    }

    public String toString() {
        String var1 = "";
        String var2 = "";
        if(this.memo != null && this.memo.length() > 0) {
            var1 = "\t\t\t\t\t\t" + this.tags.transactionMemoBegin + this.memo + this.tags.transactionMemoEnd + "\n";
        }

        if(this.checknum != null && this.checknum.length() > 0) {
            var2 = "\t\t\t\t\t\t" + this.tags.transactionCheckNumBegin + this.checknum + this.tags.transactionCheckNumEnd + "\n";
        }

        StringBuilder var3 = (new StringBuilder("\t\t\t\t\t<STMTTRN>\n\t\t\t\t\t\t")).append(this.tags.transactionTypeBegin).append(this.transactionTypeAsString()).append(this.tags.transactionTypeEnd).append("\n").append("\t\t\t\t\t\t").append(this.tags.transactionDatePostedBegin).append(OFXClass.dateAsString(this.dtuser)).append(this.tags.transactionDatePostedEnd).append("\n").append("\t\t\t\t\t\t").append(this.tags.transactionAmountBegin).append(OFXClass.amountAsOFXAmount(this.amount)).append(this.tags.transactionAmountEnd).append("\n").append("\t\t\t\t\t\t").append(this.tags.transactionFitIDBegin);
        String var4;
        if(this.fitID != null && this.fitID.length() != 0) {
            var4 = this.fitID;
        } else {
            var4 = OFXClass.dateAsString(this.dtuser);
        }

        return var3.append(var4).append(this.tags.transactionFitIDEnd).append("\n").append("\t\t\t\t\t\t").append(this.tags.transactionNameBegin).append(this.name).append(this.tags.transactionNameEnd).append("\n").append(var2).append(var1).append("\t\t\t\t\t</STMTTRN>\n").toString();
    }

    public String transactionTypeAsString() {
        switch($SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_TransactionType()[this.transactionType.ordinal()]) {
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
