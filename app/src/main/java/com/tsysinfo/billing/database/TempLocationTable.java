package com.tsysinfo.billing.database;

public class TempLocationTable {

    public static final String DATABASE_TABLE = "temploc";


    public static final String KEY_DATA = "date";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "(" + KEY_DATA + " TEXT);";
}
