package wamd.main;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

import wamd.listeners.MyLocationListener;
/*
 * TODO setup app key
 * TODO accept disable signal from http response
 * TODO set timeout for GPS check ?? not sure I need this anymore
 */

/**
 * @author Jacob Truman <jacob.truman@gmail.com>
 */
public class WaMd extends Service {
	private String TAG = "WaMdMain";
	private int _waitTime = 300000; // 5 min
	private int _minDist = 0;
	private int _startId;
	private LocationManager _locationManager;
	private MyLocationListener[] locationListeners;
	public static final String BROADCAST_ACTION = "wamd.displayevent";
	private final Handler handler = new Handler();
	private String[] _providers = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
	Intent intent;
	int counter = 0;

	//@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "WAIT TIME: " + (this._waitTime / 60000) + " min");
		this.locationListeners = new MyLocationListener[]{
				new MyLocationListener(this, this._providers[0]),
				new MyLocationListener(this, this._providers[1])
		};
		this._initializeLocationManager();
		try {
			this._locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, this._waitTime, this._minDist,
					locationListeners[1]);
		} catch (java.lang.SecurityException ex) {
			Log.i(TAG, "fail to request location update, ignore", ex);
		} catch (IllegalArgumentException ex) {
			Log.d(TAG, "network provider does not exist, " + ex.getMessage());
		}
		try {
			this._locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, this._waitTime, this._minDist,
					locationListeners[0]);
		} catch (java.lang.SecurityException ex) {
			Log.i(TAG, "fail to request location update, ignore", ex);
		} catch (IllegalArgumentException ex) {
			Log.d(TAG, "gps provider does not exist " + ex.getMessage());
		}
		String versionName = "";
		try {
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException ex) {
			Logger.getLogger(WaMd.class.getName()).log(Level.SEVERE, null, ex);
		}
		Log.i(TAG, "WAMD VERSION: " + versionName);

		super.onCreate();
		intent = new Intent(BROADCAST_ACTION);
	}

	// Use the LocationManager class to obtain GPS locations
	private void _initializeLocationManager() {
		Log.i(TAG, "initializeLocationManager");
		if (this._locationManager == null) {
			this._locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		}
	}

	public void ChangeDisplayText(String text, String tvid) {
		if(text.length() > 0) {
			Log.d(TAG, "Extra ID: " + tvid);
			Log.d(TAG, "Text: " + text);

			intent.putExtra(tvid, text);
			sendBroadcast(intent);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this._startId = startId;

		handler.removeCallbacks(sendUpdatesToUI);
		handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

		return Service.START_STICKY;
	}

	private Runnable sendUpdatesToUI = new Runnable() {
		public void run() {
			//ChangeDisplayText("");
			handler.postDelayed(this, 5000); // 5 seconds
		}
	};

	public void updateWaitTime(int newTime) {
		if (this._waitTime != newTime) {
			this._waitTime = newTime;
			Log.i(TAG, "WAIT TIME: " + (this._waitTime / 60000) + " min");
			Log.i(TAG, "WAIT TIME: " + this._waitTime + " sec");
			for (int i = 0; i < this.locationListeners.length; i++) {
				this.locationListeners[i].updateWaitTime(newTime);
			}
			this._restartService();
		}
	}

	private void _restartService() {
		Log.i(TAG, "RESTARTING SERVICE");
		for (int i = 0; i < this._providers.length; i++) {
			this._locationManager.removeUpdates(this.locationListeners[i]);
			this._locationManager.requestLocationUpdates(this._providers[i], this._waitTime, this._minDist, this.locationListeners[i]);
		}
	}

	public void stopService() {
		this.stopSelfResult(this._startId);
	}
}