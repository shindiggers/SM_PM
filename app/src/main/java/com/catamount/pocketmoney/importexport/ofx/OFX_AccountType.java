package com.catamount.pocketmoney.importexport.ofx;

public enum OFX_AccountType {
    OFX_CHECKING,
    OFX_CMA,
    OFX_CREDITCARD,
    OFX_CREDITLINE,
    OFX_INVESTMENT,
    OFX_MONEYMRKT,
    OFX_SAVINGS,
    OFX_UNKOWN;

    static {
        OFX_AccountType[] var0 = new OFX_AccountType[]{OFX_CHECKING, OFX_SAVINGS, OFX_MONEYMRKT, OFX_CREDITLINE, OFX_CMA, OFX_CREDITCARD, OFX_INVESTMENT, OFX_UNKOWN};
    }
}
