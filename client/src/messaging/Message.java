package messaging;

public class Message {
	String id = "";
	String timeStamp = "";
	String posterId = "";
	public String messageString = "";
	
	public Message(String id, String timeStamp, String posterId, String messageString)
	{
		this.id = id;
		this.timeStamp = timeStamp;
		this.posterId = posterId;
		this.messageString = messageString;
	}
}
