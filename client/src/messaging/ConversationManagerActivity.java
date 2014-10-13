package messaging;

import gps.GpsManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import jsonengine.JSONObjectParser;
import jsonengine.JSONStruct;
import networkddatabaseapi.DataBaseApiWrapper;
import networkddatabaseapi.DataBaseApiWrapper.DataBaseApiWrapperListener;

import org.json.JSONObject;

import com.google.code.linkedinapi.schema.Person;
import com.networkd.R;
import com.networkd.R.id;
import com.networkd.R.layout;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationManagerActivity extends Activity implements  DataBaseApiWrapperListener {
	
	private DataBaseApiWrapper dataBaseApiWrapper;
	
	ConversationsListAdapter conversationsListAdapter;
	
	GpsManager gpsManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation_manager);
		
		dataBaseApiWrapper = new DataBaseApiWrapper( this, this,true);
		dataBaseApiWrapper.getAllConversationIdsAndParticipants();
	}
	
	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode ==DataBaseApiWrapper.GET_ALL_CONVERSATION_IDS_AND_PARTICIPANTS) 
		{
			ArrayList<Conversation> conversations = new ArrayList<Conversation>();
			conversations = JSONObjectParser.convertJSONResponseToConversations(result);
			createList(conversations);
		}
	}
	
	private void createList(ArrayList<Conversation> conversations )
	{
		ListView lvUserssContent = (ListView) findViewById(R.id.conversationsList);
		conversationsListAdapter = new ConversationsListAdapter(this, conversations);
        lvUserssContent.setAdapter(conversationsListAdapter);
        lvUserssContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		      @Override
		      public void onItemClick(AdapterView<?> parent,  final View view,int position, long id) {
		    	   	openMessagingManagerForConversation((Conversation)conversationsListAdapter.getItem(position));
		      }
		    });
	}
	
	private void openMessagingManagerForConversation(Conversation conversation)
	{
		Intent intent = new Intent(this, MessagingManagerActivity.class);
		intent.putExtra(MessagingManagerActivity.CONVERSATION_ID_EXTRA_KEY, conversation.id);
        this.startActivity(intent);
	}
}

class ConversationsListAdapter extends BaseAdapter 
{
	public static final String TAG = "ConversationsListAdapter";
	
	// context
    private Context context;

    // views
    private LayoutInflater inflater;

    // data
    private ArrayList<Conversation> displayConversations;
    private ArrayList<Conversation> conversations;
    
    public ConversationsListAdapter(Context context, ArrayList<Conversation> conversations) 
    {
    	Log.d(this.TAG,"creating list adapter with "+conversations.size()+" conversations");
    	this.context = context;
    	this.displayConversations = conversations;
    	this.conversations = conversations;
    	inflater = LayoutInflater.from(context);
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return displayConversations.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return displayConversations.get(arg0);
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
		    v = inflater.inflate(R.layout.layout_conversation, parent, false);
		}

		Conversation conversation = (Conversation) getItem(position);
		
		Log.d(this.TAG,"making view for conversation "+ position);
		
		//get views
		TextView namesOfUsersInConversationTextView = (TextView) v.findViewById(R.id.namesofusersinconversation);
		
		//assign name
		namesOfUsersInConversationTextView.setText(conversation.usersInConversationToString());
		
		return v;
	}
}

