
package com.newonevpn.vpn;

import android.Manifest.permission;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Base64;
import android.widget.ArrayAdapter;

import com.dave.onevpnfresh.R;
import com.dave.newonevpn.model.Global;
import com.dave.newonevpn.model.ServerListInfo;
import com.newonevpn.vpn.core.ConfigParser;
import com.newonevpn.vpn.core.ConfigParser.ConfigParseError;
import com.newonevpn.vpn.core.ProfileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class ConfigConverter {

	public static final String IMPORT_PROFILE = "com.newonevpn.vpn.IMPORT_PROFILE";
	
	private VpnProfile mResult;
	private ArrayAdapter<String> mArrayAdapter;

	private List<String> mPathsegments;

	private String mAliasName=null;

	private ProgressDialog pDialog;
	private int RESULT_INSTALLPKCS12 = 7;
	private int RESULT_VPN_OK = 19;
	private String mPossibleName=null;
	
	private Activity pActivity;
	private Context pContext;
	private LaunchVPNManager pLaunchVPN;
	private boolean bResult;
	
	public ConfigConverter()
	{
	
	}
	public ConfigConverter(Activity activity, LaunchVPNManager launchVPN){
		pContext = activity.getBaseContext();
		pActivity = activity;
		pLaunchVPN = launchVPN;
	}
	
	
	public int getPort(int realPort){
		int resPort = realPort;
		int n = 0;
	//	if( NetSee.settings.getRandomPort() )
		{
			Random rand = new Random();
			n = rand.nextInt(100) + 1;
		}
		return resPort + n;
	}

	private String getFileContent(String targetFilePath){
		File file = new File(targetFilePath);

		try {
			FileInputStream fileInputStream = new FileInputStream(file);

			StringBuilder sb = null;
			while(fileInputStream.available() > 0) {
				if(null == sb)  sb = new StringBuilder();
				sb.append((char)fileInputStream.read());
			}

			String fileContent = null;
			if(null != sb){
				fileContent= sb.toString();
			}
			fileInputStream.close();

			return fileContent;
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getAssetsFile(String filename){
		StringBuilder buf=new StringBuilder();
		InputStream json= null;
		try {
			json = pActivity.getAssets().open(filename);
			BufferedReader in=
					new BufferedReader(new InputStreamReader(json, "UTF-8"));
			String str;

			while ((str=in.readLine()) != null) {
				buf.append(str + "\n");
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buf.toString();
	}
	public boolean StartConfigConverter(){
		String contents = "";
		if( Global.g_selectedServerInfo.protocol.equals("tcp") ){
			contents = getAssetsFile("tcp.ovpn");
		}else{
			contents = getAssetsFile("udp.ovpn");
		}
		contents = "\nremote " + Global.g_selectedServerInfo.dns + " " + Global.g_selectedServerInfo.port + "\n"
			+ "\nredirect-gateway def1\n" + contents;
		doImport(contents);

		mResult.mUsername = Global.sp.getString("email", "");
		mResult.mPassword = Global.sp.getString("password", "");

		Intent in = installPKCS12();
		saveProfile();


		VpnProfile profile = null;
		// delete
		Collection<VpnProfile> vpnlist = getPM().getProfiles();

		for (VpnProfile vpnProfile : vpnlist) {
			profile = vpnProfile;
		}
		startVPN(profile);
		return true;
	}
	public boolean StartConfigConverterWithServerListInfo(ServerListInfo info){

		String yourFilePath = info.path;
		String contentStr = getFileContent(yourFilePath);
		if( contentStr == null )	return false;

		String contents = contentStr +
				"\nredirect-gateway def1\n";
		//contents = contentStr;
		doImport(contents);

		SharedPreferences sharedPref = pActivity.getSharedPreferences("userinfo", 1);
		String swLogin = sharedPref.getString("setting_username", "");
		String swPassword = sharedPref.getString("setting_password", "");
		mResult.mUsername = swLogin;
		mResult.mPassword = swPassword;


		Intent in = installPKCS12();
		saveProfile();


		VpnProfile profile = null;
		// delete
		Collection<VpnProfile> vpnlist = getPM().getProfiles();

		for (VpnProfile vpnProfile : vpnlist) {
			profile = vpnProfile;
		}
		startVPN(profile);

		return true;
	}
	
	private void doImport(String contents) {
		//Toast.makeText(this, "DoImport", Toast.LENGTH_SHORT).show();
		
		ConfigParser cp = new ConfigParser();
		try {
			//String dd;
			//InputStream ss = new InputStream(dd);
			BufferedReader br=new BufferedReader(new StringReader(contents));
		    	
			
			cp.parseConfig(br);
			VpnProfile vp = cp.convertProfile();
			mResult = vp;
			embedFiles();
			displayWarnings();
			log(R.string.import_done);
			br.close();
			return;
			
			
		} catch (IOException e) {
			log(R.string.error_reading_config_file);
			
		} catch (ConfigParseError e) {
			log(R.string.error_reading_config_file);
						
		}
		mResult=null;

	}
	
	private void saveProfile() {
		Intent result = new Intent();
		ProfileManager vpl = ProfileManager.getInstance(pContext);
		vpl.removeAllProfile();
		setUniqueProfileName(vpl);
		
		vpl.addProfile(mResult);
		vpl.saveProfile(pContext, mResult);
		vpl.saveProfileList(pContext);
		result.putExtra(VpnProfile.EXTRA_PROFILEUUID,mResult.getUUID().toString());
		pActivity.setResult(Activity.RESULT_OK, result);
		//finish();
	}

	private Intent installPKCS12() {
		
			setAuthTypeToEmbeddedPKCS12();
			return null;
	}



	private void setAuthTypeToEmbeddedPKCS12() {
		if(mResult.mPKCS12Filename!=null && mResult.mPKCS12Filename.startsWith(VpnProfile.INLINE_TAG)) {
			if(mResult.mAuthenticationType==VpnProfile.TYPE_USERPASS_KEYSTORE)
				mResult.mAuthenticationType=VpnProfile.TYPE_USERPASS_PKCS12;
			
			if(mResult.mAuthenticationType==VpnProfile.TYPE_KEYSTORE)
				mResult.mAuthenticationType=VpnProfile.TYPE_PKCS12;
			
		}
	}





	private void setUniqueProfileName(ProfileManager vpl) {
		String newname = mPossibleName;

		// 	Default to 
		if(mResult.mName!=null && !ConfigParser.CONVERTED_PROFILE.equals(mResult.mName))
			newname=mResult.mName;
			
		
		mResult.mName=newname;
	}

	
	private String embedFile(String filename) {
		return embedFile(filename, false);		
	}

	private String embedFile(String filename, boolean base64encode)
	{
		if(filename==null)
			return null;

		// Already embedded, nothing to do
		if(filename.startsWith(VpnProfile.INLINE_TAG))
			return filename;

		File possibleFile = findFile(filename);
		if(possibleFile==null)
			return filename;
		else
			return readFileContent(possibleFile,base64encode);

	}

	private File findFile(String filename) {
		File foundfile =findFileRaw(filename);
		
		if (foundfile==null && filename!=null && !filename.equals(""))
			log(R.string.import_could_not_open,filename);

		return foundfile;
	}



	private File findFileRaw(String filename)
	{
		if(filename == null || filename.equals(""))
			return null;

		// Try diffent path relative to /mnt/sdcard
		File sdcard = Environment.getExternalStorageDirectory();
		File root = new File("/");

		Vector<File> dirlist = new Vector<File>();

		for(int i=mPathsegments.size()-1;i >=0 ;i--){
			String path = "";
			for (int j = 0;j<=i;j++) {
				path += "/" + mPathsegments.get(j);
			}
			dirlist.add(new File(path));
		}
		dirlist.add(sdcard);
		dirlist.add(root);


		String[] fileparts = filename.split("/");
		for(File rootdir:dirlist){
			String suffix="";
			for(int i=fileparts.length-1; i >=0;i--) {
				if(i==fileparts.length-1)
					suffix = fileparts[i];
				else
					suffix = fileparts[i] + "/" + suffix;

				File possibleFile = new File(rootdir,suffix);
				if(!possibleFile.canRead())
					continue;

				// read the file inline
				return possibleFile;

			}
		}
		return null;
	}

	String readFileContent(File possibleFile, boolean base64encode) {
		byte [] filedata;
		try {
			filedata = readBytesFromFile(possibleFile);
		} catch (IOException e) {
			return null;
		}
		
		String data;
		if(base64encode) {
			data = Base64.encodeToString(filedata, Base64.DEFAULT);
		} else {
			data = new String(filedata);

		}
		return VpnProfile.INLINE_TAG + data;
		
	}


	private byte[] readBytesFromFile(File file) throws IOException {
		InputStream input = new FileInputStream(file);

		long len= file.length();


		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) len];

		// Read in the bytes
		int offset = 0;
		int bytesRead = 0;
		while (offset < bytes.length
				&& (bytesRead=input.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += bytesRead;
		}

		input.close();
		return bytes;
	}

	void embedFiles() {
		// This where I would like to have a c++ style
		// void embedFile(std::string & option)

		if (mResult.mPKCS12Filename!=null) {
			File pkcs12file = findFileRaw(mResult.mPKCS12Filename);
			if(pkcs12file!=null) {
				mAliasName = pkcs12file.getName().replace(".p12", "");
			} else {
				mAliasName = "Imported PKCS12";
			}
		}
			
		
		mResult.mCaFilename = embedFile(mResult.mCaFilename);
		mResult.mClientCertFilename = embedFile(mResult.mClientCertFilename);
		mResult.mClientKeyFilename = embedFile(mResult.mClientKeyFilename);
		mResult.mTLSAuthFilename = embedFile(mResult.mTLSAuthFilename);
		mResult.mPKCS12Filename = embedFile(mResult.mPKCS12Filename,true);
		

		if(mResult.mUsername == null && mResult.mPassword != null ){
			String data =embedFile(mResult.mPassword);
			ConfigParser.useEmbbedUserAuth(mResult, data);			
		}
	}

	
	
	
	private void doSendBroadcast() {
		Intent vpnstatus = new Intent();
		vpnstatus.setAction("com.newonevpn.vpn.VPN_STATUS");
		vpnstatus.putExtra("status", "VPNConnect Failed");
		vpnstatus.putExtra("detailstatus", "loading profile failed");
		pContext.sendBroadcast(vpnstatus, permission.ACCESS_NETWORK_STATE);
	}
	private void startVPN(VpnProfile profile) {
		
		getPM().saveProfile(pActivity.getBaseContext(), profile);
	//	DashBoard.bCancelButton = true;
		pLaunchVPN.StartLanchVPN(Intent.ACTION_MAIN, profile.getUUID().toString(), null);
		//Intent intent = new Intent(pActivity.getBaseContext(),LaunchVPN.class);
		//intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
		//intent.setAction(Intent.ACTION_MAIN);
		
	//	pActivity.startActivityForResult(intent, RESULT_VPN_OK);
	}
	
	private ProfileManager getPM() {
		return ProfileManager.getInstance(pActivity.getBaseContext());
	}

	

	private void displayWarnings() {
		if(mResult.mUseCustomConfig) {
			log(R.string.import_warning_custom_options);
			String copt = mResult.mCustomConfigOptions;
			if(copt.startsWith("#")) {
				int until = copt.indexOf('\n');
				copt = copt.substring(until+1);
			}
		}


	}

	private void log(int ressourceId, Object... formatArgs) {
		//log(getString(ressourceId,formatArgs));
	}
}
