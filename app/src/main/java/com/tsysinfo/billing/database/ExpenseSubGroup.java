package com.tsysinfo.billing.database;

public class ExpenseSubGroup {
    public static final String DATABASE_TABLE = "expensesubgroup";

    public static final String KEY_ID = "id";
    public static final String KEY_EXPENSESUBTYPE = "expensesubtypename";

    static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
            + KEY_ID + " TEXT," + KEY_EXPENSESUBTYPE + " TEXT);";
}
