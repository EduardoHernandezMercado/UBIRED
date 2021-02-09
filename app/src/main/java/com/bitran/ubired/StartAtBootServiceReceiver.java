package com.bitran.ubired;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.POWER_SERVICE;

public class StartAtBootServiceReceiver extends BroadcastReceiver
{
    static boolean wasScreenOn;
    private boolean screenOff;


    public void onReceive(Context context, Intent intent)
    {




        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            wasScreenOn = false;
            Toast.makeText(context, "Power Off", Toast.LENGTH_SHORT).show();
            Log.i("Key", "keycode 1");
        }
        else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            wasScreenOn = true;
            Log.i("Key", "keycode 2");
        }
      //  Intent i = new Intent(context, Inicio.class);
       // i.putExtra("screen_state", screenOff);
       // i.setAction("com.example.antitheft.SampleService");
       // context.startService(i);
//      
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Log.i("Key", "keycode 3 ");
         //   Intent i1 = new Intent();
          //  i1.setAction("com.example.sampleonkeylistener.MainActivity");
           // context.startService(i1);
        }
        Log.i("Key", "keycode 4");
    }
}