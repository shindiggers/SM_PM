package com.example.smmoney.importexport.ofx;

public enum OFX_DataFormatType {
    OFX_DataFormatSGML,
    OFX_DataFormatXML10,
    OFX_DataFormatXML20;

    static {
        OFX_DataFormatType[] var0 = new OFX_DataFormatType[]{OFX_DataFormatSGML, OFX_DataFormatXML10, OFX_DataFormatXML20};
    }
}
