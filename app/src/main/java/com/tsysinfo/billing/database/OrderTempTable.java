package com.tsysinfo.billing.database;

public class OrderTempTable {

	public static final String DATABASE_TABLE = "temp";
	public static final String KEY_PRODUCTID = "pid";//2
	public static final String KEY_PRODUCTNAME = "pname";
	public static final String KEY_PRODUCTDESC = "pdesc";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_QUANTITY = "qty";//3
	public static final String KEY_PRICE = "price";
	public static final String KEY_DISCOUNT = "discount";//6,7
	public static final String KEY_ROT = "rot";//8
	public static final String KEY_PRATE = "prate";//9
	public static final String KEY_DP = "dp";//5
	public static final String KEY_UNIT= "unit";//10
	public static final String KEY_CSZ= "csz";//11
	public static final String KEY_REMARK= "remark";//12

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_PRODUCTID + " TEXT," + KEY_PRODUCTNAME + " TEXT,"
			+ KEY_PRODUCTDESC + " TEXT,"
			+ KEY_IMAGE + " TEXT,"+ KEY_QUANTITY + " TEXT,"+ KEY_PRICE + " TEXT,"+ KEY_DISCOUNT + " TEXT,"
			+ KEY_ROT + " TEXT,"
			+ KEY_PRATE + " TEXT,"+ KEY_DP + " TEXT,"+ KEY_UNIT + " TEXT,"+ KEY_CSZ + " TEXT,"+ KEY_REMARK + " TEXT);";

}
