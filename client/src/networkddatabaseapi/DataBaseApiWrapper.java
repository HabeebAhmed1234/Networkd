package networkddatabaseapi;

import gps.GpsCoordinate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import jsonengine.JSONFetcher;
import jsonengine.JSONStruct;
import jsonengine.JSONFetcher.JSONFetcherOnCompleteListener;

import org.json.JSONObject;

import preferences.PreferencesHandler;
import utilities.Utilities;

import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;

import contacts.ContactCard;
import adts.Event;
import adts.User;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class DataBaseApiWrapper implements JSONFetcherOnCompleteListener {
	/*
	 * Listener class that handles the callback.
	 */
	public interface DataBaseApiWrapperListener {
		public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success);
	}
	
	public static final String BASE_DATABASE_URL = "network-api.herokuapp.com/api/";
	public static final String DATABASE_VERSION_URL = "v1/";
	
	//request codes
	public static final int GET_ALL_USERS_ROUTE = 0;
	public static final int GET_USER_BY_ID_ROUTE = 1;
	public static final int CREATE_USER_ROUTE = 2;
	public static final int UPDATE_USER_GPS_ROUTE = 3;
	public static final int USER_ATTEND_EVENT_ROUTE = 4;
	//public static final int USER_ADD_EVENT_ROUTE = 13;
	public static final int UPDATE_USER_INFO_ROUTE = 5;
	public static final int GET_USER_WISHLIST_ROUTE = 6;
	public static final int DELETE_USER_ROUTE = 7;
	public static final int GET_ALL_EVENTS_ROUTE = 8;
	public static final int GET_EVENT_BY_ID_ROUTE = 9;
	public static final int CREATE_EVENT_ROUTE = 10;
	public static final int EVENT_UPDATE_ROUTE = 11;
	public static final int EVENT_DELETE_ROUTE = 12;
	public static final int CHECK_IF_REGISTERED = 14;
	public static final int ADD_NOTE = 15;
	public static final int GET_ALL_NOTES = 16;
	public static final int DELETE_NOTE = 17;
	public static final int GET_SHORTLIST_USERS = 18;
	public static final int DELETE_USER_FROM_SHORT_LIST = 19;
	public static final int ADD_USER_TO_SHORT_LIST = 20;
	public static final int GET_ALL_CONVERSATION_IDS_AND_PARTICIPANTS = 21;
	public static final int GET_ALL_CONVERSATION_MESSAGES = 22;
	public static final int SEND_MESSAGE_TO_CONVERSATION = 23;
	public static final int CREATE_CONVERSATION = 24;
	public static final int GET_CONVERSATION_PARTICIPANTS = 25;
	public static final int GET_CONVERSATION_ID_FROM_PARTICIPANT_LIST = 26;
	public static final int DELETE_CONTACT_CARD_FROM_INVENTORY = 27;
	public static final int GET_CONTACT_CARDS = 28;
	public static final int SEND_CONTACT_CARD = 29;
	public static final int GET_CONTACT_CARD_BY_ID = 30;
	
	public static int currentRequestCode = -1;
	
	private static final String TAG = "DataBaseAPIWrapper";// debug/log tag
	private static final String URL_FORMAT = "http://network-api.herokuapp.com/";

	public static boolean setSpinnerOn = true;
	
	private static String current_user_apiKey;
	private static String current_user_id;
	
	private Context context_;
	private DataBaseApiWrapperListener listener_;

	public DataBaseApiWrapper(DataBaseApiWrapperListener listener, Context ctx, boolean setSpinnerOn) {
		listener_ = listener;
		context_ = ctx;
		this.setSpinnerOn = setSpinnerOn;
		
		PreferencesHandler prefsHandler = new PreferencesHandler(context_);
		this.current_user_id = prefsHandler.getUserDataBaseId();
		this.current_user_apiKey = prefsHandler.getDataBaseApiKey();
	}
	
	public String getCurrentUserDatabaseId()
	{
		return this.current_user_id;
	}
	
	public String getCurrentUserDatabaseApiKey()
	{
		return this.current_user_apiKey;
	}
	
	public void onJsonFetcherComplete(JSONStruct result, boolean success) {
		listener_.onDataBaseAPIRequestComplete(result, success);
	}
	
	public void clearTokens()
	{
		//for now we will assume only one user is on a device
		//this.context_.getSharedPreferences(Constants.APP_PREFERENCES_KEY, context_.MODE_PRIVATE).edit().remove(Constants.API_KEY_PREFERENCES_KEY).commit();
	}
	
	//appends proper ending and version onto url
	private String parseToJSONUrl(String endUrl, String parameters)
	{
		parameters = parameters.replace(" ", "+");
		return BASE_DATABASE_URL+DATABASE_VERSION_URL+ endUrl + ".json" + parameters;
	}
	
	//all the api calls
	public void getAllUsers() {
		currentRequestCode = this.GET_ALL_USERS_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("users","");
		fetcher.execute(Url);
	}
	
	public void getUserById(String id) {
		currentRequestCode = this.GET_USER_BY_ID_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("user/"+id+"/"+this.current_user_apiKey,"");
		fetcher.execute(Url);
	}
	
	public void createUser(User user, LinkedInAccessToken linkedinAccessToken) {
		//TODO finish this
		currentRequestCode = this.CREATE_USER_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("user/create","?first_name="
								+user.firstName
								+"&last_name="
								+user.lastName
								+"&linkedin="
								+user.linkedin_id
								+"&email="+user.email
								+"&gps="+user.gps_coord.toString()
								+"&linkedin_key=" + linkedinAccessToken.getToken());
		fetcher.execute(Url);
	}
	
	public void updateUserGpsPosition(GpsCoordinate gps_coord) {
		currentRequestCode = this.UPDATE_USER_GPS_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_PUT);
		String Url = parseToJSONUrl("user/update_gps","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&coords="+gps_coord);
		fetcher.execute(Url);
	}
	
	public void userAttendEvent(User user, Event event) {
		currentRequestCode = this.USER_ATTEND_EVENT_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void updateUserInfo(User oldUserInfo, User newUserInfo) {
		currentRequestCode = this.UPDATE_USER_INFO_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void getUserWishlist(User user) {
		currentRequestCode = this.GET_USER_WISHLIST_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void deleteUser(User user) {
		currentRequestCode = this.DELETE_USER_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_DELETE);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void getAllEvents() {
		currentRequestCode = this.GET_ALL_EVENTS_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void getEventById(String id) {
		currentRequestCode = this.GET_EVENT_BY_ID_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void createEvent(User creator, Event event) {
		currentRequestCode = this.CREATE_EVENT_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void updateEvent(Event oldEvent, Event newEvent)
	{
		currentRequestCode = this.EVENT_UPDATE_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void deleteEvent(Event Event)
	{
		currentRequestCode = this.EVENT_DELETE_ROUTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_DELETE);
		String Url = parseToJSONUrl("user","");
		fetcher.execute(Url);
	}
	
	public void checkIfRegistered()
	{
		currentRequestCode = this.CHECK_IF_REGISTERED;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("user"+this.current_user_id+"/"+this.current_user_apiKey,"");
		fetcher.execute(Url);
	}
	
	public void addNote(String linkedinId,String note)
	{
		currentRequestCode = this.ADD_NOTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("note/add","?api_key="+this.current_user_apiKey+"&id="+this.current_user_id+"&note="+note+"&linkedin_id="+linkedinId);
		fetcher.execute(Url);
	}
	
	public void getAllNotes()
	{
		currentRequestCode = this.GET_ALL_NOTES;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("notes/"+this.current_user_apiKey+"/"+this.current_user_id,"");
		fetcher.execute(Url);
	}
	
	public void deleteNote(String noteId)
	{
		currentRequestCode = this.DELETE_NOTE;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_DELETE);
		String Url = parseToJSONUrl("note/remove","?api_key="+this.current_user_apiKey+"&id="+this.current_user_id+"&note_id="+noteId);
		fetcher.execute(Url);
	}
	
	public void getShortListUsers()
	{
		currentRequestCode = this.GET_SHORTLIST_USERS;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("shortlist/"+this.current_user_id,"");
		fetcher.execute(Url);
	}
	
	public void addUserToShortList(User user)
	{
		currentRequestCode = this.ADD_USER_TO_SHORT_LIST;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_PUT);
		String Url = parseToJSONUrl("shortlist/add","?id="+this.current_user_id +"&api_key="+this.current_user_apiKey+"&linkedin_id="+user.linkedin_id);
		fetcher.execute(Url);
	}
	
	public void deleteUserFromShortList(User user)
	{
		currentRequestCode = this.DELETE_USER_FROM_SHORT_LIST;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_DELETE);
		String Url = parseToJSONUrl("shortlist/remove","?id="+this.current_user_id +"&api_key="+this.current_user_apiKey+"&linkedin_id="+user.linkedin_id);
		
		fetcher.execute(Url);
	}
	
	public void createConversation(ArrayList<String> userIds)
	{
		currentRequestCode = this.CREATE_CONVERSATION;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("conversation/create","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&members="+Utilities.stringListToDelimitedString(userIds, ","));
		
		fetcher.execute(Url);
	}
	
	public void getAllConversationIdsAndParticipants()
	{
		currentRequestCode = this.GET_ALL_CONVERSATION_IDS_AND_PARTICIPANTS;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("user/conversations","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey);
		
		fetcher.execute(Url);
	}
	
	public void getAllConversationMessages(String conversationId)
	{
		currentRequestCode = this.GET_ALL_CONVERSATION_MESSAGES;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("conversation/messages","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&conversation_id="+conversationId);
		fetcher.execute(Url);
	}
	
	public void sendMessageToConversation(String conversationId,String message)
	{
		currentRequestCode = this.SEND_MESSAGE_TO_CONVERSATION;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("message/create","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&conversation_id="+conversationId+"&message="+message);
		fetcher.execute(Url);
	}
	
	public void getConversationParticipants(String conversationId)
	{
		currentRequestCode = this.GET_CONVERSATION_PARTICIPANTS;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("conversation/participants","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&conversation_id="+conversationId);
		fetcher.execute(Url);
	}
	
	public void getConversationIdFromParticipantList(ArrayList<User> participants)
	{
		currentRequestCode = this.GET_CONVERSATION_ID_FROM_PARTICIPANT_LIST;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("conversation/common","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&user_list="+Utilities.userListToDelimitedUserIdString(participants,","));
		fetcher.execute(Url);
	}
	
	public void sendContactCard(User toUser,String summary, String skills, String notes, String jobTitle)
	{
		currentRequestCode = this.SEND_CONTACT_CARD;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_POST);
		String Url = parseToJSONUrl("contact/send","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&receiver=" + toUser.database_id + "&summary=" +jobTitle+"   "+summary + "&skills=" + skills + "&notes=" + notes);
		fetcher.execute(Url);
	}
	
	public void getContactCards()
	{
		currentRequestCode = this.GET_CONTACT_CARDS;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("contact/all","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey);
		fetcher.execute(Url);
	}
	
	public void getContactCardById(String cardId)
	{
		currentRequestCode = this.GET_CONTACT_CARD_BY_ID;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_GET);
		String Url = parseToJSONUrl("contact/get","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&contact_id="+cardId);
		fetcher.execute(Url);
	}
	
	public void deleteContactCardFromInventory(ContactCard card)
	{
		currentRequestCode = this.DELETE_CONTACT_CARD_FROM_INVENTORY;
		JSONFetcher fetcher = new JSONFetcher(this, context_,JSONFetcher.TYPE_DELETE);
		String Url = parseToJSONUrl("contact/delete","?id="+this.current_user_id+"&api_key="+this.current_user_apiKey+"&contact_id="+card.id);
		fetcher.execute(Url);
	}
}
