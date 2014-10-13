package messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import preferences.PreferencesHandler;
import networkddatabaseapi.DataBaseApiWrapper;
import networkddatabaseapi.DataBaseApiWrapper.DataBaseApiWrapperListener;
import utilities.Utilities;
import jsonengine.JSONObjectParser;
import jsonengine.JSONReplySuccessChecker;
import jsonengine.JSONStruct;

import com.networkd.R;
import com.networkd.R.id;
import com.networkd.R.layout;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MessagingManagerActivity extends Activity implements  DataBaseApiWrapperListener {
    // Debugging
    private static final String TAG = "MessagingManagerActivity";
    
    //this key is used if the conversation already exists
    public static final String CONVERSATION_ID_EXTRA_KEY = "conversation_id_extra_key";
    //if no conversation exists between these users make a new conversation by passing this in as an extra
    public static final String CONVERSATION_ID_NEW_CONVERSATION = "new_conversation";
    //if no conversation exists between these users pass in an extra with a delimited (using the MEMBER_DELIMITER) list of all of the conversation members' linkedin ids
    public static final String NEW_CONVERSATION_MEMBERS_IDS_EXTRA_KEY = "new_conversation_members_ids_extra_key";
    //used to delimit the list of members passed in
    public static final String MEMBER_DELIMITER = ",";
    
    //rate at which we listen for new messages
    public static final int LISTENING_INTERVAL_IN_MILISECONDS = 300000;
    
    private Bundle extras;
    
    private String currentConversationId = "";
    
    private DataBaseApiWrapper dataBaseApiWrapper;
    
    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;

    //message that will be sent
    private String messageToSend = "";
    
    //preferences
    private PreferencesHandler prefsHandler;
    
    //if true then we are ready to send a message. make this true after all messages have been retrieved or when the new conversation has been created
    private boolean isInitialized = false;
    
    //timer to trigger handler to listen for incoming messages
    private Timer listenTimer;
    
    //handler for listening to incoming messages
    Handler listenerHandler;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
        setContentView(R.layout.activity_messaging_manager);
        
		extras = getIntent().getExtras();
		if (extras != null) {
			currentConversationId = extras.getString(CONVERSATION_ID_EXTRA_KEY);
		}
		
		 // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.layout_message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });
        
        dataBaseApiWrapper = new DataBaseApiWrapper( this, this,false);
        prefsHandler = new PreferencesHandler(this);
        
        if(currentConversationId.compareTo(CONVERSATION_ID_NEW_CONVERSATION)!=0)
        {
        	isInitialized = true;
        	dataBaseApiWrapper.getAllConversationMessages(currentConversationId);
        }else{
        	dataBaseApiWrapper.createConversation(getNewConversationMembersIds());
        }
        
        listenerHandler = new Handler() {
        	@Override
        	public void dispatchMessage(android.os.Message msg) {
        	    super.dispatchMessage(msg);
        	    listen();
        	}
		};
		
        listenTimer = new Timer();
		listenTimer.schedule(new TimerTask() {          
	        @Override
	        public void run() {
	        	Log.d(TAG,"listenTimer: listenTimer went off");
	        	listenerHandler.sendEmptyMessage(0);
	        }

	    }, 0, LISTENING_INTERVAL_IN_MILISECONDS);
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if(listenTimer!=null)listenTimer.cancel();
    }

    private void sendMessage(String message) {
    	//make sure we are ready to send a message
    	if(!isInitialized) return;
    	message = prefsHandler.getUserName()+"- " + message;
    	Log.d(TAG,"sendMessage: sending message " + message);
        // Check that there's actually something to send
        if (message.length() > 0) {
        	messageToSend = message;
        	dataBaseApiWrapper.sendMessageToConversation(currentConversationId, message);
            mOutEditText.setText("");
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                //add in the user's name to the message
                sendMessage(message);
                
            }
            return true;
        }
    };
    
    private void addMessageToListView(String message)
    {
    	mConversationArrayAdapter.add(message);
    }
    
    private ArrayList<String> getNewConversationMembersIds()
    {
    	String members = extras.getString(NEW_CONVERSATION_MEMBERS_IDS_EXTRA_KEY);
    	String [] membersArray = members.split(MEMBER_DELIMITER);
    	return Utilities.converStrinArrayToStringArrayList(membersArray);
    }
    
    private void listen()
	{
    	if(!isInitialized) return;
    	Log.d(TAG,"listen: getting all messages");
		dataBaseApiWrapper.getAllConversationMessages(currentConversationId);
	}
    
	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.CREATE_CONVERSATION)
		{
			if(JSONReplySuccessChecker.isReplySuccess(result))
			{
				currentConversationId = JSONObjectParser.getConversationIdFromCreateConversationResponse(result);
				Log.d(TAG,"New conversation created");
				Toast.makeText(this, "Conversation created!", Toast.LENGTH_LONG).show();
				isInitialized = true;
			}else{
				Log.d(TAG,"ERROR: new conversation not created");
				Toast.makeText(this, "Error! cannot create conversation!", Toast.LENGTH_LONG).show();
				finish();
			}
		}
		
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.GET_ALL_CONVERSATION_MESSAGES)
		{
			ArrayList<Message> messages = new ArrayList<Message>();
			messages = JSONObjectParser.convertJSONResponseToMessages(result);
			mConversationArrayAdapter.clear();
			ArrayList<String> stringMessages = Utilities.messageListToStringListOfMessages(messages);
			for(int i = 0 ; i < messages.size() ; i++)
			{
				mConversationArrayAdapter.add(stringMessages.get(i));
			}
			
			isInitialized = true;
		}
		
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.SEND_MESSAGE_TO_CONVERSATION)
		{
			if(JSONReplySuccessChecker.isReplySuccess(result))
			{
				Log.d(TAG,"Message sent!");
				Toast.makeText(this, "Message sent", Toast.LENGTH_LONG).show();
				
				addMessageToListView(messageToSend);
				
			}else{
				Log.d(TAG,"Message failed to send!");
				Toast.makeText(this, "Message failed to send! Please try again later", Toast.LENGTH_LONG).show();
			}
		}
	}
}
