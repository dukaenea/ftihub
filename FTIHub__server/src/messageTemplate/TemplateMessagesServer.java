package messageTemplate;

import java.util.HashMap;

import server.ServerClient;

public class TemplateMessagesServer extends TemplateMessages {

	public String loginSuccess(int id) {

		return stringify(type("login-success") + ",\"id\": \"" + id + "\"}");
	}

	public String loginInitial(int id,HashMap<Integer,ServerClient> allClients) {
		return "";
	}
	
	public String allUsers(int id,HashMap<Integer,ServerClient> allClients) {
		StringBuilder s=new StringBuilder();
		s.append(type("all-users")+",\"users\":[");
		int n=0;
		for (ServerClient c : allClients.values()) {
			s.append("{\"online\":\""+Boolean.toString(c.online)+"\",\"username\":\""+c.name+"\",\"id\":\""+c.getID()+"\"}");
			if(n!=allClients.size()-1){
				s.append(",");			
			}
			n++;
		}
		s.append("]}");
		
		return stringify(s.toString());
	}

	public String loginFail() {

		return stringify(type("login-fail") + "}");
	}

	public String ping() {
		return stringify(type("ping") + "}");
	}

	public String preparePrivateMessageFromServer(int idOfSender, String privateMessage) {
		return stringify(type("private-message") + ",\"idOfSender\":\"" + idOfSender + "\"" + ",\"privateMessage\":\""
				+ privateMessage + "\"}");
	}

	public String onlineUsers() {
		return "";
	}

	public String serverMessage(String text) {
		return stringify(type("server-message") + ",\"message\": \"" + text + "\"}");
	}
}
