package notes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import networkddatabaseapi.DataBaseApiWrapper;
import networkddatabaseapi.DataBaseApiWrapper.DataBaseApiWrapperListener;
import utilities.Utilities;
import jsonengine.JSONObjectParser;
import jsonengine.JSONReplySuccessChecker;
import jsonengine.JSONStruct;

import com.google.code.linkedinapi.schema.Person;
import com.networkd.R;

import adts.Note;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NoteManagerActivity extends Activity implements OnClickListener,DataBaseApiWrapperListener {
	public static final String TAG = "NoteManagerActivity";
	Button addNoteButton, deleteNoteButton;
	EditText noteEditText;
	
	DataBaseApiWrapper dataBaseApiWrapper;
	
	ListView lvNotesContent;
	NotesListAdapter notesListAdapter;
	
	private String observedUserLinkedinId = "";
	
	public static final String EXTRA_USER_LINKEDIN_ID = "User_Linkedin_id";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(this.TAG, "onCreate");
				
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_note_manager);
		
		noteEditText = (EditText) findViewById(R.id.noteedittext);
		
		addNoteButton =  (Button) findViewById(R.id.addnotebutton);
		addNoteButton.setOnClickListener(this);
		deleteNoteButton =  (Button) findViewById(R.id.deletenotesbutton);
		deleteNoteButton.setOnClickListener(this);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    observedUserLinkedinId = extras.getString(EXTRA_USER_LINKEDIN_ID);
			Log.d(this.TAG, "recieved Extra linkedinid ="+observedUserLinkedinId);
		}else
		{
			Log.d(this.TAG, "extra is null!");
		}
		
		if(observedUserLinkedinId == null)
		{
			Log.d(this.TAG, "observedUserLinkedinId is null! exiting");
		}
		
		dataBaseApiWrapper = new DataBaseApiWrapper(this,this,true);
		dataBaseApiWrapper.getAllNotes();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_add_note, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if(id == R.id.addnotebutton)
		{
			Log.d(this.TAG, "add note button pressed");
			String note = noteEditText.getText().toString();
			
			if(note!=null && note.compareTo("")!=0) 
			{
				Log.d(this.TAG, "adding note to database note = "+note);
				notesListAdapter.addNote(observedUserLinkedinId,note);
			}
		}
		
		if(id == R.id.deletenotesbutton)
		{
			Log.d(this.TAG, "delete note button pressed");
			notesListAdapter.deleteSelectedNotes();
		}
	}

	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(!success) return;
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.GET_ALL_NOTES)
		{
			createList(JSONObjectParser.convertJSONResponseToNotes(result));
		}
	}
	
	private void createList(ArrayList<Note> notes)
	{
		lvNotesContent = (ListView) findViewById(R.id.noteList);
        notesListAdapter = new NotesListAdapter(this, notes,observedUserLinkedinId);
        lvNotesContent.setAdapter(notesListAdapter);
        
        lvNotesContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		      @Override
		      public void onItemClick(AdapterView<?> parent,  final View view,int position, long id) {
		    	    Note selectedNote = (Note)parent.getAdapter().getItem(position);
		    	    Log.d(TAG,"toggling note selected");
		    	    ((Note)parent.getAdapter().getItem(position)).toggleSelected();
		    	    notesListAdapter.notifyDataSetChanged();
		            view.refreshDrawableState();
		      }
		    });
	}

}


class NotesListAdapter extends BaseAdapter implements DataBaseApiWrapperListener
{
	public static final String TAG = "NotesListAdapter";
	// context
    private Context context;

    // views
    private LayoutInflater inflater;

    // data
    private ArrayList<Note> notes;
    
    //linkedin id of the user about which these notes are
    private String observedUserLinkedinId;
    
    DataBaseApiWrapper dataBaseApiWrapper;
    
    public NotesListAdapter(Context context, ArrayList<Note> inNotes, String inObservedUserLinkedinId) 
    {
    	Log.d(this.TAG,"creating list adapter with "+inNotes.size()+" Notes");
    	this.context = context;
    	this.observedUserLinkedinId = inObservedUserLinkedinId;
    	this.notes = Utilities.filterByLinkedinId(inNotes, this.observedUserLinkedinId);
    	inflater = LayoutInflater.from(context);
    	dataBaseApiWrapper = new DataBaseApiWrapper(this,context,true);
    }
    
	@Override
	public int getCount() {
		return notes.size();
	}

	@Override
	public Object getItem(int arg0) {
		return notes.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		if (convertView != null) {
			v = convertView;
		} else {
		    v = inflater.inflate(R.layout.layout_note, parent, false);
		    //v = new View(context);
		}

		Note note = (Note) getItem(position);
		
		Log.d(this.TAG,"making view for Note "+ position);
		
		if(note.selected) 
		{
			Log.d(this.TAG,"Note is selected");
			v.setBackgroundColor(Color.BLUE);
		}else{
			Log.d(this.TAG,"Note is not selected");
			v.setBackgroundColor(Color.WHITE);
		}
		
		TextView tvLAYOUTNote = (TextView) v.findViewById(R.id.tvLAYOUTNote);
		tvLAYOUTNote.setText(note.note);
		
		return v;
	}
	
	@Override
	public void onDataBaseAPIRequestComplete(JSONStruct result, boolean success) {
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.ADD_NOTE)
		{
			if(JSONReplySuccessChecker.isReplySuccess(result))
			{
				notes = Utilities.filterByLinkedinId(JSONObjectParser.convertNoteAddDeleteResponseToNewNotesList(result),observedUserLinkedinId);
				this.notifyDataSetChanged();
				Toast.makeText(context,"Note Added!",Toast.LENGTH_LONG).show();
			}
		}
		
		if(DataBaseApiWrapper.currentRequestCode == DataBaseApiWrapper.DELETE_NOTE)
		{
			if(JSONReplySuccessChecker.isReplySuccess(result))
			{
				notes = Utilities.filterByLinkedinId(JSONObjectParser.convertNoteAddDeleteResponseToNewNotesList(result),observedUserLinkedinId);
				this.notifyDataSetChanged();
				Toast.makeText(context,"Note Deleted!",Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public void addNote(String observedUserLinkedinId, String note)
	{
		dataBaseApiWrapper.addNote(observedUserLinkedinId, note);
	}
	
	public void deleteSelectedNotes()
	{
		for(int i = 0; i < this.notes.size() ; i++)
		{
			if(this.notes.get(i).selected)
			{
				dataBaseApiWrapper.deleteNote(notes.get(i).note_id);
			}
		}
	}
}
