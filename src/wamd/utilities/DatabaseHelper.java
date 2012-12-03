package wamd.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author Jacob Truman <jacob.truman@gmail.com>
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
	static final String dbName = "WaMd";
	static final String tableName = "coords";
	static final String colTime = "location_time";
	private String _nullField = "";
	private ArrayList _fields;
	private SQLiteDatabase _db;
	private String TAG = "WaMd.DatabaseHelper";

	public DatabaseHelper(Context context, ArrayList fields)
	{
		super(context, dbName, null, 3);
		Log.i(TAG, "Creating Table "+dbName);
		this._fields = fields;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.i(TAG, "Creating Table "+tableName);
		String fields = "id INTEGER PRIMARY KEY AUTOINCREMENT";
		
		Iterator fields_iterator = this._fields.iterator();
		while(fields_iterator.hasNext())
		{
			fields += ", "+fields_iterator.next()+" VARCHAR(25)";
		}
		String sql = "CREATE TABLE "+tableName;
		Log.i(TAG, sql+"("+fields+")");
		db.execSQL(sql+"("+fields+")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.i(TAG, "Dropping Table "+tableName);
		db.execSQL("DROP TABLE IF EXISTS "+tableName);
		onCreate(db);
	}

	public void addCoords(List<NameValuePair> postValues)
	{
		
		this._db = this.getWritableDatabase();
		//Log.i(TAG, "Adding coordinates "+location.getLatitude()+"_"+location.getLongitude());
		ContentValues cv = new ContentValues();
		
		NameValuePair temp;
		Iterator<NameValuePair> postIterator = postValues.iterator();
		while(postIterator.hasNext())
		{
			temp = postIterator.next();
			if(this._nullField.isEmpty())
				this._nullField = temp.getName();
			cv.put(temp.getName(), temp.getValue());
		}

		Log.i(TAG, "INSERTING");
		this._db.insert(tableName, _nullField, cv);
		this._db.close();
	}
	
	/**
	* Select All that returns an ArrayList
	* @return the ArrayList for the DB selection
	*/
	public List<List> listSelectAll()
	{
		this._db = this.getWritableDatabase();
		String fields = "id";
		Iterator fields_iterator = this._fields.iterator();
		while(fields_iterator.hasNext())
		{
			fields += ", "+fields_iterator.next();
		}
		List<NameValuePair> list;
		List<List> list_array = new ArrayList<List>();
		Cursor cursor = this._db.query(
			tableName,
			new String[] {fields},
			null,
			null,
			null,
			null,
			colTime);
		if(cursor.moveToFirst())
		{
			list = new ArrayList<NameValuePair>();
			int columnCount = cursor.getColumnCount();
			do
			{
				for(int i = 1; i < columnCount; i++)
				{
					list.add(new BasicNameValuePair(cursor.getColumnName(i), cursor.getString(i)));
				}
				list_array.add(list);
				Log.i(TAG, list.toString());
				
				Log.i(TAG, "DELETING RECORD "+cursor.getString(0));
				this._db.delete(tableName, "id=?", new String[] {cursor.getString(0)});
			}
			while(cursor.moveToNext());
		}
		if(cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}
		this._db.close();
		return list_array;
	}
}
