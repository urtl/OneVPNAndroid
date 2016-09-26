package com.dave.newonevpn.netutil;



import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class is reponsible for all network operations.
 * 
 * @author |Autumn Trees|
 *
 */
public class NetworkManager {
	
	/**
	 * Checks if the device is connected to a network.
	 * 
	 * @param context
	 *            application context
	 * @return true if the device is connected
	 */
	
	public static boolean isNetworkConnected(Context context) {
		
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}
}
