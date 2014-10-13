package contacts;

import adts.User;

public class ContactCard {
	public String id = "";
	public String senderId = "";
	public User user;
	public String jobTitle = "";
	public String skills = "";
	public String summary = "";
	public String extraNotes = "";

	public ContactCard()
	{
		
	}
	
	public ContactCard(String id, String skills, String summary, String extraNotes, String senderId)
	{
		this.id = id;
		this.skills = skills;
		this.summary = summary;
		this.extraNotes = extraNotes;
		this.senderId = senderId;
	}
	
	public boolean equals(ContactCard other)
	{
		return this.id.compareTo(other.id) == 0;
	}
	
	public boolean containsString(String in)
	{
		boolean userContainsString = false;
		if(user!=null){
			//TODO make user not null (populate it from contactcard manager activity
			userContainsString = user.containsString(in);
		}
		return jobTitle.contains(in) || skills.contains(in) || userContainsString || summary.contains(in) || extraNotes.contains(in);
	}
}
