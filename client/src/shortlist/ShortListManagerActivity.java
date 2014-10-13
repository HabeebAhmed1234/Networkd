package shortlist;

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

import contacts.ContactCard;
import adts.ProfilePictureProvider;
import adts.User;
import adts.ProfilePictureProvider.OnProfilePictureBitmapRecieved;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShortListManagerActivity extends Activity implements OnClickListener, DataBaseApiWrapperListener, EditText.OnEditorActionListener {
	public static final String TAG = "ShortListManagerActivity";
	
	private DataBaseApiWrapper dataBaseApiWrapper;
	
	EditText searchText;
	Button searchSubmitButton;
	LinearLayout searchbox;
	ImageView searchexpanderbutton;
	
	UserShortListAdapter userListAdapter;
	
	ArrayList<User> users = new ArrayList<User>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_short_list_manager); 
		 
		searchText = (EditText) findViewById(R.id.searchinput);
		searchText.setOnEditorActionListener(this);
		
		searchSubmitButton = (Button) findViewById(R.id.submitsearchbutton);
		searchSubmitButton.setOnClickListener(this);
		
		searchbox = (LinearLayout)findViewById(R.id.searchbox);
		searchexpanderbutton = (ImageView) findViewById(R.id.searchexpanderbutton);
		searchexpanderbutton.setOnClickListener(this);
		
		dataBaseApiWrapper = new DataBaseApiWrapper( this, this,true);
		dataBaseApiWrapper.getShortListUsers();
		
		searchbox.setVisibility(LinearLayout.GONE);
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_short_list_manager, menu);
		return true; 
	}

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode ==DataBaseApiWrapper.GET_SHORTLIST_USERS) 
		{
			users = JSONObjectParser.convertJSONResponseToUsers(result);
			createList(users);
		}	
	}
	
	private void createList(ArrayList<User> users )
	{
		ListView lvUserssContent = (ListView) findViewById(R.id.userList);
        userListAdapter = new UserShortListAdapter(this, users);
        lvUserssContent.setAdapter(userListAdapter);
        lvUserssContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		      @Override
		      public void onItemClick(AdapterView<?> parent,  final View view,int position, long id) {
		    	   	openUserProfile((User)userListAdapter.getItem(position));
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
	
	@Override
	public void onClick(View v) {
		//TODO: implement search functionality
		if(v.getId() == R.id.submitsearchbutton)
		{
			submitSearchButtonClicked();
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
	
	private void submitSearchButtonClicked(){
		String searchString = searchText.getText().toString();
		this.userListAdapter.search(searchString);
		
		searchbox.setVisibility(LinearLayout.GONE);
		Utilities.hideKeyboard(this,searchbox);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			submitSearchButtonClicked();
            return true;
        }
		return false;
	}
}

class UserShortListAdapter extends BaseAdapter implements DataBaseApiWrapperListener, LinkedinApiWrapperListener, OnProfilePictureBitmapRecieved
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
    
    public UserShortListAdapter(Context context, ArrayList<User> Users) 
    {
    	Log.d(this.TAG,"creating list adapter with "+Users.size()+" users");
    	this.context = context;
    	this.displayUsers = Users;
    	this.users = Users;
    	inflater = LayoutInflater.from(context);
    	
    	dataBaseApiWrapper = new DataBaseApiWrapper( this, this.context,true);
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
		TextView tvLAYOUTUserName = (TextView) v.findViewById(R.id.tvLAYOUTUserName);
		ImageView profilePic = (ImageView)v.findViewById(R.id.userprofilepic);
		ImageView deleteIcon = (ImageView)v.findViewById(R.id.deleteicon);
		ImageView sendContactCardIcon = (ImageView)v.findViewById(R.id.sendcontactcardicon);
		//invisible
		ImageView addUserToShortListIcon = (ImageView)v.findViewById(R.id.addtoshortlisticon);
		addUserToShortListIcon.setVisibility(ImageView.GONE);
		ImageView inviteUserToNetworkd = (ImageView)v.findViewById(R.id.inviteicon);
		inviteUserToNetworkd.setVisibility(ImageView.GONE);
		
		//assign name
		tvLAYOUTUserName.setText(user.firstName+" "+user.lastName);
		
		//add profile bitmap if available
		if(user.profilePictureProvider!=null && user.profilePictureProvider.bmp!=null)
		{	
			profilePic.setImageBitmap(user.profilePictureProvider.bmp); 
		}
		
		//assign onClickListeners
		deleteIcon.setOnClickListener(new OnClickListener() {
            private int pos = position;

            public void onClick(View v) {
            	userToDelete = (User)getItem(position);
            	dataBaseApiWrapper.deleteUserFromShortList((User)getItem(position));
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
	
	private void deleteUserFromLists(User userToDelete)
	{
		for(int i = 0 ; i < displayUsers.size() ; i++)
		{
			if(displayUsers.get(i).equals(userToDelete))displayUsers.remove(i);
		}
		
		for(int i = 0 ; i < users.size() ; i++)
		{
			if(users.get(i).equals(userToDelete))users.remove(i);
		}
	}

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.DELETE_USER_FROM_SHORT_LIST)
		{
			if(success && JSONReplySuccessChecker.isReplySuccess(result))
			{
				deleteUserFromLists(userToDelete);
				this.notifyDataSetChanged();
			}else{
				Toast.makeText(this.context,"DataBase error! User was not deleted!",Toast.LENGTH_LONG).show();
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
	
	public void search(String searchString)
	{
		Log.d(TAG,"Searching for "+searchString);
		
		if(searchString == null || searchString.compareTo("")==0)
		{
			displayUsers = users;
			notifyDataSetChanged();
			return;
		}
		
		displayUsers = new ArrayList<User>();
		for(int i = 0 ; i < users.size() ; i++)
		{
			if(users.get(i).containsString(searchString))
			{
				Log.d(TAG,"found match "+users.get(i).firstName + " " + users.get(i).lastName);
				displayUsers.add(users.get(i));
			}
		}
		
		Log.d(TAG,"found "+displayUsers.size()+" matches");
		this.notifyDataSetChanged();
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
		
		for(int i = 0 ; i < this.users.size() ; i++)
		{
			if(users.get(i).linkedin_id.compareTo(linkedinId)==0)
			{
				users.get(i).profilePictureProvider = new ProfilePictureProvider(url,users.get(i).linkedin_id);
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

