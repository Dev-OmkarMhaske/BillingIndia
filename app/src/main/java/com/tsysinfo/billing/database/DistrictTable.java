package com.tsysinfo.billing.database;

/**
 * Created by tsysinfo on 1/9/2017.
 */
public class DistrictTable {

    public static final String DATABASE_TABLE = "dist";

    public static final String KEY_ID = "id";
    public static final String KEY_Dist = "distname";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_ID + " TEXT," + KEY_Dist + " TEXT);";
}
