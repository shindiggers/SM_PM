package com.example.smmoney.importexport.ofx;

import android.util.Log;

import androidx.annotation.NonNull;

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
        return s != null && !s.isEmpty() ? s : lineEnding;
    }

    static String amountAsOFXAmount(double amt) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setGroupingUsed(false);
        return format.format(amt).replace(" ", "").replace(",", ".");
    }

    static double amountFromOFXAmount(String text) {
        if (text != null && !text.trim().isEmpty()) {
            try {
                return Double.parseDouble(text.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                Log.e(SMMoney.TAG, "OFXClass: NumberFormatException in amountFromOFXAmount: " + text, e);
            }
        }
        return 0.0D;
    }

    static String dateAsString(GregorianCalendar date) {
        if (date == null) {
            return dateFormatterForOFXDateTime().format(new Date());
        }
        return dateFormatterForOFXDateTime().format(date.getTime());
    }

    static GregorianCalendar dateFromString(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        String cleanDateString = dateString.trim();
        SimpleDateFormat dateTimeFormat = dateFormatterForOFXDateTime();
        SimpleDateFormat dateFormat = dateFormatterForOFXDate();
        Date date = null;

        if (cleanDateString.length() >= 14) {
            try {
                date = dateTimeFormat.parse(cleanDateString.substring(0, 14));
            } catch (ParseException ignored) {
            }
        }

        if (date == null && cleanDateString.length() >= 8) {
            try {
                date = dateFormat.parse(cleanDateString.substring(0, 8));
            } catch (ParseException ignored) {
            }
        }

        if (date == null) {
            Log.e(SMMoney.TAG, "OFXClass: failed to parse dateString : " + cleanDateString);
            return null;
        }

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(date.getTime());
        return gregorianCalendar;
    }

    private static SimpleDateFormat dateFormatterForOFXDate() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US);
    }

    private static SimpleDateFormat dateFormatterForOFXDateTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    }

    static String stringBetween(String text, String begin, String end, String lineEnding) {
        if (text == null || begin == null || begin.isEmpty()) {
            return "";
        }
        try {
            int startIndex = text.indexOf(begin);
            if (startIndex == -1) {
                return "";
            }
            startIndex += begin.length();
            int endIndex = text.indexOf(TAGofEOL(end, lineEnding), startIndex);
            if (endIndex == -1) {
                endIndex = text.length();
            }
            return text.substring(startIndex, endIndex).trim();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private String bankMessage() {
        if (this.account != null && this.account.getType() == 0) {
            OFX_Statement ofxStatement = new OFX_Statement(this.transactions, this.tags);
            return "\t<BANKMSGSRSV1>\n" + ofxStatement + "\t</BANKMSGSRSV1>\n";
        } else {
            OFX_CreditCardStatement ofxCreditCardStatement = new OFX_CreditCardStatement(this.transactions, this.tags);
            return "\t<CREDITCARDMSGSRSV1>\n" + ofxCreditCardStatement + "\t</CREDITCARDMSGSRSV1>\n";
        }
    }

    // Retain explicit string concatenation with \n rather than text blocks to guarantee exact line endings for OFX protocol headers
    @SuppressWarnings("TextBlockMigration")
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
        if (text == null || text.isEmpty()) {
            return;
        }

        if (text.contains("</CODE>")) {
            this.format = OFX_DataFormatType.OFX_DataFormatXML10;
        } else {
            this.format = OFX_DataFormatType.OFX_DataFormatSGML;
        }

        String lineEnding = text.contains("\r") ? "\r" : "\n";
        this.tags = new OFX_Tags(this.format, lineEnding);

        String replaceBlock = lineEnding + "<";
        String normalizedText = text.replace(replaceBlock, "<").replace("<", replaceBlock);

        String statementBlock = stringBetween(normalizedText, this.tags.bankStatementTransmissionBegin, this.tags.bankStatementTransmissionEnd, lineEnding);
        if (!statementBlock.isEmpty()) {
            this.statement = new OFX_Statement(statementBlock, this.tags);
        }

        if (this.statement == null || this.statement.ofxtransactions == null || this.statement.ofxtransactions.isEmpty()) {
            String ccStatementBlock = stringBetween(normalizedText, this.tags.creditCardStatementTransmissionBegin, this.tags.creditCardStatementTransmissionEnd, lineEnding);
            if (!ccStatementBlock.isEmpty()) {
                this.statement = new OFX_CreditCardStatement(ccStatementBlock, this.tags);
            }
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

    @Override
    @NonNull
    public String toString() {
        return this.header() +
                "<OFX>\n" +
                this.signOnMessage() +
                this.bankMessage() +
                "</OFX>";
    }
}
