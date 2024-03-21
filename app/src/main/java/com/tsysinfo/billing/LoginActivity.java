package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.BranchTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.SyncLogin;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

public class LoginActivity extends Activity {

	EditText editUserName, editPassword;
	TextView changeIp,forgotPassword;
	String strUserName, strPassword;
	Button btnLogin;
	SessionManager session;
	DataBaseAdapter dba;
	Models mod;
    SyncLogin sData;
	ProgressDialog dialog, waitDialog;
	Button backAction;
	JSONArray serverResponse;
	static boolean errored = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		// References
		session = new SessionManager(this);
		dba = new DataBaseAdapter(this);
		mod = new Models();
        sData = new SyncLogin();

		waitDialog = new ProgressDialog(this);
		waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		waitDialog.setMessage("Please Wait");

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		editUserName = (EditText) findViewById(R.id.textUserId);
		editPassword = (EditText) findViewById(R.id.textPassword);
		changeIp = (TextView) findViewById(R.id.changeIP);
		forgotPassword = (TextView) findViewById(R.id.forgotPassword);

		btnLogin = (Button) findViewById(R.id.buttonLogin);
		
		changeIp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), SetIPActivity.class);
				startActivity(intent);
				finish();
			}
		});

		forgotPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(
						getApplicationContext(),
						ForgotPasswordActivity.class);
				startActivity(intent);
				finish();
			}
		});

		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				strUserName = editUserName.getText().toString().trim();
				strPassword = editPassword.getText().toString().trim();

				if (strUserName.length() > 0 && strPassword.length() > 0) {
					AsyncCallWS task = new AsyncCallWS();
					// Call execute
					task.execute();
				} else {
					Toast.makeText(getApplicationContext(),
							"Enter User Name and Password", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

	}

	// WebService Code

	private class AsyncCallWS extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			// Call Web Method
			try {

				Log.w("RegistrationActivity", "Start");

				dba.open();
				serverResponse = WebService.Login(strUserName, strPassword,
						"LogIn");
				dba.close();

			} catch (Exception e) {
				Log.w("RegistrationActivity", "Timeout ");
			}
			return null;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		// Once WebService returns response
		protected void onPostExecute(Void result) {

			Log.w("LoginActivity", "TimeOutFlag : " + WebService.timeoutFlag);
			Log.w("LoginActivity", "ResponseString : "
					+ WebService.responseString);

			// Make Progress Bar invisible
			waitDialog.cancel();
			Log.w("LoginActivity", "Dialog Closed");
			if (WebService.timeoutFlag == 1) {
				Log.w("LoginActivity", "Timeout");
				AlertDialog.Builder builder = new AlertDialog.Builder(
						LoginActivity.this);
				builder.setTitle("Connection Time Out!");

				builder.setMessage("Please Try Again!!!")
						.setCancelable(false)
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();

									}
								});

				AlertDialog alert = builder.create();
				alert.show();

			} else {
				Log.w("LoginActivity", "Try");
				try {

					/*
					 * Toast.makeText(LoginActivity.this, "JSON Data:  " +
					 * WebService.responseString, Toast.LENGTH_LONG).show();
					 */
					// Error status is false
					if (!errored) {
						// Based on Boolean value returned from WebService
						if (serverResponse != null) {

							if (serverResponse.getJSONObject(0).getString("EmployeeNo").trim().equalsIgnoreCase("No")) {

								AlertDialog.Builder builder2 = new AlertDialog.Builder(
										LoginActivity.this);
								builder2.setTitle("Login Failure..");
								builder2.setMessage("User Id or Password Incorrect. Please try again.");
								builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												Log.e("info", "OK");
											}
										});
								builder2.show();

							} else {
								
								String strEmpNo = serverResponse.getJSONObject(0).getString("EmployeeNo").trim();
								String strEmpName = serverResponse.getJSONObject(0).getString("EmployeeName").trim();
                                String strAddress = serverResponse.getJSONObject(0).getString("BranchAddress").trim();

								if(serverResponse.length() > 1){
									ContentValues cv = new ContentValues();
									
									dba.open();
									mod.clearDatabase(BranchTable.DATABASE_TABLE);
									for(int i = 0;i<serverResponse.length();i++){
										cv = new ContentValues();
										cv.put(BranchTable.KEY_ID, serverResponse.getJSONObject(i).getString("BranchNo").trim());
										cv.put(BranchTable.KEY_BRANCH, serverResponse.getJSONObject(i).getString("BranchName").trim());
										mod.insertdata(BranchTable.DATABASE_TABLE, cv);
									}
									dba.close();
									Intent intent = new Intent(getApplicationContext(), BranchActivity.class);
									intent.putExtra("strUserName", strUserName);
									intent.putExtra("strPassword", strPassword);
									intent.putExtra("strEmpNo", strEmpNo);
									intent.putExtra("strEmpName", strEmpName);
                                    intent.putExtra("strAddress", strAddress);
									startActivity(intent);
									finish();
									
								}else{
									
									String strBranchNo = serverResponse.getJSONObject(0).getString("BranchNo").trim();
									/*String Comp = serverResponse
											.getJSONObject(0)
											.getString("ComputerName").trim();
*/
									session.createLoginSession(strUserName,
											strPassword, strEmpNo, strEmpName,strBranchNo,"0.0310686","0.00310686",strAddress,"a");

                                    waitDialog.show();

									Thread timer = new Thread() {
										@Override
										public void run() {
                                            dba.open();
											sData.syncLoginData(session.getEmpNo(), session.getBranchNo());
                                            dba.close();
											waitDialog.cancel();
                                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            finish();
										}
									};

									timer.start();

								}

							}

						}

						// Error status is true
					} else {

						Toast.makeText(LoginActivity.this, "Server Error",
								Toast.LENGTH_LONG).show();
						// statusTV.setText("Error occured in invoking webservice");
					}
					// Re-initialize Error Status to False

					errored = false;

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}

		@Override
		// Make Progress Bar visible
		protected void onPreExecute() {
			waitDialog.show();
			waitDialog.setCanceledOnTouchOutside(false);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

}
