package com.example.smmoney.views;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.misc.PocketMoneyThemes;

public class PocketMoneyPreferenceFragment extends PreferenceFragmentCompat {
    public static final String ARG_XML_RES_ID = "xml_res_id";

    public static PocketMoneyPreferenceFragment newInstance(int xmlResId) {
        PocketMoneyPreferenceFragment fragment = new PocketMoneyPreferenceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_XML_RES_ID, xmlResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        int xmlResId = getArguments() != null ? getArguments().getInt(ARG_XML_RES_ID) : 0;
        if (xmlResId != 0) {
            setPreferencesFromResource(xmlResId, rootKey);
        }
        if (getActivity() instanceof PocketMoneyPreferenceActivityV2) {
            ((PocketMoneyPreferenceActivityV2) getActivity()).onPreferencesCreated(this);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getListView() != null) {
            getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        }
    }
}
