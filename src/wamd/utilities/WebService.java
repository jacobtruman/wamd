package wamd.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import wamd.main.WaMd;

/**
 * WebService connects to a web address send data
 *
 * @author Jacob Truman <jacob.truman@gmail.com>
 */
public class WebService {
	private WaMd _wamd;
	private Context _Context;
	private HttpClient _httpclient;
	private HttpPost _httppost;
	private HttpResponse _response;
	private String _url;
	private DatabaseHelper _dbHelper;
	private List<NameValuePair> _postValues = new ArrayList<NameValuePair>();
	private List<List> _postValuesArray = new ArrayList<List>();
	private String TAG = "WaMd.WebService";
	private ArrayList<String> _fields = new ArrayList<String>();
	private boolean _dbValues = false;
	private HttpEntity _entity;
	private InputStream _is;
	private Intent _batteryStatus;
	private IntentFilter _ifilter;
	private int _chargingStatus;
	private boolean _isCharging;
	private int _chargePlug;
	private String _chargingHow;

	public WebService(WaMd wamd) {
		Log.i(TAG, "Init");
		this._wamd = wamd;
		this._Context = this._wamd.getApplicationContext();

		// list of fields being tracked
		this._fields.add("client_id");
		this._fields.add("device_id");
		this._fields.add("lat");
		this._fields.add("lon");
		this._fields.add("provider");
		this._fields.add("speed");
		this._fields.add("accuracy");
		this._fields.add("altitude");
		this._fields.add("bearing");
		this._fields.add("battery");
		this._fields.add("location_time");
		this._fields.add("location_timezone");
		this._fields.add("charging");
		this._fields.add("charging_how");

		this._ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		this._batteryStatus = this._wamd.getApplicationContext().registerReceiver(null, this._ifilter);

		// the url for the web service
		this._url = "http://wamd.trucraft.net/add_coords.php";

		// Create a new HttpClient and Post Header
		this._httpclient = new DefaultHttpClient();

		Log.i(TAG, "Creating DatabaseHelper");
		this._dbHelper = new DatabaseHelper(this._Context, this._fields);
	}

	public void postData(Location location) {
		Log.i(TAG, "BEARING: " + location.getBearing());
		Log.i(TAG, "LAT: " + location.getLatitude());
		Log.i(TAG, "LON: " + location.getLongitude());
		Log.i(TAG, "DATE/TIME: " + DateFormat.format("yyyy-MM-dd kk:mm:ss", location.getTime()));

		// Are we charging / charged?
		this._chargingStatus = this._batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		this._isCharging = this._chargingStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
				this._chargingStatus == BatteryManager.BATTERY_STATUS_FULL;

		if (this._isCharging) {
			// How are we charging?
			this._chargePlug = this._batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			//boolean usbCharge = this._chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
			//boolean acCharge = this._chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
			this._chargingHow = this._chargePlug == BatteryManager.BATTERY_PLUGGED_USB ? "USB" : (this._chargePlug == BatteryManager.BATTERY_PLUGGED_AC ? "AC" : "unknown");
		} else {
			this._chargingHow = "";
		}
		Log.i(TAG, "CHARGING: " + this._isCharging);
		Log.i(TAG, "CHARGING HOW: " + this._chargingHow);

		// build data array
		this._postValues.add(new BasicNameValuePair("client_id", this._getMyPhoneNumber()));
		this._postValues.add(new BasicNameValuePair("device_id", this._getDeviceId()));
		this._postValues.add(new BasicNameValuePair("lat", String.valueOf(location.getLatitude())));
		this._postValues.add(new BasicNameValuePair("lon", String.valueOf(location.getLongitude())));
		this._postValues.add(new BasicNameValuePair("provider", location.getProvider()));
		this._postValues.add(new BasicNameValuePair("speed", String.valueOf(location.getSpeed())));
		this._postValues.add(new BasicNameValuePair("accuracy", String.valueOf(location.getAccuracy())));
		this._postValues.add(new BasicNameValuePair("altitude", String.valueOf(location.getAltitude())));
		this._postValues.add(new BasicNameValuePair("bearing", String.valueOf(location.getBearing())));
		this._postValues.add(new BasicNameValuePair("battery", String.valueOf(this._getBatterLevel())));
		this._postValues.add(new BasicNameValuePair("location_time", String.valueOf(DateFormat.format("yyyy-MM-dd kk:mm:ss", location.getTime()))));
		this._postValues.add(new BasicNameValuePair("location_timezone", String.valueOf(DateFormat.format("zz", location.getTime()))));
		this._postValues.add(new BasicNameValuePair("charging", String.valueOf(this._isCharging)));
		this._postValues.add(new BasicNameValuePair("charging_how", this._chargingHow));

		// first try to upload the coords via http
		try {
			this._httppost = new HttpPost(this._url);
			this._httppost.setEntity(new UrlEncodedFormEntity(this._postValues));

			// Execute HTTP Post Request
			Log.i(TAG, "Posting coords to " + this._url);
			this._response = this._httpclient.execute(this._httppost);

			Log.i(TAG, "HTTP RESPONSE: " + this._response.getStatusLine().getStatusCode());
			// if a bad response is returned, save the corrds to the local db
			if (this._response.getStatusLine().getStatusCode() != 200) {
				this._dbHelper.addCoords(this._postValues);
				this._dbValues = true;
			} else if (this._dbValues) {
				// get any existing records in the local db and upload them
				this._postValuesArray = this._dbHelper.listSelectAll();
				Iterator postValuesIterator = this._postValuesArray.iterator();
				while (postValuesIterator.hasNext()) {
					this._postValues = (List) postValuesIterator.next();
					this._httppost = new HttpPost(this._url);
					this._httppost.setEntity(new UrlEncodedFormEntity(this._postValues));

					// Execute HTTP Post Request
					Log.i(TAG, "Posting coords to " + this._url);
					this._response = this._httpclient.execute(this._httppost);
				}
				this._dbValues = false;
			} else {
				//Log.i(TAG, "RESPONSE TEXT: "+this._response.getEntity().getContent());
				this._entity = this._response.getEntity();
				this._is = this._entity.getContent();
				this._processResponse();
			}
		}
		// catch a client error here
		catch (ClientProtocolException e) {
			Log.e(TAG, "CLIENT ERROR: " + e.getMessage());
			// save coords to local db on failure
			this._dbHelper.addCoords(this._postValues);
			this._dbValues = true;
		} catch (IOException e) {
			Log.e(TAG, "IO ERROR: " + e.getMessage());
			// save coords to local db on failure
			this._dbHelper.addCoords(this._postValues);
			this._dbValues = true;
		}

		// update the UI
		this._wamd.ChangeDisplayText("LAT: " + location.getLatitude() + "\nLON: " + location.getLongitude(), "coords_" + location.getProvider());
	}

	private String _getDeviceId() {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager)
				this._Context.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getDeviceId();
	}

	private String _getMyPhoneNumber() {
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager)
				this._Context.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}

	private String _getMy10DigitPhoneNumber() {
		String s = this._getMyPhoneNumber();
		return s.substring(2);
	}

	private float _getBatterLevel() {
		Intent battery = this._Context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		//Log.i(TAG, "BATTERY LEVEL: "+level);
		//Log.i(TAG, "BATTERY SCALE: "+scale);

		float batteryPercent = (level / (float) scale) * 100;
		return batteryPercent;
	}

	private void _processResponse() {
		String result;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(this._is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			this._is.close();
			result = sb.toString();
			Log.i(TAG, result);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			Log.e(TAG, e.toString());
		}
		//this._wamd.updateWaitTime(10000);
	}
}
