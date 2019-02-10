package com.example.smmoney.misc;

import com.example.smmoney.records.AccountClass;

public interface ExchangeRateCallbackInterface {
    void lookupExchangeRateCallback(ExchangeRateClass exchangeRateClass, double d, AccountClass accountClass);
}
