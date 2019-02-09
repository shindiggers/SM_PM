package com.catamount.pocketmoney.prefs;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.PocketMoneyActivity;

public class RepeatingTransactionPrefsActivity extends PocketMoneyActivity {
    private CheckBox postCheckBox;
    private EditText postEditText;
    private RelativeLayout postView;
    private String suffix;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LayoutInflater.from(this).inflate(R.layout.prefs_repeating, null));
        setTitle(Locales.kLOC_REPEATING_TRANSACTIONS);
        this.postCheckBox = findViewById(R.id.postcheckbox);
        this.postCheckBox.setButtonDrawable(Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android"));
        this.postEditText = findViewById(R.id.postedittext);
        this.postView = (RelativeLayout) this.postEditText.getParent();
        this.suffix = Locales.kLOC_PREFERENCES_DAYS;
        setupViews();
    }

    private void setupViews() {
        this.postCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RepeatingTransactionPrefsActivity.this.postView.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            }
        });
        this.postEditText.setText(new StringBuilder(String.valueOf(Prefs.getIntPref(Prefs.RECURRDAYSINADVANCE))).toString());
        this.postView.setVisibility(Prefs.getBooleanPref(Prefs.RECURPOSTINGENABLED) ? View.VISIBLE : View.INVISIBLE);
        this.postCheckBox.setChecked(Prefs.getBooleanPref(Prefs.RECURPOSTINGENABLED));
        ((TextView) findViewById(R.id.post_repeating_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        ((TextView) findViewById(R.id.post_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        ((TextView) findViewById(R.id.daysinadvance)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.postView.setBackgroundColor(PocketMoneyThemes.alternatingRowColor());
        ((View) this.postCheckBox.getParent()).setBackgroundColor(PocketMoneyThemes.alternatingRowColor());
        findViewById(R.id.the_parent).setBackgroundColor(PocketMoneyThemes.alternatingRowColor());
    }

    private void save() {
        try {
            if (Integer.parseInt(this.postEditText.getText().toString().contains(this.suffix) ? this.postEditText.getText().toString().replace(this.suffix, "") : this.postEditText.getText().toString()) == 0) {
                Prefs.setPref(Prefs.RECURRDAYSINADVANCE, 0);
            } else {
                Prefs.setPref(Prefs.RECURRDAYSINADVANCE, Integer.parseInt(this.postEditText.getText().toString().contains(this.suffix) ? this.postEditText.getText().toString().replace(this.suffix, "") : this.postEditText.getText().toString()));
            }
        } catch (Exception e) {
            Prefs.setPref(Prefs.RECURRDAYSINADVANCE, 0);
        }
        Prefs.setPref(Prefs.RECURPOSTINGENABLED, this.postCheckBox.isChecked());
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        save();
        finish();
        return true;
    }
}
