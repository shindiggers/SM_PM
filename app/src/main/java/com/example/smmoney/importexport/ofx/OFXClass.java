package com.example.smmoney.importexport.ofx;

import android.util.Log;

import com.example.smmoney.records.AccountClass;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

class OFXClass {
    private AccountClass account;
    private OFX_DataFormatType format;
    OFX_Statement statement;
    private OFX_Tags tags;
    List transactions;

    OFXClass() {
        this.format = OFX_DataFormatType.OFX_DataFormatSGML;
        this.tags = new OFX_Tags(this.format, "\n");
    }

    public OFXClass(String var1) {
        this.parse(var1);
    }

    public static String TAGofEOL(String var0, String var1) {
        return var0 != null && var0.length() > 0?var0:var1;
    }

    static String amountAsOFXAmount(double var0) {
        NumberFormat var2 = NumberFormat.getInstance();
        var2.setMinimumFractionDigits(2);
        return var2.format(var0).replace(" ", "").replace(",", ".");
    }

    static double amountFromOFXAmount(String var0) {
        if(var0 != null) {
            try {
                if(var0.length() == 0) {
                    return 0.0D;
                }

                return Double.parseDouble(var0.trim().replace(",", "."));
            } catch (NumberFormatException var4) {
                var4.printStackTrace();
            }
        }

        return 0.0D;
    }

    private String bankMessage() {
        if(this.account != null && this.account.getType() == 2) {
            OFX_Statement var2 = new OFX_Statement(this.transactions, this.tags);
            return "\t<BANKMSGSRSV1>\n" + var2.toString() + "\t</BANKMSGSRSV1>\n";
        } else {
            OFX_CreditCardStatement var1 = new OFX_CreditCardStatement(this.transactions, this.tags);
            return "\t<CREDITCARDMSGSRSV1>\n" + var1.toString() + "\t</CREDITCARDMSGSRSV1>\n";
        }
    }

    static String dateAsString(GregorianCalendar var0) {
        return dateFormatterForOFXDateTime().format(var0.getTime());
    }

    private static SimpleDateFormat dateFormatterForOFXDate() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US);
    }

    private static SimpleDateFormat dateFormatterForOFXDateTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    }

    static GregorianCalendar dateFromString(String var0) {
        SimpleDateFormat var1 = dateFormatterForOFXDateTime();
        if(var0 == null) {
            Log.i("com.catamount.pocketmon", "nullString (OFXClass[dateFromString])");
            return null;
        } else {
            int var2 = var0.length();
            int var3 = "yyyyMMddHHmmss".length();
            Date var4 = null;
            if(var2 >= var3) {
                label27: {
                    Date var11;
                    try {
                        var11 = var1.parse(var0.substring(0, "yyyyMMddHHmmss".length()));
                    } catch (ParseException var13) {
                        var13.printStackTrace();
                        var4 = null;
                        break label27;
                    }

                    var4 = var11;
                }
            }

            if(var4 == null) {
                SimpleDateFormat var6 = dateFormatterForOFXDate();

                Date var9;
                try {
                    var9 = var6.parse(var0);
                } catch (ParseException var12) {
                    Log.i("com.catamount.pocketmon", "failed to parse dateString (OFXClass[dateFromString]) : " + var0);
                    var12.printStackTrace();
                    return null;
                }

                var4 = var9;
            }

            GregorianCalendar var5 = new GregorianCalendar();
            var5.setTimeInMillis(var4.getTime());
            return var5;
        }
    }

    private String header() {
        return "OFXHEADER:100\nDATA:OFXSGML\nVERSION:102\nSECURITY:TYPE1\nENCODING:USASCII\nCHARSET:1252\nCOMPRESSION:NONE\nOLDFILEUID:NONE\nNEWFILEUID:NONE\n\n";
    }

    private void parse(String var1) {
        if(!var1.contains("</CODE>")) {
            this.format = OFX_DataFormatType.OFX_DataFormatSGML;
        } else {
            this.format = OFX_DataFormatType.OFX_DataFormatXML10;
        }

        String var2;
        if(!var1.contains("\r")) {
            var2 = "\n";
        } else {
            var2 = "\r";
        }

        this.tags = new OFX_Tags(this.format, var2);
        String var3 = var2 + "<";
        String var4 = var1.replace(var3, "<").replace("<", var3);
        String var5 = stringBetween(var4, this.tags.bankStatementTransmissionBegin, this.tags.bankStatementTransmissionEnd, var2);
        if(var5.length() > 0) {
            this.statement = new OFX_Statement(var5, this.tags);
        }

        if(var5.length() == 0 || this.statement == null || this.statement.ofxtransactions.size() == 0) {
            this.statement = new OFX_CreditCardStatement(stringBetween(var4, this.tags.creditCardStatementTransmissionBegin, this.tags.creditCardStatementTransmissionEnd, var2), this.tags);
        }

    }

    private String signOnMessage() {
        return "\t<SIGNONMSGSRSV1>\n\t\t<SONRS>\n" + this.statusMessage("OK", "0", "INFO") + "\t\t\t<DTSERVER>" + dateAsString(new GregorianCalendar()) + "\n" + "\t\t\t<LANGUAGE>ENG\n" + "\t\t\t<FI>\n" + "\t\t\t\t<ORG>xxxx-optional\n" + "\t\t\t\t<FID>xxxx-optional\n" + "\t\t\t</FI>\n" + "\t\t\t<INTU.BID>zzzz-required(has to match the value Quicken already has for the bank associated with the account of the export file)\n" + "\t\t\t<INTU.USERID>xxxx-optional\n" + "\t\t</SONRS>\n" + "\t</SIGNONMSGSRSV1>\n";
    }

    private String statusMessage(String var1, String var2, String var3) {
        return "\t\t\t" + this.tags.statusBegin + "\n" + "\t\t\t\t" + this.tags.statusCodeBegin + var2 + this.tags.statusCodeEnd + "\n" + "\t\t\t\t" + this.tags.statusSeverityBegin + var3 + this.tags.statusSeverityEnd + "\n" + "\t\t\t\t" + this.tags.statusMessageBegin + var1 + this.tags.statusMessageEnd + "\n" + "\t\t\t" + this.tags.statusEnd + "\n";
    }

    static String stringBetween(String param0, String param1, String param2, String param3) {
        // $FF: Couldn't be decompiled
        return "TEST";
    }

    public String toString() {
        StringBuilder var1 = new StringBuilder();
        var1.append(this.header());
        var1.append("<OFX>\n");
        var1.append(this.signOnMessage());
        var1.append(this.bankMessage());
        var1.append("</OFX>");
        return var1.toString();
    }
}
