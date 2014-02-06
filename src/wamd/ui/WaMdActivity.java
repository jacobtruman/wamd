package wamd.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import wamd.main.R;
import wamd.main.WaMd;

//import android.widget.TextView;
//import android.widget.LinearLayout;
//import android.view.Gravity;
//import android.view.ViewGroup.LayoutParams;

public class WaMdActivity extends Activity {
	private Intent intent;
	private String TAG = "WaMdActivity";
	private String[] textViews = {"coords_gps", "coords_network"};
	private int[] textViewInts = {R.id.coords_gps, R.id.coords_network};

	// Called when the activity is first created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		intent = new Intent(this, WaMd.class);
/*
	    TextView tv1 = new TextView(this);
	    tv1.setText("WaMd");
	    tv1.setTextSize(30);
	    tv1.setGravity(Gravity.CENTER);
	    TextView tv2 = new TextView(this);
	    tv2.setTextSize(20);
	    tv2.setGravity(Gravity.CENTER);
	    tv2.setText("Loading...");
	    TextView tv3 = new TextView(this);
	    tv3.setTextSize(10);
	    tv3.setGravity(Gravity.CENTER);
	    tv3.setText("Author: Jacob Truman");
	    LinearLayout ll = new LinearLayout(this);
	    ll.setOrientation(LinearLayout.VERTICAL);
	    ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	    ll.setGravity(Gravity.CENTER);
	    ll.addView(tv1);
	    ll.addView(tv2);
	    ll.addView(tv3);
	    setContentView(ll);
*/
		// start the service
		Intent intent = new Intent(this, WaMd.class);
		//intent.putExtra("_waitTime", 100000);
		startService(intent);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateUI(intent);
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		startService(intent);
		registerReceiver(broadcastReceiver, new IntentFilter(WaMd.BROADCAST_ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(broadcastReceiver);
		stopService(intent);
	}

	private void updateUI(Intent intent) {
		String textValue;
		TextView txtView;
		for(int i = 0; i < this.textViews.length; i++) {
			Log.d(TAG, "Trying to update '" + this.textViews[i] + "' TextView");
			textValue = intent.getStringExtra(this.textViews[i]);
			if(textValue != null) {
				Log.d(TAG, textValue);

				txtView = (TextView) findViewById(this.textViewInts[i]);
				txtView.setText(textValue);
			}
		}
	}
}