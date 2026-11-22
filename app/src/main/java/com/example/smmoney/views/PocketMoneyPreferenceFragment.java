package com.example.smmoney.views;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

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
        if (getActivity() instanceof PocketMoneyPreferenceActivity) {
            ((PocketMoneyPreferenceActivity) getActivity()).onPreferencesCreated(this);
        }
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView listView = getListView();
        if (listView != null) {
            listView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
            listView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(@NonNull View childView) {
                    updateChildAlpha(childView);
                }

                @Override
                public void onChildViewDetachedFromWindow(@NonNull View childView) {
                }
            });

            if (listView.getAdapter() != null) {
                listView.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onChanged() {
                        refreshAllChildrenAlpha();
                    }

                    @Override
                    public void onItemRangeChanged(int positionStart, int itemCount) {
                        refreshAllChildrenAlpha();
                    }
                });
            }
        }
    }

    private void updateChildAlpha(View childView) {
        RecyclerView listView = getListView();
        if (listView == null || childView == null) return;
        RecyclerView.ViewHolder vh = listView.getChildViewHolder(childView);
        if (vh instanceof PreferenceViewHolder) {
            int pos = vh.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Preference pref = findPreferenceAtPosition(getPreferenceScreen(), pos, new int[]{0});
                if (pref != null) {
                    childView.setAlpha(pref.isEnabled() ? 1.0f : 0.38f);
                    return;
                }
            }
            childView.setAlpha(childView.isEnabled() ? 1.0f : 0.38f);
        }
    }

    private void refreshAllChildrenAlpha() {
        RecyclerView listView = getListView();
        if (listView == null) return;
        listView.post(() -> {
            for (int i = 0; i < listView.getChildCount(); i++) {
                View child = listView.getChildAt(i);
                if (child != null) {
                    updateChildAlpha(child);
                }
            }
        });
    }

    private Preference findPreferenceAtPosition(PreferenceGroup group, int targetPos, int[] currentPos) {
        if (group == null) return null;
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference p = group.getPreference(i);
            if (currentPos[0] == targetPos) {
                return p;
            }
            currentPos[0]++;
            if (p instanceof PreferenceGroup) {
                Preference found = findPreferenceAtPosition((PreferenceGroup) p, targetPos, currentPos);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
