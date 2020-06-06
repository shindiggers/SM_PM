package com.example.smmoney.importexport.ofx;

import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.TransactionClass;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

class OFXClass {
    AccountClass account;
    private OFX_DataFormatType format;
    OFX_Statement statement;
    private OFX_Tags tags;
    List<TransactionClass> transactions;

    OFXClass() {
        this.format = OFX_DataFormatType.OFX_DataFormatSGML;
        this.tags = new OFX_Tags(this.format, "\n");
    }

    OFXClass(String text) {
        this.parse(text);
    }

    private static String TAGofEOL(String s, String lineEnding) {
        return s != null && s.length() > 0 ? s : lineEnding;
    }

    static String amountAsOFXAmount(double amt) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(2);
        return format.format(amt).replace(" ", "").replace(",", ".");
    }

    static double amountFromOFXAmount(String text) {
        if (text != null) {
            try {
                if (text.length() == 0) {
                    return 0.0D;
                }

                return Double.parseDouble(text.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return 0.0D;
    }

    static String dateAsString(GregorianCalendar date) {
        return dateFormatterForOFXDateTime().format(date.getTime());
    }

    static GregorianCalendar dateFromString(String dateString) {
        SimpleDateFormat format = dateFormatterForOFXDateTime();
        if (dateString == null) {
            Log.i(SMMoney.TAG, "nullString (OFXClass[dateFromString])");
            return null;
        } else {
            int dateStringLength = dateString.length();
            int simpleDateTimeLength = "yyyyMMddHHmmss".length();
            Date date = null;
            if (dateStringLength >= simpleDateTimeLength) {
                try {
                    date = format.parse(dateString.substring(0, "yyyyMMddHHmmss".length()));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = null;
                }
            }
            if (date == null) {
                try {
                    date = dateFormatterForOFXDate().parse(dateString);
                } catch (ParseException e) {
                    Log.i("com.catamount.pocketmon", "failed to parse dateString (OFXClass[dateFromString]) : " + dateString);
                    e.printStackTrace();
                    //date = new Date();
                    return null;
                }
            }
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeInMillis(date.getTime());
            return gregorianCalendar;
        }
    }

    private static SimpleDateFormat dateFormatterForOFXDate() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US);
    }

    private static SimpleDateFormat dateFormatterForOFXDateTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    }

    static String stringBetween(String text, String begin, String end, String lineEnding) {
        // $FF: Couldn't be decompiled
        try {
            int startIndex = text.indexOf(begin);
            if (startIndex == -1) {
                return "";
            }
            startIndex += begin.length();
            int endIndex = text.indexOf(TAGofEOL(end, lineEnding), startIndex);
            if (startIndex != -1 && endIndex == -1) {
                endIndex = text.length();
            }
            //noinspection UnnecessaryLocalVariable
            final String trim = text.substring(startIndex, endIndex).trim();
            return trim;
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private String bankMessage() {
        if (this.account != null && this.account.getType() == 0) {
            OFX_Statement ofx_statement = new OFX_Statement(this.transactions, this.tags);
            return "\t<BANKMSGSRSV1>\n" + ofx_statement.toString() + "\t</BANKMSGSRSV1>\n";
        } else {
            OFX_CreditCardStatement ofx_creditCardStatement = new OFX_CreditCardStatement(this.transactions, this.tags);
            return "\t<CREDITCARDMSGSRSV1>\n" + ofx_creditCardStatement.toString() + "\t</CREDITCARDMSGSRSV1>\n";
        }
    }

    private String header() {
        return "OFXHEADER:100\n" +
                "DATA:OFXSGML\n" +
                "VERSION:102\n" +
                "SECURITY:TYPE1\n" +
                "ENCODING:USASCII\n" +
                "CHARSET:1252\n" +
                "COMPRESSION:NONE\n" +
                "OLDFILEUID:NONE\n" +
                "NEWFILEUID:NONE\n\n";
    }

    private void parse(String text) {
        if (!text.contains("</CODE>")) {
            this.format = OFX_DataFormatType.OFX_DataFormatSGML;
        } else {
            this.format = OFX_DataFormatType.OFX_DataFormatXML10;
        }

        String lineEnding;
        if (!text.contains("\r")) {
            lineEnding = "\n";
        } else {
            lineEnding = "\r";
        }

        this.tags = new OFX_Tags(this.format, lineEnding);
        String replaceBlock = lineEnding + "<";
        text = text.replace(replaceBlock, "<").replace("<", replaceBlock);
        String statement = stringBetween(text, this.tags.bankStatementTransmissionBegin, this.tags.bankStatementTransmissionEnd, lineEnding);
        if (statement.length() > 0) {
            this.statement = new OFX_Statement(statement, this.tags);
        }

        if (statement.length() == 0 || this.statement == null || this.statement.ofxtransactions.size() == 0) {
            this.statement = new OFX_CreditCardStatement(stringBetween(text, this.tags.creditCardStatementTransmissionBegin, this.tags.creditCardStatementTransmissionEnd, lineEnding), this.tags);
        }

    }

    private String signOnMessage() {
        return "\t<SIGNONMSGSRSV1>\n" +
                "\t\t<SONRS>\n"
                + this.statusMessage("OK", "0", "INFO")
                + "\t\t\t<DTSERVER>" + dateAsString(new GregorianCalendar()) + "\n"
                + "\t\t\t<LANGUAGE>ENG\n"
                + "\t\t\t<FI>\n"
                + "\t\t\t\t<ORG>xxxx-optional\n"
                + "\t\t\t\t<FID>xxxx-optional\n"
                + "\t\t\t</FI>\n"
                + "\t\t\t<INTU.BID>zzzz-required(has to match the value Quicken already has for the bank associated with the account of the export file)\n"
                + "\t\t\t<INTU.USERID>xxxx-optional\n"
                + "\t\t</SONRS>\n"
                + "\t</SIGNONMSGSRSV1>\n";
    }

    private String statusMessage(@SuppressWarnings("SameParameterValue") String msg,
                                 @SuppressWarnings("SameParameterValue") String code,
                                 @SuppressWarnings("SameParameterValue") String severity) {
        return "\t\t\t" + this.tags.statusBegin + "\n"
                + "\t\t\t\t" + this.tags.statusCodeBegin + code + this.tags.statusCodeEnd + "\n"
                + "\t\t\t\t" + this.tags.statusSeverityBegin + severity + this.tags.statusSeverityEnd + "\n"
                + "\t\t\t\t" + this.tags.statusMessageBegin + msg + this.tags.statusMessageEnd + "\n"
                + "\t\t\t" + this.tags.statusEnd + "\n";
    }

    public String toString() {
        return this.header() +
                "<OFX>\n" +
                this.signOnMessage() +
                this.bankMessage() +
                "</OFX>";
    }
}
