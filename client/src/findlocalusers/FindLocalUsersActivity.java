package findlocalusers;

import gps.GpsCoordinate;
import gps.GpsManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import jsonengine.JSONObjectParser;
import jsonengine.JSONReplySuccessChecker;
import jsonengine.JSONStruct;
import linkedinapi.LinkedinApiWrapper;
import linkedinapi.LinkedinApiWrapperListener;
import linkedinapi.LinkedinApiWrapperResult;
import networkddatabaseapi.DataBaseApiWrapper;
import networkddatabaseapi.DataBaseApiWrapper.DataBaseApiWrapperListener;

import org.json.JSONObject;

import preferences.PreferencesHandler;
import profileviewer.ProfileViewerActivity;
import utilities.Utilities;

import com.google.code.linkedinapi.schema.Person;
import com.networkd.R;
import com.networkd.R.id;
import com.networkd.R.layout;
import com.networkd.R.menu;

import contacts.ContactCard;
import adts.ProfilePictureProvider;
import adts.User;
import adts.ProfilePictureProvider.OnProfilePictureBitmapRecieved;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class FindLocalUsersActivity extends Activity implements OnClickListener, DataBaseApiWrapperListener, OnSeekBarChangeListener {
	public static final String TAG = "FindLocalUsersActivity";
	
	private DataBaseApiWrapper dataBaseApiWrapper;
	
	SeekBar rangeSeekBar;
	Button submitButton;
	TextView rangeText;
	ImageView searchexpanderbutton;
	LinearLayout searchbox;
	LocalUsersListAdapter LocalUsersListAdapter;
	
	GpsManager gpsManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_find_local_users);
		
		rangeSeekBar = (SeekBar) findViewById(R.id.rangeinput);
		rangeSeekBar.setOnSeekBarChangeListener(this);
		rangeText = (TextView)findViewById(R.id.rangetext);
		submitButton = (Button) findViewById(R.id.submitrangebutton);
		submitButton.setOnClickListener(this);
		searchexpanderbutton = (ImageView) findViewById(R.id.searchexpanderbutton);
		searchexpanderbutton.setOnClickListener(this);
		searchbox = (LinearLayout)findViewById(R.id.searchbox);
		
		dataBaseApiWrapper = new DataBaseApiWrapper( this, this,true);
		gpsManager = new GpsManager(this);
		
		searchbox.setVisibility(LinearLayout.GONE);
		
		findUsersInRange();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_find_local_users, menu);
		return true;
	}

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode !=DataBaseApiWrapper.GET_ALL_USERS_ROUTE) return;
		if(result == null)
		{
			Log.d(TAG, "onDataBaseAPIRequestComplete: GET_ALL_USERS_ROUTE response is null");
			return;
		}
		Log.d(TAG, "onDataBaseAPIRequestComplete: GET_ALL_USERS_ROUTE response recieved");
		
		ArrayList<User> users = filterUsersByProximity(rangeSeekBar.getProgress(),JSONObjectParser.convertJSONResponseToUsers(result));
		Log.d(TAG, "onDataBaseAPIRequestComplete: recieved "+users.size()+" users");
		createList(users);
	}
	
	private void createList(ArrayList<User> users )
	{
		Log.d(TAG,"createList: creating list with "+users.size()+" users");
		ListView lvUserssContent = (ListView) findViewById(R.id.userList);
        LocalUsersListAdapter = new LocalUsersListAdapter(this, users);
        lvUserssContent.setAdapter(LocalUsersListAdapter);
        Log.d(TAG,"createList: setting onclick listener on lvUserssContent");
        lvUserssContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		      @Override
		      public void onItemClick(AdapterView<?> parent,  final View view,int position, long id) {
		    	  	Log.d(TAG,"createList: onItemClick: view "+position+" clicked");
		    	   	openUserProfile((User)LocalUsersListAdapter.getItem(position));
		      }
		    });
	}
	
	private void openUserProfile(User user)
	{
		Log.d(TAG,"opening user profile: "+user.firstName+" "+user.lastName);
		Intent viewIntent = new Intent(this, ProfileViewerActivity.class);
		viewIntent.putExtra(ProfileViewerActivity.USER_DATABASE_ID_EXTRA_KEY,user.database_id);
		viewIntent.putExtra(ProfileViewerActivity.USER_LINKEDIN_ID_EXTRA_KEY,user.linkedin_id);
        startActivity(viewIntent);
	}
	
	private ArrayList<User> filterUsersByProximity(double range, ArrayList<User> users)
	{
		GpsCoordinate currentUserCoordinates = gpsManager.getLocation();
		ArrayList <User> newList = new ArrayList<User>();
		
		for(int i  = 0 ; i < users.size() ; i++)
		{
			if(currentUserCoordinates.getDistance(users.get(i).gps_coord)<=range 
		       && !(dataBaseApiWrapper.getCurrentUserDatabaseId().compareTo(users.get(i).database_id)==0))
			{
				newList.add(users.get(i));
			}
		}
		return newList;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.submitrangebutton)
		{
			Log.d(TAG, "onClick: submitrangebutton clicked");
			findUsersInRange();
		}
		if(v.getId() == R.id.searchexpanderbutton)
		{
			if(searchbox.getVisibility() == LinearLayout.VISIBLE){
				searchbox.setVisibility(LinearLayout.GONE);
			}else{
				searchbox.setVisibility(LinearLayout.VISIBLE);
			}
		}
		
	}
	
	private void findUsersInRange(){
		dataBaseApiWrapper.getAllUsers();
		searchbox.setVisibility(LinearLayout.GONE);
		Utilities.hideKeyboard(this,searchbox);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		rangeText.setText("Range "+progress+" km");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}
}

class LocalUsersListAdapter extends BaseAdapter implements DataBaseApiWrapperListener, LinkedinApiWrapperListener, OnProfilePictureBitmapRecieved
{
	public static final String TAG = "UserShortListAdapter";
	private DataBaseApiWrapper dataBaseApiWrapper;
	private LinkedinApiWrapper linkedinApiWrapper;
	public static User userToDelete = null ; 
	
	// context
    private Context context;

    // views
    private LayoutInflater inflater;

    // data
    private ArrayList<User> displayUsers;
    private ArrayList<User> users;
    
    public LocalUsersListAdapter(Context context, ArrayList<User> Users) 
    {
    	Log.d(this.TAG,"creating list adapter with "+Users.size()+" users");
    	this.context = context;
    	this.displayUsers = Users;
    	inflater = LayoutInflater.from(context);
    	
    	dataBaseApiWrapper = new DataBaseApiWrapper(this, this.context,true);
    	linkedinApiWrapper = new LinkedinApiWrapper(this.context, this);
    	
    	getProfilePicUrls();
    }
    
    private void getProfilePicUrls()
    {
    	for(int i = 0 ; i < displayUsers.size() ; i++)
    	{
    		linkedinApiWrapper.getUserInfoById(displayUsers.get(i).linkedin_id);	
    	}
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return displayUsers.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return displayUsers.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = null;
		if (convertView != null) {
			v = convertView;
		} else {
		    v = inflater.inflate(R.layout.layout_user, parent, false);
		}

		User user = (User) getItem(position);
		
		Log.d(this.TAG,"making view for user "+ position);
		
		//get views
		//visible
		ImageView sendContactCardIcon = (ImageView)v.findViewById(R.id.sendcontactcardicon);
		TextView tvLAYOUTUserName = (TextView) v.findViewById(R.id.tvLAYOUTUserName);
		ImageView profilePic = (ImageView)v.findViewById(R.id.userprofilepic);
		ImageView addUserToShortListIcon = (ImageView)v.findViewById(R.id.addtoshortlisticon);
		//invisible
		ImageView deleteIcon = (ImageView)v.findViewById(R.id.deleteicon);
		deleteIcon.setVisibility(ImageView.GONE);
		ImageView inviteUserToNetworkd = (ImageView)v.findViewById(R.id.inviteicon);
		inviteUserToNetworkd.setVisibility(ImageView.GONE);
		
		//assign name
		tvLAYOUTUserName.setText(user.firstName+" "+user.lastName);
		

		//add profile bitmap if available
		if(user.profilePictureProvider!=null  && user.profilePictureProvider.bmp!=null)
		{	
			profilePic.setImageBitmap(user.profilePictureProvider.bmp); 
		}
		
		//assign onClickListeners
		addUserToShortListIcon.setOnClickListener(new OnClickListener() {
            private int pos = position;

            public void onClick(View v) {
            	dataBaseApiWrapper.addUserToShortList((User)getItem(position));
            }
        });
		
		sendContactCardIcon.setOnClickListener(new OnClickListener() {
            private int pos = position;

            public void onClick(View v) {
            	PreferencesHandler prefsHandler = new PreferencesHandler(context);
            	ContactCard prefsUserContactCard = prefsHandler.getUserContactCard();
            	dataBaseApiWrapper.sendContactCard((User)getItem(position),prefsUserContactCard.summary,prefsUserContactCard.skills,prefsUserContactCard.extraNotes,prefsUserContactCard.jobTitle); 
            }
        });
		
		return v;
	}

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.ADD_USER_TO_SHORT_LIST)
		{
			//TODO: check if succeeded then show success toast
			if(JSONReplySuccessChecker.isReplySuccess(result))
			{
				Toast.makeText(context, "User has been added to your short list!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(context, "User is already in your shortlist", Toast.LENGTH_LONG).show();
			}
		}
		
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.SEND_CONTACT_CARD)
		{
			if(JSONReplySuccessChecker.isReplySuccess(result))
			{
				Toast.makeText(context, "Contact card has been sent!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(context, "Error! Contact card not sent", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void addProfilePicToUserEntry(String linkedinId, String url)
	{
		for(int i = 0 ; i < this.displayUsers.size() ; i++)
		{
			if(displayUsers.get(i).linkedin_id.compareTo(linkedinId)==0)
			{
				displayUsers.get(i).profilePictureProvider = new ProfilePictureProvider(url,displayUsers.get(i).linkedin_id);
				displayUsers.get(i).profilePictureProvider.requestBitmap(this);
			}
		}
	}
	
	@Override
	public void onLinkedinApiCallComplete(int apiCallKey,LinkedinApiWrapperResult result) {
		if(apiCallKey == LinkedinApiWrapper.GET_USER_INFO_BY_ID_API_CALL_KEY)
		{
			addProfilePicToUserEntry(result.person.getId(),result.person.getPictureUrl());
		}
	}

	@Override
	public void onProfilePictureBitmapRecieved() {
		this.notifyDataSetChanged();
	}
}

