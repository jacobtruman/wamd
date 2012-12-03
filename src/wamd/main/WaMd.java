package wamd.main;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
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
 *
 * @author Jacob Truman <jacob.truman@gmail.com>
 */
public class WaMd extends Service
{
	private String TAG = "WaMd.main";
	private int _waitTime = 300000; // 5 min
	private int _minDist = 0;
	private int _startId;
	private LocationManager _locationManager;
	public MyLocationListener locationListener;

	//@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		Log.i(TAG, "WAIT TIME: "+(this._waitTime / 60000)+" min");
		// Use the LocationManager class to obtain GPS locations
		this._locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		this.locationListener = new MyLocationListener(this);
		this._locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, this._waitTime, this._minDist, this.locationListener);
		String versionName = "";
		try {
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException ex) {
			Logger.getLogger(WaMd.class.getName()).log(Level.SEVERE, null, ex);
		}
		Log.i(TAG, "WAMD VERSION: "+versionName);

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		this._startId = startId;
		return Service.START_STICKY;
	}
	
	public void updateWaitTime(int newTime)
	{
		if(this._waitTime != newTime)
		{
			this._waitTime = newTime;
			Log.i(TAG, "WAIT TIME: "+(this._waitTime / 60000)+" min");
			Log.i(TAG, "WAIT TIME: "+this._waitTime+" sec");
			this.locationListener.updateWaitTime(newTime);
			this._restartService();
		}
	}
	
	private void _restartService()
	{
		Log.i(TAG, "RESTARTING SERVICE");
		this._locationManager.removeUpdates(this.locationListener);
		this._locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, this._waitTime, this._minDist, this.locationListener);
	}

	public void stopService()
	{
		this.stopSelfResult(this._startId);
	}
}