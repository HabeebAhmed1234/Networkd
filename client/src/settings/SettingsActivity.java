package settings;

import preferences.PreferencesHandler;
import services.NetworkdService;
import linkedinapi.LinkedinApiWrapper;
import linkedinapi.LinkedinApiWrapperListener;
import linkedinapi.LinkedinApiWrapperResult;
import gps.GpsManager;

import com.networkd.R;
import com.networkd.R.id;
import com.networkd.R.layout;
import com.networkd.R.menu;

import contacts.ContactCard;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;
import authentication.AuthenticateActivity;

public class SettingsActivity extends Activity implements OnClickListener, OnCheckedChangeListener,LinkedinApiWrapperListener {
	public static final String TAG = "SettingsActivity";
	
	ToggleButton serviceToggleButton; 
	EditText summaryEditText, skillsEditText, notesEditText,jobTitleEditText;
	Button saveButton;
	LinearLayout logoutButton;
	
	LinkedinApiWrapper linkedinApiWrapper;
	
	private PreferencesHandler preferencesHandler;
	
	GpsManager gpsManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_settings);
		
		preferencesHandler = new PreferencesHandler (this);
		gpsManager = new GpsManager(this); 
		
		summaryEditText = (EditText) this.findViewById(R.id.contactcardsummaryedittext); 
		skillsEditText  = (EditText) this.findViewById(R.id.contactcardskilleditstext);
		notesEditText = (EditText) this.findViewById(R.id.contactcardnotesedittext);
		jobTitleEditText = (EditText) this.findViewById(R.id.jobtitleedittext);
		
		ContactCard tempCard = preferencesHandler.getUserContactCard();
		if(tempCard.summary!=null && tempCard.summary.compareTo("")!=0) summaryEditText.setText(tempCard.summary);
		if(tempCard.skills!=null && tempCard.skills.compareTo("")!=0) skillsEditText.setText(tempCard.skills);
		if(tempCard.extraNotes!=null && tempCard.extraNotes.compareTo("")!=0) notesEditText.setText(tempCard.extraNotes);
		if(tempCard.jobTitle!=null && tempCard.jobTitle.compareTo("")!=0) jobTitleEditText.setText(tempCard.jobTitle);
		
		saveButton = (Button) this.findViewById(R.id.savebutton);
		saveButton.setOnClickListener(this);
		
		logoutButton = (LinearLayout)findViewById(R.id.logoutbutton);     
		logoutButton.setOnClickListener(this);     
		
		serviceToggleButton = (ToggleButton) findViewById(R.id.servicetogglebutton);     
		serviceToggleButton.setOnCheckedChangeListener(this);     
		
		if (NetworkdService.isInstanceCreated()) {
			Log.d(TAG, "NetworkdService is on"); 
			serviceToggleButton.setChecked(true);     
		}else{
			Log.d(TAG, "NetworkdService is off"); 
			serviceToggleButton.setChecked(false);     
		}  
		
		linkedinApiWrapper = new LinkedinApiWrapper(this,this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_settings, menu);
		return true;
	}	
	
	private void redirectToLocationServices()
	{
        Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(viewIntent);
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.servicetogglebutton)
		{
			Log.d(TAG,"servicetogglebutton toggled");
			if (isChecked)
			{	
				Log.d(TAG,"servicetogglebutton is checked");
				if(!gpsManager.isServiceEnabled())
				{
					Log.d(TAG, "gps service is not enabled");  
					Toast.makeText(getBaseContext(),"Please enable location services!",Toast.LENGTH_LONG).show();
					v.setChecked(false);
					redirectToLocationServices();
					return;
				}
				Log.d(TAG, "starting service");     
				startService(new Intent(this, NetworkdService.class));     
			}else
			{
				Log.d(TAG,"servicetogglebutton is not checked");
				Log.d(TAG, "stoping service");     
				stopService(new Intent(this, NetworkdService.class));     
			}
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.savebutton)
		{
			Log.d(TAG,"save button clicked");
			ContactCard userCard = new ContactCard();
			userCard.skills = skillsEditText.getText().toString();
			userCard.summary = summaryEditText.getText().toString();
			userCard.extraNotes = notesEditText.getText().toString();
			userCard.jobTitle = jobTitleEditText.getText().toString();
			preferencesHandler.setContactCard(userCard);
			
			Toast.makeText(this,"Contact Card Saved!",Toast.LENGTH_SHORT).show();
		}
		
		if(v.getId() == R.id.logoutbutton)
		{
			linkedinApiWrapper.clearTokens();   
			Intent intent = new Intent(this, AuthenticateActivity.class);     
		    startActivity(intent);     
		    finish();     
		}
		
	}

	@Override
	public void onLinkedinApiCallComplete(int apiCallKey,
			LinkedinApiWrapperResult result) {
		
	}

}
