package adts;

import gps.GpsCoordinate;

import java.util.Date;

import android.text.format.Time;

public class Event {
	public User admin;
	public String title;
	public Date start_date;
	public Date end_date;
	public Time start_time; 
	public Time end_time;
	public String address;
	public GpsCoordinate gps;
	
	Event(User admin,String title,Date start_date,Date end_date,Time start_time, Time end_time,String address,GpsCoordinate gps)
	{
		this.admin = admin;
		this.title=title;
		this.start_date=start_date;
		this.end_date=end_date;
		this.start_time=start_time; 
		this.end_time=end_time;
		this.address=address;
		this.gps=gps;
	}
}
