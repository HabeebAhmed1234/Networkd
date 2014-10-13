package linkedinapi;

import com.google.code.linkedinapi.schema.Person;

//add in all database response interfaces here

public interface LinkedinAsyncResponse {
	void onAuthorizationCompleteReciever();
	void getCurrentUserInfoReciever(Person output);
}