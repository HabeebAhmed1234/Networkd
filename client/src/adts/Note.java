package adts;

public class Note {
	//linkedin id of the user who the note is about
	public String linkedinId = "";
	//note id that is returned once a note is created
	public String note_id = "";
	//note
	public String note = "";
	
	//for user in list views
	public boolean selected = false;
	
	public void toggleSelected()
	{
		if(selected) 
		{
			selected = false;
			return;
		}
		selected = true;
		
	}
	
	public boolean containsString(String in)
	{
		String fullInfoString = note;
		if(fullInfoString.contains(in)) return true;
		return false;
	}
	
	public String toString()
	{
		return "linkedin_id = "+ linkedinId+ " " +
			   "note_id = "+ note_id + " " +
			   "note = "+ note +" ";
	}
}
