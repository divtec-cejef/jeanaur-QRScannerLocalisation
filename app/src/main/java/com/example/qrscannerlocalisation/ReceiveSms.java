package com.example.qrscannerlocalisation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReceiveSms extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "SMS Received !", Toast.LENGTH_SHORT).show();
    }
}
