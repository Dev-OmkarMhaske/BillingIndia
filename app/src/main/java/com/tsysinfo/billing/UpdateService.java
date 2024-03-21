package com.tsysinfo.billing;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by tsysinfo on 11/15/2016.
 */public class UpdateService extends Service {

    BroadcastReceiver mReceiver;
    public static int countOn = 0;
    public static int countOff = 0;
    MyCounter timer;
    Boolean isRunning=true;
    @Override
    public void onCreate() {
        super.onCreate();
        timer = new MyCounter(840000,1000);
        // register receiver that handles screen on and screen off logic
       //Toast.makeText(UpdateService.this, "Service started", Toast.LENGTH_SHORT).show();
        Log.w("UpdateService", "Started");
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_ANSWER);
        mReceiver = new MyReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {


        unregisterReceiver(mReceiver);
        Log.w("onDestroy Reciever", "Called");
        System.exit(0);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        boolean screenOn = intent.getBooleanExtra("screen_state", false);
        if (!screenOn) {

            if(timer!=null)
            {
                timer.cancel();
                if(!isRunning)
                {

                    sendBroadcast(new Intent("xyz"));
                    KeyguardManager kgMgr =
                            (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                    boolean showing = kgMgr.inKeyguardRestrictedInputMode();
                    if (showing){
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                        Toast.makeText(UpdateService.this, "Closed", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(UpdateService.this, "unLocked", Toast.LENGTH_SHORT).show();
                    }


                }

            }


        } else {
            timer.start();
        }


        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    private class MyCounter extends CountDownTimer {

        public MyCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            isRunning=false;

        }

        @Override
        public void onTick(long millisUntilFinished) {

            isRunning=true;

            String a=String.valueOf((millisUntilFinished/1000));


          Log.w("On tick",a);
        }
    }


}