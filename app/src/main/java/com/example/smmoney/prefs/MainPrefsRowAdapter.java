package com.example.smmoney.prefs;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.accounts.AccountsActivity;

import java.util.ArrayList;

class MainPrefsRowAdapter extends BaseAdapter {
    private Context context;
    private int[] imageList;
    private LayoutInflater inflater;
    private ArrayList<OnClickListener> listenerList;
    private String[] nameList;

    static class ViewHolder {
        ImageView image;
        FrameLayout theRow;
        TextView title;

        ViewHolder() {
        }
    }

    MainPrefsRowAdapter(Context aContext) {
        this.context = aContext;
        this.inflater = LayoutInflater.from(aContext);
        setupTheLists();
    }

    private void setupTheLists() {
        this.listenerList = new ArrayList<>();
        PackageInfo pInfo = null;
        String version = "SMMoney ";
        String translations = Locales.kLOC_PREFERENCES_ABOUT_TRANSLATION;
        String text = "\n \u00a9 2016, Catamount Software\nhttp://www.catamount.com";
        try {
            pInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (pInfo != null) {
            final String theText;
            theText = version + pInfo.versionName + text + (translations.length() > 0 ? "\n\n" + translations : "");

            this.listenerList.add(new OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(MainPrefsRowAdapter.this.context, theText, Toast.LENGTH_LONG).show();
                }
            });
        }
        this.listenerList.add(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.intent.action.VIEW");
                i.setData(Uri.parse("http://www.catamount.com/AndroidApps/PocketMoneyHelp/"));
                try {
                    MainPrefsRowAdapter.this.context.startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainPrefsRowAdapter.this.context, "Browser not found.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (AccountsActivity.IS_GOOGLE_MARKET) {
            this.listenerList.add(new OnClickListener() {
                public void onClick(View v) {
                    if (SMMoney.isLiteVersion()) {
                        new Builder(MainPrefsRowAdapter.this.context).setTitle("In-App Purchases").setMessage("In-App Purchases are not available in the Lite version").setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                    } else {
                        new Builder(MainPrefsRowAdapter.this.context).setTitle("In-App Purchases").setMessage("In-App Purchases now included!").setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                    }
                }
            });
        }
        this.listenerList.add(new OnClickListener() {
            public void onClick(View v) {
                MainPrefsRowAdapter.this.context.startActivity(new Intent(MainPrefsRowAdapter.this.context, SecurityPrefsActivity.class));
            }
        });
        this.listenerList.add(new OnClickListener() {
            public void onClick(View v) {
                MainPrefsRowAdapter.this.context.startActivity(new Intent(MainPrefsRowAdapter.this.context, CurrencyPrefsActivity.class));
            }
        });
        this.listenerList.add(new OnClickListener() {
            public void onClick(View v) {
                MainPrefsRowAdapter.this.context.startActivity(new Intent(MainPrefsRowAdapter.this.context, DataTransfersPrefsActivity.class));
            }
        });
        this.listenerList.add(new OnClickListener() {
            public void onClick(View v) {
                MainPrefsRowAdapter.this.context.startActivity(new Intent(MainPrefsRowAdapter.this.context, DisplayOptionsPrefsActivity.class));
            }
        });
        this.listenerList.add(new OnClickListener() {
            public void onClick(View v) {
                MainPrefsRowAdapter.this.context.startActivity(new Intent(MainPrefsRowAdapter.this.context, ManagedListsPrefsActivity.class));
            }
        });
        this.listenerList.add(new OnClickListener() {
            public void onClick(View v) {
                MainPrefsRowAdapter.this.context.startActivity(new Intent(MainPrefsRowAdapter.this.context, RepeatingTransactionPrefsActivity.class));
            }
        });
        this.listenerList.add(new OnClickListener() {
            public void onClick(View v) {
                Prefs.resetHints();
            }
        });
        if (AccountsActivity.IS_GOOGLE_MARKET) {
            this.listenerList.add(new OnClickListener() {
                public void onClick(View v) {
                    new Builder(MainPrefsRowAdapter.this.context).setTitle("Restore Purchases").setMessage("This will restore your in-app-purchases. Please ensure that you are connected to the internet.").setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            });
        }
        if (AccountsActivity.IS_GOOGLE_MARKET) {
            this.nameList = new String[]{Locales.kLOC_PREFERENCES_ABOUT_TITLE, Locales.kLOC_PREFERENCES_HELP_TITLE, "In App Purchases", Locales.kLOC_PREFS_SECURITY, Locales.kLOC_ACCOUNT_CURRENCY_LABEL, Locales.kLOC_PREFS_DATATRANFER, Locales.kLOC_PREFS_VIEWOPTIONS, Locales.kLOC_PREFS_MANAGEDLISTS, Locales.kLOC_REPEATING_TRANSACTIONS, Locales.kLOC_PREFERENCES_TIPS_TITLE};
            int[] atemp = new int[10];
            atemp[0] = R.drawable.abouticon;
            atemp[1] = R.drawable.helpicon;
            atemp[2] = R.drawable.savings;
            atemp[3] = R.drawable.securityicon;
            atemp[4] = R.drawable.currencyicon;
            atemp[5] = R.drawable.filetransfersicon;
            atemp[6] = R.drawable.viewprefs;
            atemp[7] = R.drawable.managedlistsicon;
            atemp[8] = R.drawable.ic_date_range;
            this.imageList = atemp;
            return;
        }
        this.nameList = new String[]{Locales.kLOC_PREFERENCES_ABOUT_TITLE, Locales.kLOC_PREFERENCES_HELP_TITLE, Locales.kLOC_PREFS_SECURITY, Locales.kLOC_ACCOUNT_CURRENCY_LABEL, Locales.kLOC_PREFS_DATATRANFER, Locales.kLOC_PREFS_VIEWOPTIONS, Locales.kLOC_PREFS_MANAGEDLISTS, Locales.kLOC_REPEATING_TRANSACTIONS, Locales.kLOC_PREFERENCES_TIPS_TITLE};
        int[] atemp = new int[9];
        atemp[0] = R.drawable.abouticon;
        atemp[1] = R.drawable.helpicon;
        atemp[2] = R.drawable.securityicon;
        atemp[3] = R.drawable.currencyicon;
        atemp[4] = R.drawable.filetransfersicon;
        atemp[5] = R.drawable.viewprefs;
        atemp[6] = R.drawable.managedlistsicon;
        atemp[7] = R.drawable.repeatingicon;
        this.imageList = atemp;
    }

    public int getCount() {
        return this.nameList.length;
    }

    public Object getItem(int arg0) {
        return this.nameList[arg0];
    }

    public long getItemId(int arg0) {
        return (long) arg0;
    }

    public View getView(int position, View convertView, ViewGroup arg2) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.prefs_row, arg2, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.prefsrowtext);
            holder.title.setTextColor(PocketMoneyThemes.primaryCellTextColor());
            holder.image = convertView.findViewById(R.id.prefsrowimage);
            holder.theRow = (FrameLayout) holder.title.getParent();
            holder.theRow.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.image.setImageResource(this.imageList[position]);
        holder.title.setText(this.nameList[position]);
        holder.theRow.setOnClickListener(this.listenerList.get(position));
        return convertView;
    }
}
