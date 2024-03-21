package com.tsysinfo.billing.database;

public class BranchTable {
	
	public static final String DATABASE_TABLE = "branch";

	public static final String KEY_ID = "id";
	public static final String KEY_BRANCH = "branchname";

	static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
			+ KEY_ID + " TEXT," + KEY_BRANCH + " TEXT);";
}
