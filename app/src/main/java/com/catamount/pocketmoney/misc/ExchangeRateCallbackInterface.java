package com.catamount.pocketmoney.misc;

import com.catamount.pocketmoney.records.AccountClass;

public interface ExchangeRateCallbackInterface {
    void lookupExchangeRateCallback(ExchangeRateClass exchangeRateClass, double d, AccountClass accountClass);
}
