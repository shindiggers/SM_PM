package com.example.smmoney.views.accounts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyActivity;

public class AccountTypeIconGridActivity extends PocketMoneyActivity {
    private static String[] icons = new String[]{
            "ic_axa.xml",
            "ic_bank_of_scotland.xml",
            "ic_bankofamerica.xml",
            "ic_barclays.xml",
            "ic_bitcoin.xml",
            "ic_bmw_black.xml",
            "ic_first_direct.xml",
            "ic_halifax.xml",
            "ic_hsbc.xml",
            "ic_ing.xml",
            "ic_irs.xml",
            "ic_lloyds_bank.xml",
            "ic_mastercard.xml",
            "ic_nabcomau.xml",
            "ic_nationwide.xml",
            "ic_natwest.xml",
            "ic_nsi.xml",
            "ic_paypal.xml",
            "ic_rbs.xml",
            "ic_sainsburys.xml",
            "ic_sainsburys_bank.xml",
            "ic_santanderbank.xml",
            "ic_scotiabank.xml",
            "ic_scottish_widows.xml",
            "ic_usaa.xml",
            "ic_usbank.xml",
            "ic_visa.xml",
            "ic_wells_fargo.xml",
            "ic_westpaccomau.xml",
            "ic_alliant_credit_union.xml",
            "ic_amex.xml",
            "ic_anz.xml",
            "ic_bank_of_china.xml",
            "ic_bank_of_montreal.xml",
            "ic_bank_of_queensland.xml",
            "ic_bank_west.xml",
            "ic_banque_casino.xml",
            "ic_bendigo_bank.xml",
            "ic_best_buy.xml",
            "ic_bhd.xml",
            "ic_bnp_paribas.xml",
            "ic_bt.xml",
            "ic_cibc.xml",
            "ic_cic.xml",
            "ic_citi.xml",
            "ic_commerz_bank.xml",
            "ic_davidienda.xml",
            "ic_deutchbank.xml",
            "ic_diners_club.xml",
            "ic_discover.xml",
            "ic_dz_bank.xml",
            "ic_etrade.xml",
            "ic_fidelity.xml",
            "ic_icbc.xml",
            "ic_keybank.xml",
            "ic_la_banque_postal.xml",
            "ic_mitsubishi.xml",
            "ic_caisse_d_epargne.xml",
            "ic_pc_financial.xml",
            "ic_populaire_banque.xml",
            "ic_progresso.xml",
            "ic_public_bank.xml",
            "ic_rabo_bank.xml",
            "ic_sns.xml",
            "ic_sparkasse_bank.xml",
            "ic_society_general.xml",
            "ic_st_george.xml",
            "ic_suntrust.xml",
            "ic_suncorp.xml",
            "ic_td.xml",
            "ic_unicredit.xml",
            "ic_uob_india.xml",

            "Chase.png",
            "CreditSuisse.png",
            "StandardChartered.png",
            "Uno-e.png",
            "SUV.png",
            "airplane.png",
            "asset.png",
            "bank.png",
            "car.png",
            "car1.png",
            "car2.png",
            "car3.png",
            "cash.png",
            "cash2.png",
            "checkbook.png",
            "dental.png",
            "eCheck.png",
            "envelope.png",
            "euro.png",
            "farm.png",
            "gambling.png",
            "gold.png",
            "grocerybag.png",
            "hotel.png",
            "house.png",
            "house2.png",
            "liability.png",
            "loans.png",
            "medical.png",
            "medical2.png",
            "online.png",
            "pound.png",
            "present.png",
            "rentalcar.png",
            "savings.png",
            "store.png",
            "textbooks.png"};

    static class ImageAdapter extends BaseAdapter {
        private Context mContext;
        // The order of this array determines display order in AccountTypeIconGridActivity.java
        private Integer[] mThumbIds = new Integer[]{
                R.drawable.ic_axa,
                R.drawable.ic_bank_of_scotland,
                R.drawable.ic_bankofamerica,
                R.drawable.ic_barclays,
                R.drawable.ic_bitcoin,
                R.drawable.ic_bmw_black,
                R.drawable.ic_first_direct,
                R.drawable.ic_halifax,
                R.drawable.ic_hsbc,
                R.drawable.ic_ing,
                R.drawable.ic_irs,
                R.drawable.ic_lloyds_bank,
                R.drawable.ic_mastercard,
                R.drawable.ic_nabcomau,
                R.drawable.ic_nationwide,
                R.drawable.ic_natwest,
                R.drawable.ic_nsi,
                R.drawable.ic_paypal,
                R.drawable.ic_rbs,
                R.drawable.ic_sainsburys,
                R.drawable.ic_sainsburys_bank,
                R.drawable.ic_santanderbank,
                R.drawable.ic_scotiabank,
                R.drawable.ic_scottish_widows,
                R.drawable.ic_usaa,
                R.drawable.ic_usbank,
                R.drawable.ic_visa,
                R.drawable.ic_wells_fargo,
                R.drawable.ic_alliant_credit_union,
                R.drawable.ic_amex,
                R.drawable.ic_anz,
                R.drawable.ic_bank_of_china,
                R.drawable.ic_bank_of_montreal,
                R.drawable.ic_bank_of_queensland,
                R.drawable.ic_bank_west,
                R.drawable.ic_banque_casino,
                R.drawable.ic_bendigo_bank,
                R.drawable.ic_best_buy,
                R.drawable.ic_bhd,
                R.drawable.ic_bnp_paribas,
                R.drawable.ic_cibc,
                R.drawable.ic_cic,
                R.drawable.ic_citi,
                R.drawable.ic_commerz_bank,
                R.drawable.ic_credit_argicole,
                R.drawable.ic_diners_club,
                R.drawable.ic_davidienda,
                R.drawable.ic_deutchbank,
                R.drawable.ic_discover,
                R.drawable.ic_dz_bank,
                R.drawable.ic_etrade,
                R.drawable.ic_fidelity,
                R.drawable.ic_icbc,
                R.drawable.ic_keybank,
                R.drawable.ic_la_banque_postal,
                R.drawable.ic_mitsubishi,
                R.drawable.ic_pc_financial,
                R.drawable.ic_populaire_banque,
                R.drawable.ic_progresso,
                R.drawable.ic_public_bank,
                R.drawable.ic_rabo_bank,
                R.drawable.ic_sns,
                R.drawable.ic_sparkasse_bank,
                R.drawable.ic_society_general,
                R.drawable.ic_st_george,
                R.drawable.ic_suntrust,
                R.drawable.ic_suncorp,
                R.drawable.ic_td,
                R.drawable.ic_unicredit,
                R.drawable.ic_uob_india,

                R.drawable.airplane,
                R.drawable.asset,
                R.drawable.bank,
                R.drawable.car1,
                R.drawable.car2,
                R.drawable.car3,
                R.drawable.cash,
                R.drawable.cash2,
                R.drawable.checkbook,
                R.drawable.dental,
                R.drawable.echeck,
                R.drawable.envelope,
                R.drawable.euro,
                R.drawable.farm,
                R.drawable.gambling,
                R.drawable.gold,
                R.drawable.grocerybag,
                R.drawable.hotel,
                R.drawable.house,
                R.drawable.house2,
                R.drawable.liability,
                R.drawable.loans,
                R.drawable.medical,
                R.drawable.medical2,
                R.drawable.online,
                R.drawable.pound,
                R.drawable.present,
                R.drawable.rentalcar,
                R.drawable.savings,
                R.drawable.store,
                R.drawable.suv,
                R.drawable.textbooks};

        ImageAdapter(Context c) {
            this.mContext = c;
        }

        public int getCount() {
            return this.mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(this.mContext);
                imageView.setLayoutParams(new LayoutParams(150, 150));
                imageView.setScaleType(ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(this.mThumbIds[position]);
            imageView.setTag(this.mThumbIds[position]);
            return imageView;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounttypeicongrid);
        setTitle("");
        GridView gridview = findViewById(R.id.gridview);
        gridview.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        gridview.setOnItemClickListener(getClickListener());
        gridview.setAdapter(new ImageAdapter(this));
    }

    private OnItemClickListener getClickListener() {
        return new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent i = new Intent();
                i.putExtra("selection", view.getTag().toString());
                AccountTypeIconGridActivity.this.setResult(1, i);
                AccountTypeIconGridActivity.this.finish();
            }
        };
    }

    public static String replaceIconNameWithUppercase(String iconName) {
        for (String icon : icons) {
            if (iconName.equals(icon.replace("&", "").replace("-", "").replace(" ", "").toLowerCase())) {
                return icon;
            }
        }
        return "checkbook.png";
    }
}
