package com.tsysinfo.billing.database;

/**
 * Created by administrator on 23/5/16.
 */
public class OfflineTable {

    public static final String DATABASE_TABLE = "offline";

    public static final String KEY_DATA = "data";
    public static final String KEY_BRANCHNO = "branchnumber";
    public static final String KEY_LONGITUDE= "longi";
    public static final String KEY_LATITUDE = "lati";
    public static final String KEY_METHOD = "method";
    public static final String KEY_TransType = "TransType";
    public static final String KEY_Mode = "Mode";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_DATA + " TEXT," + KEY_BRANCHNO + " TEXT," + KEY_LONGITUDE
            + " TEXT," + KEY_LATITUDE + " TEXT,"+KEY_TransType+ " TEXT,"+ KEY_Mode + " TEXT," + KEY_METHOD + " TEXT);";

}
