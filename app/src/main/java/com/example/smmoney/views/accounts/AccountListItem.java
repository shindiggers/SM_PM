package com.example.smmoney.views.accounts;

import com.example.smmoney.records.AccountClass;

public class AccountListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ACCOUNT = 1;
    public static final int TYPE_CUSTOM = 2;

    public final int type;
    public final String label; // For HEADER and CUSTOM
    public final AccountClass account; // For ACCOUNT
    public final int sectionIndex; // For HEADER

    public AccountListItem(int type, String label, AccountClass account, int sectionIndex) {
        this.type = type;
        this.label = label;
        this.account = account;
        this.sectionIndex = sectionIndex;
    }

    public static AccountListItem createHeader(String label, int sectionIndex) {
        return new AccountListItem(TYPE_HEADER, label, null, sectionIndex);
    }

    public static AccountListItem createAccount(AccountClass account) {
        return new AccountListItem(TYPE_ACCOUNT, null, account, -1);
    }

    public static AccountListItem createCustom(String label) {
        return new AccountListItem(TYPE_CUSTOM, label, null, -1);
    }
}
