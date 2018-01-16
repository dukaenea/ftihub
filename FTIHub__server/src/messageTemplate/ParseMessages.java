package messageTemplate;

import org.json.JSONObject;

public class ParseMessages {

	public String getTypeOfMessage(String message) {

		 return new JSONObject(message.split("/e/")[0]).getString("type");
	}
	
	public JSONObject parse(String message) {
		return new JSONObject(message);
	}
}
