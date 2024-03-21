package com.tsysinfo.billing.database;

public class AuthorityTable {

	public static final String DATABASE_TABLE = "authority";

	private static final String KEY_FORM = "form";
	private static final String KEY_VISIBLE= "visible";
	private static final String KEY_INSERT = "insertt";
	private static final String KEY_PRINT = "print";

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_FORM + " TEXT," + KEY_VISIBLE + " TEXT," + KEY_INSERT
			+ " TEXT," + KEY_PRINT + " TEXT);";

}
