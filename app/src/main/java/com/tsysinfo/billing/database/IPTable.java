package com.tsysinfo.billing.database;

public class IPTable {
	
	public static final String DATABASE_TABLE = "iptable";

	public static final String KEY_IPADDRESS = "ipaddress";
    public static final String KEY_WCF_PORT = "wcfport";
    public static final String KEY_PORT = "portno";

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_IPADDRESS + " TEXT," + KEY_WCF_PORT + " TEXT," + KEY_PORT + " TEXT);";
}
