package jsonengine;

import gps.GpsCoordinate;

import java.util.ArrayList;

import messaging.Conversation;
import messaging.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import contacts.ContactCard;
import adts.Note;
import adts.User;
import android.util.Log;

public class JSONObjectParser {
	public static final String TAG = "JSONObjectParser";
	
	public static User convertJSONResponseToUser(JSONStruct input)
	{
		User newUser = new User();
		
		if(input == null) 
		{
			Log.d(TAG, "convertJSONResponseToUser: input was null");
			return newUser;
		}
		if(!input.isObject) 
		{
			Log.d(TAG, "convertJSONResponseToUser: input was not an object");
			return newUser;
		}
		
		JSONObject userJSONObject;
		try {
			userJSONObject = input.object.getJSONObject("user");
			newUser.database_id = userJSONObject.getString("id");
			newUser.firstName = userJSONObject.getString("first_name");
			newUser.lastName = userJSONObject.getString("last_name");
			newUser.linkedin_id = userJSONObject.getString("linkedin_id");
			newUser.email = userJSONObject.getString("email");
			newUser.gps_coord = new GpsCoordinate(userJSONObject.getString("gps_coord"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return newUser;
	}
	
	public static ArrayList<User> convertJSONResponseToUsers(JSONStruct input)
	{
		Log.d(TAG, "convertJSONResponseToUsers: converting JSON to Users");
		
		ArrayList<User> users = new ArrayList<User>();
		if(input == null) 
		{
			Log.d(TAG, "convertJSONResponseToUsers: input was null");
			return users;
		}
		if(input.isObject) 
		{
			Log.d(TAG, "convertJSONResponseToUsers: input was not an array");
			return users;
		}
		
		JSONArray usersArray = input.array;
		
		for(int i = 0 ; i<usersArray.length();i++)
		{
			User tempUser = new User();
			try {
				JSONObject usersArrayElementObject = usersArray.getJSONObject(i).getJSONObject("user");
				tempUser.firstName = usersArrayElementObject.getString("first_name");
				tempUser.lastName= usersArrayElementObject.getString("last_name");
				tempUser.email= usersArrayElementObject.getString("email");
				tempUser.gps_coord= new GpsCoordinate(usersArrayElementObject.getString("gps_coord"));
				tempUser.linkedin_id= usersArrayElementObject.getString("linkedin_id");
				tempUser.database_id = usersArrayElementObject.getString("id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG,"parsed user "+tempUser.toString());
			users.add(tempUser);
		}
		Log.d(JSONObjectParser.TAG, "convertJSONResponseToUsers is returning "+users.size()+" users");
		return users;
	}
	
	public static ArrayList<Note> convertJSONResponseToNotes(JSONStruct input)
	{
		//TODO: make sure this is correct
		ArrayList<Note> notes = new ArrayList<Note>();
		if(input.isObject) 
		{
			Log.d(TAG,"convertJSONResponseToNotes: response is not parseable");
			return notes;
		}
		
		JSONArray notesArray = input.array;
		if(notesArray == null)
		{
			Log.d(TAG,"convertJSONResponseToNotes: notesArray is null");
			return notes;
		}
		
		for(int i = 0 ; i<notesArray.length();i++)
		{
			Note tempNote = new Note();
			try {
				JSONObject notesArrayElementObject = notesArray.getJSONObject(i).getJSONObject("note");
				tempNote.note_id = notesArrayElementObject.getString("id");
				tempNote.linkedinId = notesArrayElementObject.getString("linkedin_id");
				tempNote.note= notesArrayElementObject.getString("note");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG, "parsed note "+tempNote.toString());
			notes.add(tempNote);
		}
		Log.d(JSONObjectParser.TAG, "convertJSONResponseToNotes is returning "+notes.size()+" notes");
		return notes;
	}
	
	public static ArrayList<Note> convertNoteAddDeleteResponseToNewNotesList(JSONStruct input)
	{
		ArrayList<Note> newList = new ArrayList<Note>();
		if(input == null) return newList;
		if(!input.isObject) return newList;
		
		JSONArray notesArray = null;
		
		try {
			notesArray = input.object.getJSONArray("notes");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(notesArray == null) return newList;
		
		for(int i = 0 ; i < notesArray.length() ; i++)
		{
			Note tempNote = new Note();
			JSONObject jsonNote = null;
			try {
				jsonNote = notesArray.getJSONObject(i);
				if(jsonNote == null) return newList;
				
				tempNote.note_id = jsonNote.getString("id");
				tempNote.linkedinId = jsonNote.getString("linkedin_id");
				tempNote.note = jsonNote.getString("note");
				tempNote.selected = false;
				
				newList.add(tempNote);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return newList;
		
	}
	
	public static ArrayList<String> convertJSONResponseToConversationIdsUsingCommonConversationsResponse(JSONStruct input)
	{
		ArrayList<String> conversationIds = new ArrayList<String>();
		if(input==null) return conversationIds;
		if(!input.isObject) 
		{
			Log.d(TAG,"convertJSONResponseToConversationIdsUsingCommonConversationsResponse: reply is not parseable");
			return conversationIds;
		}
		
		JSONObject conversationIdsJSONObject = input.object;
		
		if(conversationIdsJSONObject == null) return conversationIds;
		
		JSONArray conversationIdsArray;
		try {
			conversationIdsArray = conversationIdsJSONObject.getJSONArray("conversations");
		
			for(int i = 0 ; i < conversationIdsArray.length() ; i++)
			{
				conversationIds.add(conversationIdsArray.getString(i));
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		
		return conversationIds;
	}
	
	public static ArrayList<Conversation> convertJSONResponseToConversations (JSONStruct input)
	{
		ArrayList<Conversation> conversations = new ArrayList<Conversation>();
		if(input==null) {
			Log.d(TAG,"input was null");
			return conversations;
		}
		if(!input.isObject) 
		{
			Log.d(TAG,"convertJSONResponseToConversations: reply is not parseable");
			Log.d(TAG,input.toString());
			return conversations;
		}
		
		JSONArray conversationsJSONArray = null;
		try {
			conversationsJSONArray = input.object.getJSONArray("conversations");
			Log.d(TAG,"making conversationsJSONArray");
		} catch (JSONException e1) {
			Log.d(TAG,e1.getMessage());
			e1.printStackTrace();
		}
		
		if(conversationsJSONArray == null) return conversations;
		
		
		for(int i = 0 ; i < conversationsJSONArray.length() ; i++)
		{
			JSONArray participantsArray = null;
			try {
				participantsArray = conversationsJSONArray.getJSONObject(i).getJSONArray("participants");
				Log.d(TAG,"found "+participantsArray.length()+" participants");
				
				ArrayList<User> participants = new ArrayList<User>();
				
				for(int x = 0 ; x < participantsArray.length() ; x++){
					String[] firstlast = participantsArray.getString(x).split(", ");
					participants.add(new User(firstlast[1],firstlast[0]));
				}
				
				String conversationId = conversationsJSONArray.getJSONObject(i).getString("id");
				Log.d(TAG,"conversationid = "+conversationId +" "+participants.size()+ " participants parsed");
				
				conversations.add(new Conversation(conversationId,participants,new ArrayList<Message>()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return conversations;
	}
	
	public static ArrayList<Message> convertJSONResponseToMessages(JSONStruct input)
	{
		ArrayList<Message> messages = new ArrayList<Message>();
		
		if(input==null) return messages;
		if(input.isObject) 
		{
			Log.d(TAG,"convertJSONResponseToMessages: reply is not parseable");
			return messages;
		}
		
		JSONArray messagesJSONArray = input.array;
		
		if(messagesJSONArray == null) return messages;
		
		JSONObject tempMessageJSONObject;
		for(int i = 0 ; i < messagesJSONArray.length() ; i++)
		{
			
			try {
				tempMessageJSONObject = messagesJSONArray.getJSONObject(i).getJSONObject("message");
				
				messages.add(new Message(tempMessageJSONObject.getString("id")
							,tempMessageJSONObject.getString("created_at")
							,tempMessageJSONObject.getString("user_id")
							,tempMessageJSONObject.getString("message")));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return messages;
	}
	
	public static String getConversationIdFromCreateConversationResponse(JSONStruct input)
	{
		String createdConversationId = "";
		if(input==null) return createdConversationId;
		if(!input.isObject)
		{
			Log.d(TAG,"getConversationIdFromCreateConversationResponse: reply is not parseable");
			return createdConversationId;
		}
		
		JSONObject createConversationResponseJSONObject = input.object;
		try{
			createdConversationId = createConversationResponseJSONObject.getString("id");
		}catch(JSONException e){
			e.printStackTrace();
		}
		return createdConversationId;
	}
	
	public static ArrayList<ContactCard> convertJSONResponseToContactCards(JSONStruct input)
	{
		ArrayList<ContactCard> contactCards = new ArrayList<ContactCard>();
		
		if(input==null) return contactCards;
		if(input.isObject)
		{
			Log.d(TAG,"convertJSONResponseToContactCards: reply is not parseable");
			return contactCards;
		}
		
		JSONArray cardsJSONArray = input.array;
		
		if(cardsJSONArray == null) return contactCards;
		
		JSONObject tempCardJSONObject;
		for(int i = 0 ; i < cardsJSONArray.length() ; i++)
		{
			
			try {
				tempCardJSONObject = cardsJSONArray.getJSONObject(i).getJSONObject("contact");
				
				contactCards.add(new ContactCard(tempCardJSONObject.getString("id")
							,tempCardJSONObject.getString("skills")
							,tempCardJSONObject.getString("summary")
							,tempCardJSONObject.getString("extra_notes")
							,tempCardJSONObject.getString("sender_id")));
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return contactCards;
	}
}
