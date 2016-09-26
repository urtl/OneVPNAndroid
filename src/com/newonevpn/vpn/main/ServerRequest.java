package com.newonevpn.vpn.main;




///////////////////////////
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ServerRequest {

	
	  InputStream is = null;
	     JSONObject jObj = null;
	     String json = "";
	    String charset;
		String result = "";
	    // constructor
	    public ServerRequest() {
	 
	    }
	 
	    // function get json from url
	    // by making HTTP POST or GET mehtod
	    public String makeHttpRequest(String url, String method,
	            List<NameValuePair> params) {
	    	
		
	   			
	   			
	        // Making HTTP request
	        try {
	 
	            // check for request method
	            if(method == "POST"){
	                // request method is POST
	                // defaultHttpClient
	                DefaultHttpClient httpClient = new DefaultHttpClient();
		   			HttpParams paramss = httpClient.getParams();
		   			HttpConnectionParams.setConnectionTimeout(paramss, 5000);
		   			HttpConnectionParams.setSoTimeout(paramss, 5000);
		   			
	                
	                HttpPost httpPost = new HttpPost(url);
	                httpPost.setEntity(new UrlEncodedFormEntity(params));
	 
	                HttpResponse httpResponse = httpClient.execute(httpPost);
	                HttpEntity httpEntity = httpResponse.getEntity();
	                is = httpEntity.getContent();
	 
	            }else if(method == "GET"){
	                // request method is GET	            
	                
	                DefaultHttpClient httpClient = new DefaultHttpClient();
		   			HttpParams paramss = httpClient.getParams();
		   			HttpConnectionParams.setConnectionTimeout(paramss, 3000);
		   			HttpConnectionParams.setSoTimeout(paramss,3000);
		   			
		   			
	                String paramString = URLEncodedUtils.format(params, "utf-8");
	                if( paramString.length() > 0 )
	                url += "?" + paramString;
	                HttpGet httpGet = new HttpGet(url);
	 
	 
	                HttpResponse httpResponse = httpClient.execute(httpGet);
	                if(httpResponse.getStatusLine().getStatusCode() != 200 )
	                	return null;
	                HttpEntity httpEntity = httpResponse.getEntity();
	                is = httpEntity.getContent();
	                
	                 charset = getContentCharSet(httpEntity);
	                 
	                 
	                 Header[] headers = httpResponse.getAllHeaders();
	             	for (Header header : headers) {
	             		Log.d("stat","Key : " + header.getName() 
	                                        + " ,Value : " + header.getValue());
	              
	             	}

	             	System.out.println("\nGet Response Header By Key ...\n");
	             	String server = httpResponse.getFirstHeader("Server").getValue();
	             	Log.d("server", server);
	             
	             	///////////////////////////////////////////////////////////////////////////////

	            }           
	 
	        } catch (UnsupportedEncodingException e) {
	        	Log.d("unsupporetedencoding", "erro");
	            e.printStackTrace();
	            return null;
	        } catch (ClientProtocolException e) {
	        	Log.d("clientprotocalexception", "erro");
	        	e.printStackTrace();
	        	return null;
	        } catch (IOException e) {
	        	Log.d("ioexception", "erro");
	            e.printStackTrace();
	            return null;
	        }
	 
	        try {
	          //  BufferedReader reader = new BufferedReader(new InputStreamReader(
	           //         is, "iso-8859-1"), 8);
	          //  BufferedReader reader = new BufferedReader(new InputStreamReader(
	            //        is, "iso-8859-1"),16);
	            
	        	
	        	
	        	if (charset == null) {

	        		charset = HTTP.UTF_8;

	        		}

	        	
	        	
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
	                    is, charset));
	            
	            
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line);
	            }
	            is.close();
	            String text = sb.toString();
	            if( text.length() == 0 )
	            	return null;
	            text = text.replace("\uFEFF", "");
	            json = Crypto3DES.Decrypte(text);
	            
	            Log.d("display", json);
	           
	        } catch (Exception e) {
	            Log.e("Buffer Error", "Error converting result " + e.toString());
	            Log.d("buffer erro", e.toString());
	            return null;
	        }
	 
	        return json;
	    }
	    
	    
	    
	 
		public String getContentCharSet(final HttpEntity entity) throws ParseException {

		    	if (entity == null) { throw new IllegalArgumentException("HTTP entity may not be null"); }
	
		    	String charset = null;
	
		    	if (entity.getContentType() != null) {
	
		    	HeaderElement values[] = entity.getContentType().getElements();
	
		    	if (values.length > 0) {
	
		    	NameValuePair param = values[0].getParameterByName("charset");
	
		    	if (param != null) {
	
		    	charset = param.getValue();
	
		    	}
	
		    	}
	
		    	}
	
		    	return charset;

	    	}

	    

		
		private String ConvertString(String responseString){
			int nIdx = 0;
			String result = "";
			int nReLen = 0;
			for(; nIdx < responseString.length(); nIdx++)
			{
				if (responseString.charAt(nIdx) != 0 && responseString.charAt(nIdx) != 92) 
					result = result + responseString.charAt(nIdx);
			}
			
			return result;
		}
		
		
		
	    
	    
}
