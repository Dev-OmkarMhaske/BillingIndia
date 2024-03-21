package com.tsysinfo.billing.database;
/**
 * Created by tsysinfo on 6/19/2017.
 */
public class TempReciept {

    public static final String DATABASE_TABLE = "tempreciept";

    public static final String KEY_custNo = "custNo";
    public static final String KEY_Rdate = "Rdate";
    public static final String KEY_dbkey = "dbkey";
    public static final String KEY_dbprfx= "dbprfx";
    public static final String KEY_dbno = "dbno";
    public static final String KEY_dbdate = "dbdate";
    public static final String KEY_dbamount = "dbamount";
    public static final String KEY_dbsofar = "dbsofar";
    public static final String KEY_dbpay = "dbpay";
    public static final String KEY_dbpending = "dbpending";


    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_custNo + " TEXT," + KEY_Rdate + " TEXT," + KEY_dbkey
            + " TEXT," + KEY_dbprfx + " TEXT," + KEY_dbno + " TEXT,"
            + KEY_dbdate + " TEXT,"
            + KEY_dbamount + " TEXT,"
            + KEY_dbsofar + " TEXT,"
            + KEY_dbpay + " TEXT,"
            + KEY_dbpending + " TEXT);";

}
