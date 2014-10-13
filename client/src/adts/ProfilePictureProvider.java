package adts;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.code.linkedinapi.client.LinkedInApiClientException;
import com.google.code.linkedinapi.schema.Person;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;


public class ProfilePictureProvider  {
	public static final String TAG = "ProfilePictureProvider";
	private URL url = null;
	public Bitmap bmp = null;
	private String userid = "";
	
	public interface OnProfilePictureBitmapRecieved
	{
		public void onProfilePictureBitmapRecieved();
	}
	
	public ProfilePictureProvider(String urlString,String userid )
	{
		this.userid = userid;
		try {
			if(urlString!=null && !urlString.isEmpty()) this.url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 
	}
	
	public void setProfilePictureUrl(String urlString){
		try {
			if(urlString!=null) this.url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 
	}
	
	public void requestBitmap(final OnProfilePictureBitmapRecieved caller)
	{
		if(url==null){
			Log.d(TAG,this.userid+" has a null url");
			return;
		}
		if(bmp!=null){
			caller.onProfilePictureBitmapRecieved();
			return;
		}
		new AsyncTask<Void, Void, Object>() {
		    @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		    }
			
			@Override
			protected Object doInBackground(Void... params) {
				try {
					Bitmap bmp = null;
					if(url!=null)bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
					return bmp;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				} 
			} 
			@Override
			protected void onPostExecute(Object result) {
				if (result instanceof Exception) {
					final Exception ex = (Exception) result;
				} else if (result instanceof Bitmap) {
					//profilePictureRecievedListener.onprofilePictureRecieved(id,(Bitmap)result);
					bmp =(Bitmap)result;
					caller.onProfilePictureBitmapRecieved();
				}
			}
		}.execute();
	}
}