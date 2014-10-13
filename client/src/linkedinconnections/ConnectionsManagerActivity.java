package linkedinconnections;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.Person;
import com.networkd.R;
import com.networkd.R.drawable;
import com.networkd.R.id;
import com.networkd.R.layout;
import com.networkd.R.menu;

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
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectionsManagerActivity extends Activity implements OnClickListener, LinkedinApiWrapperListener, DataBaseApiWrapperListener, EditText.OnEditorActionListener,OnProfilePictureBitmapRecieved {
	public static final String TAG = "ConnectionsManagerActivity";
	private LinkedinApiWrapper linkedinApiWrapper;
	private DataBaseApiWrapper dataBaseApiWrapper;
	
	//when this number has reached 2 then the list is ready to make. (add 1 to the list for each user list that is retrieved - ie: connections and networkdUsers)
	private int readyToMakeListCount = 0;
	
	EditText searchText;
	Button searchSubmitButton;
	ImageView searchexpanderbutton;
	
	LinearLayout searchbox;
	
	UserConnectionsListAdapter userListAdapter;
	ListView lvUserssContent;
	
	ArrayList<User> connections = new ArrayList<User>();
	ArrayList<User> networkdUsers = new ArrayList<User>();
	
	ArrayList<User> listUsers = new ArrayList<User>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_connections_manager);
		
		searchText = (EditText) findViewById(R.id.searchinput);
		searchText.setOnEditorActionListener(this);
		
		searchSubmitButton = (Button) findViewById(R.id.submitsearchbutton);
		searchSubmitButton.setOnClickListener(this);
		
		searchexpanderbutton = (ImageView) findViewById(R.id.searchexpanderbutton);
		searchexpanderbutton.setOnClickListener(this);

		searchbox = (LinearLayout)findViewById(R.id.searchbox);
		
		linkedinApiWrapper = new LinkedinApiWrapper( this, this);
		linkedinApiWrapper.getCurrentUserConnections();
		
		dataBaseApiWrapper = new DataBaseApiWrapper( this, this,true);
		dataBaseApiWrapper.getAllUsers();
		
		searchbox.setVisibility(LinearLayout.GONE);
	};
	
	private void createList()
	{
		listUsers = Utilities.deactivateLinkedinUsersInListIfNotNetworkdMembers(connections, networkdUsers);
		getAllUserProfilePictures();
		lvUserssContent = (ListView) findViewById(R.id.userList);
        userListAdapter = new UserConnectionsListAdapter(this, listUsers);
        lvUserssContent.setAdapter(userListAdapter);
        lvUserssContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		      @Override
		      public void onItemClick(AdapterView<?> parent,  final View view,int position, long id) {
		    	   	openUserProfile((User)userListAdapter.getItem(position));
		      }
		    });
	}
	
	private void getAllUserProfilePictures()
	{
		for(int i = 0 ; i < listUsers.size() ; i++)
		{
			if(listUsers.get(i).profilePictureProvider!=null)listUsers.get(i).profilePictureProvider.requestBitmap(this);	
		}
	}
	
	@Override
	public void onClick(View v) {
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
	
	private void openUserProfile(User user)
	{
		Log.d(TAG,"opening user profile: "+user.firstName+" "+user.lastName);
		Intent viewIntent = new Intent(this, ProfileViewerActivity.class);
		viewIntent.putExtra(ProfileViewerActivity.USER_DATABASE_ID_EXTRA_KEY,user.database_id);
		viewIntent.putExtra(ProfileViewerActivity.USER_LINKEDIN_ID_EXTRA_KEY,user.linkedin_id);
        startActivity(viewIntent);
	}
	
	ArrayList<User> linkedinPersonListToUserList(List <Person> in)
	{
		ArrayList<User> out = new ArrayList<User>();
		
		for(int i = 0 ; i < in.size() ; i++)
		{
			User newUser = new User();
			newUser.linkedin_id = in.get(i).getId();
			newUser.firstName = in.get(i).getFirstName();
			newUser.lastName = in.get(i).getLastName();
			Log.d(TAG,newUser.firstName+" "+ newUser.lastName+" has picture url "+ in.get(i).getPictureUrl());
			newUser.profilePictureProvider = new ProfilePictureProvider(in.get(i).getPictureUrl(),newUser.linkedin_id);
			out.add(newUser);
		}
		return out;
	}

	@Override
	public void onLinkedinApiCallComplete(int apiCallKey,LinkedinApiWrapperResult result) {
		if(apiCallKey == LinkedinApiWrapper.GET_CURRENT_USER_CONNECTIONS_API_CALL_KEY)
		{
			connections = linkedinPersonListToUserList(result.connections.getPersonList());
			readyToMakeListCount+=1;
			if(readyToMakeListCount == 2) createList();
		}
		
	}

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.GET_ALL_USERS_ROUTE)
		{
			this.networkdUsers = JSONObjectParser.convertJSONResponseToUsers(result);
			readyToMakeListCount+=1;
			if(readyToMakeListCount == 2) createList();
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_find_local_users, menu);
		return true;
	}

	@Override
	public void onProfilePictureBitmapRecieved() {
		userListAdapter.notifyDataSetChanged();
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

class UserConnectionsListAdapter extends BaseAdapter implements DataBaseApiWrapperListener, LinkedinApiWrapperListener,OnProfilePictureBitmapRecieved
{
	public static final String TAG = "UserConnectionsListAdapter";
	private DataBaseApiWrapper dataBaseApiWrapper;
	private LinkedinApiWrapper linkedinApiWrapper;
	// context
    private Context context;

    // views
    private LayoutInflater inflater;

    // data
    private ArrayList<User> displayUsers;
    private ArrayList<User> users;
    
    public UserConnectionsListAdapter(Context context, ArrayList<User> Users) 
    {
    	Log.d(this.TAG,"creating list adapter with "+Users.size()+" users");
    	this.context = context;
    	this.users = Users;
    	this.displayUsers = Users;
    	inflater = LayoutInflater.from(context);
    	dataBaseApiWrapper = new DataBaseApiWrapper(this,context,true);
    	linkedinApiWrapper= new LinkedinApiWrapper(context,this);
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
		// TODO Auto-generated method stub
		View v = null;
		if (convertView != null) {
			v = convertView;
		} else {
		    v = inflater.inflate(R.layout.layout_user, parent, false);
		}

		User user = (User) getItem(position);
		
		//Log.d(this.TAG,"making view for user "+ position);
		
		//get views
		TextView tvLAYOUTUserName = (TextView) v.findViewById(R.id.tvLAYOUTUserName);
		ImageView profilePic = (ImageView)v.findViewById(R.id.userprofilepic);
		ImageView deleteIcon = (ImageView)v.findViewById(R.id.deleteicon);
		deleteIcon.setVisibility(ImageView.GONE);
		ImageView addUserToShortListIcon = (ImageView)v.findViewById(R.id.addtoshortlisticon);
		addUserToShortListIcon.setVisibility(ImageView.GONE);
		ImageView inviteUserToNetworkd = (ImageView)v.findViewById(R.id.inviteicon);
		inviteUserToNetworkd.setVisibility(ImageView.GONE);
		ImageView sendContactCardIcon = (ImageView)v.findViewById(R.id.sendcontactcardicon);
		sendContactCardIcon.setVisibility(ImageView.GONE);
		
		if(user.isNetworkdUser)
		{
			v.setBackgroundColor(Color.WHITE);
			//onclick listeners
			addUserToShortListIcon.setVisibility(ImageView.VISIBLE);
			addUserToShortListIcon.setOnClickListener(new OnClickListener() {
	            private int pos = position;
	
	            public void onClick(View v) {
	            	dataBaseApiWrapper.addUserToShortList((User)getItem(position));
	            }
	        });
			
			sendContactCardIcon.setVisibility(ImageView.VISIBLE);
			sendContactCardIcon.setOnClickListener(new OnClickListener() {
	            private int pos = position;
	
	            public void onClick(View v) {
	            	PreferencesHandler prefsHandler = new PreferencesHandler(context);
	            	ContactCard prefsUserContactCard = prefsHandler.getUserContactCard();
	            	dataBaseApiWrapper.sendContactCard((User)getItem(position),prefsUserContactCard.summary,prefsUserContactCard.skills,prefsUserContactCard.extraNotes,prefsUserContactCard.jobTitle); 
	            }
	        });
			
		}else{
			v.setBackgroundColor(Color.DKGRAY);
			inviteUserToNetworkd.setVisibility(ImageView.VISIBLE);
			inviteUserToNetworkd.setOnClickListener(new OnClickListener() {
	            private int pos = position;
	
	            public void onClick(View v) {
	            	linkedinApiWrapper.inviteUserToNetworkd((User)getItem(position));
	            }
	        });
		}
		
		//assign name else request it
		tvLAYOUTUserName.setText(user.firstName+" "+user.lastName);
		
		//add profile bitmap if available
		if(user.profilePictureProvider!=null && user.profilePictureProvider.bmp!=null)
		{	
			Log.d(TAG,"user "+ user.firstName+" bitmap was already loaded");
			profilePic.setImageBitmap(user.profilePictureProvider.bmp); 
		}else{
			profilePic.setImageResource(R.drawable.default_profile_pic_list);
		}
		
		return v;
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

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.ADD_USER_TO_SHORT_LIST)
		{
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

	@Override
	public void onLinkedinApiCallComplete(int apiCallKey,LinkedinApiWrapperResult result) {
		if(apiCallKey == LinkedinApiWrapper.INVITE_USER_TO_LINKEDIN_API_CALL_KEY)
		{
			Toast.makeText(context, "User has been invited to Networkd!", Toast.LENGTH_LONG).show();
		}
		
	}

	@Override
	public void onProfilePictureBitmapRecieved() {
		this.notifyDataSetChanged();
	}
}

