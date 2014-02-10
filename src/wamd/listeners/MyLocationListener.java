package wamd.listeners;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import wamd.main.WaMd;
import wamd.utilities.WebService;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jacob Truman <jacob.truman@gmail.com>
 */
public class MyLocationListener implements LocationListener {
	private Context _Context;
	private WaMd _wamd;
	private WebService _ws;
	private long[] _prevTime;
	private String TAG = "WaMd.MyLocationListener";
	private Location _lastLocation;
	private String[] _providers;
	private List<String> _providerList;

	public MyLocationListener(WaMd wamd) {
		this._wamd = wamd;
		this._providers = this._wamd.getProviders();
		this._providerList = Arrays.asList(this._providers);
		// initialize previous time for each provider
		this._prevTime = new long[this._providers.length];
		this._Context = wamd.getApplicationContext();
		this._ws = new WebService(this._wamd);
		Log.i(TAG, "Init");
	}

	@Override
	public void onLocationChanged(Location loc) {
		// get the provider index
		int index = this._providerList.indexOf(loc.getProvider());

		// get the time of the location change
		long time = loc.getTime();
		// get the difference between now and the time of the previous uploaded location
		long diff = time - this._prevTime[index];

		if (this._prevTime[index] == 0 || diff >= this._wamd.getWaitTime()) {
			Log.i(TAG, "SENDING COORDS FROM PROVIDER: " + loc.getProvider());
			// update the previous uploaded location time
			this._prevTime[index] = time;

			this._lastLocation = loc;
			this._ws.postData(loc);
		} else {
			Log.i(TAG, "PROVIDER (" + loc.getProvider() + ") NOT READY: " + diff + " < " + this._wamd.getWaitTime());
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this._Context.getApplicationContext(), provider + " Disabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this._Context.getApplicationContext(), provider + " Enabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public void updateWaitTime(int newTime) {
		if (this._wamd.getWaitTime() != newTime) {
			this._wamd.setWaitTime(newTime);
			Log.i(TAG, "WAIT TIME: " + (this._wamd.getWaitTime() / 60000) + " min");
			Log.i(TAG, "WAIT TIME: " + this._wamd.getWaitTime() + " sec");
		}
	}

	public Location getLastLocation() {
		return this._lastLocation;
	}
}
