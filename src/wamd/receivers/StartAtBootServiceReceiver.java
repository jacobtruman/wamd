package wamd.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import wamd.main.R;
import wamd.main.WaMd;

public class StartAtBootServiceReceiver extends BroadcastReceiver {
	private String TAG = "StartAtBootServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            context.startService(new Intent(context, WaMd.class));
        }
    }
}