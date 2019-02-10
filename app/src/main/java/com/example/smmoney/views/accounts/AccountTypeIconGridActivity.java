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
    private static String[] icons = new String[]{"AXA.png", "Alliance_&_Leicester.png", "Alliant.png", "BHD.png", "BNP_Paribas.png", "BT.png", "BancoPopular.png", "BankOfChina.png", "Bitcoin.png", "Bank_of_Montreal.png", "BanqueCasino.png", "Barclays.png", "BestBuy.png", "BofA.png", "CA.png", "CIBC.png", "CIC.png", "CapitalOne.png", "Chase.png", "Citigroup.png", "CommerzBank.png", "CreditSuisse.png", "DZBank.png", "Davivienda.png", "DeutscheBank.png", "DinersClub.png", "FrBnk.png", "HBOS.png", "HSBC_Direct.png", "Halifax.png", "ICBC.png", "ING.png", "KeyBank.png", "LaBanquePostale.png", "Leon.png", "LloydsTSB.png", "Mitsubishi.png", "NS&I.png", "Natwest.png", "PCFinancial.png", "Populaire.png", "Popular.png", "Progreso.png", "PublicBank.png", "RBC.png", "RBS.png", "Rabo.png", "SNS.png", "Santander.png", "Scotia.png", "SocieteGeneral.png", "Sparkasse.png", "St George.png", "StandardChartered.png", "SunTrust.png", "TD.png", "UOB.png", "USAA.png", "USBank.png", "UniCredit.png", "Uno-e.png", "Wachovia.png", "Wells Fargo.png", "anz.png", "bankofqld.png", "bankwest.png", "bendigobank.png", "commbank.png", "etrade.png", "fidelity.png", "nab.png", "nationwide.png", "suncorp.png", "westpac.png", "IRS.png", "MasterCard2.png", "SUV.png", "airplane.png", "amex.png", "asset.png", "bank.png", "car.png", "car1.png", "car2.png", "car3.png", "cash.png", "cash2.png", "checkbook.png", "dental.png", "discover.png", "eCheck.png", "envelope.png", "euro.png", "farm.png", "gambling.png", "gold.png", "grocerybag.png", "hotel.png", "house.png", "house2.png", "liability.png", "loans.png", "mastercard.png", "medical.png", "medical2.png", "online.png", "paypal.png", "pound.png", "present.png", "rentalcar.png", "savings.png", "store.png", "textbooks.png", "visa.png", "visa2.png"};

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private Integer[] mThumbIds = new Integer[]{R.drawable.alliance__leicester, R.drawable.alliant, R.drawable.airplane, R.drawable.amex, R.drawable.asset, R.drawable.anz, R.drawable.axa, R.drawable.bancopopular, R.drawable.bank, R.drawable.bank_of_montreal, R.drawable.bankofchina, R.drawable.bankofqld, R.drawable.bankwest, R.drawable.banquecasino, R.drawable.barclays, R.drawable.bendigobank, R.drawable.bestbuy, R.drawable.bhd, R.drawable.bitcoin, R.drawable.bnp_paribas, R.drawable.bofa, R.drawable.bt, R.drawable.ca, R.drawable.capitalone, R.drawable.car, R.drawable.car1, R.drawable.car2, R.drawable.car3, R.drawable.cash, R.drawable.cash2, R.drawable.checkbook, R.drawable.cibc, R.drawable.cic, R.drawable.citigroup, R.drawable.commbank, R.drawable.commerzbank, R.drawable.davivienda, R.drawable.dental, R.drawable.deutschebank, R.drawable.dinersclub, R.drawable.discover, R.drawable.dzbank, R.drawable.echeck, R.drawable.envelope, R.drawable.etrade, R.drawable.euro, R.drawable.farm, R.drawable.fidelity, R.drawable.frbnk, R.drawable.gambling, R.drawable.gold, R.drawable.grocerybag, R.drawable.halifax, R.drawable.hbos, R.drawable.hotel, R.drawable.house, R.drawable.house2, R.drawable.hsbc_direct, R.drawable.icbc, R.drawable.ing, R.drawable.irs, R.drawable.keybank, R.drawable.labanquepostale, R.drawable.leon, R.drawable.liability, R.drawable.lloydstsb, R.drawable.loans, R.drawable.mastercard, R.drawable.mastercard2, R.drawable.medical, R.drawable.medical2, R.drawable.mitsubishi, R.drawable.nab, R.drawable.nationwide, R.drawable.natwest, R.drawable.nsi, R.drawable.online, R.drawable.paypal, R.drawable.pcfinancial, R.drawable.populaire, R.drawable.popular, R.drawable.pound, R.drawable.present, R.drawable.progreso, R.drawable.publicbank, R.drawable.rabo, R.drawable.rbc, R.drawable.rbs, R.drawable.rentalcar, R.drawable.santander, R.drawable.savings, R.drawable.scotia, R.drawable.sns, R.drawable.sparkasse, R.drawable.societegeneral, R.drawable.stgeorge, R.drawable.standardchartered, R.drawable.store, R.drawable.suncorp, R.drawable.suntrust, R.drawable.suv, R.drawable.td, R.drawable.textbooks, R.drawable.unicredit, R.drawable.unoe, R.drawable.uob, R.drawable.usaa, R.drawable.usbank, R.drawable.visa, R.drawable.visa2, R.drawable.wachovia, R.drawable.wellsfargo, R.drawable.westpac};

        public ImageAdapter(Context c) {
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
                imageView.setLayoutParams(new LayoutParams(100, 100));
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
