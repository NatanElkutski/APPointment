package com.example.appointment.SendNotificationPack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1)
    {
        final Intent intent = new Intent(context, MyFireBaseMessagingService.class);
        context.startService(intent);
    }
}
