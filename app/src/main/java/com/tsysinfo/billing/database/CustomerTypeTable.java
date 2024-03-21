package com.tsysinfo.billing.database;

/**
 * Created by administrator on 22/4/16.
 */
public class CustomerTypeTable {

    public static final String DATABASE_TABLE = "custtype";

    public static final String KEY_ID = "id";
    public static final String KEY_CUSTTYPE = "type";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_ID + " TEXT," + KEY_CUSTTYPE + " TEXT);";

}
