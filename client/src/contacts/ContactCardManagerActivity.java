package contacts;

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

import profileviewer.ProfileViewerActivity;
import utilities.Utilities;

import com.google.code.linkedinapi.schema.Person;
import com.networkd.R;
import com.networkd.R.id;
import com.networkd.R.layout;
import com.networkd.R.menu;

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
import android.view.inputmethod.EditorInfo;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactCardManagerActivity extends Activity implements OnClickListener, DataBaseApiWrapperListener, EditText.OnEditorActionListener {
	public static final String TAG = "ContactCardManagerActivity";
	
	private DataBaseApiWrapper dataBaseApiWrapper;
	
	EditText searchText;
	Button searchSubmitButton;
	LinearLayout searchbox;
	ImageView searchexpanderbutton;
	
	ContactCardsAdapter contactCardsListAdapter;
	
	ArrayList<ContactCard> contactCards = new ArrayList<ContactCard>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_contact_card_manager); 
		 
		searchText = (EditText) findViewById(R.id.searchinput);
		searchText.setOnEditorActionListener(this);
		
		searchSubmitButton = (Button) findViewById(R.id.submitsearchbutton);
		searchSubmitButton.setOnClickListener(this);
		
		searchbox = (LinearLayout)findViewById(R.id.searchbox);
		searchexpanderbutton = (ImageView) findViewById(R.id.searchexpanderbutton);
		searchexpanderbutton.setOnClickListener(this);
		
		dataBaseApiWrapper = new DataBaseApiWrapper( this, this,true);
		dataBaseApiWrapper.getContactCards();
		
		searchbox.setVisibility(LinearLayout.GONE);
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_contact_card_manager, menu);
		return true;
	}

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode ==DataBaseApiWrapper.GET_CONTACT_CARDS) 
		{
			contactCards = JSONObjectParser.convertJSONResponseToContactCards(result);
			createList(contactCards);
		}	
	}
	
	private void createList(ArrayList<ContactCard> ContactCards )
	{
		ListView lvContactCardsContent = (ListView) findViewById(R.id.contactcardList);
		contactCardsListAdapter = new ContactCardsAdapter(this, ContactCards);
		lvContactCardsContent.setAdapter(contactCardsListAdapter);
		lvContactCardsContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		      @Override
		      public void onItemClick(AdapterView<?> parent,  final View view,int position, long id) {
		    	   	openUserProfile((ContactCard)contactCardsListAdapter.getItem(position));
		      }
		    });
	}
	
	private void openUserProfile(ContactCard card)
	{
		Log.d(TAG,"opening user profile: "+card.user.firstName+" "+card.user.lastName);
		Intent viewIntent = new Intent(this, ProfileViewerActivity.class);
		viewIntent.putExtra(ProfileViewerActivity.USER_DATABASE_ID_EXTRA_KEY,card.user.database_id);
		viewIntent.putExtra(ProfileViewerActivity.USER_LINKEDIN_ID_EXTRA_KEY,card.user.linkedin_id);
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
	
	private void submitSearchButtonClicked()
	{
		String searchString = searchText.getText().toString();
		this.contactCardsListAdapter.search(searchString);
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

class ContactCardsAdapter extends BaseAdapter implements DataBaseApiWrapperListener, LinkedinApiWrapperListener, OnProfilePictureBitmapRecieved
{
	public static final String TAG = "ContactCardsAdapter";
	private DataBaseApiWrapper dataBaseApiWrapper;
	private LinkedinApiWrapper linkedinApiWrapper;
	public static ContactCard cardToDelete = null ; 
	
	// context
    private Context context;

    // views
    private LayoutInflater inflater;

    // data
    private ArrayList<ContactCard> displayCards;
    private ArrayList<ContactCard> cards;
    
    ContactCardsAdapter(Context context, ArrayList<ContactCard> cards) 
    {
    	Log.d(this.TAG,"creating list adapter with "+cards.size()+" users");
    	this.context = context;
    	this.displayCards = cards;
    	this.cards = cards;
    	inflater = LayoutInflater.from(context);
    	
    	dataBaseApiWrapper = new DataBaseApiWrapper( this, this.context,true);
    	linkedinApiWrapper = new LinkedinApiWrapper(this.context, this);
    	
    	getUsersInfo();
    }
    
    private void getUsersInfo()
    {
    	for(int i = 0 ; i < displayCards.size() ; i++)
    	{
    		dataBaseApiWrapper.getUserById(displayCards.get(i).senderId);	
    	}
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return displayCards.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return displayCards.get(arg0);
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
		    v = inflater.inflate(R.layout.layout_contactcard, parent, false);
		}

		ContactCard card = (ContactCard) getItem(position);
		
		Log.d(this.TAG,"making view for card "+ position);
		
		//get views
		TextView tvLAYOUTUserName = (TextView) v.findViewById(R.id.tvLAYOUTUserName);
		TextView tvLAYOUTEmail = (TextView) v.findViewById(R.id.tvLAYOUTEmail);
		ImageView profilePic = (ImageView)v.findViewById(R.id.userprofilepic);
		ImageView deleteIcon = (ImageView)v.findViewById(R.id.deleteicon);
		ImageView addUserToShortListIcon = (ImageView)v.findViewById(R.id.addtoshortlisticon);
		
		//set views
		
		if(card.user!=null)
		{
			if(card.user.firstName != null && card.user.lastName != null)
			{
				tvLAYOUTUserName.setText(card.user.firstName+" "+card.user.lastName);
			}else{
				tvLAYOUTUserName.setVisibility(TextView.GONE);
			}
			
			if(card.user.email != null ) 
			{
				tvLAYOUTEmail.setText(card.user.email);
			}else{
				tvLAYOUTEmail.setVisibility(TextView.GONE);
			}
			
			//add profile bitmap if available
			if(card.user.profilePictureProvider!=null && card.user.profilePictureProvider.bmp!=null)
			{	
				profilePic.setImageBitmap(card.user.profilePictureProvider.bmp); 
			}
		} 
		
		//assign onClickListeners
		deleteIcon.setOnClickListener(new OnClickListener() {
            private int pos = position;

            public void onClick(View v) {
            	cardToDelete = (ContactCard)getItem(position);
            	dataBaseApiWrapper.deleteContactCardFromInventory((ContactCard)getItem(position));
            }
        });
		
		addUserToShortListIcon.setOnClickListener(new OnClickListener() {
            private int pos = position;

            public void onClick(View v) {
            	dataBaseApiWrapper.addUserToShortList((User)((ContactCard)getItem(position)).user);
            }
        });
		
		return v;
	}
	
	private void deleteCardFromLists(ContactCard cardToDelete)
	{
		for(int i = 0 ; i < displayCards.size() ; i++)
		{
			if(displayCards.get(i).equals(cardToDelete))displayCards.remove(i);
		}
		
		for(int i = 0 ; i < cards.size() ; i++)
		{
			if(cards.get(i).equals(cardToDelete))cards.remove(i);
		}
	}
	
	public void search(String searchString)
	{
		Log.d(TAG,"Searching for "+searchString);
		
		if(searchString == null || searchString.compareTo("")==0)
		{
			displayCards = cards;
			notifyDataSetChanged();
			return;
		}
		
		displayCards = new ArrayList<ContactCard>();
		for(int i = 0 ; i < cards.size() ; i++)
		{
			if(cards.get(i).containsString(searchString))
			{
				Log.d(TAG,"found match "+cards.get(i).user.firstName + " " + cards.get(i).user.lastName);
				displayCards.add(cards.get(i));
			}
		}
		
		Log.d(TAG,"found "+displayCards.size()+" matches");
		this.notifyDataSetChanged();
	}
	
	private void addProfilePicToContactCardEntry(String linkedinId, String url)
	{
		for(int i = 0 ; i < this.displayCards.size() ; i++)
		{
			if(displayCards.get(i).user!=null)
			{
				if(displayCards.get(i).user.linkedin_id.compareTo(linkedinId)==0)
				{
					displayCards.get(i).user.profilePictureProvider = new ProfilePictureProvider(url,displayCards.get(i).user.linkedin_id);
					displayCards.get(i).user.profilePictureProvider.requestBitmap(this);
				}
			}
		}
		
		for(int i = 0 ; i < this.cards.size() ; i++)
		{
			if(cards.get(i).user!=null){
				if(cards.get(i).user.linkedin_id.compareTo(linkedinId)==0)
				{
					cards.get(i).user.profilePictureProvider = new ProfilePictureProvider(url,cards.get(i).user.linkedin_id);
					cards.get(i).user.profilePictureProvider.requestBitmap(this);
				}
			}
		}
	}
	
	private void addUserToContactCardEntryThenGetProfilePicture(User user)
	{
		for(int i = 0 ; i < this.displayCards.size() ; i++)
		{
			if(displayCards.get(i).senderId.compareTo(user.database_id)==0)
			{
				displayCards.get(i).user = user;
			}
		}
		
		for(int i = 0 ; i < this.cards.size() ; i++)
		{
			if(cards.get(i).senderId.compareTo(user.database_id)==0)
			{
				cards.get(i).user = user;
			}
		}
		
		linkedinApiWrapper.getUserInfoById(user.linkedin_id);	
		
		this.notifyDataSetChanged();
	}

	@Override
	public void onLinkedinApiCallComplete(int apiCallKey,LinkedinApiWrapperResult result) {
		if(apiCallKey == LinkedinApiWrapper.GET_USER_INFO_BY_ID_API_CALL_KEY)
		{
			addProfilePicToContactCardEntry(result.person.getId(),result.person.getPictureUrl());
		}
	}
	
	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.DELETE_CONTACT_CARD_FROM_INVENTORY)
		{
			if(success && JSONReplySuccessChecker.isReplySuccess(result))
			{
				deleteCardFromLists(cardToDelete);
				this.notifyDataSetChanged();
			}else{
				Toast.makeText(this.context,"DataBase error! Card was not deleted!",Toast.LENGTH_LONG).show();
			}
		}
		
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.GET_USER_BY_ID_ROUTE)
		{
			if(success)
			{
				User tempUser = JSONObjectParser.convertJSONResponseToUser(result); 
				addUserToContactCardEntryThenGetProfilePicture(tempUser);
			}else{
				Toast.makeText(this.context,"DataBase error! User info was not retrieved!",Toast.LENGTH_LONG).show();
			}
		}
		
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.ADD_USER_TO_SHORT_LIST)
		{
			if(JSONReplySuccessChecker.isReplySuccess(result))
			{
				Toast.makeText(context, "User has been added to your short list!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(context, "Error! User has not been added to your short list!", Toast.LENGTH_LONG).show();
			}
		}
		
	}

	@Override
	public void onProfilePictureBitmapRecieved() {
		this.notifyDataSetChanged();
	}
}

