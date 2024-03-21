package com.tsysinfo.billing.database;

public class ReceiptTable {

	public static final String DATABASE_TABLE = "receipts";

	public static final String KEY_BILLDATE = "date";
	public static final String KEY_CUSTID = "custid";
	public static final String KEY_CUSTNAME = "custname";
	public static final String KEY_BILLID = "billid";
	public static final String KEY_TOTALAMT = "totalamt";
	public static final String KEY_OUTSTANDINGAMT = "outstandingamt";
	public static final String KEY_OutSFkey = "OutSFkey";
	public static final String KEY_OutCus = "OutCust";
	public static final String KEY_OutBkey = "OutBkey";
	public static final String KEY_outrcvSofar = "outrcvSofar";
	public static final String KEY_outpending = "outPending";
	public static final String KEY_prfx = "prfx";

	public static final String KEY_partpaymen = "partpaymen";
	public static final String KEY_Not_Aproval = "Not_Aproval";
	public static final String KEY_days = "days";
	public static final String KEY_SoName = "SoShort";


	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_BILLDATE + " TEXT," + KEY_CUSTID + " TEXT," + KEY_CUSTNAME
			+ " TEXT," + KEY_BILLID + " TEXT," + KEY_TOTALAMT + " TEXT,"
			+ KEY_OUTSTANDINGAMT + " TEXT,"
			+ KEY_OutSFkey + " TEXT,"
			+ KEY_OutCus + " TEXT,"
			+ KEY_OutBkey + " TEXT,"+ KEY_outrcvSofar + " TEXT," + KEY_outpending + " TEXT," + KEY_prfx + " TEXT,"+ KEY_partpaymen + " TEXT," + KEY_Not_Aproval + " TEXT," + KEY_SoName + " TEXT," + KEY_days + " TEXT);";

}
