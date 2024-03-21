package com.tsysinfo.billing.database;

/**
 * Created by administrator on 23/5/16.
 */
public class OfflineFeedbackTable {

    public static final String DATABASE_TABLE = "offlinefeedback";

    public static final String KEY_EMPID = "empid";
    public static final String KEY_BRANCHNO = "branchnumber";
    public static final String KEY_DATA = "data";
    public static final String KEY_IMAGEPATH= "imagepath";
    public static final String KEY_METHOD = "method";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_EMPID + " TEXT," + KEY_BRANCHNO + " TEXT," + KEY_DATA + " TEXT," + KEY_IMAGEPATH
            + " TEXT," + KEY_METHOD + " TEXT);";

}
