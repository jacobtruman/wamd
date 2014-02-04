package wamd.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import wamd.main.R;
import wamd.main.WaMd;

public class WaMdActivity extends Activity {
    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // start the service
        Intent intent = new Intent(this, WaMd.class);
        //intent.putExtra("_waitTime", 100000);
        startService(intent);
    }
}