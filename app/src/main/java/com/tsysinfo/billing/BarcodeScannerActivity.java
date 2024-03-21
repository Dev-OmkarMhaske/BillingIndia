package com.tsysinfo.billing;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.zxing.Result;

import java.util.Collection;
import java.util.Iterator;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(mScannerView);
        mScannerView.setFlash(true);
        mScannerView.setAutoFocus(true);
        // Set the scanner view as the content view

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("TAG", rawResult.getText()); // Prints scan results
        Log.v("TAG", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Intent intent = new Intent();
        intent.putExtra("BarCodeValue",rawResult.getText());
        setResult(RESULT_OK, intent);
        finish();
        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }
}