package homepage;     

import findlocalusers.FindLocalUsersActivity;
import gps.GpsCoordinate;

import java.io.IOException;     
import java.net.MalformedURLException;     
import java.net.URL;     
import java.util.ArrayList;
import java.util.List;     

import jsonengine.JSONStruct;
import linkedinapi.LinkedinApiWrapper;
import linkedinapi.LinkedinApiWrapperListener;
import linkedinapi.LinkedinApiWrapperResult;
import linkedinconnections.ConnectionsManagerActivity;
import messaging.ConversationManagerActivity;
import networkddatabaseapi.DataBaseApiWrapper;
import networkddatabaseapi.DataBaseApiWrapper.DataBaseApiWrapperListener;

import org.json.JSONObject;     

import preferences.PreferencesHandler;
import settings.SettingsActivity;
import shortlist.ShortListManagerActivity;
import adts.ProfilePictureProvider;
import adts.User;
import adts.ProfilePictureProvider.OnProfilePictureBitmapRecieved;
import android.app.Activity;     
import android.content.Intent;     
import android.content.SharedPreferences;     
import android.graphics.Bitmap;     
import android.graphics.BitmapFactory;     
import android.location.LocationManager;
import android.net.Uri;     
import android.os.AsyncTask;     
import android.os.Bundle;     
import android.provider.Settings;
import android.util.Log;     
import android.view.View;     
import android.view.View.OnClickListener;     
import android.view.Window;     
import android.view.WindowManager;     
import android.widget.Button;     
import android.widget.CompoundButton;     
import android.widget.CompoundButton.OnCheckedChangeListener;     
import android.widget.ImageButton;     
import android.widget.ImageView;     
import android.widget.LinearLayout;
import android.widget.TextView;     
import android.widget.Toast;     
import android.widget.ToggleButton;     

import com.google.code.linkedinapi.client.LinkedInApiClient;     
import com.google.code.linkedinapi.client.LinkedInApiClientException;     
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;     
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;     
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;     
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;     
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;     
import com.google.code.linkedinapi.schema.Location;     
import com.google.code.linkedinapi.schema.Person;     
import com.google.code.linkedinapi.schema.Skill;     
import com.google.code.linkedinapi.schema.Skills;     
import com.networkd.R;
import com.networkd.R.id;
import com.networkd.R.layout;

import contacts.ContactCardManagerActivity;

public class MainActivity extends Activity implements OnClickListener,DataBaseApiWrapperListener,LinkedinApiWrapperListener,OnProfilePictureBitmapRecieved {
	public static final String TAG = "MainActivity";     
	TextView  greeting;  
	
	ImageView profilePic;     
	
	LinearLayout findLocalProfessionalsButton, settingsButton, shortListButton, connectionsButton, messagingButon,contactCardsButton;    
	
	LinkedinApiWrapper linkedinApiWrapper;
	
	DataBaseApiWrapper dataBaseApiWrapper;
    
	private User user;

		@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);     
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   
		
		setContentView(R.layout.main);     
		
		greeting = (TextView) findViewById(R.id.greeting);        
		profilePic = (ImageView) findViewById(R.id.profilepic);     
		
		//set up buttons
		findLocalProfessionalsButton = (LinearLayout) findViewById(R.id.findlocalusersbutton);     
		findLocalProfessionalsButton.setOnClickListener(this);     
		
		shortListButton = (LinearLayout)findViewById(R.id.shortlistbutton);     
		shortListButton.setOnClickListener(this);
		
		connectionsButton = (LinearLayout)findViewById(R.id.connectionsbutton);     
		connectionsButton.setOnClickListener(this);
		
		messagingButon = (LinearLayout)findViewById(R.id.messagingbutton);     
		messagingButon.setOnClickListener(this);
		
		contactCardsButton = (LinearLayout)findViewById(R.id.contactcardsinventorybutton);     
		contactCardsButton.setOnClickListener(this);
		
		settingsButton = (LinearLayout)findViewById(R.id.settingsbutton);     
		settingsButton.setOnClickListener(this);
		
		PreferencesHandler prefsHandler = new PreferencesHandler(this);
		String apiKey = prefsHandler.getDataBaseApiKey();
		dataBaseApiWrapper = new DataBaseApiWrapper(this,this,true);
		
		linkedinApiWrapper = new LinkedinApiWrapper(this,this);
		linkedinApiWrapper.getCurrentUserInfo();  
	}
	
	private void updateProfileViews(User user)
	{
		if(user == null) return;     
		
		//request profile bitmap
		user.profilePictureProvider.requestBitmap(this);
		
		if(user.firstName!=null && user.lastName!=null)greeting.setText("Welcome "+user.firstName+" "+user.lastName+"!");
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.findlocalusersbutton)
		{
			Intent intent = new Intent(this, FindLocalUsersActivity.class);     
		    startActivity(intent);     
		}
		
		if(v.getId() == R.id.shortlistbutton)
		{
			Intent intent = new Intent(this, ShortListManagerActivity.class);     
		    startActivity(intent);     
		}
		
		if(v.getId() == R.id.connectionsbutton)
		{
			Intent intent = new Intent(this, ConnectionsManagerActivity.class);     
		    startActivity(intent);     
		}
		
		if(v.getId() == R.id.messagingbutton)
		{
			Intent intent = new Intent(this, ConversationManagerActivity.class);     
		    startActivity(intent);  
		}
		
		if(v.getId() == R.id.contactcardsinventorybutton)
		{
			Intent intent = new Intent(this, ContactCardManagerActivity.class);     
		    startActivity(intent);  
		}
		
		if(v.getId() == R.id.settingsbutton)
		{
			Intent intent = new Intent(this, SettingsActivity.class);     
		    startActivity(intent);  
		}
	}
	
	//asynch task reciever for database requests
	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		// TODO Auto-generated method stub
		int requestCode = DataBaseApiWrapper.currentRequestCode;
		
		switch (requestCode) {
             	case DataBaseApiWrapper.GET_ALL_USERS_ROUTE : 
             		
             		break;
            	case DataBaseApiWrapper.GET_USER_BY_ID_ROUTE : 
            		
            		break;
            	case DataBaseApiWrapper.CREATE_USER_ROUTE :   
            		
            		break;
            	case DataBaseApiWrapper.UPDATE_USER_GPS_ROUTE : 
            		
            		break;
            	case DataBaseApiWrapper.USER_ATTEND_EVENT_ROUTE :  
            		
            		break;
            	case DataBaseApiWrapper.UPDATE_USER_INFO_ROUTE : 
            		
            		break;
            	case DataBaseApiWrapper.GET_USER_WISHLIST_ROUTE :  
            		
            		break;
            	case DataBaseApiWrapper.DELETE_USER_ROUTE :   
            		
            		break;
            	case DataBaseApiWrapper.GET_ALL_EVENTS_ROUTE :  
            		
            		break;
            	case DataBaseApiWrapper.GET_EVENT_BY_ID_ROUTE :    
            		
            		break;
            	case DataBaseApiWrapper.CREATE_EVENT_ROUTE :   
            		
            		break;
            	case DataBaseApiWrapper.EVENT_UPDATE_ROUTE : 
            		
            	case DataBaseApiWrapper.EVENT_DELETE_ROUTE :   
            		
            		break;
            	default:
            		
            		break;
		}
	}

	@Override
	public void onLinkedinApiCallComplete(int apiCallKey,LinkedinApiWrapperResult result) {
		if(apiCallKey == LinkedinApiWrapper.GET_CURRENT_USER_INFO_API_CALL_KEY)
		{
			Person p = result.person;
			Log.d("linkedinApiWrapper","getCurrentUserInfoReciever");     
			// TODO Auto-generated method stub
			Log.d(this.TAG,"User profile pic url is "+p.getPictureUrl());
			user = new User(p.getPictureUrl()
					   ,p.getFirstName()
					   ,p.getLastName()
					   ,dataBaseApiWrapper.getCurrentUserDatabaseId()
					   ,p.getId()
					   ,"" 
					   ,new GpsCoordinate(0,0));
			updateProfileViews(user);  
		}
		
	}

	@Override
	public void onProfilePictureBitmapRecieved() {
		if(user!=null)profilePic.setImageBitmap(user.profilePictureProvider.bmp);
		
	}
}