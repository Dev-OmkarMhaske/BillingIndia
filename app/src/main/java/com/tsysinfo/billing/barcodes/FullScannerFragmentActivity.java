package com.tsysinfo.billing.barcodes;

import android.os.Bundle;

import com.tsysinfo.billing.R;

public class FullScannerFragmentActivity extends BaseScannerActivity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_full_scanner_fragment);
        setupToolbar();
    }
}