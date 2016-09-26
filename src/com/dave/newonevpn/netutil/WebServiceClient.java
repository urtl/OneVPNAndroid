package com.dave.newonevpn.netutil;

import android.content.Context;
import android.text.format.DateUtils;


import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


public class WebServiceClient {

	private Context mContext = null;
	private String baseURL = "";
	private static int some_reasonable_timeout = (int) (180 * DateUtils.SECOND_IN_MILLIS);
		
	public WebServiceClient(Context context){
		mContext = context;
		//baseURL = mContext.getResources().getString(R.string.base_url);
	}
	
	public File downloadFileFromServer(String filepath){

		URL url = null;
		HttpURLConnection httpConn = null;
		String fileURL = filepath.trim();
		File out_File = null;
		try{
			url = new URL(fileURL);
			//Logger.v("File URL --> ", fileURL);
			
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new MyTrustManager() }, new SecureRandom());

			//HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			//HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
			httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setDoInput(true);
			httpConn.connect();
			
	        int responseCode = httpConn.getResponseCode();
	        if(responseCode == HttpURLConnection.HTTP_OK){
	            String fileName = null;
	            String disposition = httpConn.getHeaderField("Content-Disposition");
	            String contentType = httpConn.getContentType();
	            int contentLength = httpConn.getContentLength();
	            if(disposition != null){
	                // extracts file name from header field
	                int index = disposition.indexOf("filename=");
	                if(index > 0){
	                    fileName = disposition.substring(index + 10, disposition.length() - 1);
	                }
	            }else{
	                // extracts file name from URL
	                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
	            }
	            if(contentType != null){
	            	//Logger.v("Content-Type = ", contentType);
	            }
	            if(disposition != null){
	            	//Logger.v("Content-Disposition = ", disposition);
	            }
	            if(fileName != null){
	            	//Logger.v("fileName = ", fileName);
	            }
	            out_File = new File("" + File.separator + fileName); //StaticData.APP_img_dir + File.separator + fileName);
				if(out_File.exists()){
					out_File.delete();
				}
				try{
					out_File.createNewFile();
				}catch(IOException e){
				//	Logger.e("IOException ==> ", e.toString());
				}
				if(out_File.exists()){
					 // opens input stream from the HTTP connection
		            InputStream inputStream = httpConn.getInputStream();
		            // opens an output stream to save into file
		            FileOutputStream file_outputStream = new FileOutputStream(out_File);
		            
		            if(contentLength  != -1){
		            	int bytesRead = -1;
			            byte[] buffer = new byte[contentLength];
			            while ((bytesRead = inputStream.read(buffer)) != -1){
			            	file_outputStream.write(buffer, 0, bytesRead);
			            }
		            }else{
		            //	AppUtils.CopyStream(inputStream, file_outputStream);
		            }
		            
		            file_outputStream.close();
		            inputStream.close();
				}
				//Logger.v("File downloaded", responseCode + "");
	        }else{
	        	//Logger.v("Failed to download file", responseCode + "");
	        }
	        httpConn.disconnect();       
		}catch(Exception e){
			//Logger.e("Exception --> ", e.toString());
			return null;
		}
		return out_File;
	}
		
	public String sendDataToServer(String url){

		String responseBody = "";
		try{
			String finalURL =  baseURL + url;
			//Logger.v("REQUEST ", finalURL);
			
			HttpClient httpclient = getHttpClient();
			HttpPost httppost = new HttpPost(finalURL);
			HttpResponse response = httpclient.execute(httppost);
			responseBody = EntityUtils.toString(response.getEntity());
			//Logger.v("SERVER RESPONSE", responseBody);

		}catch(Exception e){
			//Logger.e("Exception ==> ", e.toString());
			return "";
		}
		if(responseBody == null || responseBody.equalsIgnoreCase("null")){
			return "";
		}else{
			return responseBody;
		}
	}


	public String sendDataToServer(String url, ArrayList<NameValuePair> PostParamsValue){

		String responseBody = "";
		try{
			String finalURL =  baseURL + url;
			//Logger.v("REQUEST ", finalURL + " " + PostParamsValue);

			HttpClient httpclient = getHttpClient();
			HttpPost httppost = new HttpPost(finalURL);

			if(PostParamsValue!=null){
				logParams(PostParamsValue);
				httppost.setEntity(new UrlEncodedFormEntity(PostParamsValue, "UTF-8"));
			}
			HttpResponse response = httpclient.execute(httppost);
			responseBody = EntityUtils.toString(response.getEntity());
		
			//Logger.v("SERVER RESPONSE", responseBody);

		}catch(Exception e){
			//Logger.e("Exception ==> ", e.toString());
			return "";
		}
		if(responseBody == null || responseBody.equalsIgnoreCase("null")){
			return "";
		}else{
			return responseBody;
		}
	}

	public String sendDataToServerGet(String url, ArrayList<NameValuePair> PostParamsValue){

		String responseBody = "";
		try{
			String finalURL =  baseURL + url;
			//Logger.v("REQUEST ", finalURL + " " + PostParamsValue);

			HttpClient httpclient = getHttpClient();
			if(PostParamsValue!=null){
				logParams(PostParamsValue);
				//httppost.set(new UrlEncodedFormEntity(PostParamsValue, "UTF-8"));
				String paramString = URLEncodedUtils.format(PostParamsValue, "utf-8");
				finalURL = finalURL + "?" + paramString;
			}

			HttpGet httppost = new HttpGet(finalURL);
			HttpResponse response = httpclient.execute(httppost);
			responseBody = EntityUtils.toString(response.getEntity());

			//Logger.v("SERVER RESPONSE", responseBody);

		}catch(Exception e){
			//Logger.e("Exception ==> ", e.toString());
			return "";
		}
		if(responseBody == null || responseBody.equalsIgnoreCase("null")){
			return "";
		}else{
			return responseBody;
		}
	}
	private static void logParams(List<NameValuePair> nameValuePairs) {
		for (NameValuePair nameValuePair : nameValuePairs) {
			//Logger.d(nameValuePair.getName(), nameValuePair.getValue());
		}
	}
	
	public HttpClient getNewHttpClient(){
		try{
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		}catch(Exception e){
			return new DefaultHttpClient();
		}
	}

	private static HttpClient getHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams,
				some_reasonable_timeout);
		HttpConnectionParams.setSoTimeout(httpParams, some_reasonable_timeout);
		HttpClient httpclient = new DefaultHttpClient(httpParams);
		return httpclient;
	}
	
}
