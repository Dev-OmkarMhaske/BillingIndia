package com.tsysinfo.billing.database;

public class Demonination {

    public static final String DATABASE_TABLE = "denomination";

    public static final String KEY_NAME = "name";
    public static final String KEY_ID = "id";


    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_NAME + " TEXT," + KEY_ID + " TEXT);";
}
