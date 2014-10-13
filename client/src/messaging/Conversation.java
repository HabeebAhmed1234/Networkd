package messaging;

import java.util.ArrayList;

import adts.User;

public class Conversation {
	String id;
	public ArrayList<User> users = new ArrayList<User>();
	ArrayList<Message> messages = new ArrayList<Message>();
	
	public Conversation(String id, ArrayList<User> users ,ArrayList<Message> messages)
	{
		this.id = id;
		this.users = users;
		this.messages = messages;
	}
	
	public String usersInConversationToString()
	{
		String out = "";
		
		for(int i = 0 ; i < users.size() ; i++)
		{
			out = out + users.get(i).firstName + " "+ users.get(i).lastName;
			if(i < users.size()-1) out+=", ";
		}
		
		return out;
	}
}
