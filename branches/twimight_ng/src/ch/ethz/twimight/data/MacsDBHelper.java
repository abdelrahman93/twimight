/*******************************************************************************
 * Copyright (c) 2011 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Paolo Carta - Implementation
 *     Theus Hossmann - Implementation
 *     Dominik Schatzmann - Message specification
 ******************************************************************************/
package ch.ethz.twimight.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Manages the macs table in the DB.
 * @author thossmann
 *
 */
public class MacsDBHelper {

	private static final String TAG = "MACS Adapter";
	
	// Database fields
	public static final String KEY_ID = "_id";
	public static final String KEY_MAC = "mac";
	public static final String KEY_ATTEMPTS = "attempts";
	public static final String KEY_SUCCESSFUL = "successful";
	public static final String KEY_ACTIVE = "active";
	
	private Context context;
	
	private SQLiteDatabase database;
	private DBOpenHelper dbHelper;

	/**
	 * Constructor.
	 * @param context
	 */
	public MacsDBHelper(Context context) {
		this.context = context;
	}

	/**
	 * Opens the DB.
	 * @return
	 * @throws SQLException
	 */
	public MacsDBHelper open() throws SQLException {
		dbHelper = DBOpenHelper.getInstance(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * We don't close the DB since there is only one instance!
	 */
	public void close() {
		
	}


	/**
	 * Create a new MAC address in the DB.
	 * @param mac MAC address to be inserted in the DB
	 * @return the row ID of the newly inserted row, or -1 for failure
	 */
	public long createMac(long mac, int active) {
		
		Cursor mCursor = fetchMac(mac);
		if(mCursor.moveToFirst())
			return -1;
		
		ContentValues initialValues = createContentValues(mac, 0, 0, active);

		long result = 0;
		try{
			result = database.insert(DBOpenHelper.TABLE_MACS, null, initialValues);
		} catch (SQLiteException e) {
			return -1;
		}
		return result;
	}


	/**
	 * Update the active status of a MAC address entry
	 */
	public boolean updateMacActive(long mac, int active) {
		ContentValues values = new ContentValues();
		values.put(KEY_ACTIVE, active);
		
		int result = 0;
		try{
			result = database.update(DBOpenHelper.TABLE_MACS, values, KEY_MAC + "=" + mac, null);
		} catch (SQLiteException e){
			Log.e(TAG, "SQLiteException " +e.toString());
			return false;
		}

		if(result > 0)
			return  true;
		else 
			return false;
	}
	
	/**
	 * De-active all MAC addresses
	 */
	public boolean updateMacsDeActive() {
		ContentValues values = new ContentValues();
		values.put(KEY_ACTIVE, 0);
		int resultCode = 0;
		try{
			resultCode = database.update(DBOpenHelper.TABLE_MACS, values, null, null);
		} catch (SQLiteException e){
			Log.e(TAG, "SQLiteException: " + e.toString());
			
		}
		
		if(resultCode > 0){
			return true;
		}else{
			return false;
		}
	}
	

	/**
	 * Update the number of attempts of a MAC address entry
	 */
	public boolean updateMacAttempts(long mac, int attempts) {
		
		Cursor tmpCursor = fetchMac(mac);
		if(tmpCursor.moveToFirst()){
			int oldAttempts = tmpCursor.getInt(tmpCursor.getColumnIndex(MacsDBHelper.KEY_ATTEMPTS));
			ContentValues values = new ContentValues();
			values.put(KEY_ATTEMPTS, oldAttempts + attempts);
			
			int resultCode = 0;
			try{
				database.update(DBOpenHelper.TABLE_MACS, values, KEY_MAC + "=" + mac, null);
			} catch (SQLiteException e){
				Log.e(TAG, "SQLiteException: " + e.toString());
			}
			if(resultCode > 0){
				return true;
			}else{
				return false;
			}
			
		} else {
			
			return false;
		}
	}

	/**
	 * Returns the number of attempts of a MAC address entry
	 */
	public int fetchMacAttempts(long mac) {
		
		
		Cursor tmpCursor = fetchMac(mac);
		if(tmpCursor.moveToFirst()){
			int oldAttempts = tmpCursor.getInt(tmpCursor.getColumnIndex(MacsDBHelper.KEY_ATTEMPTS));
			return oldAttempts;
			
		} else {
			
			return -1;
		}
	}
	
	/**
	 * Update the number of successful connections of a MAC address entry
	 */
	public boolean updateMacSuccessful(long mac, int successful) {
		
		Cursor tmpCursor = fetchMac(mac);
		if(tmpCursor.moveToFirst()){
			int oldSuccessful = tmpCursor.getInt(tmpCursor.getColumnIndex(MacsDBHelper.KEY_SUCCESSFUL));
			ContentValues values = new ContentValues();
			values.put(KEY_SUCCESSFUL, oldSuccessful + successful);
			
			int resultCode = 0;
			try{
				database.update(DBOpenHelper.TABLE_MACS, values, KEY_MAC + "=" + mac, null);
			} catch (SQLiteException e){
				Log.e(TAG, "SQLiteException: " + e.toString());
			}
			if(resultCode > 0){
				return true;
			}else{
				return false;
			}
			
		} else {
			
			return false;
		}
	}

	/**
	 * Returns the number of successful connections to a MAC address entry
	 */
	public int fetchMacSuccessful(long mac) {
		
		
		Cursor tmpCursor = fetchMac(mac);
		if(tmpCursor.moveToFirst()){
			int oldAttempts = tmpCursor.getInt(tmpCursor.getColumnIndex(MacsDBHelper.KEY_SUCCESSFUL));
			return oldAttempts;
			
		} else {
			
			return -1;
		}
	}

	/**
	 * Deletes a MAC address from list
	 */
	public boolean deleteMac(long mac) {
		return database.delete(DBOpenHelper.TABLE_MACS, KEY_MAC + "=" + mac, null) > 0;
	}


	/**
	 * Return a Cursor over the list of all MAC adresses in the database
	 * 
	 * @return Cursor over all notes
	 */

	public Cursor fetchAllMacs() {
		return database.query(DBOpenHelper.TABLE_MACS, new String[] {
				KEY_MAC, KEY_ATTEMPTS, KEY_SUCCESSFUL, KEY_ACTIVE}, null, null, null,
				null, null);
	}

	/**
	 * Return a Cursor to all active Macs
	 */
	public Cursor fetchActiveMacs() throws SQLException {
		Cursor mCursor = database.query(true, DBOpenHelper.TABLE_MACS, new String[] {
				KEY_MAC, KEY_ATTEMPTS, KEY_SUCCESSFUL, KEY_ACTIVE},
				KEY_ACTIVE + "= 1 ", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	
	/**
	 * Return a Cursor positioned at the defined Mac
	 */
	public Cursor fetchMac(long mac) throws SQLException {
		
		Cursor mCursor = database.query(true, DBOpenHelper.TABLE_MACS, new String[] {
				KEY_MAC, KEY_ATTEMPTS, KEY_SUCCESSFUL, KEY_ACTIVE},
				KEY_MAC + "=" + mac, null, null, null, null, null);
		
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		
		return mCursor;
	}

	/**
	 * Creates a MAC address record to insert in the DB
	 * @param mac long
	 * @param attempts how many connection attempts?
	 * @param successful how many successful connection attempts?
	 * @param active is the MAC address activated for scanning?
	 * @return
	 */
	private ContentValues createContentValues(long mac, int attempts, int successful, int active) {
		ContentValues values = new ContentValues();
		values.put(KEY_MAC, mac);
		values.put(KEY_ATTEMPTS, attempts);
		values.put(KEY_SUCCESSFUL, successful);
		values.put(KEY_ACTIVE, active);
		return values;
	}
	
	/**
	 * Helper to convert human readable MAC address to long
	 * @param MAC address in human readable form 00:00:00...
	 * @return long representation of the MAC address
	 */
	public long mac2long(String mac){
		
		return Long.parseLong(mac.replace(":", ""), 16);
	}
	
	/**
	 * Helper to convert long to human readable MAC address
	 * @param macLong long
	 * @return human readable MAC address
	 */
	public String long2mac(long macLong){
		String addressHex = Long.toHexString(macLong);
		StringBuilder buffer = new StringBuilder("000000000000".substring(addressHex.length()) + addressHex);
		for (int index = 2; index < buffer.length(); index += 3) {
			buffer.insert(index, ':');
		}
		
		return buffer.toString().toUpperCase();
	}
}
