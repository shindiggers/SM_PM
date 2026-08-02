package com.example.smmoney.views.accounts;

import com.example.smmoney.records.AccountClass;

/**
 * A data record representing an item in the Accounts list.
 * Using a Java Record (available in Java 17) automatically handles final fields, 
 * constructor, and accessor methods.
 */
public record AccountListItem(int type, String label, AccountClass account, int sectionIndex) {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ACCOUNT = 1;
    public static final int TYPE_CUSTOM = 2;

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
