package com.tsysinfo.billing.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionLocation {
	// Shared Preferences
	SharedPreferences pref;

	// Editor for Shared preferences
	Editor editor;

	// Context
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

    private static final String IS_LOGIN_LOCATION = "IsLoggedIn";

	// Sharedpref file name
	private static final String PREF_NAME = "eBillingLocationPref";

	// All Shared Preferences Keys

	// User name (make variable public to access from outside)
	public static final String KEY_LONGITUDE = "longi";
	public static final String KEY_LATITUDE = "lati";
	public static final String KEY_DATETIME = "datetime";

	// Constructor
	public SessionLocation(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	/**
	 * Create login session
	 * */
	public void createLocationSession(String longi, String lati, String datetime) {
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN_LOCATION, true);

		// Storing name in pref
		editor.putString(KEY_LATITUDE, lati);
		editor.putString(KEY_LONGITUDE, longi);
		editor.putString(KEY_DATETIME, datetime);

		// commit changes
		editor.commit();
	}




	/**
	 * Get stored session data
	 * */
	public String getLongi() {

		// longi
		String longi = pref.getString(KEY_LONGITUDE, null);

		return longi;
	}

	public String getLati() {

		// lati
		String lati = pref.getString(KEY_LATITUDE, null);

		return lati;
	}

	public String getDateTime() {

		// lati
		String datetime = pref.getString(KEY_DATETIME, null);

		return datetime;
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
		return pref.getBoolean(IS_LOGIN_LOCATION, false);
	}
}
