package com.tsysinfo.billing.database;

public class ExpenseType {
    public static final String DATABASE_TABLE = "expensetype";

    public static final String KEY_ID = "id";
    public static final String KEY_EXPENSE = "expensename";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "(" + KEY_ID + " TEXT," + KEY_EXPENSE + " TEXT);";
}
