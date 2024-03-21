package com.tsysinfo.billing.database;

public class ProductTable {

	public static final String DATABASE_TABLE = "products";

	public static final String KEY_PRODUCTID = "pid";
	public static final String KEY_NAME = "name";
	public static final String KEY_GROUP = "groups";
	public static final String KEY_SUBGROUP = "subgroups";
	public static final String KEY_BRAND = "brand";
	public static final String KEY_COMMODITY = "commodity";
	public static final String KEY_BARCODE = "barcode";
	public static final String KEY_MRP = "mrp";
	public static final String KEY_DP = "dp";
	public static final String KEY_MASTERCARTON = "mastercorton";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_ROT = "rot";
	public static final String KEY_PRATE = "prate";
	public static final String KEY_BRAND_SERIAL = "brd_serial";
	public static final String KEY_CARTON_SIZE = "unit";
	public static final String KEY_UQC = "uqc";
	public static final String KEY_Stock = "Stock";








	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_PRODUCTID + " TEXT," + KEY_NAME + " TEXT," + KEY_GROUP
			+ " TEXT," + KEY_SUBGROUP + " TEXT," + KEY_BRAND + " TEXT,"
			+ KEY_COMMODITY + " TEXT," + KEY_BARCODE + " TEXT," + KEY_MRP
			+ " TEXT," + KEY_DP + " TEXT," + KEY_MASTERCARTON + " TEXT,"
			+ KEY_IMAGE + " TEXT,"
			+ KEY_ROT + " TEXT,"
			+ KEY_BRAND_SERIAL + " TEXT,"+ KEY_PRATE + " TEXT,"+ KEY_CARTON_SIZE + " TEXT," +KEY_UQC + " TEXT,"+ KEY_Stock+ " TEXT);";

}
