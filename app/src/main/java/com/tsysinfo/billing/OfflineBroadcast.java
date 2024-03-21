package com.tsysinfo.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by administrator on 19/4/16.
 */
public class OfflineBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        // Make sure it's an event we're listening for ...
        if (!intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) &&
                !intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) &&
                !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            return;
        }

        ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));

        if (cm == null) {
            return;
        }

        // Now to check if we're actually connected
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            // Start the service to do our thing
            context.startService(new Intent(context, SyncService.class));

            Log.w("BR", "BR");

        }
    }
}