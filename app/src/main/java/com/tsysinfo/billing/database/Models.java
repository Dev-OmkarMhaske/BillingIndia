package com.tsysinfo.billing.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

public class Models {

	protected static final String TAG = "DataBaseAdapter GAME";

	public ArrayList<String> getSpinnerData(String tablename, String columnname) {
		Log.w("In Models ", "getSpinnerData Start");
		ArrayList<String> SpinnerList;
		SpinnerList = new ArrayList<String>();
		String selectQuery = "select distinct " + columnname + " from "
				+ tablename;
		Log.w("query", "" + selectQuery);
		Cursor cursor = DataBaseAdapter.ourDatabase.rawQuery(selectQuery, null);
		Log.w("GetAllData", "" + cursor);
		if (cursor.moveToFirst()) {
			do {
				SpinnerList.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		// DataBaseAdapter.ourDatabase.close();
		cursor.close();
		Log.w("GetAllData", "" + SpinnerList);
		return SpinnerList;
	}

	public ArrayList<String> getSpinnerDuration(String plan) {
		Log.w("In Models ", "getSpinnerDuration Start");
		ArrayList<String> SpinnerDurationList;
		SpinnerDurationList = new ArrayList<String>();
		String selectQuery = "select duration from plandetails where planname='"
				+ plan + "'";
		Log.w("query", "" + selectQuery);
		Cursor cursor = DataBaseAdapter.ourDatabase.rawQuery(selectQuery, null);
		Log.w("GetAllData", "" + cursor);
		if (cursor.moveToFirst()) {
			do {
				SpinnerDurationList.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		// DataBaseAdapter.ourDatabase.close();
		cursor.close();
		Log.w("GetAllData", "" + SpinnerDurationList);
		return SpinnerDurationList;
	}

	public long insertdata(String tablename, ContentValues contentV) {
		long result = DataBaseAdapter.ourDatabase.insert(tablename, null,
				contentV);
		return result;
	}

	public Cursor getSessionMRP(String planname, String duration) {
		try {
			String sql = "";
			sql = "select sessions,mrp from plandetails where planname='"
					+ planname + "' and duration='" + duration + "'";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getData(String tablename) {
		try {
			String sql = "";
			sql = "select * from " + tablename;
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur.toString());
			return mCur;
		} catch (Exception mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getDataWhere(String tablename, String where) {
		try {
			String sql = "";
			sql = "select * from " + tablename + " where checked='" + where + "' ";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getSpinGrpData(String column) {
		try {
			String sql = "";
			sql = "select distinct " + column + " from products order by " + column + " desc";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getDataDistinct(String tablename) {
		try {
			String sql = "";
			sql = "select DISTINCT visitortype, companyname, safetytype from "
					+ tablename;
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getReceiptData() {
		try {
			String sql = "";
			sql = "select paymode,amount,cardno,chequedate,bankname,commission from receipt";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public void DeleteRow(String tablename, int rownum) {
		try {
			Log.w("Models", "Start Delete");
			Log.w("Models", "Table Name " + tablename);
			Log.w("Models", "SRNO " + rownum);
			String sql = "";
			sql = "DELETE FROM " + tablename + " WHERE rowid =" + rownum;
			DataBaseAdapter.ourDatabase.execSQL(sql);
			Log.w("GetAllData", "" + sql);

		} catch (SQLException e) {
			Log.w("Database", "Error in Delete" + e.getStackTrace());
		}
	}

	public Cursor getDataBranch(String tablename, String column) {
		try {
			String sql = "";
			sql = "select * from " + tablename + " where branchname='" + column
					+ "'";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getUserName(String tablename, String userno) {
		try {
			String sql = "";
			sql = "select username from " + tablename + " where userno="
					+ userno;
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getUserN(String tablename, String usern) {
		try {
			String sql = "";
			sql = "select username from " + tablename + " where userid='"
					+ usern + "'";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	/*public Cursor getMessageUserName(String branch) {
		try {
			String sql = "";
			sql = "select username from branchdetails where branchname='"
					+ branch + "'";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}

	}*/

	public Cursor getMessages(String sendto, String status) {
		try {
			String sql = "";
			sql = "select * from messages where sendto=" + sendto
					+ " and status='" + status + "'";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getSentMessages(String sentfrom) {
		try {
			String sql = "";
			sql = "select * from messages where sendby='" + sentfrom + "'";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "Quiry " + sql);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public void clearDatabase(String tablename) {
		String sql = "delete from " + tablename;
		try {
			DataBaseAdapter.ourDatabase.execSQL(sql);
			Log.e("Tracking7", tablename + " Clear Successful");
		} catch (Exception e) {
			Log.w("Database",
					"Error in Clear Outsytanding Database" + e.getStackTrace());
		}
	}

	public Cursor getPayType() {
		try {
			String sql = "";
			sql = "select paymode from receipt";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public Cursor getComission(String column) {
		try {
			String sql = "";
			sql = "select " + column + " from setting";
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("URL", "" + sql);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}

	public ArrayList<String> getAllUsers(String memberName) {
		Log.w("In Models ", "" + memberName);
		ArrayList<String> usersList;
		usersList = new ArrayList<String>();
		String selectQuery = "select memberno,membername from memberdetails where membername like '%"
				+ memberName + "%'";
		Log.w("query", "" + selectQuery);
		Cursor cursor = DataBaseAdapter.ourDatabase.rawQuery(selectQuery, null);
		Log.w("GetAllData", "" + cursor);
		if (cursor.moveToFirst()) {
			do {
				usersList.add(cursor.getString(1) + " (" + cursor.getString(0)
						+ ")");
			} while (cursor.moveToNext());
		}
		// DataBaseAdapter.ourDatabase.close();
		cursor.close();
		Log.w("GetAllData", "" + usersList);
		return usersList;
	}

	public Cursor getMemberNo(String memberno) {
		try {
			String sql = "";
			sql = "select memberno from memberdetails where memberno="
					+ memberno;
			Cursor mCur = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
			Log.w("GetAllData", "Quiry " + sql);
			Log.w("GetAllData", "" + mCur);
			return mCur;
		} catch (SQLException mSQLException) {
			Log.e(TAG, "getTestData >>" + mSQLException.toString());
			throw mSQLException;
		}
	}
}
