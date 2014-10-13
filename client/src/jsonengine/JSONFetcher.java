/*
 * Created 12/11/13
 * Author: Bo Yin (bo@uwmobile.ca)
 */
package jsonengine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import networkddatabaseapi.DataBaseApiWrapper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/*
 * Async task that fetches a JSON object from HTTP.
 */
public class JSONFetcher extends AsyncTask<String, Boolean, JSONStruct> {
	public interface JSONFetcherOnCompleteListener {
		public void onJsonFetcherComplete(JSONStruct object, boolean success);
	}
	
	public static final int TYPE_POST = 0;
	public static final int TYPE_GET = 1;
	public static final int TYPE_DELETE = 2;
	public static final int TYPE_PUT = 3;
	public static final int HTTP_REQUEST_TIMEOUT_IN_MILLIS = 30000;
	
	public static int fetcherType = 1;
	
	ProgressDialog progDailog;
	private Context mContext;
	private HttpClient httpClient;
	
	private static final String TAG = "DataBaseApiWrapper/JsonFetcher";// debug/log tag

	private JSONFetcherOnCompleteListener listener_;

	public JSONFetcher(JSONFetcherOnCompleteListener listener, Context ctx, int type) {
		listener_ = listener;
		mContext = ctx;
		
		final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_REQUEST_TIMEOUT_IN_MILLIS);
		httpClient = new DefaultHttpClient(httpParams);
		
		this.fetcherType = type;
	}

	private String requestJsonData(String Url)throws ClientProtocolException, IOException {
		Url = "http://"+Url;
		Log.d(this.TAG,"request url is "+ Url);
		if(httpClient == null) Log.d(this.TAG,"!!httpClient is null!!");
		HttpResponse response = null;
		
		if(fetcherType == this.TYPE_POST) 
		{	
			Log.d(TAG,"requestJsonData: making http POST request");
			HttpPost httpRequest = new HttpPost(Url);
			response = httpClient.execute(httpRequest);
		}
		if(fetcherType == this.TYPE_GET) 
		{
			Log.d(TAG,"requestJsonData: making http GET request");
			HttpGet httpRequest = new HttpGet(Url);
			response = httpClient.execute(httpRequest);
		}
		if(fetcherType == this.TYPE_DELETE) 
		{
			Log.d(TAG,"requestJsonData: making http DELETE request");
			HttpDelete httpRequest = new HttpDelete(Url);
			response = httpClient.execute(httpRequest);
		}
		if(fetcherType == this.TYPE_PUT) 
		{
			Log.d(TAG,"requestJsonData: making http PUT request");
			HttpPut httpRequest = new HttpPut(Url);
			response = httpClient.execute(httpRequest);
		}
		
		StatusLine statusLine = response.getStatusLine();

		String jsonString = null;
		Log.d(TAG, "requestJsonData: response HttpStatus = "+statusLine.toString());
		if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			jsonString = out.toString();
		} else {
			response.getEntity().getContent().close();
			throw new IOException(statusLine.getReasonPhrase());
			//return null;
		}
		return jsonString;
	}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(DataBaseApiWrapper.setSpinnerOn == false) return;
        progDailog = new ProgressDialog(mContext);
        progDailog.setMessage("Loading...");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(false);
        progDailog.show();
    }
	
	@Override
	protected JSONStruct doInBackground(String... params) {
		try {
			String jsonString = requestJsonData(params[0]);
			if(jsonString == null || jsonString.compareTo("")==0) return null;
			JSONStruct struct = new JSONStruct();
			
			if(jsonString.charAt(0)=='[')
			{
				struct.isObject = false;
				struct.array = new JSONArray(jsonString);
			}else{
				struct.isObject = true;
				struct.object = new JSONObject(jsonString);
			}
			
			return struct;
		} catch (ClientProtocolException e) {
			Log.d(TAG, e.toString());
			return null;
		} catch (IOException e) {
			Log.d(TAG, e.toString());
			return null;
		} catch (JSONException e) {
			Log.d(TAG, e.toString());
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(JSONStruct result) {
		if (result == null) {
			Log.d(TAG,"onPostExecute: http request result was null");
		    if(DataBaseApiWrapper.setSpinnerOn && progDailog!=null)progDailog.dismiss();
			listener_.onJsonFetcherComplete(null, false);
		} else {
			if(DataBaseApiWrapper.setSpinnerOn && progDailog!=null) progDailog.dismiss();
			listener_.onJsonFetcherComplete(result, true);
		}
	}
}
