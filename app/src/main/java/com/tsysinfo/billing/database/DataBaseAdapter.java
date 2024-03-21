package com.tsysinfo.billing.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DataBaseAdapter {
	private static final String DATABASE_NAME = "billing";
	private static final int DATABASE_VERSION = 4;
	static Context con;
	private DBHelper ourHelper;
	private final Context ourContext;
	public static SQLiteDatabase ourDatabase;


	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);

			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			
			db.execSQL(IPTable.CREATE_TABLE);
			db.execSQL(OrderTable.CREATE_TABLE);
			db.execSQL(OrderTempTable.CREATE_TABLE);
			db.execSQL(ProductTable.CREATE_TABLE);
			db.execSQL(CustomerTable.CREATE_TABLE);
			db.execSQL(AllCustomer.CREATE_TABLE);
			db.execSQL(DeliveryTable.CREATE_TABLE);
			db.execSQL(ReceiptTable.CREATE_TABLE);
			db.execSQL(BillingTable.CREATE_TABLE);
			db.execSQL(ReturnTable.CREATE_TABLE);
			db.execSQL(BranchTable.CREATE_TABLE);
			db.execSQL(SalesTypeTable.CREATE_TABLE);
			db.execSQL(CustomerTypeTable.CREATE_TABLE);
			db.execSQL(TownTable.CREATE_TABLE);
			db.execSQL(LocationTable.CREATE_TABLE);
			db.execSQL(AuthorityTable.CREATE_TABLE);
			db.execSQL(OfflineTable.CREATE_TABLE);
			db.execSQL(OfflineFeedbackTable.CREATE_TABLE);
			db.execSQL(DistrictTable.CREATE_TABLE);
			db.execSQL(TempReciept.CREATE_TABLE);
			db.execSQL(Demonination.CREATE_TABLE);
			db.execSQL(TempLocationTable.CREATE_TABLE);
			db.execSQL(ExpenseType.CREATE_TABLE);
			db.execSQL(ExpenseSubGroup.CREATE_TABLE);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

			String upgradeQuery = "ALTER TABLE "+CustomerTable.DATABASE_TABLE+" ADD COLUMN "+CustomerTable.KEY_CUSTOMER_CODE+" TEXT";
			if (oldVersion == 3 && newVersion == 4) {
				db.execSQL(upgradeQuery);
			}

		/*
			db.execSQL("DROP TABLE IF EXISTS " + IPTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + OrderTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + OrderTempTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + ProductTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + CustomerTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DeliveryTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + ReceiptTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + BillingTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + ReturnTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + BranchTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SalesTypeTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + CustomerTypeTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TownTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + LocationTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + AuthorityTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + OfflineTable.DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + OfflineFeedbackTable.DATABASE_TABLE);
			onCreate(db);*/
		}

	}

	public DataBaseAdapter(Context c) {
		ourContext = c;
		con=c;
	}

	public DataBaseAdapter open() throws SQLException{
		ourHelper = new DBHelper(ourContext);
		ourDatabase = ourHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		ourHelper.close();
	}

}
