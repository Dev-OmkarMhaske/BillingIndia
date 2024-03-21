package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;


public class ForgotPasswordActivity extends AppCompatActivity {

	EditText editUserId;
	String strUserId;
	Button btnProceed;

	DataBaseAdapter dba;
	Models mod;
	Bundle bundle;
	ProgressDialog waitDialog;
	JSONArray serverResponse;
	static boolean errored = false;

	@SuppressLint({ "ShowToast", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot_password_activity);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// References
		dba = new DataBaseAdapter(this);
		mod = new Models();

		editUserId = (EditText) findViewById(R.id.textUserId);
		btnProceed = (Button) findViewById(R.id.buttonProceed);

		waitDialog = new ProgressDialog(this);
		waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		waitDialog.setMessage("Please Wait");

	

		btnProceed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				strUserId = editUserId.getText().toString().trim();
				if (strUserId.length() > 0) {

					AsyncCallWS task = new AsyncCallWS();
					// Call execute
					task.execute();

				} else {
					Toast.makeText(getApplicationContext(),
							"Enter Mobile Number", Toast.LENGTH_LONG).show();
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

				Log.w("Home Activity", "Start");

				dba.open();
				serverResponse = WebService.ForgotPasswordRequest(strUserId,"ForgotPassword");
				dba.close();

			} catch (Exception e) {
				Log.w("ForgotPasswordActivity", "Timeout ");
			}
			return null;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		// Once WebService returns response
		protected void onPostExecute(Void result) {

			Log.w("ForgotPasswordActivity", "TimeOutFlag : "
					+ WebService.timeoutFlag);
			Log.w("ForgotPasswordActivity", "ResponseString : "
					+ WebService.responseString);

			// Make Progress Bar invisible
			waitDialog.cancel();
			Log.w("ForgotPasswordActivity", "Dialog Closed");
			if (WebService.timeoutFlag == 1) {
				Log.w("ForgotPasswordActivity", "Timeout");
				AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
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
				Log.w("ForgotPasswordActivity", "Try");
				try {

				/*	Toast.makeText(ForgotPasswordActivity.this,
							"JSON Data:  " + WebService.responseString,
							Toast.LENGTH_LONG).show();*/

					// Error status is false
					if (!errored) {
						// Based on Boolean value returned from WebService
						if (serverResponse != null) {

							if (serverResponse.length() > 0) {

								Log.w("FPA", "res2 ");

								if (serverResponse.getJSONObject(0)
										.getString("Password").trim()
										.equalsIgnoreCase("No")) {

									AlertDialog.Builder builder2 = new AlertDialog.Builder(ForgotPasswordActivity.this);
									builder2.setTitle("Mobile Number Incorrect");
									builder2.setMessage("Please Try Again...");
									builder2.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
													Log.e("info", "OK");
													dialog.cancel();

												}
											});
									builder2.show();
								} else {

									AlertDialog.Builder builder2 = new AlertDialog.Builder(ForgotPasswordActivity.this);
									builder2.setTitle("Dear "+serverResponse.getJSONObject(0)
                                            .getString("UserId").trim());
									builder2.setMessage("Password is send on Your mobile and Email");
                                    builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) { dialog.cancel();

                                                        Intent intent = new Intent(ForgotPasswordActivity.this,
                                                            LoginActivity.class);
                                                    startActivity(intent);
                                                    finish();

													Log.e("info", "OK");

												}
											});
									builder2.show();
								}

							}

						}

						// Error status is true
					} else {

						Toast.makeText(ForgotPasswordActivity.this,
								"Server Error", Toast.LENGTH_LONG).show();
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
