package profileviewer;

import gps.GpsCoordinate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import preferences.PreferencesHandler;
import networkddatabaseapi.DataBaseApiWrapper;
import networkddatabaseapi.DataBaseApiWrapper.DataBaseApiWrapperListener;
import notes.NoteManagerActivity;
import linkedinapi.LinkedinApiWrapper;
import linkedinapi.LinkedinApiWrapperListener;
import linkedinapi.LinkedinApiWrapperResult;
import messaging.MessagingManagerActivity;
import utilities.Utilities;
import jsonengine.JSONObjectParser;
import jsonengine.JSONReplySuccessChecker;
import jsonengine.JSONStruct;

import com.google.code.linkedinapi.schema.Person;
import com.networkd.R;
import com.networkd.R.id;
import com.networkd.R.layout;
import com.networkd.R.menu;

import contacts.ContactCard;
import adts.ProfilePictureProvider;
import adts.User;
import adts.ProfilePictureProvider.OnProfilePictureBitmapRecieved;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class ProfileViewerActivity extends Activity implements OnClickListener,LinkedinApiWrapperListener, DataBaseApiWrapperListener,OnProfilePictureBitmapRecieved {
	public static final String USER_LINKEDIN_ID_EXTRA_KEY = "linkedin_id_extra_key";
	public static final String USER_DATABASE_ID_EXTRA_KEY = "linkedin_database_extra_key";
	
	public static final String TAG = "ProfileViewerActivity";
	
	private TextView  firstName;     
	private TextView  lastName;     
	private TextView  status;     
	private TextView  summary;   
	
	private ImageView profilePic; 
	
	private LinearLayout addNoteButton, addConnectionButton, addShortlistButton, sendContactCardButton, 
		   viewLinkedinButton, messagingButton,inviteToNetworkdButton;
	
	private User user = new User();
	
	private LinkedinApiWrapper linkedinApiWrapper = null;
	private DataBaseApiWrapper dataBaseApiWrapper = null;
	
	//this is true if we are currently opening a conversation
	private boolean isStartingMessagingManager = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_profile_viewer);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			user.linkedin_id = extras.getString(USER_LINKEDIN_ID_EXTRA_KEY);
			user.database_id = extras.getString(USER_DATABASE_ID_EXTRA_KEY);
			
			Log.d(TAG,"viewing user "+user.database_id);
		}
		
		firstName = (TextView) findViewById(R.id.firstName);     
		lastName = (TextView) findViewById(R.id.lastname);     
		status	= (TextView) findViewById(R.id.status);     
		summary = (TextView) findViewById(R.id.summary);     
		profilePic = (ImageView) findViewById(R.id.profilepic);  
		
		addNoteButton = (LinearLayout) findViewById(R.id.addnotesbutton);
		addNoteButton.setOnClickListener(this);
		addConnectionButton = (LinearLayout) findViewById(R.id.addtoconnectionssbutton);
		addConnectionButton.setOnClickListener(this);
		addShortlistButton = (LinearLayout) findViewById(R.id.addshortlistbutton);
		addShortlistButton.setOnClickListener(this);
		sendContactCardButton = (LinearLayout) findViewById(R.id.sendcontactcardbutton); 
		sendContactCardButton.setOnClickListener(this);
		inviteToNetworkdButton = (LinearLayout) findViewById(R.id.invitetonetworkdbutton); 
		inviteToNetworkdButton.setOnClickListener(this);
		viewLinkedinButton = (LinearLayout) findViewById(R.id.viewlinkedinbutton); 
		viewLinkedinButton.setOnClickListener(this);
		messagingButton = (LinearLayout) findViewById(R.id.messagebutton);
		messagingButton.setOnClickListener(this);
 		
		linkedinApiWrapper = new LinkedinApiWrapper(this,this);
		linkedinApiWrapper.getUserInfoById(user.linkedin_id); 
		
		dataBaseApiWrapper = new DataBaseApiWrapper( this, this,true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_profile_viewer, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if(id == R.id.addnotesbutton)
		{
			Log.d(this.TAG, "add note button clicked");
			addNote(user.linkedin_id);
		}
		
		if(id == R.id.addtoconnectionssbutton)
		{
			linkedinApiWrapper.sendInviteToUser(user);
		}
		
		if(id == R.id.addshortlistbutton)
		{
			dataBaseApiWrapper.addUserToShortList(user);
		}
		
		if(id == R.id.sendcontactcardbutton)
		{
			PreferencesHandler prefsHandler = new PreferencesHandler(this);
        	ContactCard prefsUserContactCard = prefsHandler.getUserContactCard();
        	dataBaseApiWrapper.sendContactCard(this.user,prefsUserContactCard.summary,prefsUserContactCard.skills,prefsUserContactCard.extraNotes,prefsUserContactCard.jobTitle); 
		}
		
		if(id == R.id.invitetonetworkdbutton)
		{
			linkedinApiWrapper.inviteUserToNetworkd(user);
		}
		
		if(id == R.id.viewlinkedinbutton)
		{
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/profile/view?id="+user.linkedin_id));
			startActivity(browserIntent);
		}
		
		if(id == R.id.messagebutton)
		{
			startMessagingManager(user);
		}
		
	}
	
	//Activity methods
	
	private void startMessagingManager(User user)
	{
		isStartingMessagingManager = true;
		//we have to check if a conversation with this user exists already
		ArrayList<User> participantList = new ArrayList<User>();
		User currentUser = new User();
		currentUser.database_id = dataBaseApiWrapper.getCurrentUserDatabaseId();
		Log.d(TAG,"starting conversation between "+user.database_id + " and "+currentUser.database_id);
		participantList.add(user);
		participantList.add(currentUser);
		dataBaseApiWrapper.getConversationIdFromParticipantList(participantList); 
	}
	
	private void startMessagingManagerActivityWithExistingConversation(String conversationId)
	{
		Log.d(TAG,"starting MessagingManagerActivity With Existing Conversation");
		
		Intent intent = new Intent(this, MessagingManagerActivity.class);
		
		//add in extras
		intent.putExtra(MessagingManagerActivity.CONVERSATION_ID_EXTRA_KEY,conversationId);
		
        this.startActivity(intent);
        finish();
	}
	
	private void startMessagingManagerActivityWithNewConversation()
	{
		Log.d(TAG,"starting MessagingManagerActivity With new Conversation");
		//add members
		ArrayList<String> members = new ArrayList<String>();
		members.add(dataBaseApiWrapper.getCurrentUserDatabaseId());
		members.add(user.database_id);
		
		Intent intent = new Intent(this, MessagingManagerActivity.class);
		
		//add in extras
		intent.putExtra(MessagingManagerActivity.CONVERSATION_ID_EXTRA_KEY,MessagingManagerActivity.CONVERSATION_ID_NEW_CONVERSATION);
		intent.putExtra(MessagingManagerActivity.NEW_CONVERSATION_MEMBERS_IDS_EXTRA_KEY, Utilities.stringListToDelimitedString(members, MessagingManagerActivity.MEMBER_DELIMITER));
		
        this.startActivity(intent);
        finish();
	}
	
	private void updateProfileViews()
	{
		if(user == null) return;     
		
		//request profile bitmap
		user.profilePictureProvider.requestBitmap(this);
		
		if(user.firstName!=null)firstName.setText(user.firstName);     
		if(user.lastName!=null)lastName.setText(user.lastName);   
	}
	
	private void addNote(String id)
	{
		Log.d(this.TAG, "launching note manager to add note");
		Intent intent = new Intent(this, NoteManagerActivity.class);
		intent.putExtra(NoteManagerActivity.EXTRA_USER_LINKEDIN_ID, this.user.linkedin_id);
        startActivity(intent);
        finish();
	}
	
	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(!success) return;
		
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.ADD_USER_TO_SHORT_LIST)
		{
			//TODO: check if succeeded then show success toast
			if(JSONReplySuccessChecker.isAddUserToShortListReplySuccess(result))
			{
				Toast.makeText(this, "User has been added to your short list!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "User is not a Networkd user! Cannot add to shortlist!", Toast.LENGTH_LONG).show();
			}
		}
		
		if(DataBaseApiWrapper.currentRequestCode ==DataBaseApiWrapper.GET_CONVERSATION_ID_FROM_PARTICIPANT_LIST)
		{
			if(isStartingMessagingManager)
			{
				ArrayList<String> conversationIds = new ArrayList<String>();
				conversationIds = JSONObjectParser.convertJSONResponseToConversationIdsUsingCommonConversationsResponse(result);
				
				if(conversationIds.size() == 0 || conversationIds == null)
				{
					startMessagingManagerActivityWithNewConversation();
				}else{
					startMessagingManagerActivityWithExistingConversation(conversationIds.get(0));
				}
			}
		}
		
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.SEND_CONTACT_CARD)
		{
			if(JSONReplySuccessChecker.isReplySuccess(result))
			{
				Toast.makeText(this, "Contact card has been sent!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "Error! Contact card not sent", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onLinkedinApiCallComplete(int apiCallKey,LinkedinApiWrapperResult result) {
		
		if(apiCallKey == LinkedinApiWrapper.GET_USER_INFO_BY_ID_API_CALL_KEY)
		{
			Person p = result.person;
			Log.d(this.TAG,"onLinkedinApiCallComplete - GET_USER_INFO_BY_ID_API_CALL_KEY");   
			Log.d(this.TAG,"User profile pic url is "+p.getPictureUrl());
			
			user.profilePictureProvider.setProfilePictureUrl(p.getPictureUrl());
			user.firstName = p.getFirstName();
			user.lastName = p.getLastName();
			user.linkedin_id = p.getId();
			user.gps_coord = new GpsCoordinate(0,0);
			
			updateProfileViews();
		}
		
		if(apiCallKey == LinkedinApiWrapper.SEND_INVITE_TO_USER_API_CALL_KEY)
		{
			Toast.makeText(this,"Sent invite",Toast.LENGTH_SHORT).show();
		}
		
		if(apiCallKey == LinkedinApiWrapper.INVITE_USER_TO_LINKEDIN_API_CALL_KEY)
		{
			Toast.makeText(this, "User has been invited to Networkd!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onProfilePictureBitmapRecieved() {
		profilePic.setImageBitmap(user.profilePictureProvider.bmp);
		
	}

}
