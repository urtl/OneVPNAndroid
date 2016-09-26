package com.dave.newonevpn.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.dave.newonevpn.model.ServerInfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ServerAPI extends AsyncTask<Void, Void, Void> {

	Context loginAct;
	private ProgressDialog progressDialog = null;
	String url;
	String contents;
	OnTaskCompleted listener;
	ArrayList<ServerInfo> res = new ArrayList<>();
	public ServerAPI(Context act, OnTaskCompleted listener){
		this.loginAct = act;
		this.listener = listener;
	}

	public interface OnTaskCompleted{
		void onTaskCompleted(ArrayList<ServerInfo> response);
	}

	public void setURL(String url){
		this.url = url;
	}
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	//	progressDialog = new ProgressDialog(loginAct);
	//	progressDialog.setTitle("Please Wait");
	//	progressDialog.setMessage("Getting Servers... ");
//		progressDialog.show();
	}
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
																				// Limit
		HttpResponse response;
	//	JSONObject json = new JSONObject();
		try {
			
			url = url.replaceAll(" ", "%20");
			HttpGet httpGet = new HttpGet(url);

			response = client.execute(httpGet);

			if (response != null) {
				InputStream in = response.getEntity().getContent();
				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					total.append(line);
				}
				final JSONArray jsonObjs = new JSONArray(total.toString());
				for(int i = 0; i < jsonObjs.length(); i++){
					JSONObject objItem = jsonObjs.getJSONObject(i);
					if (objItem.getString("protocol").equals("OpenVPN") == false )
						continue;

					ServerInfo info = new ServerInfo();
					info.hostname = objItem.getString("hostname");
					info.name = objItem.getString("name");
					info.ipaddress = objItem.getString("ipaddress");
					info.location = objItem.getString("location");
					info.topology = objItem.getString("topology");
					info.protocol = objItem.getString("protocol");
					info.region = objItem.getString("region");
					info.countryName = objItem.getString("countryName");
					info.countryCode = objItem.getString("countryCode");
					res.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	//	progressDialog.dismiss();
		listener.onTaskCompleted(res);
	}
}
