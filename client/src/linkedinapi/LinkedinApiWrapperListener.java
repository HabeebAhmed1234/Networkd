package linkedinapi;

import java.util.ArrayList;

import com.google.code.linkedinapi.schema.Person;

//add in all database response interfaces here

public interface LinkedinApiWrapperListener {
	void onLinkedinApiCallComplete(int apiCallKey, LinkedinApiWrapperResult result);
}