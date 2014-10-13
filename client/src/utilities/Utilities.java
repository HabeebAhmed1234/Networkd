package utilities;

import java.util.ArrayList;

import messaging.Conversation;
import messaging.Message;
import adts.Note;
import adts.User;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Utilities {
		public static final String TAG = "Utilities";
		
		//return a delimited string of the string list
		public static String stringListToDelimitedString(ArrayList<String> list, String delimiter)
		{
			String delimitedString = "";
			if(list.size() == 0) return delimitedString;
			delimitedString = list.get(0);
			if(list.size() == 1) return delimitedString;
			for(int i = 1 ; i < list.size() ; i++)
			{
				delimitedString = delimitedString + delimiter + list.get(i); 
			}
			return delimitedString;
		}
		
		//returns a delimited list of the user database ids
		public static String userListToDelimitedUserIdString(ArrayList<User> list, String delimiter)
		{
			String delimitedString = "";
			if(list.size() == 0) return delimitedString;
			delimitedString = list.get(0).database_id;
			if(list.size() == 1) return delimitedString;
			for(int i = 1 ; i < list.size() ; i++)
			{
				delimitedString = delimitedString + delimiter + list.get(i).database_id; 
			}
			Log.d(TAG,"userListToDelimitedUserIdString returning "+delimitedString+" from array "+list.get(0)+" "+list.get(1));
			return delimitedString;
		}
		
		//returns the conversation that contains this user. null otherwise
		public static Conversation conversationListContainsUser(ArrayList <Conversation> conversations, User user)
		{
			ArrayList<User> tempUsers = new ArrayList<User>();
			Conversation tempConversation = null;
			
			for(int i = 0 ; i < conversations.size() ; i++)
			{
				tempConversation = conversations.get(i);
				tempUsers = tempConversation.users;	
				
				for(int x = 0 ; x < tempUsers.size() ; x ++)
				{
					if(tempUsers.get(x).equals(user)) return tempConversation;
				}
				
			}
			
			return null;
		}
		
		//returns an arraylist of string messages from a list of messages
		public static ArrayList<String> messageListToStringListOfMessages(ArrayList<Message> messages) 
		{
			ArrayList<String> out = new ArrayList<String>();
			for(int i =0; i< messages.size() ; i++)
			{
				out.add(messages.get(i).messageString);
			}
			return out;
		}
		
		//checks if a user is within the list by thier database id
		public static boolean isUserInListByDataBaseId(ArrayList<User> userList, User user)
		{
			for(int i = 0 ; i < userList.size() ; i++)
			{
				if(userList.get(i).database_id.compareTo(user.database_id)==0)
				{
					return true;
				}
			}
			return false;
		}
		
		//converts a string array to a string arraylist
		public static ArrayList<String> converStrinArrayToStringArrayList(String [] in)
		{
			ArrayList<String> out = new ArrayList<String>();
			for(int i = 0 ; i < in.length ; i++)
			{
				out.add(in[i]);
			}
			return out;
		}
		
		//outputs a note list of notes that are about users with the given linkedin_id
		public static ArrayList<Note> filterByLinkedinId(ArrayList<Note> notes , String linkedinId)
		{
			Log.d(Utilities.TAG, "filtering " + notes.size()+" notes by linkedin id "+linkedinId);
			ArrayList<Note> newNotes = new ArrayList<Note>();
			for(int i = 0 ;i<notes.size();i++)
			{
				if(notes.get(i).linkedinId.compareTo(linkedinId)==0) newNotes.add(notes.get(i));
			}
			return newNotes; 
		}
		
		//returns the index of the first user in the list with the given linked_in id. return -1 if user is not in list
		public static int indexOfUserInListWithLinkedinId(ArrayList<User> list, String linkedin_id)
		{
			for(int i = 0 ; i< list.size() ; i++)
			{
				if(list.get(i).linkedin_id.compareTo(linkedin_id)==0) return i;
			}
			return -1;
		}
		
		//looks though the linkedin user list and if they are not in the connections list deactivates them (sets isNetwordUser to false)
		public static ArrayList<User> deactivateLinkedinUsersInListIfNotNetworkdMembers(ArrayList<User> connections, ArrayList<User> networkdUsers)
		{
			for(int i = 0 ; i < networkdUsers.size() ; i++)
			{
				int index = Utilities.indexOfUserInListWithLinkedinId(connections, networkdUsers.get(i).linkedin_id);
				if(index >= 0)
				{
					networkdUsers.get(i).profilePictureProvider = connections.get(index).profilePictureProvider;
					connections.set(index, networkdUsers.get(i));
					connections.get(index).isNetworkdUser = true;
				}
			}
			
			return connections;
		}
		
		//Checks if internet connection (any type) is available. returns true if there is at least one connection available
		public static boolean isNetworkAvailable(Context context) {
			Log.d(TAG,"checking isNetworkAvailable");
		    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
		
		public static void hideKeyboard(Context context, View v)
		{
			InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
}
