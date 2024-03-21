package com.tsysinfo.billing.database;

public class DeliveryTable {

	public static final String DATABASE_TABLE = "delivery";

	public static final String KEY_ID = "id";
	public static final String KEY_ORDERDATE = "date";
	public static final String KEY_CUSTID = "custid";
	public static final String KEY_CUSTNAME = "custname";
	public static final String KEY_PRODUCTID = "pid";
	public static final String KEY_PRODUCTNAME = "productname";
	public static final String KEY_QUANTITY = "qty";
	public static final String KEY_DISCOUNT = "discount";
	public static final String KEY_PRICE = "price";
	public static final String KEY_STATUS = "status";
	public static final String KEY_BIINO = "billno";
	public static final String KEY_BILLDATE = "billdate";

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_ID + " TEXT," + KEY_ORDERDATE + " TEXT," + KEY_CUSTID + " TEXT,"
			+ KEY_CUSTNAME + " TEXT," + KEY_PRODUCTID + " TEXT," + KEY_PRODUCTNAME
			+ " TEXT," + KEY_QUANTITY + " TEXT," + KEY_DISCOUNT + " TEXT," + KEY_PRICE
			+ " TEXT," + KEY_STATUS + " TEXT," + KEY_BIINO + " TEXT," + KEY_BILLDATE + " TEXT);";

}
