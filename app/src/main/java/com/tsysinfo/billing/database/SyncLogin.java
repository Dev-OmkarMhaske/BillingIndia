package com.tsysinfo.billing.database;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;

public class SyncLogin {

	Models mod;

	public void syncLoginData(String EmpNo,String BranchNo) {

		JSONArray jobj;

		mod = new Models();
		mod.clearDatabase(TownTable.DATABASE_TABLE);
		mod.clearDatabase(ExpenseType.DATABASE_TABLE);
		mod.clearDatabase(CustomerTypeTable.DATABASE_TABLE);
		mod.clearDatabase(ExpenseSubGroup.DATABASE_TABLE);

		ContentValues cv = new ContentValues();
		ContentValues cv1 = new ContentValues();
		ContentValues cv2 = new ContentValues();




		// get Town data
		jobj = new JSONArray();
		cv = new ContentValues();

		jobj = WebService.getData(EmpNo,BranchNo, "GetTown");
		try {
			if (jobj != null) {
				if (jobj.getJSONObject(0).getString("AreaId").trim().equalsIgnoreCase("No")) {

				} else {
					for (int i = 0; i < jobj.length(); i++) {

						if (!jobj.getJSONObject(i).getString("AreaId").trim().equalsIgnoreCase("null")) {

							cv.put(TownTable.KEY_ID, jobj.getJSONObject(i).getString("AreaId").trim());

						} else {
							cv.put(TownTable.KEY_ID, "");
						}

						if (!jobj.getJSONObject(i).getString("Area").trim().equalsIgnoreCase("null")) {

							cv.put(TownTable.KEY_TOWN, jobj.getJSONObject(i).getString("Area").trim());

						} else {
							cv.put(TownTable.KEY_TOWN, "");
						}

						if (!jobj.getJSONObject(i).getString("DistId").trim().equalsIgnoreCase("null")) {

							cv.put(TownTable.KEY_DistId, jobj.getJSONObject(i).getString("DistId").trim());

						} else {
							cv.put(TownTable.KEY_DistId, "");
						}



						mod.insertdata(TownTable.DATABASE_TABLE, cv);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cv1 = new ContentValues();
		jobj = WebService.getExpense(BranchNo, "GetExpenseType");
		try {
			if (jobj != null) {
				if (jobj.getJSONObject(0).getString("KeySerial").trim().equalsIgnoreCase("No")) {

				} else {
					for (int i = 0; i <= jobj.length(); i++) {

						if (!jobj.getJSONObject(i).getString("KeySerial").trim().equalsIgnoreCase("null")) {

							cv1.put(ExpenseType.KEY_ID, jobj.getJSONObject(i).getString("KeySerial").trim());

						} else {
							cv1.put(ExpenseType.KEY_ID, "");
						}

						if (!jobj.getJSONObject(i).getString("DESCR").trim().equalsIgnoreCase("null")) {

							cv1.put(ExpenseType.KEY_EXPENSE, jobj.getJSONObject(i).getString("DESCR").trim());

						} else {
							cv1.put(ExpenseType.KEY_EXPENSE, "");
						}

						mod.insertdata(ExpenseType.DATABASE_TABLE, cv1);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



//		cv2 = new ContentValues();
//		jobj = WebService.GetExpenseSubGroup(BranchNo, ExpenseType1,"GetExpenseSubGroup");
//		try {
//			if (jobj != null) {
//				if (jobj.getJSONObject(0).getString("KeySerial").trim().equalsIgnoreCase("No")) {
//
//				} else {
//					for (int i = 0; i < jobj.length(); i++) {
//
//						if (!jobj.getJSONObject(i).getString("KeySerial").trim().equalsIgnoreCase("null")) {
//
//							cv2.put(ExpenseSubGroup.KEY_ID, jobj.getJSONObject(i).getString("KeySerial").trim());
//
//						} else {
//							cv2.put(ExpenseSubGroup.KEY_ID, "");
//						}
//
//						if (!jobj.getJSONObject(i).getString("expDescr").trim().equalsIgnoreCase("null")) {
//
//							cv2.put(ExpenseSubGroup.KEY_EXPENSESUBTYPE, jobj.getJSONObject(i).getString("expDescr").trim());
//
//						} else {
//							cv2.put(ExpenseSubGroup.KEY_EXPENSESUBTYPE, "");
//						}
//
//						mod.insertdata(ExpenseSubGroup.DATABASE_TABLE, cv1);
//					}
//				}
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


	}
}
