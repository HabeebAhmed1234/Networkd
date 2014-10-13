package gps;

import android.util.Log;

public class GpsCoordinate {
		public static final String TAG = "GpsCoordinate";
		
		private double latitude;
		private double longditude;
		
		public GpsCoordinate(double latitude, double longditude)
		{
			this.latitude = latitude;
			this.longditude = longditude;
		}
		
		public GpsCoordinate(String input)
		{
			Log.d(this.TAG, "parsing string input"+input);
			if(input.compareTo("null")==0||input == null)
			{
				this.latitude = 0;
				this.longditude = 0;
				return;
			}
			String coords[] = input.split(","); 
			this.latitude = Double.parseDouble(coords[0].trim());
			this.longditude = Double.parseDouble(coords[1].trim());
		}
		public void setlatitude(double latitude)
		{
			this.latitude = latitude;
		}
		
		public void setlongditude(double longditude)
		{
			this.longditude = longditude;
		}
		
		public double getlatitude()
		{
			return this.latitude;
		}
		
		public double getlongditude()
		{
			return this.longditude;
		}
		
		public double getDistance(GpsCoordinate other)
		{
			double distance = 0;
			//2 is this 1 is other
			double R = 6371; // rad of earth km
			double dLat = Math.toRadians(this.latitude-other.latitude);
			double dLon = Math.toRadians(this.longditude-other.longditude);
			double lat1 = Math.toRadians(other.latitude);
			double lat2 = Math.toRadians(this.latitude);
			double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
			        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
			return R * c;
		}
		
		public String toString()
		{
			return Double.toString(this.latitude)+","+Double.toString(this.longditude);
		}
}
