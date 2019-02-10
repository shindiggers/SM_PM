package com.example.smmoney.importexport.ofx;

public enum OFX_TransactionType {
    OFX_ATM,
    OFX_CASH,
    OFX_CHECK,
    OFX_CREDIT,
    OFX_DEBIT,
    OFX_DEP,
    OFX_DIRECTDEBIT,
    OFX_DIRECTDEP,
    OFX_DIV,
    OFX_FEE,
    OFX_INT,
    OFX_OTHER,
    OFX_PAYMENT,
    OFX_POS,
    OFX_REPEATPMT,
    OFX_SRVCHG,
    OFX_UKNOWN,
    OFX_XFER;

    static {
        OFX_TransactionType[] var0 = new OFX_TransactionType[]{OFX_CREDIT, OFX_DEBIT, OFX_INT, OFX_DIV, OFX_FEE, OFX_SRVCHG, OFX_DEP, OFX_ATM, OFX_POS, OFX_XFER, OFX_CHECK, OFX_PAYMENT, OFX_CASH, OFX_DIRECTDEP, OFX_DIRECTDEBIT, OFX_REPEATPMT, OFX_OTHER, OFX_UKNOWN};
    }
}
