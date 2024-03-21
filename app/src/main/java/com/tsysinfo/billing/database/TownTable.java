package com.tsysinfo.billing.database;

/**
 * Created by administrator on 22/4/16.
 */
public class TownTable {

    public static final String DATABASE_TABLE = "town";

    public static final String KEY_ID = "id";
    public static final String KEY_TOWN = "townname";
    public static final String KEY_DistId = "distId";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_ID + " TEXT," + KEY_TOWN + " TEXT,"+ KEY_DistId + " Text);";

}
