package messageTemplate;

import org.json.JSONObject;

public class ParseMessages {

	public String getTypeOfMessage(String message) {
		
		 return new JSONObject(message).getString("type");
	}
	
	public JSONObject parse(String message) {
		return new JSONObject(message);
	}
}
