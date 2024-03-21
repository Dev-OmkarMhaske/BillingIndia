package com.tsysinfo.billing.database;

public class BillingTable {

	public static final String DATABASE_TABLE = "bill";
	public static final String KEY_DATE = "date";
	public static final String KEY_CUSTID = "custid";
	public static final String KEY_ORDERID = "orderid";
	public static final String KEY_PRODUCTID = "pid";
	public static final String KEY_QUANTITY = "qty";
	public static final String KEY_EMPNO = "empno";
	public static final String KEY_RATE = "rate";
	public static final String KEY_DISCOUNT = "discount";
	public static final String KEY_DISCPRICE = "disprice";
	public static final String KEY_PAIDAMT = "paidamt";

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_DATE + " TEXT," + KEY_CUSTID + " TEXT," + KEY_ORDERID
			+ " TEXT," + KEY_PRODUCTID + " TEXT," + KEY_QUANTITY + " TEXT,"
			+ KEY_EMPNO + " TEXT," + KEY_RATE + " TEXT," + KEY_DISCOUNT
			+ " TEXT," + KEY_DISCPRICE + " TEXT," + KEY_PAIDAMT + " TEXT);";

}
