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
	//private int _waitTime = 60; // 10 seconds
	private int _minDist = 0;
	private int _startId;
	private LocationManager _locationManager;
	private MyLocationListener locationListener;
	public static final String BROADCAST_ACTION = "wamd.displayevent";
	private final Handler handler = new Handler();
	private String[] _providers = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
	Intent intent;

	//@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "WAIT TIME: " + (this._waitTime / 60000) + " min");
		this.locationListener = new MyLocationListener(this);
		this._initializeLocationManager();

		for (int i = 0; i < this._providers.length; i++) {
			try {
				// create a location update requester. Add some time to the wait time, for buffer
				this._locationManager.requestLocationUpdates(
						this._providers[i], this._waitTime + 100, this._minDist,
						this.locationListener);
				//this.locationListeners[i]);
			} catch (java.lang.SecurityException ex) {
				Log.i(TAG, "fail to request location update, ignore", ex);
			} catch (IllegalArgumentException ex) {
				Log.d(TAG, this._providers[i] + " provider does not exist, " + ex.getMessage());
			}
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
		if (text.length() > 0) {
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
			handler.postDelayed(this, 5000); // 5 seconds
		}
	};

	public void updateWaitTime(int newTime) {
		if (this._waitTime != newTime) {
			this._waitTime = newTime;
			Log.i(TAG, "WAIT TIME: " + (this._waitTime / 60000) + " min");
			Log.i(TAG, "WAIT TIME: " + this._waitTime + " sec");
			this.locationListener.updateWaitTime(newTime);
			this._restartService();
		}
	}

	private void _restartService() {
		Log.i(TAG, "RESTARTING SERVICE");
		this._locationManager.removeUpdates(this.locationListener);
		for (int i = 0; i < this._providers.length; i++) {
			this._locationManager.requestLocationUpdates(this._providers[i], this._waitTime, this._minDist, this.locationListener);
		}
	}

	public void stopService() {
		this.stopSelfResult(this._startId);
	}

	public int getWaitTime() {
		return this._waitTime;
	}

	public void setWaitTime(int val) {
		this._waitTime = val;
	}

	public String[] getProviders() {
		return this._providers;
	}
}