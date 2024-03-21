package com.tsysinfo.billing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomePageActivity extends Activity {

	Button btnMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homepage_activity);

		btnMenu = (Button) findViewById(R.id.button1);

		btnMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				// finish();
			}
		});

	}
}
