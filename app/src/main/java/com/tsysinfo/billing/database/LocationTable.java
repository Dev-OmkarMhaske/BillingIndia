package com.tsysinfo.billing.database;

/**
 * Created by administrator on 30/4/16.
 */
public class LocationTable {

    public static final String DATABASE_TABLE = "location";

    public static final String KEY_LONGITUDE = "longi";
    public static final String KEY_LATITUDE = "lati";
    public static final String KEY_DATE = "date";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_LONGITUDE + " TEXT," + KEY_LATITUDE + " TEXT," + KEY_DATE + " TEXT);";

}
