package preferences;

import contacts.ContactCard;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferencesHandler {
	public static final String TAG = "PreferencesHandler";
	
	private Context context;
	private static final String PREFS_NAME="APP_CONFIG";
	
	//preferences keys
	public static final String LINKEDIN_REQUEST_PREF_TOKENSECRET_KEY = "linkedin_request_pref_tokensecret_key";
	public static final String LINKEDIN_PREF_TOKEN_KEY = "linkedin_pref_token_key";
	public static final String LINKEDIN_PREF_TOKENSECRET_KEY = "linkedin_pref_tokensecret_key";
	public static final String DATABASE_API_KEY_PREFERENCES_KEY = "database_api_key";
	public static final String USER_DATABASE_ID_PREFERENCES_KEY = "user_database_id_key";
	public static final String USER_NAME_PREFERENCES_KEY = "user_name_key";
	public static final String USER_SUMMARY_PREFERENCES_KEY = "user_summary_key";
	public static final String USER_SKILLS_PREFERENCES_KEY = "user_skills_key";
	public static final String USER_CONTACT_CARD_NOTES_PREFERENCES_KEY = "user_contact_card_notes_key";
	public static final String USER_CONTACT_CARD_JOB_TITLE_PREFERENCES_KEY = "user_contact_card_job_title_key";
	
	private SharedPreferences settings ;
	
	public PreferencesHandler(Context con)
	{
		context=con;
		settings = context.getSharedPreferences(PREFS_NAME, 0);
	}
	
	private void set(String settingname, String settingvalue)
	{
		Log.d(TAG,"set key "+settingname+" = " + settingvalue);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(settingname, settingvalue);
        editor.commit();
	}
	
	private String get(String key)
	{
		String out = settings.getString(key, null);
		Log.d(TAG,"get key "+key+" = " + out);
        return out;
	}
	
	//preference set methods
	public void setDataBaseApiKey(String apiKey)
	{
		set(this.DATABASE_API_KEY_PREFERENCES_KEY,apiKey);
	}
	
	public void setUserDataBaseId(String id)
	{
		set(this.USER_DATABASE_ID_PREFERENCES_KEY,id);
	}
	
	public void setUserName(String userName)
	{
		set(this.USER_NAME_PREFERENCES_KEY,userName);
	}
	
	private void setUserSummary(String summary)
	{
		set(this.USER_SUMMARY_PREFERENCES_KEY,summary);
	}
	
	private void setUserSkills(String skills)
	{
		set(this.USER_SKILLS_PREFERENCES_KEY,skills);
	}
	
	private void setUserContactCardNotes(String notes)
	{
		set(this.USER_CONTACT_CARD_NOTES_PREFERENCES_KEY,notes);
	}
	
	private void setUserJobTitle(String jobTitle)
	{
		set(this.USER_CONTACT_CARD_JOB_TITLE_PREFERENCES_KEY,jobTitle);
	}
	
	public void setLinkedinPrefToken(String token)
	{
		set(this.LINKEDIN_PREF_TOKEN_KEY,token);
	}
	
	public void setLinkedinPrefTokenSecret(String token)
	{
		set(this.LINKEDIN_PREF_TOKENSECRET_KEY,token);
	}
	
	public void setLinkedinRequestPrefTokenSecret(String token)
	{
		set(this.LINKEDIN_REQUEST_PREF_TOKENSECRET_KEY,token);
	}
	
	public void setContactCard(ContactCard card)
	{
		setUserSummary(card.summary);
		setUserSkills(card.skills);
		setUserContactCardNotes(card.extraNotes);
		setUserJobTitle(card.jobTitle);
		
	}
	
	//preference get methods
	public String getDataBaseApiKey()
	{
		return get(this.DATABASE_API_KEY_PREFERENCES_KEY);
	}
	
	public String getUserDataBaseId()
	{
		return get(this.USER_DATABASE_ID_PREFERENCES_KEY);
	}
	
	public String getUserName()
	{
		return get(this.USER_NAME_PREFERENCES_KEY);
	}
	
	private String getUserSumary()
	{
		return get(USER_SUMMARY_PREFERENCES_KEY);
	}
	
	private String getUserSkills()
	{
		return get(USER_SKILLS_PREFERENCES_KEY);
	}
	
	private String getUserContactCardNotes()
	{
		return get(USER_CONTACT_CARD_NOTES_PREFERENCES_KEY);
	}
	
	private String getUserContactCardJobTitle()
	{
		return get(USER_CONTACT_CARD_JOB_TITLE_PREFERENCES_KEY);
	}
	
	public String getLinkedinPrefToken()
	{
		return get(this.LINKEDIN_PREF_TOKEN_KEY);
	}
	
	public String getLinkedinPrefTokenSecret()
	{
		return get(this.LINKEDIN_PREF_TOKENSECRET_KEY);
	}
	
	public String getLinkedinRequestPrefTokenSecret()
	{
		return get(this.LINKEDIN_REQUEST_PREF_TOKENSECRET_KEY);
	}
	
	public ContactCard getUserContactCard()
	{
		ContactCard card = new ContactCard();
		card.summary = this.getUserSumary();
		card.skills = this.getUserSkills();
		card.extraNotes = this.getUserContactCardNotes();
		card.jobTitle = this.getUserContactCardJobTitle();
		return card;
	}
}
