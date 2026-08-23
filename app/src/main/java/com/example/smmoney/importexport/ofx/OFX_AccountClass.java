package com.example.smmoney.importexport.ofx;

import com.example.smmoney.misc.Enums;
import com.example.smmoney.records.AccountClass;

class OFX_AccountClass {
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
        this.ledgerBalance = account.balanceOfType(Enums.kBalanceTypeCurrent);
        if (this.ledgerBalance > -1.0E-8D && this.ledgerBalance < 0.0D) {
            this.ledgerBalance = 0.0D;
        }
    }

    OFX_AccountClass(String text, OFX_Tags tags) {
        this.tags = tags;
        this.parse(text);
    }

    String accountTypeAsString() {
        if (this.accountType == null) {
            return null;
        }
        return switch (this.accountType) {
            case OFX_CHECKING -> "CHECKING";
            case OFX_SAVINGS -> "SAVINGS";
            case OFX_MONEYMRKT -> "MONEYMRKT";
            case OFX_CREDITLINE -> "CREDITLINE";
            case OFX_CMA -> "CMA";
            case OFX_CREDITCARD -> "CREDITCARD";
            case OFX_INVESTMENT -> "INVESTMENT";
            default -> null;
        };
    }

    @SuppressWarnings("unused")
    public String description() {
        return "(bankID=" + this.bankID + "\taccountID=" + this.accountID + "\taccountType=" + this.accountTypeAsString() + ")";
    }

    int ofxAccountTypeAsSMMoneyAccountType() {
        if (this.accountType == null) {
            return -1;
        }
        return switch (this.accountType) {
            case OFX_CHECKING -> Enums.kAccountTypeChecking;
            case OFX_SAVINGS -> Enums.kAccountTypeSavings;
            case OFX_MONEYMRKT -> Enums.kAccountTypeMoneyMarket;
            case OFX_CREDITLINE -> Enums.kAccountTypeCreditLine;
            case OFX_CMA -> Enums.kAccountTypeAsset;
            case OFX_CREDITCARD -> Enums.kAccountTypeCreditCard;
            case OFX_INVESTMENT -> Enums.kAccountTypeInvestment;
            default -> -1;
        };
    }

    private void parse(String text) {
        if (text == null || text.isEmpty() || this.tags == null) {
            return;
        }
        this.bankID = OFXClass.stringBetween(text, this.tags.bankIDBegin, this.tags.bankIDEnd, this.tags.lineEnding);
        this.accountID = OFXClass.stringBetween(text, this.tags.accountIDBegin, this.tags.accountIDEnd, this.tags.lineEnding);
        this.setAccountTypeFromString(OFXClass.stringBetween(text, this.tags.accountTypeBegin, this.tags.accountTypeEnd, this.tags.lineEnding));
    }

    private OFX_AccountType smMoneyAccountTypeToOFXType(int type) {
        return switch (type) {
            case Enums.kAccountTypeChecking -> OFX_AccountType.OFX_CHECKING;
            case Enums.kAccountTypeCreditCard -> OFX_AccountType.OFX_CREDITCARD;
            case Enums.kAccountTypeAsset -> OFX_AccountType.OFX_CMA;
            case Enums.kAccountTypeSavings -> OFX_AccountType.OFX_SAVINGS;
            case Enums.kAccountTypeMoneyMarket -> OFX_AccountType.OFX_MONEYMRKT;
            case Enums.kAccountTypeCreditLine -> OFX_AccountType.OFX_CREDITLINE;
            case Enums.kAccountTypeInvestment -> OFX_AccountType.OFX_INVESTMENT;
            default -> OFX_AccountType.OFX_UNKOWN;
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
