package wamd.listeners;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import wamd.main.WaMd;
import wamd.utilities.WebService;

/**
 * @author Jacob Truman <jacob.truman@gmail.com>
 */
public class MyLocationListener implements LocationListener {
    private Context _Context;
    private WaMd _wamd;
    private WebService _ws;
    private long _prevTime = 0;
    private int _waitTime = 300000;
    private String TAG = "WaMd.MyLocationListener";
    private Location _lastLocation;

    public MyLocationListener(WaMd wamd, String provider) {
        this._lastLocation = new Location(provider);
        this._wamd = wamd;
        this._Context = wamd.getApplicationContext();
        this._ws = new WebService(this._wamd);
        Log.i(TAG, "Init");
    }

    @Override
    public void onLocationChanged(Location loc) {
        // get the time of the location change
        long time = loc.getTime();
        // get the difference between now and the time of the previous uploaded location
        long diff = time - this._prevTime;
        if (this._prevTime == 0 || diff > this._waitTime) {
            Log.i(TAG, "SENDING COORDS");
            // update the previous uploaded location time
            this._prevTime = time;

            this._ws.postData(loc);
        } else {
            Log.i(TAG, "NOT READY: " + diff + " < " + this._waitTime);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this._Context.getApplicationContext(), "GPS Disabled", Toast.LENGTH_SHORT).show();
        this.turnGPSOn();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this._Context.getApplicationContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private void turnGPSOn() {
        Toast.makeText(this._Context.getApplicationContext(), "Attempting to enable GPS", Toast.LENGTH_SHORT).show();
        String provider = Settings.Secure.getString(this._Context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) {//if gps is disabled
            final Intent gpsintent = new Intent();
            gpsintent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            gpsintent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            gpsintent.setData(Uri.parse("3"));
            this._Context.sendBroadcast(gpsintent);

			/*Intent gpsintent = new Intent("android.location.GPS_ENABLED_CHANGE");
            gpsintent.putExtra("enabled", true);
            this._Context.sendBroadcast(gpsintent);*/
        }
    }

    private void turnGPSOff() {
        Toast.makeText(this._Context.getApplicationContext(), "Attempting to disable GPS", Toast.LENGTH_SHORT).show();
        String provider = Settings.Secure.getString(this._Context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (provider.contains("gps")) {//if gps is enabled
            final Intent gpsintent = new Intent();
            gpsintent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            gpsintent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            gpsintent.setData(Uri.parse("3"));
            this._Context.sendBroadcast(gpsintent);
			
			/*Intent gpsintent = new Intent("android.location.GPS_ENABLED_CHANGE");
			gpsintent.putExtra("enabled", false);
			_Context.sendBroadcast(gpsintent);*/
        }
    }

    public void updateWaitTime(int newTime) {
        if (this._waitTime != newTime) {
            this._waitTime = newTime;
            Log.i(TAG, "WAIT TIME: " + (this._waitTime / 60000) + " min");
            Log.i(TAG, "WAIT TIME: " + this._waitTime + " sec");
        }
    }
}
