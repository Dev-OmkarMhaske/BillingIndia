package com.tsysinfo.billing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.SyncLogin;

import java.util.ArrayList;
import java.util.List;

public class BranchActivity extends Activity {

	SessionManager session;
	DataBaseAdapter dba;
	Models mod;
	SyncLogin sData;
	ProgressDialog waitDialog;

	Spinner spinBranch;
	TextView txtUserId;
	Button btnProceed;
	
	String strUserName,strPassword, strEmpNo, strEmpName,strAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_branch);
		
		//References
		session = new SessionManager(this);
		dba = new DataBaseAdapter(this);
		mod = new Models();
        sData = new SyncLogin();

        waitDialog = new ProgressDialog(this);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");
		
		spinBranch = (Spinner) findViewById(R.id.spinnerBranch);
		txtUserId = (TextView) findViewById(R.id.textUserId);
		btnProceed = (Button) findViewById(R.id.buttonBranch);
		
		
		Intent intent= getIntent();
		strUserName = intent.getStringExtra("strUserName");
		strPassword = intent.getStringExtra("strPassword");
		strEmpNo = intent.getStringExtra("strEmpNo");
		strEmpName = intent.getStringExtra("strEmpName");
		strAddress = intent.getStringExtra("strAddress");

		txtUserId.setText(strUserName);
		
		List<String> categories = new ArrayList<String>();
		categories.add("[ Sales Branch ]");
		
		dba.open();
		String sql = "select branchname from branch";
		Cursor curBranch = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
		if(curBranch.getCount() > 0){
			while (curBranch.moveToNext()) {
				
				categories.add(curBranch.getString(0).trim());
			}
		}
		curBranch.close();
		dba.close();
		
		
		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, categories);

		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinBranch.setAdapter(dataAdapter);
		
		
		btnProceed.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String branch = spinBranch.getSelectedItem().toString().trim();
				if(branch.equalsIgnoreCase("[ Sales Branch ]")){
					Toast.makeText(getApplicationContext(), "Please Select Branch First", Toast.LENGTH_LONG).show();
				}else{
					dba.open();
					String sql = "select id from branch where branchname='"+ branch + "'";
					
					Cursor curBra = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
					if(curBra.getCount() > 0){
						curBra.moveToFirst();
						String bra = curBra.getString(0).trim();
						
						session.createLoginSession(strUserName,
								strPassword, strEmpNo, strEmpName,bra,"0.0310686","0.00310686",strAddress,"Computer1");

						waitDialog.show();

						Thread timer = new Thread() {
							@Override
							public void run() {
								dba.open();
								sData.syncLoginData(session.getEmpNo(), session.getBranchNo());
								dba.close();
								waitDialog.cancel();
								Intent intent = new Intent(
										getApplicationContext(),
										MainActivity.class);
								startActivity(intent);
								finish();
							}
						};

						timer.start();


					}
					curBra.close();
					dba.close();
					
					
				}
			}
		});
		
		
	}
	
	
}
