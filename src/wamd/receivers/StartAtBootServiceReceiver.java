package wamd.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import wamd.main.WaMd;

public class StartAtBootServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            context.startService(new Intent(context, WaMd.class));
        }
    }
} 