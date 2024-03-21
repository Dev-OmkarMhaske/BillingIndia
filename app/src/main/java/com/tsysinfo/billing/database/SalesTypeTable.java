package com.tsysinfo.billing.database;

public class SalesTypeTable {

	public static final String DATABASE_TABLE = "salestype";

	public static final String KEY_ID = "id";
	public static final String KEY_SALESTYPE = "type";

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_ID + " TEXT," + KEY_SALESTYPE + " TEXT);";

}
