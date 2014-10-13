package gps;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

public class GpsManager {
	
	private Context context;
	
	LocationManager locationManager;
	
	public GpsManager(Context context)
	{
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
	}
	
	public boolean isServiceEnabled()
	{
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
		        return false;
		}
		return true;
	}
	
	public GpsCoordinate getLocation()
    {
		 Criteria criteria = new Criteria();
		 String bestProvider = null;
		 if(locationManager!=null)
		 {
		 	bestProvider = locationManager.getBestProvider(criteria, false);
		 }else{
			 return new GpsCoordinate(0,0);
		 }
		 if(bestProvider == null)
		 {
			 return new GpsCoordinate(0,0);
		 }
		 Location location = locationManager.getLastKnownLocation(bestProvider);
		 if(location==null) 
		 {
		 	return new GpsCoordinate(0,0);
		 }
		 Double lat,lon;
		 try {
		   lat = location.getLatitude ();
		   lon = location.getLongitude ();
		   return new GpsCoordinate(lat, lon);
		 }
		 catch (NullPointerException e){
		     e.printStackTrace();
		   return new GpsCoordinate(0,0);
		 }
    }
	
}
