package com.example.smmoney.misc;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.smmoney.R;

import java.util.List;

public class CurrencyPickerDialog extends AppCompatDialogFragment {

    public static final String TAG = "CurrencyPickerDialog";

    public interface OnCurrencySelectedListener {
        void onCurrencySelected(String currencyCode, String displayText);
    }

    private OnCurrencySelectedListener listener;

    public void setOnCurrencySelectedListener(OnCurrencySelectedListener listener) {
        this.listener = listener;
    }

    public static void show(FragmentManager fragmentManager, OnCurrencySelectedListener listener) {
        CurrencyPickerDialog dialog = new CurrencyPickerDialog();
        dialog.setOnCurrencySelectedListener(listener);
        dialog.show(fragmentManager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        List<CurrencyExt.CurrencyPickerItem> items = CurrencyExt.getSegmentedCurrencyList();

        CurrencyPickerAdapter adapter = new CurrencyPickerAdapter(context, items);

        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(context, PocketMoneyThemes.dialogTheme())
                .setTitle(R.string.ACCOUNT_CURRENCY_LABEL)
                .setView(listView)
                .create();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            CurrencyExt.CurrencyPickerItem item = items.get(position);
            if (!item.isHeader() && item.currencyCode() != null) {
                CurrencyExt.addRecentCurrency(item.currencyCode());
                if (listener != null) {
                    listener.onCurrencySelected(item.currencyCode(), item.displayText());
                }
                dialog.dismiss();
            }
        });

        return dialog;
    }

    private static class CurrencyPickerAdapter extends BaseAdapter {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        private final List<CurrencyExt.CurrencyPickerItem> items;
        private final LayoutInflater inflater;
        private final int headerTextColor;
        private final int itemTextColor;

        public CurrencyPickerAdapter(Context context, List<CurrencyExt.CurrencyPickerItem> items) {
            this.items = items;
            this.inflater = LayoutInflater.from(context);
            this.headerTextColor = PocketMoneyThemes.fieldLabelColor();
            this.itemTextColor = PocketMoneyThemes.primaryCellTextColor();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public boolean isEnabled(int position) {
            return !items.get(position).isHeader();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CurrencyExt.CurrencyPickerItem item = items.get(position);
            View rowView = convertView;
            if (item.isHeader()) {
                if (rowView == null) {
                    rowView = inflater.inflate(R.layout.currency_picker_header_row, parent, false);
                }
                TextView headerView = rowView.findViewById(R.id.currency_picker_header_text);
                headerView.setText(item.displayText());
                headerView.setTextColor(headerTextColor);
            } else {
                if (rowView == null) {
                    rowView = inflater.inflate(R.layout.currency_picker_item_row, parent, false);
                }
                TextView textView = rowView.findViewById(R.id.currency_picker_item_text);
                textView.setText(item.displayText());
                textView.setTextColor(itemTextColor);
            }
            return rowView;
        }
    }
}
