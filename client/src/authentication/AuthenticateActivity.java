package authentication;

import gps.GpsCoordinate;
import homepage.MainActivity;

import java.util.ArrayList;

import jsonengine.JSONStruct;
import linkedinapi.LinkedinApiWrapper;
import linkedinapi.LinkedinApiWrapperListener;
import linkedinapi.LinkedinApiWrapperResult;
import networkddatabaseapi.DataBaseApiWrapper;
import networkddatabaseapi.DataBaseApiWrapper.DataBaseApiWrapperListener;

import org.json.JSONException;
import org.json.JSONObject;

import preferences.PreferencesHandler;
import utilities.Utilities;

import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceException;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Person;
import com.networkd.R;
import com.networkd.R.id;
import com.networkd.R.layout;

import adts.User;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AuthenticateActivity extends Activity implements LinkedinApiWrapperListener,OnClickListener,DataBaseApiWrapperListener{
	LinkedinApiWrapper linkedinManager;
	DataBaseApiWrapper dataBaseApiWrapper;
	Button loginButton,registerButton,submitButton;
	TextView enterEmailLabel;
	EditText enterEmailInput;
	
	String newUserEmail = null;
	Person newUserLinkedinPerson = null;
	
	public static final String TAG = "AuthenticateActivity";
	final LinkedInOAuthService oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(LinkedinApiWrapper.CONSUMER_KEY,LinkedinApiWrapper.CONSUMER_SECRET);
	
	private boolean isRegistering = false;
	
	private PreferencesHandler preferencesHandler;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(this.TAG,"onCreate method fired");
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_authenticate);
		
		preferencesHandler = new PreferencesHandler(this);
		
		assertNetworkConnection();
	}
	
	private void initialize()
	{
		loginButton = (Button)findViewById(R.id.loginbutton); 
		loginButton.setOnClickListener(this);
		registerButton = (Button)findViewById(R.id.registerbutton);
		registerButton.setOnClickListener(this);
		submitButton = (Button)findViewById(R.id.emailsubmitbutton);
		submitButton.setOnClickListener(this);
		submitButton.setVisibility(Button.INVISIBLE);
		
		enterEmailLabel = (TextView)findViewById(R.id.enteremaillabel);
		enterEmailLabel.setVisibility(TextView.INVISIBLE);
		enterEmailInput = (EditText)findViewById(R.id.enteremailedittext);
		enterEmailInput.setVisibility(EditText.INVISIBLE);
		
		dataBaseApiWrapper = new DataBaseApiWrapper(this,this,true);
		
		linkedinManager = new LinkedinApiWrapper(this,this);     
		if(this.isLinkedinAuthenticated() && this.isDatabaseAuthenticated())
		{
			launchMainActivity();
		}
	}
	
	private void assertNetworkConnection()
	{
		if(!Utilities.isNetworkAvailable(this))
		{
			Log.d(TAG,"internet connection is not available.opening dialogue");
			showNoConnectionDialog(this);
		}else{
			Log.d(TAG,"internet connection is available. initializing");
			initialize();
		}
	}
	
	//shows a no connection dialogue on the ui attached to the given context. gives the option to go to internet settings. 
	//passed in context must be of an activity
	private void showNoConnectionDialog(final Context ctx) 
	{
		Log.d(TAG,"showing no connection dialogue");
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setMessage("Internet connection is required");
        builder.setTitle("No connection");
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	Log.d(TAG,"clicked connect opening Internet Settings");
            	openInternetSettings();
            }
        });
        
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	Log.d(TAG,"clicked cancel closing activity");
            	finish();
                return;
            }
        });
        
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            	Log.d(TAG,"oncancel closing activity");
            	finish();
                return;
            }
        });

        builder.show();
    }
	
	private void openInternetSettings()
	{
		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
		finish();
	}
	
	//_____________________________________________________________________________authentication logic  _____________________________________________________________________________
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.loginbutton)
		{
			Log.d(this.TAG,"loginbutton clicked");
			
			isRegistering = false;
			boolean linkedinAuth = this.isLinkedinAuthenticated();
			boolean databaseAuth = this.isDatabaseAuthenticated();
			if(linkedinAuth  &&  databaseAuth) 
			{
				isRegistering = false;
				this.launchMainActivity();
			}
			if(linkedinAuth  &&  !databaseAuth) 
			{
				this.isRegistering = true;
				this.setupRegisterScreen(); 
			}
			if(!linkedinAuth && databaseAuth) 
			{
				this.isRegistering = false;
				this.linkedinLogin();
			}
			if(!linkedinAuth && !databaseAuth)
			{
				this.isRegistering = true;
				this.setupRegisterScreen(); 
			}
		}
		if(v.getId() == R.id.registerbutton)
		{
			Log.d(this.TAG,"registerbutton clicked");
			
			isRegistering = true;
			boolean linkedinAuth = this.isLinkedinAuthenticated();
			boolean databaseAuth = this.isDatabaseAuthenticated();
			if(linkedinAuth  &&  databaseAuth) 
			{
				Toast.makeText(getBaseContext(),"You are already registered! Logging you in!",Toast.LENGTH_LONG).show();
				this.isRegistering = false;
				this.launchMainActivity();
			}
			if(linkedinAuth && !databaseAuth) 
			{
				this.isRegistering = true;
				this.setupRegisterScreen(); 
			}
			if(!linkedinAuth && databaseAuth) 
			{
				Toast.makeText(getBaseContext(),"You are already registered! Please log in",Toast.LENGTH_LONG).show();
				this.isRegistering = false;
				this.linkedinLogin();
			}
			if(!linkedinAuth && !databaseAuth)
			{
				this.isRegistering = true;
				this.setupRegisterScreen(); 
			}
		}
		if(v.getId() == R.id.emailsubmitbutton)
		{
			Log.d(this.TAG,"emailsubmitbutton clicked");
			register();
		}
		
	}
	
	private void register()
	{
		Log.d(this.TAG,"register method fired");
		
		if(getEmail())
		{
			this.startAutheniticateLinkedin();
		}else{
			Log.d(this.TAG,"register: Failed to get email");
			Toast.makeText(getBaseContext(),"Please provide a valid email!",Toast.LENGTH_LONG).show();
		}
	}
	
	private void linkedinLogin()
	{
		Log.d(this.TAG,"linkedinLogin method fired");
		
		startAutheniticateLinkedin();
	}
	
	private boolean isLinkedinAuthenticated()
	{
		Log.d(this.TAG,"isLinkedinAuthenticated method fired");
		
		final String token = preferencesHandler.getLinkedinPrefToken();
		final String tokenSecret = preferencesHandler.getLinkedinPrefTokenSecret();
		if (token == null || token.compareTo("") == 0 || tokenSecret == null || token.compareTo("")==0) {
			Log.d(this.TAG,"linkedin is not authenticated");
			return false;
		} else {
			Log.d(this.TAG,"linkedin is authenticated");
			return true;
		}
	}
	
	private boolean isDatabaseAuthenticated()
	{
		Log.d(this.TAG,"isDatabaseAuthenticated method fired");
		
		final PreferencesHandler prefsHandler = new PreferencesHandler(this); 
		final String apiKey = prefsHandler.getDataBaseApiKey();
		final String id = prefsHandler.getUserDataBaseId();
		if (apiKey == null || apiKey.compareTo("") == 0 || id == null || id.compareTo("")==0) {
			Log.d(this.TAG,"database is not authenticated");
			return false;
		} else {
			Log.d(this.TAG,"database is authenticated");
			return true;
		}
	}
	
	private void launchMainActivity()
	{
		Log.d(this.TAG,"launchMainActivity method fired");
		
		Intent intent = new Intent(this, MainActivity.class);       
	    startActivity(intent); 
	    finish();   
	}
	
	private void populatePreferences(String apiKey, String database_id)
	{
		Log.d(this.TAG,"populatePreferences method fired");
		
		final PreferencesHandler prefsHandler = new PreferencesHandler(this); 
		Log.d(this.TAG,"saving api key "+apiKey+" and id "+database_id);
		prefsHandler.setUserDataBaseId(database_id);
		prefsHandler.setDataBaseApiKey(apiKey);
		prefsHandler.setUserName(newUserLinkedinPerson.getFirstName() + " " +newUserLinkedinPerson.getLastName());
	}
	
	
	private void setupRegisterScreen()
	{
		Log.d(this.TAG,"setupRegisterScreen method fired");
		
		Toast.makeText(getBaseContext(),"Please Register!",Toast.LENGTH_LONG).show();
		this.enterEmailInput.setVisibility(EditText.VISIBLE);
		this.submitButton.setVisibility(Button.VISIBLE);
		this.enterEmailLabel.setVisibility(TextView.VISIBLE);
		this.loginButton.setVisibility(Button.INVISIBLE);
		this.registerButton.setVisibility(Button.INVISIBLE);
	}
	
	private void resetScreen()
	{
		Log.d(this.TAG,"resetScreen method fired");
		
		Toast.makeText(getBaseContext(),"Error! Please try again later",Toast.LENGTH_LONG).show();
		isRegistering = false;
		this.enterEmailInput.setVisibility(EditText.INVISIBLE);
		this.submitButton.setVisibility(Button.INVISIBLE);
		this.enterEmailLabel.setVisibility(TextView.INVISIBLE);
		this.loginButton.setVisibility(Button.VISIBLE);
		this.registerButton.setVisibility(Button.VISIBLE);
	}
	
	private void isDataBaseAuthenticated()
	{
		Log.d(this.TAG,"isDataBaseAuthenticated method fired");
		
		this.dataBaseApiWrapper.checkIfRegistered();
	}
	
	private boolean getEmail()
	{
		Log.d(this.TAG,"getEmail method fired");
		
		this.newUserEmail = this.enterEmailInput.getText().toString();
		Log.d(this.TAG,"setting email as "+this.newUserEmail);
		if(newUserEmail == null || newUserEmail.compareTo("")==0) return false;
		return true;
	}
	
	private String extractApiKey(JSONStruct struct)
	{
		Log.d(this.TAG,"extractApiKey method fired");
		
		if(struct == null) return null;
		if(!struct.isObject) return null;
		JSONObject object = struct.object;
		String key = null;
		try {
			key = object.getJSONObject("user").getString("api_key");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return key;
	}
	
	private String extractDataBaseId(JSONStruct struct)
	{
		Log.d(this.TAG,"extractDataBaseId method fired");
		
		if(struct == null) return null;
		if(!struct.isObject) return null;
		JSONObject object = struct.object;
		String key = null;
		try {
			key = object.getJSONObject("user").getString("id");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return key;
	}
	
	//asynch task reciever for database requests
	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		//if(!success) return;
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.CREATE_USER_ROUTE)
		{
			Log.d(this.TAG,"onDataBaseAPIRequestComplete method fired with CREATE_USER_ROUTE");
			
			isRegistering = false;
			
			String apiKey = extractApiKey(result);
			String id = extractDataBaseId(result);
			
			if(apiKey == null || id == null)
			{
				Toast.makeText(getBaseContext(),"Error! register failed!",Toast.LENGTH_LONG).show();
				resetScreen();
				return;
			}
			
			Log.d(this.TAG, "new user created api key is "+apiKey);
			populatePreferences(apiKey,id);
			launchMainActivity();	
		}
	}
	
	void startAutheniticateLinkedin() {
		Log.d(this.TAG,"startAutheniticateLinkedin executing method");
		new AsyncTask<Void, Void, LinkedInRequestToken>() {

			@Override
			protected LinkedInRequestToken doInBackground(Void... params) {
				Log.d(TAG,"StartAuthenticate Asynch task do in background");
				LinkedInRequestToken reqToken = null;
				try {
					reqToken = oAuthService.getOAuthRequestToken(LinkedinApiWrapper.OAUTH_CALLBACK_URL);
				} catch (LinkedInOAuthServiceException e) {
					Log.d(TAG,"request token was null check stack trace");
					e.printStackTrace();
					return null;
				}
				return reqToken;
			}

			@Override
			protected void onPostExecute(LinkedInRequestToken liToken) {
				Log.d(TAG,"StartAuthenticate Asynch task on post execute");
				if(liToken == null)
				{
					isRegistering = false;
					resetScreen();
					return;
				}
				final String uri = liToken.getAuthorizationUrl();
				preferencesHandler.setLinkedinRequestPrefTokenSecret(liToken.getTokenSecret());
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(i);
			}
		}.execute();
	}

	void finishAuthenticateLinkedin(final Uri uri) {
		Log.d(this.TAG,"finishAuthenticateLinkedin executing method");
		if (uri != null  &&  uri.getScheme().equals(LinkedinApiWrapper.OAUTH_CALLBACK_SCHEME)) {
			final String problem = uri.getQueryParameter(LinkedinApiWrapper.OAUTH_QUERY_PROBLEM);
			if (problem == null) {

				new AsyncTask<Void, Void, LinkedInAccessToken>() {
					
					@Override
					protected LinkedInAccessToken doInBackground(Void... params) {
						Log.d("LinkedinManager","finishAuthenticateLinkedin Asynch task do in background");
						final LinkedInAccessToken accessToken = oAuthService.getOAuthAccessToken(new LinkedInRequestToken(uri.getQueryParameter(LinkedinApiWrapper.OAUTH_QUERY_TOKEN),preferencesHandler.getLinkedinRequestPrefTokenSecret()),uri.getQueryParameter(LinkedinApiWrapper.OAUTH_QUERY_VERIFIER));
						preferencesHandler.setLinkedinPrefToken(accessToken.getToken());
						preferencesHandler.setLinkedinPrefTokenSecret(accessToken.getTokenSecret());
						preferencesHandler.setLinkedinRequestPrefTokenSecret("");
						return accessToken;
					}

					@Override
					protected void onPostExecute(LinkedInAccessToken accessToken) {
						Log.d("LinkedinManager","finishAuthenticateLinkedin Asynch task on post execute");
						linkedinManager.postAuthorizationEnvironmentSetup();
						onAuthorizationComplete();
					}
				}.execute();

			} else {
				isRegistering = false;
				resetScreen();
				Toast.makeText(this,"Applicaction down due OAuth problem: " + problem,Toast.LENGTH_LONG).show();
				return;
			}

		}
	}
	
	public void onAuthorizationComplete() {
		// TODO Auto-generated method stub
		if(isRegistering)
		{
			this.linkedinManager.getCurrentUserInfo();
		}else
		{
			this.launchMainActivity();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		finishAuthenticateLinkedin(intent.getData());     
	}

	@Override
	public void onLinkedinApiCallComplete(int apiCallKey,LinkedinApiWrapperResult result) {
		if(apiCallKey == LinkedinApiWrapper.GET_CURRENT_USER_INFO_API_CALL_KEY)
		{
			Person output = result.person;
			Log.d(this.TAG,"getCurrentUserInfoReciever fired");
			if(!isRegistering) return;
			newUserLinkedinPerson = output;
			Log.d(this.TAG,"creating user in database with linkedin id "+output.getId());
			User newUser = new User("", output.getFirstName(), output.getLastName() ,"", output.getId(), this.newUserEmail, new GpsCoordinate(0,0));
			this.dataBaseApiWrapper.createUser(newUser,linkedinManager.getAccessToken());
		}
		
	}
}
