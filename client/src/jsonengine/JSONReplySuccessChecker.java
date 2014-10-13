package jsonengine;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONReplySuccessChecker {
	public static String TAG = "JSONReplySuccessChecker";
	
	public static boolean isReplySuccess(JSONStruct reply)
	{
		if(reply==null) return false;
		if(!reply.isObject) return false;
		
		JSONObject object = reply.object;
		
		if(object == null) return false;
		
		Object statusObject = null;
		try {
			statusObject = object.get("status");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(statusObject == null) return false;
		
		if(statusObject.toString().compareTo("fail")==0)
		{
			Log.d(TAG,"isAddUserToShortListReplySuccess reply is fail");
			return false;
		}
		
		return true;
	}
	
	public static boolean isAddUserToShortListReplySuccess(JSONStruct reply)
	{
		if(!reply.isObject) return false;
		
		JSONObject object = reply.object;
		
		if(object == null) return false;
		
		Object statusObject = null;
		Object messageObject = null;
		try {
			statusObject = object.get("status");
			messageObject = object.get("message");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(statusObject == null) return false;
		
		if(statusObject.toString().compareTo("fail")==0)
		{
			Log.d(TAG,"isAddUserToShortListReplySuccess reply is fail with message " + messageObject.toString());
			return false;
		}
		
		return true;
	}
}
