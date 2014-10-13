package services;

import gps.GpsCoordinate;
import gps.GpsManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import jsonengine.JSONStruct;
import networkddatabaseapi.DataBaseApiWrapper;
import networkddatabaseapi.DataBaseApiWrapper.DataBaseApiWrapperListener;

import org.json.JSONObject;

import preferences.PreferencesHandler;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class NetworkdService extends Service implements DataBaseApiWrapperListener  {
	private static final String TAG = "NetworkdService";
	
	private Timer GpsUpdateTimer;
	private GpsManager gpsManager;
	private static NetworkdService instance = null;
	public static final int GPS_UPDATE_INTERVAL = 10000;
	private DataBaseApiWrapper dataBaseApiWrapper;
	
		@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public static boolean isInstanceCreated() { 
	      if(NetworkdService.instance == null)
	      {
	    	  return false;
	      }
	      return true;
	}
	
	@Override
	public void onCreate() {
		instance = this;
		Log.d(TAG,"Service Oncreate");
		
		Toast.makeText(getBaseContext(),"Networkd tracking is on",Toast.LENGTH_LONG).show();
		
		final PreferencesHandler prefsHandler = new PreferencesHandler(this);
		String apiKey = prefsHandler.getDataBaseApiKey();
		dataBaseApiWrapper = new DataBaseApiWrapper( this, getBaseContext(),true);
		
		gpsManager = new GpsManager(this);
		
		GpsUpdateTimer = new Timer();
	    GpsUpdateTimer.schedule(new TimerTask() {          
	        @Override
	        public void run() {
	        	updateGpsCoordinates();
	        }

	    }, 0, GPS_UPDATE_INTERVAL);
	    
	    //incomingMessagesListener = new IncomingMessagesListener(this);
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG,"stop");
		Toast.makeText(getBaseContext(),"Networkd tracking is off",Toast.LENGTH_LONG).show();
		instance = null;
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		instance = this;
		Log.d(TAG,"start");
	}
	
	private void updateGpsCoordinates()
	{
		if(!isInstanceCreated())return;
		//update GPS on DB
		Log.d(TAG, "Updating GPS");
		//get fake data for now
		GpsCoordinate gps_coord = gpsManager.getLocation();
		dataBaseApiWrapper.setSpinnerOn = false;
		dataBaseApiWrapper.updateUserGpsPosition(gps_coord);
    	Log.d(TAG, "Finished Updating GPS");
	}

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		// TODO Auto-generated method stub
		
	}
}