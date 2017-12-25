package messageTemplate;

import org.json.JSONObject;

public class TemplateMessages {
	
	public String nullify(String string) {
		return string+"/e/";
	}

	public String stringify(String string) {
		return nullify(new JSONObject(string).toString());
	}
	
	public String type(String type) {
		return "{\"type\": \""+type+"\"";
	}
	
}
