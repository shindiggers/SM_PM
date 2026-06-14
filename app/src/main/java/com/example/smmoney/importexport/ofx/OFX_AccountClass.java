package com.example.smmoney.importexport.ofx;

import com.example.smmoney.records.AccountClass;

class OFX_AccountClass {
    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType;
    String accountID;
    OFX_AccountType accountType;
    String bankID;
    double ledgerBalance;
    private OFX_Tags tags;

    OFX_AccountClass(AccountClass account) {
        this.bankID = account.getRoutingNumber();
        if (this.bankID == null) {
            this.bankID = "";
        }

        this.accountID = account.getAccountNumber();
        if (this.accountID == null) {
            this.accountID = "";
        }

        this.accountType = this.smMoneyAccountTypeToOFXType(account.getType());
        this.ledgerBalance = account.balanceOfType(2);
        if (this.ledgerBalance > -1.0E-8D && this.ledgerBalance < 0.0D) {
            this.ledgerBalance = 0.0D;
        }

    }

    OFX_AccountClass(String text, OFX_Tags tags) {
        this.tags = tags;
        this.parse(text);
    }

    // $FF: synthetic method
    private static int[] $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType() {
        int[] iArr = $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType;
        if (iArr == null) {
            iArr = new int[OFX_AccountType.values().length];

            try {
                iArr[OFX_AccountType.OFX_CHECKING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }

            try {
                iArr[OFX_AccountType.OFX_CMA.ordinal()] = 5;
            } catch (NoSuchFieldError e2) {
                e2.printStackTrace();
            }

            try {
                iArr[OFX_AccountType.OFX_CREDITCARD.ordinal()] = 6;
            } catch (NoSuchFieldError e3) {
                e3.printStackTrace();
            }

            try {
                iArr[OFX_AccountType.OFX_CREDITLINE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
                e4.printStackTrace();
            }

            try {
                iArr[OFX_AccountType.OFX_INVESTMENT.ordinal()] = 7;
            } catch (NoSuchFieldError e5) {
                e5.printStackTrace();
            }

            try {
                iArr[OFX_AccountType.OFX_MONEYMRKT.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
                e6.printStackTrace();
            }

            try {
                iArr[OFX_AccountType.OFX_SAVINGS.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
                e7.printStackTrace();
            }

            try {
                iArr[OFX_AccountType.OFX_UNKOWN.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
                e8.printStackTrace();
            }

            $SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType = iArr;
        }
        return iArr;
    }

    String accountTypeAsString() {
        return switch ($SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType()[this.accountType.ordinal()]) {
            case 1 -> "CHECKING";
            case 2 -> "SAVINGS";
            case 3 -> "MONEYMRKT";
            case 4 -> "CREDITLINE";
            case 5 -> "CMA";
            case 6 -> "CREDITCARD";
            case 7 -> "INVESTMENT";
            default -> null;
        };
    }

    @SuppressWarnings("unused")
    public String description() {
        return "(bankID=" + this.bankID + "\taccountID=" + this.accountID + "\taccountType" + this.accountTypeAsString() + ")";
    }

    int ofxAccountTypeAsSMMoneyAccountType() {
        return switch ($SWITCH_TABLE$com$catamount$pocketmoney$importexport$ofx$OFX_AccountType()[this.accountType.ordinal()]) {
            case 1 -> 0;
            case 2 -> 6;
            case 3 -> 7;
            case 4 -> 8;
            case 5 -> 3;
            case 6 -> 2;
            case 7 -> 9;
            default -> -1;
        };
    }

    private void parse(String text) {
        this.bankID = OFXClass.stringBetween(text, this.tags.bankIDBegin, this.tags.bankIDEnd, this.tags.lineEnding);
        this.accountID = OFXClass.stringBetween(text, this.tags.accountIDBegin, this.tags.accountIDEnd, this.tags.lineEnding);
        this.setAccountTypeFromString(OFXClass.stringBetween(text, this.tags.accountTypeBegin, this.tags.accountTypeEnd, this.tags.lineEnding));
    }

    private OFX_AccountType smMoneyAccountTypeToOFXType(int type) {
        return switch (type) {
            case 0 -> OFX_AccountType.OFX_CHECKING;
            default -> OFX_AccountType.OFX_UNKOWN;
            case 2 -> OFX_AccountType.OFX_CREDITCARD;
            case 3 -> OFX_AccountType.OFX_CMA;
            case 6 -> OFX_AccountType.OFX_SAVINGS;
            case 7 -> OFX_AccountType.OFX_MONEYMRKT;
            case 8 -> OFX_AccountType.OFX_CREDITLINE;
            case 9 -> OFX_AccountType.OFX_INVESTMENT;
        };
    }

    private void setAccountTypeFromString(String text) {
        if ("CHECKING".equalsIgnoreCase(text)) {
            this.accountType = OFX_AccountType.OFX_CHECKING;
        } else if ("SAVINGS".equalsIgnoreCase(text)) {
            this.accountType = OFX_AccountType.OFX_SAVINGS;
        } else if ("MONEYMRKT".equalsIgnoreCase(text)) {
            this.accountType = OFX_AccountType.OFX_MONEYMRKT;
        } else if ("CREDITLINE".equalsIgnoreCase(text)) {
            this.accountType = OFX_AccountType.OFX_CREDITLINE;
        } else if ("CMA".equalsIgnoreCase(text)) {
            this.accountType = OFX_AccountType.OFX_CMA;
        } else if ("CREDITCARD".equalsIgnoreCase(text)) {
            this.accountType = OFX_AccountType.OFX_CREDITCARD;
        } else if ("INVESTMENT".equalsIgnoreCase(text)) {
            this.accountType = OFX_AccountType.OFX_INVESTMENT;
        } else {
            this.accountType = OFX_AccountType.OFX_UNKOWN;
        }
    }
}
