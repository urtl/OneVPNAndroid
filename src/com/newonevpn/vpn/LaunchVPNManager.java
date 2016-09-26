package com.newonevpn.vpn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.dave.onevpnfresh.R;
import com.dave.newonevpn.model.Global;
import com.newonevpn.vpn.core.OpenVPN;
import com.newonevpn.vpn.core.OpenVPN.ConnectionStatus;
import com.newonevpn.vpn.core.ProfileManager;
import com.newonevpn.vpn.core.VPNLaunchHelper;

import java.io.IOException;

/**
 * This Activity actually handles two stages of a launcher shortcut's life cycle.
 * 
 * 1. Your application offers to provide shortcuts to the launcher.  When
 *    the user installs a shortcut, an activity within your application
 *    generates the actual shortcut and returns it to the launcher, where it
 *    is shown to the user as an icon.
 *
 * 2. Any time the user clicks on an installed shortcut, an intent is sent.
 *    Typically this would then be handled as necessary by an activity within
 *    your application.
 *    
 * We handle stage 1 (creating a shortcut) by simply sending back the information (in the form
 * of an {@link android.content.Intent} that the launcher will use to create the shortcut.
 * 
 * You can also implement this in an interactive way, by having your activity actually present
 * UI for the user to select the specific nature of the shortcut, such as a contact, picture, URL,
 * media item, or action.
 * 
 * We handle stage 2 (responding to a shortcut) in this sample by simply displaying the contents
 * of the incoming {@link android.content.Intent}.
 * 
 * In a real application, you would probably use the shortcut intent to display specific content
 * or start a particular operation.
 */
public class LaunchVPNManager{

	public static final String EXTRA_KEY = "com.newonevpn.vpn.shortcutProfileUUID";
	public static final String EXTRA_NAME = "com.newonevpn.vpn.shortcutProfileName";
	public static final String EXTRA_HIDELOG =  "com.newonevpn.vpn.showNoLogWindow";

	public static final int START_VPN_PROFILE= 70;
	public static final int RESULT_OK = 71;

	private ProfileManager mPM;
	public VpnProfile mSelectedProfile;
	public boolean mhideLog=false;

	private boolean mCmfixed=false;
	
	private Activity pActivity;
	private Context pContext;
	
	public LaunchVPNManager(Activity activity)
	{
		pActivity = activity;
		pContext = activity.getBaseContext();
	}
	protected void StartLanchVPN(String action, String Extra_Key, String Extra_Name) {
		mPM =ProfileManager.getInstance(pContext);
		

		if(Intent.ACTION_MAIN.equals(action)) {
			// we got called to be the starting point, most likely a shortcut
			String shortcutUUID = Extra_Key;
			String shortcutName = Extra_Name;
			mhideLog = false;//intent.getBooleanExtra(EXTRA_HIDELOG, false);

			VpnProfile profileToConnect = ProfileManager.get(shortcutUUID);
			if(shortcutName != null && profileToConnect ==null)
				profileToConnect = ProfileManager.getInstance(pContext).getProfileByName(shortcutName);

			if(profileToConnect ==null) {
				OpenVPN.logError(R.string.shortcut_profile_notfound);
				showLogWindow();
				return;
			}

			mSelectedProfile = profileToConnect;
			launchVPN();
		}
	}


	/**
	 * This function creates a shortcut and returns it to the caller.  There are actually two 
	 * intents that you will send back.
	 * 
	 * The first intent serves as a container for the shortcut and is returned to the launcher by 
	 * setResult().  This intent must contain three fields:
	 * 
	 * <ul>
	 * <li>{@link android.content.Intent#EXTRA_SHORTCUT_INTENT} The shortcut intent.</li>
	 * <li>{@link android.content.Intent#EXTRA_SHORTCUT_NAME} The text that will be displayed with
	 * the shortcut.</li>
	 * <li>{@link android.content.Intent#EXTRA_SHORTCUT_ICON} The shortcut's icon, if provided as a
	 * bitmap, <i>or</i> {@link android.content.Intent#EXTRA_SHORTCUT_ICON_RESOURCE} if provided as
	 * a drawable resource.</li>
	 * </ul>
	 * 
	 * If you use a simple drawable resource, note that you must wrapper it using
	 * {@link android.content.Intent.ShortcutIconResource}, as shown below.  This is required so
	 * that the launcher can access resources that are stored in your application's .apk file.  If 
	 * you return a bitmap, such as a thumbnail, you can simply put the bitmap into the extras 
	 * bundle using {@link android.content.Intent#EXTRA_SHORTCUT_ICON}.
	 * 
	 * The shortcut intent can be any intent that you wish the launcher to send, when the user 
	 * clicks on the shortcut.  Typically this will be {@link android.content.Intent#ACTION_VIEW} 
	 * with an appropriate Uri for your content, but any Intent will work here as long as it 
	 * triggers the desired action within your Activity.
	 * @param profile 
	 */
	private void setupShortcut(VpnProfile profile) {
		// First, set up the shortcut intent.  For this example, we simply create an intent that
		// will bring us directly back to this activity.  A more typical implementation would use a 
		// data Uri in order to display a more specific result, or a custom action in order to 
		// launch a specific operation.

		Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
		shortcutIntent.setClass(pContext, LaunchVPN.class);
		shortcutIntent.putExtra(EXTRA_KEY,profile.getUUID().toString());

		// Then, set up the container intent (the response to the caller)

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, profile.getName());
		Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
				pContext,  R.drawable.icon);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
		pActivity.setResult(RESULT_OK, intent);
	}


	public void askForPW(final int type) {
		final EditText entry = new EditText(pContext);
        final View userpwlayout = pActivity.getLayoutInflater().inflate(R.layout.userpass, null);

		entry.setSingleLine();
		entry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		entry.setTransformationMethod(new PasswordTransformationMethod());

		AlertDialog.Builder dialog = new AlertDialog.Builder(pContext);
		dialog.setTitle("Need " + pActivity.getString(type));
		dialog.setMessage("Enter the password for profile " + mSelectedProfile.mName);

        if (type == R.string.password) {
            ((EditText)userpwlayout.findViewById(R.id.username)).setText(mSelectedProfile.mUsername);
            ((EditText)userpwlayout.findViewById(R.id.password)).setText(mSelectedProfile.mPassword);
            ((CheckBox)userpwlayout.findViewById(R.id.save_password)).setChecked(!TextUtils.isEmpty(mSelectedProfile.mPassword));

            dialog.setView(userpwlayout);
        } else {
    		dialog.setView(entry);
        }

        
        	//mSelectedProfile.mUsername = "androidtest";//NetSee.mUser;
          //  mSelectedProfile.mPassword= "t3st4nd";//NetSee.mPass;
 
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);

	}
	public boolean onActivityResult (int requestCode, int resultCode, Intent data) {
	
		if(requestCode==START_VPN_PROFILE) {
			if(resultCode == Activity.RESULT_OK) {
				int needpw = mSelectedProfile.needUserPWInput();
				if(needpw !=0) {
					OpenVPN.updateStateString("USER_VPN_PASSWORD", "", R.string.state_user_vpn_password,
							ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
					askForPW(needpw);
				} else {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);        
					boolean showlogwindow = Global.sp.getBoolean("chkLogWindow", false);//NetSee.sp.getBoolean("showlogwindow", false);//NetSee.sp.getBoolean("showlogwindow", false);

					if(!mhideLog && showlogwindow)
						showLogWindow();
					new startOpenVpnThread().start();
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// User does not want us to start, so we just vanish
				OpenVPN.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled, 
						ConnectionStatus.LEVEL_NOTCONNECTED);
				return false;
			}
		}
		return true;
	}
	public void showLogWindow() {
	//	NetSee.pPrepareDialog.dismiss();
		Intent startLW = new Intent(pActivity,LogWindow.class);
		startLW.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		pActivity.startActivity(startLW);

		//Intent i = new Intent(getBaseContext(), TabTestActivity.class);
		//startActivity(i);
	}

	void showConfigErrorDialog(int vpnok) {
		AlertDialog.Builder d = new AlertDialog.Builder(pContext);
		d.setTitle(R.string.config_error_found);
		d.setMessage(vpnok);
		d.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		d.show();
	}

	void launchVPN () {
		int vpnok = mSelectedProfile.checkProfile(pContext);
		if(vpnok!= R.string.no_error_found) {
			showConfigErrorDialog(vpnok);
			return;
		}

		Intent intent = VpnService.prepare(pContext);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pContext);        
		boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
		boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

		if(loadTunModule)
			execeuteSUcmd("insmod /system/lib/modules/tun.ko");

		if(usecm9fix && !mCmfixed ) {
			execeuteSUcmd("chown system /dev/tun");
		}


		if (intent != null) {
			OpenVPN.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_password,
					ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
			// Start the query
			try {
				pActivity.startActivityForResult(intent, START_VPN_PROFILE);
			} catch (ActivityNotFoundException ane) {
				OpenVPN.logError(R.string.no_vpn_support_image);
				showLogWindow();
			}
		} else {
			onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
		}

	}

	private void execeuteSUcmd(String command) {
		ProcessBuilder pb = new ProcessBuilder("su","-c",command);
		try {
			Process p = pb.start();
			int ret = p.waitFor();
			if(ret ==0)
				mCmfixed=true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class startOpenVpnThread extends Thread {

		@Override
		public void run() {
			VPNLaunchHelper.startOpenVpn(mSelectedProfile, pContext);
		}
		

	}


}
