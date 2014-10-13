package linkedinapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import preferences.PreferencesHandler;
import adts.User;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.code.linkedinapi.client.CommunicationsApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientException;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.LinkedInCommunicationClient;
import com.google.code.linkedinapi.client.constant.ApplicationConstants;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.ApiStandardProfileRequest;
import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.Headers;
import com.google.code.linkedinapi.schema.HttpHeader;
import com.google.code.linkedinapi.schema.Location;
import com.google.code.linkedinapi.schema.Person;
import com.google.code.linkedinapi.schema.Skill;
import com.google.code.linkedinapi.schema.Skills;

import constants.Constants;

public class LinkedinApiWrapper{
	static final String TAG = "LinkedinApiWrapper";
	
	//types of api calls
	public static final int GET_CURRENT_USER_INFO_API_CALL_KEY = 1;
	public static final int GET_USER_INFO_BY_ID_API_CALL_KEY  = 2;
	public static final int SEND_INVITE_TO_USER_API_CALL_KEY = 3;
	public static final int GET_CURRENT_USER_CONNECTIONS_API_CALL_KEY = 4;
	public static final int INVITE_USER_TO_LINKEDIN_API_CALL_KEY = 5;
	
	//linkedin api stuff
	public static final String CONSUMER_KEY = "77xiv2v72zzkjg";
	public static final String CONSUMER_SECRET = "XP521rRgUSmB6g4k";

	static final String APP_NAME = "Networkd";
	public static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-linkedin";
	static final String OAUTH_CALLBACK_HOST = "litestcalback";
	public static final String OAUTH_CALLBACK_URL = String.format("%s://%s",OAUTH_CALLBACK_SCHEME, OAUTH_CALLBACK_HOST);
	public static final String OAUTH_QUERY_TOKEN = "oauth_token";
	public static final String OAUTH_QUERY_VERIFIER = "oauth_verifier";
	public static final String OAUTH_QUERY_PROBLEM = "oauth_problem";

	final LinkedInOAuthService oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(CONSUMER_KEY,CONSUMER_SECRET);
	
	final LinkedInApiClientFactory linkedInApiClientFactory = LinkedInApiClientFactory.newInstance(CONSUMER_KEY, CONSUMER_SECRET);
	LinkedInApiClient linkedinApiClient = null;
	
	private LinkedInAccessToken accessToken;
	static boolean isAuthorized = false;

	Context context;
	
	ProgressDialog progressDailog;
	
	public LinkedinApiWrapperListener delegate;
	
	public PreferencesHandler preferencesHandler;
	
	private final Set<ProfileField> profileFields = EnumSet.of(ProfileField.ID, ProfileField.FIRST_NAME, 
			ProfileField.LAST_NAME, ProfileField.CURRENT_STATUS,
			ProfileField.API_STANDARD_PROFILE_REQUEST, ProfileField.PUBLIC_PROFILE_URL, 
			ProfileField.POSITIONS,ProfileField.PICTURE_URL,
			ProfileField.API_STANDARD_PROFILE_REQUEST,
			ProfileField.API_STANDARD_PROFILE_REQUEST_HEADERS);
	
	public LinkedinApiWrapper(Context context,LinkedinApiWrapperListener delegate)
	{
		this.context = context;
		this.delegate = delegate;
		preferencesHandler = new PreferencesHandler(context);
		initProgressDialogue();
		if(isAuthenticated())postAuthorizationEnvironmentSetup();
	}
	
	public boolean isAuthenticated()
	{
		final String token = preferencesHandler.getLinkedinPrefToken();
		final String tokenSecret = preferencesHandler.getLinkedinPrefTokenSecret();
		if (token == null || token.compareTo("")==0 || tokenSecret == null || tokenSecret.compareTo("")==0) {
			Log.d(this.TAG,"linkedin is not authenticated");
			return false;
		} else {
			Log.d(this.TAG,"linkedin is authenticated");
			return true;
		}
	}

	public void clearTokens() {
		preferencesHandler.setLinkedinPrefToken("");
		preferencesHandler.setLinkedinPrefTokenSecret("");
		preferencesHandler.setLinkedinRequestPrefTokenSecret("");
	}
	
	public void postAuthorizationEnvironmentSetup() 
	{
		accessToken = new LinkedInAccessToken(preferencesHandler.getLinkedinPrefToken()
											, preferencesHandler.getLinkedinPrefTokenSecret());
		this.isAuthorized = true;
		this.linkedinApiClient = linkedInApiClientFactory.createLinkedInApiClient(this.accessToken);
	}
	
	//progress dialogue controls
	private void initProgressDialogue()
	{
		progressDailog = new ProgressDialog(this.context);
		progressDailog.setMessage("Loading...");
		progressDailog.setIndeterminate(false);
		progressDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDailog.setCancelable(false);
	}
	
	private void startProgressDialogue()
	{
		progressDailog.show();
	}
	
	private void stopProgressDialog()
	{
		if(progressDailog!=null)progressDailog.dismiss();
	}
	
	public LinkedInAccessToken getAccessToken()
	{
		return accessToken;
	}
		
	//api calls
	public void getCurrentUserInfo() {
		if(!isAuthorized) 
		{
			Log.d(this.TAG,"from getCurrentUserInfo WARNING USER IS NOT AUTHENTICATED!");
			return;
		}
		new AsyncTask<Void, Void, Object>() {
		    @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        startProgressDialogue();
		    }
			
			@Override
			protected Object doInBackground(Void... params) {
				Log.d("LinkedinManager","getCurrentUserInfo do in background");
				startProgressDialogue();
				try {
					return linkedinApiClient.getProfileForCurrentUser(profileFields);
				} catch (LinkedInApiClientException ex) {
					return ex;
				}
			} 
			@Override
			protected void onPostExecute(Object result) {
				Log.d("LinkedinManager","getCurrentUserInfo on post excecute");
				stopProgressDialog();
				if (result instanceof Exception) {
					//result is an Exception :) 
					final Exception ex = (Exception) result;
					Toast.makeText(context,"Appliaction down due to LinkedInApiClientException: "+ ex.getMessage()+ " try run application again.",Toast.LENGTH_LONG).show();
				} else if (result instanceof Person) {
					LinkedinApiWrapperResult apiCallResult = new LinkedinApiWrapperResult();
					apiCallResult.person = (Person) result;
					delegate.onLinkedinApiCallComplete(LinkedinApiWrapper.GET_CURRENT_USER_INFO_API_CALL_KEY,apiCallResult);
				}
			}
		}.execute();

	}
	
	public void getUserInfoById(final String id) {
		if(!isAuthorized) 
		{
			Log.d(this.TAG,"from getUserInfoById WARNING USER IS NOT AUTHENTICATED!");
			return;
		}
		new AsyncTask<Void, Void, Object>() {
		    @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        startProgressDialogue();
		    }
		    
			@Override
			protected Object doInBackground(Void... params) {
				Log.d("LinkedinManager","getUserInfoById do in background");
				try {
					Person userById = null;
					
					if(id!=null && id.compareTo("")!=0)
					{
						userById = linkedinApiClient.getProfileById(id, profileFields);;
					}
					return userById;
				} catch (LinkedInApiClientException ex) {
					return ex;
				}
			} 
			@Override
			protected void onPostExecute(Object result) {
				Log.d("LinkedinManager","getUserInfo on post excecute");
				stopProgressDialog();
				if (result instanceof Exception) {
					//result is an Exception :) 
					final Exception ex = (Exception) result;
					//clearTokens();
					Toast.makeText(context,"Appliaction down due to LinkedInApiClientException: "+ ex.getMessage()+ " try running the application again.",Toast.LENGTH_LONG).show();
				} else if (result instanceof Person) {
					LinkedinApiWrapperResult apiCallResult = new LinkedinApiWrapperResult();
					apiCallResult.person = (Person) result;
					delegate.onLinkedinApiCallComplete(LinkedinApiWrapper.GET_USER_INFO_BY_ID_API_CALL_KEY,apiCallResult);
				}
			}
		}.execute();

	}
	
	public void sendInviteToUser(final User user)
	{
		if(!isAuthorized) 
		{
			Log.d(this.TAG,"from sendInviteToUser WARNING USER IS NOT AUTHENTICATED!");
			return;
		}
		new AsyncTask<Void, Void, Object>() {
		    @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        startProgressDialogue();
		    }
		    
			@Override
			protected Object doInBackground(Void... params) {
				Log.d("LinkedinManager","sendInviteToUser do in background");
				try {
					Log.d(TAG,"searching for user with id = " + user.linkedin_id + " in linkedin database");
					Person p = linkedinApiClient.getProfileById(user.linkedin_id,profileFields);
					if(p == null) 
					{
						Log.d(TAG,"User with id = " + user.linkedin_id + " not found");
						return null;
					}
					Log.d(TAG,"found " + p.getFirstName() + " " + p.getLastName());
					ApiStandardProfileRequest apiStandardProfileRequest = p.getApiStandardProfileRequest();
					if(apiStandardProfileRequest == null)Log.d(TAG,"apiStandardProfileRequest is null");
					Headers headers = apiStandardProfileRequest.getHeaders();
					if(headers == null) Log.d(TAG,"headers is null");
					List<HttpHeader> httpHeaderList = headers.getHttpHeaderList();
					if(httpHeaderList == null || httpHeaderList.size() == 0) Log.d(TAG,"httpHeaderList is null or empty");
					HttpHeader authHeader = getAuthHeaderFromList(httpHeaderList);
					linkedinApiClient.sendInviteById(user.linkedin_id, "Please add me to your network!", "Hello, I would like to connect with you on Linkedin!\n Sent from Networkd", authHeader.getValue());
					return null;
				} catch (LinkedInApiClientException ex) {
					return ex;
				}
			} 
			@Override
			protected void onPostExecute(Object result) {
				stopProgressDialog();
				delegate.onLinkedinApiCallComplete(LinkedinApiWrapper.SEND_INVITE_TO_USER_API_CALL_KEY,null);
			}
		}.execute();
	}
	
	private HttpHeader getAuthHeaderFromList(List<HttpHeader> headers)
	{
		for(int i = 0 ; i < headers.size() ; i++)
		{
			if(headers.get(i).getName().compareTo(ApplicationConstants.AUTH_HEADER_NAME) == 0) return headers.get(i);
		}
		return null;
	}
	
	public void getCurrentUserConnections()
	{
		if(!isAuthorized) 
		{
			Log.d(this.TAG,"from getCurrentUserConnections WARNING USER IS NOT AUTHENTICATED!");
			return;
		}
		new AsyncTask<Void, Void, Object>() {
		    @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        startProgressDialogue();
		    }
		    
			@Override
			protected Object doInBackground(Void... params) {
				Log.d("LinkedinManager","getCurrentUserConnections do in background");
				try {
					return linkedinApiClient.getConnectionsForCurrentUser(profileFields);
				} catch (LinkedInApiClientException ex) {
					return ex;
				}
			} 
			@Override
			protected void onPostExecute(Object result) {
				stopProgressDialog();
				LinkedinApiWrapperResult apiCallResult = new LinkedinApiWrapperResult();
				apiCallResult.connections = (Connections)result;
				delegate.onLinkedinApiCallComplete(LinkedinApiWrapper.GET_CURRENT_USER_CONNECTIONS_API_CALL_KEY,apiCallResult);
			}
		}.execute();
	}
	
	public void inviteUserToNetworkd(final User user)
	{
		if(!isAuthorized) 
		{
			Log.d(this.TAG,"from inviteUserToNetworkd WARNING USER IS NOT AUTHENTICATED!");
			return;
		}
		new AsyncTask<Void, Void, Object>() {
		    @Override
		    protected void onPreExecute() {
		        super.onPreExecute();
		        startProgressDialogue();
		    }
		    
			@Override
			protected Object doInBackground(Void... params) {
				Log.d("LinkedinManager","inviteUserToNetworkd do in background");
				try {
					linkedinApiClient.sendMessage(Arrays.asList(user.linkedin_id), "Congratulations from Networkd!", "hello! You have been invited to Networkd!\n A Networking app\n download: "+Constants.APP_GOOGLE_PLAY_URL);
					return null;
				} catch (LinkedInApiClientException ex) {
					return ex;
				}
			} 
			@Override
			protected void onPostExecute(Object result) {
				stopProgressDialog();
				LinkedinApiWrapperResult apiCallResult = new LinkedinApiWrapperResult();
				apiCallResult.connections = (Connections)result;
				delegate.onLinkedinApiCallComplete(LinkedinApiWrapper.INVITE_USER_TO_LINKEDIN_API_CALL_KEY,apiCallResult);
			}
		}.execute();
	}
}

