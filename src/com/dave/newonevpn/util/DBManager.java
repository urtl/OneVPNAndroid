package com.dave.newonevpn.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dave.newonevpn.model.FileNameInfo;

import java.io.File;
import java.util.ArrayList;


public class DBManager extends SQLiteOpenHelper {
	public static final String DBNAME = "liquidvpndb.db";
	public static final String TABLENAME_FILEBANEPARSE = "tbl_parsednametable";
	public static final String db_path = "/data/data/com.dave.newonevpn/" + DBNAME;

	private SQLiteDatabase mDB;
	private Context mContext;
	
	public DBManager(Context context)
	{
		super(context, DBNAME, null, 1);
		try {
			File file = new File(db_path);
			if( file.exists() == false )
				file.createNewFile();
			if (file.exists() && !file.isDirectory())
				SQLiteDatabase.openOrCreateDatabase(db_path, null);
		} catch (SQLiteException e) {
			Log.d("Exception:%s", e.getMessage());
		}catch(Exception e){
			Log.d("Exception:%s", e.getMessage());
		}

		mDB = getWritableDatabase();
		this.mContext = context;
	}

	public void closeDB()
	{
		mDB.close();
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLENAME_FILEBANEPARSE + "(hostname varchar(100), ca varchar(20), topology varchar(50), protocol varchar(10), port varchar(10), path varchar(100));");
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLENAME_FILEBANEPARSE);
		onCreate(sqLiteDatabase);
	}
	public void clearFileNameTable(){
		mDB.delete(TABLENAME_FILEBANEPARSE, null, null);
	}
	public void addFileName(FileNameInfo info){
		ContentValues values = new ContentValues();
		values.put("hostname", info.hostname);
		values.put("ca", info.CA);
		values.put("topology", info.topology);
		values.put("protocol", info.protocol);
		values.put("port", info.port);
		values.put("path", info.path);
		mDB.insert(TABLENAME_FILEBANEPARSE, null, values);
	}
	public ArrayList<FileNameInfo> getFileInfo(String hostname, String topology, String ca){
		ArrayList<FileNameInfo> res = new ArrayList<>();
		String sql = "select * from " + TABLENAME_FILEBANEPARSE + " where hostname = ? and topology = ? and ca = ?";
		Cursor cursor = mDB.rawQuery(sql, new String[]{hostname, topology, ca});
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++){
			FileNameInfo info = new FileNameInfo();
			info.hostname = cursor.getString(cursor.getColumnIndex("hostname"));
			info.CA = cursor.getString(cursor.getColumnIndex("ca"));
			info.topology = cursor.getString(cursor.getColumnIndex("topology"));
			info.protocol = cursor.getString(cursor.getColumnIndex("protocol"));
			info.port = cursor.getString(cursor.getColumnIndex("port"));
			info.path = cursor.getString(cursor.getColumnIndex("path"));
			res.add(info);
			cursor.moveToNext();
		}
		return res;
	}
	public ArrayList<String> getTopolists(){
		ArrayList<String> res = new ArrayList<>();
		String sql = "select topology from " + TABLENAME_FILEBANEPARSE + " group by topology";
		Cursor cursor = mDB.rawQuery(sql, null);
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++){
			String topology = cursor.getString(0);
			res.add(topology);
			cursor.moveToNext();
		}
		return res;
	}

	public ArrayList<String> getProtocols(String topology){
		ArrayList<String> res = new ArrayList<>();
		String sql = "select protocol from " + TABLENAME_FILEBANEPARSE + " where topology = ? group by protocol";
		Cursor cursor = mDB.rawQuery(sql, new String[]{topology});
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++){
			String port = cursor.getString(0);
			res.add(port);
			cursor.moveToNext();
		}
		return res;
	}
	public ArrayList<String> getPorts(String topology, String protocol){
		ArrayList<String> res = new ArrayList<>();
		String sql = "select port from " + TABLENAME_FILEBANEPARSE + " where topology = ? and protocol = ? group by port";
		Cursor cursor = mDB.rawQuery(sql, new String[]{topology, protocol});
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++){
			String port = cursor.getString(0);
			res.add(port);
			cursor.moveToNext();
		}
		return res;
	}

}
