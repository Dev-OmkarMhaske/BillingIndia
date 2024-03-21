package com.tsysinfo.billing.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "eBillingPref";

	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";

	// User name (make variable public to access from outside)
	public static final String KEY_USERID = "userid";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_EMPNO = "empno";
	public static final String KEY_EMPNAME = "empname";
	public static final String KEY_BRANCHNO = "branchno";
	public static final String KEY_DISTANCE = "dist";
	public static final String KEY_MIN_DISTANCE = "mindist";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_ComputerName = "compname";


	// Constructor
	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	/**
	 * Create login session
	 * */
	public void createLoginSession(String userid, String password,
			String empno, String empname, String branchno,String dist,String mindist,String address,String CompName) {
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN, true);

		// Storing name in pref
		editor.putString(KEY_USERID, userid);
		editor.putString(KEY_PASSWORD, password);
		editor.putString(KEY_EMPNO, empno);
		editor.putString(KEY_EMPNAME, empname);
		editor.putString(KEY_BRANCHNO, branchno);
		editor.putString(KEY_DISTANCE, dist);
		editor.putString(KEY_MIN_DISTANCE, mindist);
		editor.putString(KEY_ADDRESS, address);
		editor.putString(KEY_ComputerName, CompName);

		// commit changes
		editor.commit();
	}

	/**
	 * Check login method wil check user login status If false it will redirect
	 * user to login page Else won't do anything
	 * 
	 * @return
	 * */
	public boolean checkLogin() {
		// Check login status
		if (!this.isLoggedIn()) {
			// user is not logged in redirect him to Login Activity

			/*
			 * Intent i = new Intent(_context, LoginActivity.class); // Closing
			 * all the Activities
			 * 
			 * 
			 * i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 * 
			 * // Add new Flag to start new Activity
			 * i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * 
			 * // Staring Login Activity _context.startActivity(i);
			 */

			return false;

		}
		return true;

	}

	private void startActivity(Intent startMain) {
		// TODO Auto-generated method stub

	}

	/**
	 * Get stored session data
	 * */
	public String getUserId() {

		// user name
		String user = pref.getString(KEY_USERID, null);
		// String pass = pref.getString(KEY_PASSWORD, null);

		// return user
		return user;
	}

	public String getUserPassword() {

		// user name
		String pass = pref.getString(KEY_PASSWORD, null);
		// String pass = pref.getString(KEY_PASSWORD, null);

		// return user
		return pass;
	}

	public String getComputerName() {

		// user name
		String comp = pref.getString(KEY_ComputerName, "");
		// String pass = pref.getString(KEY_PASSWORD, null);

		// return user
		return comp;
	}

	public String getEmpNo() {

		// user name
		String memberno = pref.getString(KEY_EMPNO, null);

		// return user
		return memberno;
	}

	public String getEmpName() {

		// user usertype
		String usertype = pref.getString(KEY_EMPNAME, null);

		// return usertype
		return usertype;
	}
	
	public String getBranchNo() {

		// user usertype
		String branchno = pref.getString(KEY_BRANCHNO, null);

		// return usertype
		return branchno;
	}



    public String getDistance() {

        // user usertype
        String dist = pref.getString(KEY_DISTANCE, null);

        // return usertype
        return dist;
    }

    public String getMinDistance() {

        // user usertype
        String mindist = pref.getString(KEY_MIN_DISTANCE, null);

        // return usertype
        return mindist;
    }

	public String getAddresse() {

		// user usertype
		String address = pref.getString(KEY_ADDRESS, null);

		// return usertype
		return address;
	}


	/**
	 * Clear session details
	 * */
	public void logoutUser() {
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();

	}

	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn() {
		return pref.getBoolean(IS_LOGIN, false);
	}
}
