package com.catamount.pocketmoney.importexport.ofx;

import com.catamount.pocketmoney.importexport.ofx.OFXClass;
import com.catamount.pocketmoney.importexport.ofx.OFX_AccountType;
import com.catamount.pocketmoney.importexport.ofx.OFX_Tags;
import com.catamount.pocketmoney.records.AccountClass;

public class OFX_AccountClass {
    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType;
    String accountID;
    OFX_AccountType accountType;
    String bankID;
    double ledgerBalance;
    OFX_Tags tags;

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType() {
        int[] var0 = $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType;
        if(var0 != null) {
            return var0;
        } else {
            int[] var1 = new int[OFX_AccountType.values().length];

            try {
                var1[OFX_AccountType.OFX_CHECKING.ordinal()] = 1;
            } catch (NoSuchFieldError var17) {
            }

            try {
                var1[OFX_AccountType.OFX_CMA.ordinal()] = 5;
            } catch (NoSuchFieldError var16) {
            }

            try {
                var1[OFX_AccountType.OFX_CREDITCARD.ordinal()] = 6;
            } catch (NoSuchFieldError var15) {
            }

            try {
                var1[OFX_AccountType.OFX_CREDITLINE.ordinal()] = 4;
            } catch (NoSuchFieldError var14) {
            }

            try {
                var1[OFX_AccountType.OFX_INVESTMENT.ordinal()] = 7;
            } catch (NoSuchFieldError var13) {
            }

            try {
                var1[OFX_AccountType.OFX_MONEYMRKT.ordinal()] = 3;
            } catch (NoSuchFieldError var12) {
            }

            try {
                var1[OFX_AccountType.OFX_SAVINGS.ordinal()] = 2;
            } catch (NoSuchFieldError var11) {
            }

            try {
                var1[OFX_AccountType.OFX_UNKOWN.ordinal()] = 8;
            } catch (NoSuchFieldError var10) {
            }

            $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType = var1;
            return var1;
        }
    }

    public OFX_AccountClass(AccountClass var1) {
        this.bankID = var1.getRoutingNumber();
        if(this.bankID == null) {
            this.bankID = "";
        }

        this.accountID = var1.getAccountNumber();
        if(this.accountID == null) {
            this.accountID = "";
        }

        this.accountType = this.pmAccountTypeToOFXType(var1.getType());
        this.ledgerBalance = var1.balanceOfType(2);
        if(this.ledgerBalance > -1.0E-8D && this.ledgerBalance < 0.0D) {
            this.ledgerBalance = 0.0D;
        }

    }

    public OFX_AccountClass(String var1, OFX_Tags var2) {
        this.tags = var2;
        this.parse(var1);
    }

    public String accountTypeAsString() {
        switch($SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType()[this.accountType.ordinal()]) {
            case 1:
                return "CHECKING";
            case 2:
                return "SAVINGS";
            case 3:
                return "MONEYMRKT";
            case 4:
                return "CREDITLINE";
            case 5:
                return "CMA";
            case 6:
                return "CREDITCARD";
            case 7:
                return "INVESTMENT";
            default:
                return null;
        }
    }

    public String description() {
        return "(bankID=" + this.bankID + "\taccountID=" + this.accountID + "\taccountType" + this.accountTypeAsString() + ")";
    }

    public int ofxAccountTypeAsPocketMoneyAccountType() {
        switch($SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType()[this.accountType.ordinal()]) {
            case 1:
                return 0;
            case 2:
                return 6;
            case 3:
                return 7;
            case 4:
                return 8;
            case 5:
                return 3;
            case 6:
                return 2;
            case 7:
                return 9;
            default:
                return -1;
        }
    }

    public void parse(String var1) {
        this.bankID = OFXClass.stringBetween(var1, this.tags.bankIDBegin, this.tags.bankIDEnd, this.tags.lineEnding);
        this.accountID = OFXClass.stringBetween(var1, this.tags.accountIDBegin, this.tags.accountIDEnd, this.tags.lineEnding);
        this.setAccountTypeFromString(OFXClass.stringBetween(var1, this.tags.accountTypeBegin, this.tags.accountTypeEnd, this.tags.lineEnding));
    }

    public OFX_AccountType pmAccountTypeToOFXType(int var1) {
        switch(var1) {
            case 0:
                return OFX_AccountType.OFX_CHECKING;
            case 1:
            case 4:
            case 5:
            default:
                return OFX_AccountType.OFX_UNKOWN;
            case 2:
                return OFX_AccountType.OFX_CREDITCARD;
            case 3:
                return OFX_AccountType.OFX_CMA;
            case 6:
                return OFX_AccountType.OFX_SAVINGS;
            case 7:
                return OFX_AccountType.OFX_MONEYMRKT;
            case 8:
                return OFX_AccountType.OFX_CREDITLINE;
            case 9:
                return OFX_AccountType.OFX_INVESTMENT;
        }
    }

    public void setAccountTypeFromString(String var1) {
        if("CHECKING".equalsIgnoreCase(var1)) {
            this.accountType = OFX_AccountType.OFX_CHECKING;
        } else if("SAVINGS".equalsIgnoreCase(var1)) {
            this.accountType = OFX_AccountType.OFX_SAVINGS;
        } else if("MONEYMRKT".equalsIgnoreCase(var1)) {
            this.accountType = OFX_AccountType.OFX_MONEYMRKT;
        } else if("CREDITLINE".equalsIgnoreCase(var1)) {
            this.accountType = OFX_AccountType.OFX_CREDITLINE;
        } else if("CMA".equalsIgnoreCase(var1)) {
            this.accountType = OFX_AccountType.OFX_CMA;
        } else if("CREDITCARD".equalsIgnoreCase(var1)) {
            this.accountType = OFX_AccountType.OFX_CREDITCARD;
        } else if("INVESTMENT".equalsIgnoreCase(var1)) {
            this.accountType = OFX_AccountType.OFX_INVESTMENT;
        } else {
            this.accountType = OFX_AccountType.OFX_UNKOWN;
        }
    }
}
