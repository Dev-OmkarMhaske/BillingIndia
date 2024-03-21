package com.tsysinfo.billing.database;

public class OrderTable {

	public static final String DATABASE_TABLE = "orderr";

	public static final String KEY_DATE = "date";
	public static final String KEY_CUSTID = "custid";
	public static final String KEY_PRODUCTID = "pid";
	public static final String KEY_QUANTITY = "qty";
	public static final String KEY_EMPNO = "empno";
	public static final String KEY_RATE= "rate";
	public static final String KEY_DISCOUNT = "discount";
	public static final String KEY_DISCPRICE = "disprice";
	public static final String KEY_ROT = "rot";
	public static final String KEY_PRATE = "prate";
	public static final String KEY_CHECKED = "checked";
	public static final String KEY_UNIT = "unit";
	public static final String KEY_CSZ = "csz";
	public static final String KEY_REMARK = "remark";

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_DATE + " TEXT," + KEY_CUSTID + " TEXT," + KEY_PRODUCTID
			+ " TEXT," + KEY_QUANTITY + " TEXT," + KEY_EMPNO + " TEXT,"
			+ KEY_RATE + " TEXT," + KEY_DISCOUNT + " TEXT," + KEY_DISCPRICE
			+ " TEXT,"
			+ KEY_ROT + " TEXT,"
			+ KEY_PRATE + " TEXT,"+ KEY_CHECKED + " TEXT,"+ KEY_UNIT + " TEXT,"+ KEY_CSZ + " TEXT,"+ KEY_REMARK + " TEXT);";

}
