package adts;

import gps.GpsCoordinate;
import android.os.Parcel;
import android.os.Parcelable;

public class User {
	
	//user information
	public ProfilePictureProvider profilePictureProvider;
	public String firstName;
	public String lastName;
	public String database_id;
	public String linkedin_id;
	public String email;
	public GpsCoordinate gps_coord;
	
	//for use in lists
	public boolean isNetworkdUser = false;
	
	public User(String first_name, String last_name){
		  this.firstName=first_name;
		  this.lastName=last_name;
		  
		  this.profilePictureProvider=new ProfilePictureProvider("","");
		  this.database_id = "";
		  this.linkedin_id = "";
		  this.email = "";
		  this.gps_coord = new GpsCoordinate(0,0);
		  
		  if(database_id != null && database_id.compareTo("")!=0)
		  {
			  isNetworkdUser = true;
		  }
	}
	
	public User()
	{
		  this.profilePictureProvider=new ProfilePictureProvider("","");
		  this.firstName="";
		  this.lastName="";
		  this.database_id = "";
		  this.linkedin_id = "";
		  this.email = "";
		  this.gps_coord = new GpsCoordinate(0,0);
		  
		  if(database_id != null && database_id.compareTo("")!=0)
		  {
			  isNetworkdUser = true;
		  }
	}
	
	public User(String pictureUrl, String first_name, String last_name, String database_id, String linkedin_id, String email, GpsCoordinate gps_coord)
	{
		  this.profilePictureProvider=new ProfilePictureProvider(pictureUrl,linkedin_id);
		  this.firstName=first_name;
		  this.lastName=last_name;
		  this.database_id = database_id;
		  this.linkedin_id = linkedin_id;
		  this.email = email;
		  this.gps_coord = gps_coord;
		  
		  if(database_id != null && database_id.compareTo("")!=0)
		  {
			  isNetworkdUser = true;
		  }
			  
	}

	public boolean containsString(String in)
	{
		String fullInfoString = firstName + lastName + email;
		if(fullInfoString.toLowerCase().contains(in.toLowerCase())) return true;
		return false;
	}
	
	public boolean equals (User other)
	{
		if(other.linkedin_id.compareTo(this.linkedin_id)==0)return true;
		return false;
	}
	
	public String toString()
	{
		return firstName + " " + lastName + " " + linkedin_id + " " + email + " " + gps_coord;
		 
	}
}
