package com.tsysinfo.billing;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ToastService extends Service {

    private final Context mContext;
    Toast toastGps;
    CountDownTimer timer;
    CountDownTimer countDownTimer;

    public ToastService(Context context) {
        this.mContext = context;

        toastGps =  Toast.makeText(context, "Searching for GPS", Toast.LENGTH_SHORT);
        toastGps.setGravity(Gravity.TOP|Gravity.CENTER,0,15);
        View view = toastGps.getView();
        view.setBackgroundResource(R.color.Transperent);
        toastGps.setView(view);

        setTimer();
   //     setCountDown();
    }

    public void setTimer(){

        timer =new CountDownTimer(5000,1000)
        {
            public void onTick(long millisUntilFinished)
            {

                toastGps.show();
            }
            public void onFinish()
            {
                GPSTracker gps = new GPSTracker(mContext);
                if(gps.canGetLocation()){
                    if(gps.getLatitude() != 0.0 && gps.getLongitude() !=0.0){
                        stopTimer();
                    }else{
                        startTimer();
                    }
                }
            }

        };

    }

    public void startTimer(){
        timer.start();
    }

    public void stopTimer(){
        timer.cancel();
    }

    public void setCountDown(){
        countDownTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {
                GPSTracker gps = new GPSTracker(mContext);
                if(gps.canGetLocation()){
                    if(gps.getLatitude() != 0.0 && gps.getLongitude() !=0.0){
                        stopTimer();
                    }else{
                        countDownTimer.start();
                    }
                }
            }
        };

    }

    public void startCountDown(){
        countDownTimer.start();
    }

    public void stopCountDown(){
        countDownTimer.cancel();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
