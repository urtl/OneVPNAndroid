package com.dave.newonevpn.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigAPI extends AsyncTask<Void, Void, Void> {

	Context loginAct;
	private ProgressDialog progressDialog = null;
	String url;
	String contents;
	OnTaskCompleted listener;
	public ConfigAPI(Context act, OnTaskCompleted listener){
		this.loginAct = act;
		this.listener = listener;
	}

	public interface OnTaskCompleted{
		void onTaskCompleted(String response);
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
	//	progressDialog.setMessage("Getting Configuration... ");
	//	progressDialog.show();
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
				contents = total.toString();
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
		listener.onTaskCompleted(contents);
	}
}
