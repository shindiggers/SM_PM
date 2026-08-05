package com.example.smmoney.misc;

import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.records.AccountClass;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handles fetching currency exchange rates from the Frankfurter API.
 * Replaces the legacy Yahoo Finance implementation.
 */
public class ExchangeRateClass {
    private final ExchangeRateCallbackInterface delegate;
    private final boolean justUpdateTheAccounts;

    public ExchangeRateClass(boolean justUpdate, ExchangeRateCallbackInterface delegate) {
        this.justUpdateTheAccounts = justUpdate;
        this.delegate = delegate;
    }

    /**
     * Updates the exchange rate for a specific account based on its currency code
     * relative to the user's home currency.
     */
    public void updateExchangeRateForAccount(AccountClass account) {
        lookupExchangeRate("latest", account.getCurrencyCode(), Prefs.getStringPref(Prefs.HOMECURRENCYCODE), account);
    }

    /**
     * Performs a network lookup for the latest exchange rate between two currencies.
     */
    public void lookupExchangeRate(String from, String to, AccountClass account) {
        lookupExchangeRate("latest", from, to, account);
    }

    /**
     * Performs a network lookup for the exchange rate between two currencies for a specific date.
     *
     * @param date    The date for the rate lookup ("latest" or "YYYY-MM-DD")
     * @param from    The source currency code (e.g., "USD")
     * @param to      The target currency code (e.g., "GBP")
     * @param account The account to update (can be null if only using the callback)
     */
    public void lookupExchangeRate(String date, String from, String to, AccountClass account) {
        if (from == null || to == null || from.equals(to)) {
            handleResult(1.0d, account);
            return;
        }

        double rate = 0.0d;
        try {
            URL url = new URL("https://api.frankfurter.app/" + date + "?from=" + from + "&to=" + to);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    JSONObject json = new JSONObject(response.toString());
                    rate = json.getJSONObject("rates").getDouble(to);
                }
            } else {
                Log.e(SMMoney.TAG, "Exchange rate lookup failed with response code: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error in lookupExchangeRate: " + e.getMessage());
        }

        handleResult(rate, account);
    }

    private void handleResult(double rate, AccountClass account) {
        if (this.justUpdateTheAccounts && account != null && rate > 0.0d) {
            updateAccount(rate, account);
        } else if (this.delegate != null) {
            this.delegate.lookupExchangeRateCallback(this, rate, account);
        }
    }

    private void updateAccount(double exchangeRate, AccountClass account) {
        account.setExchangeRate(exchangeRate);
        account.saveToDatabase();
    }
}
