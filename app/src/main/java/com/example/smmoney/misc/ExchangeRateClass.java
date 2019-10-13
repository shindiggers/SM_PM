package com.example.smmoney.misc;

import com.example.smmoney.records.AccountClass;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ExchangeRateClass {
    private int accountUpdateCount = 0;
    private ExchangeRateCallbackInterface delegate;
    private boolean inverseLookup;
    private boolean justUpdateTheAccounts;

    public ExchangeRateClass(boolean justUpdate, ExchangeRateCallbackInterface delegate) {
        this.justUpdateTheAccounts = justUpdate;
        this.delegate = delegate;
    }

    public void updateExchangeRateForAccount(AccountClass account) {
        lookupExchangeRate(account.getCurrencyCode(), Prefs.getStringPref(Prefs.HOMECURRENCYCODE), account);
    }

    public void lookupExchangeRate(String from, String to, AccountClass account) {
        double exchangeRate = 1.0d;
        try {
            String wtf = downloadText("http://download.finance.yahoo.com/d/quotes.csv?s=" + from + to + "=X&f=l1");
            if (!wtf.equals("TIMEOUT")) {
                exchangeRate = Double.parseDouble(wtf);
                if (exchangeRate == 0.0d) {
                    return;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (exchangeRate >= 0.01d) {
            if (!this.inverseLookup) {
                exchangeRate = 1.0d / exchangeRate;
            }
            if (this.justUpdateTheAccounts) {
                updateAccount(exchangeRate, account);
            } else {
                this.delegate.lookupExchangeRateCallback(this, exchangeRate, account);
            }
        } else if (!this.inverseLookup) {
            this.inverseLookup = true;
            lookupExchangeRate(to, from, account);
        } else if (this.justUpdateTheAccounts) {
            updateAccount(exchangeRate, account);
        } else {
            this.delegate.lookupExchangeRateCallback(this, exchangeRate, account);
        }
    }

    private void updateAccount(double exchangeRate, AccountClass account) {
        if (exchangeRate > 0.0d) {
            account.setExchangeRate(exchangeRate);
            account.saveToDatabase();
        }
    }

    private String downloadText(String url) {
        StringBuffer result = new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(new URL(url).openStream());
            BufferedReader in = new BufferedReader(isr);
            while (true) {
                String inputLine = in.readLine();
                if (inputLine == null) {
                    break;
                }
                result.append(inputLine);
            }
            in.close();
            isr.close();
        } catch (Exception e) {
            result = new StringBuffer("TIMEOUT");
        }
        return result.toString();
    }
}
