package linkedinapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.Activity;
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

public class LinkedinManager{
	static final String TAG = "LinkedinManager";
	
	static final String CONSUMER_KEY = "77xiv2v72zzkjg";
	static final String CONSUMER_SECRET = "XP521rRgUSmB6g4k";

	static final String APP_NAME = "Networkd";
	static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-linkedin";
	static final String OAUTH_CALLBACK_HOST = "litestcalback";
	static final String OAUTH_CALLBACK_URL = String.format("%s://%s",OAUTH_CALLBACK_SCHEME, OAUTH_CALLBACK_HOST);
	static final String OAUTH_QUERY_TOKEN = "oauth_token";
	static final String OAUTH_QUERY_VERIFIER = "oauth_verifier";
	static final String OAUTH_QUERY_PROBLEM = "oauth_problem";

	final LinkedInOAuthService oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(CONSUMER_KEY,CONSUMER_SECRET);
	final LinkedInApiClientFactory factory = LinkedInApiClientFactory.newInstance(CONSUMER_KEY, CONSUMER_SECRET);
	LinkedInApiClient client = null;
	
	static final String OAUTH_PREF = "LIKEDIN_OAUTH";
	static final String PREF_TOKEN = "token";
	static final String PREF_TOKENSECRET = "tokenSecret";
	static final String PREF_REQTOKENSECRET = "requestTokenSecret";
	
	public static String ACCESS_TOKEN_KEY = "access_token";
	
	private LinkedInAccessToken accessToken;
	static boolean isAuthorized = false;

	Context context;
	
	public LinkedinAsyncResponse delegate;
	
	LinkedinManager(Context context,LinkedinAsyncResponse delegate)
	{
		this.context = context;
		this.delegate = delegate;
		if(isAuthenticated())postAuthorizationEnvironmentSetup();
	}
	
	public boolean isAuthenticated()
	{
		final SharedPreferences pref = context.getSharedPreferences(OAUTH_PREF,context.MODE_PRIVATE);
		final String token = pref.getString(PREF_TOKEN, null);
		final String tokenSecret = pref.getString(PREF_TOKENSECRET, null);
		if (token == null || tokenSecret == null) {
			Log.d(this.TAG,"linkedin is not authenticated");
			return false;
		} else {
			Log.d(this.TAG,"linkedin is authenticated");
			return true;
		}
	}

	void clearTokens() {
		context.getSharedPreferences(OAUTH_PREF, context.MODE_PRIVATE).edit().remove(PREF_TOKEN).remove(PREF_TOKENSECRET).remove(PREF_REQTOKENSECRET).commit();
	}
	
	void postAuthorizationEnvironmentSetup() 
	{
		accessToken = new LinkedInAccessToken(context.getSharedPreferences(OAUTH_PREF, context.MODE_PRIVATE).getString(this.PREF_TOKEN, "null")
											, context.getSharedPreferences(OAUTH_PREF, context.MODE_PRIVATE).getString(this.PREF_TOKENSECRET, "null"));
		this.isAuthorized = true;
		this.client = factory.createLinkedInApiClient(this.accessToken);
		delegate.onAuthorizationCompleteReciever();
	}
	
	//api calls
	void getCurrentUserInfo() {
		if(!isAuthorized) 
		{
			Log.d(this.TAG,"from getCurrentUserInfo WARNING USER IS NOT AUTHENTICATED!");
			return;
		}
		new AsyncTask<Void, Void, Object>() {
			@Override
			protected Object doInBackground(Void... params) {
				Log.d("LinkedinManager","getCurrentUserInfo do in background");
				try {
					return client.getProfileForCurrentUser();
				} catch (LinkedInApiClientException ex) {
					return ex;
				}
			} 
			@Override
			protected void onPostExecute(Object result) {
				Log.d("LinkedinManager","getCurrentUserInfo on post excecute");
				if (result instanceof Exception) {
					//result is an Exception :) 
					final Exception ex = (Exception) result;
					//clearTokens();
					Toast.makeText(context,"Appliaction down due to LinkedInApiClientException: "+ ex.getMessage()+ " try run application again.",Toast.LENGTH_LONG).show();
				} else if (result instanceof Person) {
					final Person p = (Person) result;
					delegate.getCurrentUserInfoReciever(p);
				}
			}
		}.execute();

	}
}