package com.tsysinfo.billing.database;

public class ReturnTable {

	public static final String DATABASE_TABLE = "retutntab";

	private static final String KEY_PRODUCTID = "pid";
	private static final String KEY_CUSTID = "custid";
	private static final String KEY_EMPID = "empid";
	private static final String KEY_QUANTITY = "qty";
	private static final String KEY_MRP = "mrp";
	private static final String KEY_DISC = "disc";

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_PRODUCTID + " TEXT," + KEY_CUSTID + " TEXT," + KEY_EMPID
			+ " TEXT," + KEY_QUANTITY + " TEXT," + KEY_MRP + " TEXT," + KEY_DISC + " TEXT);";

}
