
package com.tsysinfo.billing;

import android.app.Activity;
import android.os.Bundle;

public class FinishActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
       /* if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }*/
    }
}
