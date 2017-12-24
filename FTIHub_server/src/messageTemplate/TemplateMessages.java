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
	
	public String loginCredentials(String username,String password) {
		return stringify(type("login")+",\"username\": \""
				+username+"\", \"password\": \""
				+password+"\"}");
	}
	
	public String loginSuccess(int id) {
		
		return stringify(type("login-success")+",\"id\": \""
				+id+
				"\"}");
	}
	
	public String loginFail() {
		
		return stringify(type("login-fail")+"}");
	}
	
	public String message(String message) {
		return stringify(type("global-message")+
				",\"message\": \""
				+message+"\"}");
	}
	public String disconnect(int id) {
		return stringify(type("disconnect")+",\"id\": \""
				+id+
				"\"}");
	}
	public String ping(){
		return stringify(type("ping")+"}");
	}
	public String privateMessage(int idOfReceiver,int idOfSender,String privateMessage){
		return stringify(type("private-message")+",\"idOfReceiver\": \""
				+idOfReceiver+
				"\",\"idOfSender\":\""+idOfSender+"\""
				+ ",\"privateMessage\":\""+privateMessage+"\"}");
	}
	
	public String preparePrivateMessageFromServer(int idOfSender,String privateMessage) {
		return stringify(type("private-message")+",\"idOfSender\":\""+idOfSender+"\""
				+ ",\"privateMessage\":\""+privateMessage+"\"}");
	}
	public String responsePingClient(int id) {
		return stringify(type("response-ping")+",\"id\": \""
				+id+
				"\"}");
	}
	public String onlineUsers() {
		return "";
	}
	public String serverMessage(String text) {
		return stringify(type("server-message")+
				",\"message\": \""
				+text+"\"}");
	}
}
